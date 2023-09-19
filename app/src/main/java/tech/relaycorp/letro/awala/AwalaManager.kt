package tech.relaycorp.letro.awala

import android.content.Context
import android.util.Base64
import android.util.Log
import androidx.annotation.RawRes
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.withContext
import tech.relaycorp.awaladroid.Awala
import tech.relaycorp.awaladroid.GatewayBindingException
import tech.relaycorp.awaladroid.GatewayClient
import tech.relaycorp.awaladroid.endpoint.FirstPartyEndpoint
import tech.relaycorp.awaladroid.endpoint.InvalidThirdPartyEndpoint
import tech.relaycorp.awaladroid.endpoint.PrivateThirdPartyEndpoint
import tech.relaycorp.awaladroid.endpoint.PublicThirdPartyEndpoint
import tech.relaycorp.awaladroid.endpoint.ThirdPartyEndpoint
import tech.relaycorp.awaladroid.messaging.OutgoingMessage
import tech.relaycorp.letro.R
import tech.relaycorp.letro.awala.message.AwalaOutgoingMessage
import tech.relaycorp.letro.awala.message.MessageRecipient
import tech.relaycorp.letro.awala.message.MessageType
import tech.relaycorp.letro.awala.processor.AwalaMessageProcessor
import tech.relaycorp.letro.ui.navigation.Route
import tech.relaycorp.letro.utils.awala.loadNonNullFirstPartyEndpoint
import tech.relaycorp.letro.utils.awala.loadNonNullThirdPartyEndpoint
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

interface AwalaManager {
    suspend fun sendMessage(
        outgoingMessage: AwalaOutgoingMessage,
        recipient: MessageRecipient,
    )
    suspend fun isAwalaInstalled(currentScreen: Route): Boolean
    suspend fun authorizeUsers(
        // TODO: after MVP handle several first party endpoints
        thirdPartyPublicKey: ByteArray,
    )
    suspend fun getFirstPartyPublicKey(): String
    suspend fun importPrivateThirdPartyAuth(auth: ByteArray): String
}

@OptIn(ExperimentalCoroutinesApi::class)
class AwalaManagerImpl @Inject constructor(
    private val awalaRepository: AwalaRepository,
    @ApplicationContext private val context: Context,
    private val processor: AwalaMessageProcessor,
) : AwalaManager {

    private val awalaScope = CoroutineScope(Dispatchers.IO)

    @OptIn(DelicateCoroutinesApi::class)
    private val awalaThreadContext = newSingleThreadContext("AwalaManagerThread")

    @OptIn(DelicateCoroutinesApi::class)
    private val messageReceivingThreadContext = newSingleThreadContext("AwalaManagerMessageReceiverThread")

    private var isAwalaSetUp = AtomicBoolean(false)
    private var awalaSetupJob: Job? = null

    @Volatile
    private var isAwalaInstalledOnDevice: Boolean? = null

    private var isReceivingMessages = false

    private var firstPartyEndpoint: FirstPartyEndpoint? = null
    private var thirdPartyServerEndpoint: ThirdPartyEndpoint? = null

    init {
        awalaSetupJob = awalaScope.launch {
            withContext(awalaThreadContext) {
                Log.i(TAG, "Setting up Awala")
                Awala.setUp(context)
                checkIfAwalaAppInstalled()
                isAwalaSetUp.compareAndSet(false, true)
                awalaSetupJob = null
            }
        }
    }

    override suspend fun sendMessage(
        outgoingMessage: AwalaOutgoingMessage,
        recipient: MessageRecipient,
    ) {
        withContext(awalaThreadContext) {
            if (outgoingMessage.type != MessageType.AuthorizeReceivingFromServer && awalaSetupJob != null) {
                Log.i(TAG, "Awala wasn't initialized while tried to send a message. Wait for completion... $outgoingMessage")
                awalaSetupJob?.join()
                Log.i(TAG, "Awala was initialized, proceed futher...")
            }
            val firstPartyEndpoint = loadFirstPartyEndpoint()
            val thirdPartyEndpoint = loadThirdPartyEndpoint(recipient)
            Log.i(TAG, "sendMessage() from ${firstPartyEndpoint.nodeId} to ${thirdPartyEndpoint.nodeId}: $outgoingMessage)")
            GatewayClient.sendMessage(
                OutgoingMessage.build(
                    type = outgoingMessage.type.value,
                    content = outgoingMessage.content,
                    senderEndpoint = firstPartyEndpoint,
                    recipientEndpoint = thirdPartyEndpoint,
                ),
            )
        }
    }

    override suspend fun isAwalaInstalled(currentScreen: Route): Boolean {
        val isInstalled = withContext(awalaThreadContext) {
            if (!isAwalaSetUp.get()) {
                awalaSetupJob?.join()
            }
            if (currentScreen == Route.AwalaNotInstalled) {
                checkIfAwalaAppInstalled()
            } else {
                isAwalaInstalledOnDevice ?: checkIfAwalaAppInstalled()
            }
        }
        return isInstalled
    }

    override suspend fun authorizeUsers(thirdPartyPublicKey: ByteArray) {
        withContext(awalaThreadContext) {
            val firstPartyEndpoint = loadFirstPartyEndpoint()
            val auth = firstPartyEndpoint.authorizeIndefinitely(thirdPartyPublicKey)
            sendMessage(
                outgoingMessage = AwalaOutgoingMessage(
                    type = MessageType.ContactPairingAuthorization,
                    content = auth,
                ),
                recipient = MessageRecipient.Server(),
            )
        }
    }

    override suspend fun getFirstPartyPublicKey(): String {
        return withContext(awalaThreadContext) {
            val firstPartyEndpoint = loadFirstPartyEndpoint()
            Base64.encodeToString(firstPartyEndpoint.publicKey.encoded, Base64.NO_WRAP)
        }
    }

    override suspend fun importPrivateThirdPartyAuth(auth: ByteArray): String {
        return PrivateThirdPartyEndpoint.import(auth).nodeId
    }

    private suspend fun loadFirstPartyEndpoint(): FirstPartyEndpoint {
        return withContext(awalaThreadContext) {
            val firstPartyEndpointNodeId = awalaRepository.getServerFirstPartyEndpointNodeId()
                ?: registerFirstPartyEndpointIfNeeded()?.nodeId
                ?: throw IllegalStateException("You should register first party endpoint first!")
            firstPartyEndpoint ?: loadNonNullFirstPartyEndpoint(firstPartyEndpointNodeId)
        }
    }

    private suspend fun loadThirdPartyEndpoint(recipient: MessageRecipient): ThirdPartyEndpoint {
        return withContext(awalaThreadContext) {
            if (recipient is MessageRecipient.Server) {
                thirdPartyServerEndpoint?.let {
                    return@withContext it
                }
            }
            val thirdPartyEndpointNodeId = when (recipient) {
                is MessageRecipient.Server -> {
                    recipient.nodeId
                        ?: awalaRepository.getServerThirdPartyEndpointNodeId()
                        ?: importServerThirdPartyEndpointIfNeeded()?.nodeId
                        ?: throw IllegalStateException("You should register third party endpoint first!")
                }

                is MessageRecipient.User -> {
                    Log.e(TAG, "Cannot find third-party endpoint ${recipient.nodeId}")
                    throw IllegalStateException("Cannot find third-party endpoint ${recipient.nodeId}")
                }
            }
            loadNonNullThirdPartyEndpoint(thirdPartyEndpointNodeId)
        }
    }

    private suspend fun startReceivingMessages() {
        awalaScope.launch(messageReceivingThreadContext) {
            if (isReceivingMessages) {
                return@launch
            }
            isReceivingMessages = true

            Log.i(TAG, "start receiving messages...")
            GatewayClient.receiveMessages().collect { message ->
                Log.i(TAG, "Receive message: ${message.type}: ($message)")
                processor.process(message, this@AwalaManagerImpl)
                Log.i(TAG, "Message processed")
                message.ack()
            }
        }
    }

    private suspend fun configureAwala() {
        withContext(awalaThreadContext) {
            registerFirstPartyEndpointIfNeeded()
            importServerThirdPartyEndpointIfNeeded()
        }
    }

    private suspend fun checkIfAwalaAppInstalled(): Boolean {
        return withContext(awalaThreadContext) {
            try {
                Log.i(TAG, "GatewayClient binding...")
                GatewayClient.bind()
                Log.i(TAG, "GatewayClient bound")
                configureAwala()
            } catch (exp: GatewayBindingException) {
                this@AwalaManagerImpl.isAwalaInstalledOnDevice = false
                return@withContext false
            }
            this@AwalaManagerImpl.isAwalaInstalledOnDevice = true
            true
        }
    }

    private suspend fun registerFirstPartyEndpointIfNeeded(): FirstPartyEndpoint? {
        return withContext(awalaThreadContext) {
            if (awalaRepository.getServerFirstPartyEndpointNodeId() != null) {
                Log.i(TAG, "First party endpoint is already registred ${awalaRepository.getServerFirstPartyEndpointNodeId()}")
                startReceivingMessages()
                return@withContext null
            }
            Log.i(TAG, "Will register first-party endpoint...")
            val firstPartyEndpoint = FirstPartyEndpoint.register()
            Log.i(TAG, "First-party endpoint registered (${firstPartyEndpoint.nodeId})")
            awalaRepository.saveServerFirstPartyEndpointNodeId(firstPartyEndpoint.nodeId)
            startReceivingMessages()
            firstPartyEndpoint
        }
    }

    private suspend fun importServerThirdPartyEndpointIfNeeded(): ThirdPartyEndpoint? {
        return withContext(awalaThreadContext) {
            if (awalaRepository.getServerThirdPartyEndpointNodeId() != null) {
                return@withContext null
            }

            val firstPartyEndpointNodeId = awalaRepository.getServerFirstPartyEndpointNodeId()
                ?: registerFirstPartyEndpointIfNeeded()?.nodeId
                ?: throw IllegalStateException("You should register first party endpoint first!")

            val thirdPartyEndpoint = importServerThirdPartyEndpoint(
                connectionParams = R.raw.server_connection_params,
            )
            Log.i(TAG, "Server third party endpoint was imported ${thirdPartyEndpoint.nodeId}")

            val firstPartyEndpoint = loadNonNullFirstPartyEndpoint(firstPartyEndpointNodeId)

            // Create the Parcel Delivery Authorisation (PDA)
            val auth = firstPartyEndpoint.authorizeIndefinitely(thirdPartyEndpoint)
            sendMessage(
                outgoingMessage = AwalaOutgoingMessage(
                    type = MessageType.AuthorizeReceivingFromServer,
                    content = auth,
                ),
                recipient = MessageRecipient.Server(
                    nodeId = thirdPartyEndpoint.nodeId,
                ),
            )
            awalaRepository.saveServerThirdPartyEndpointNodeId(thirdPartyEndpoint.nodeId)
            thirdPartyEndpoint
        }
    }

    @Throws(InvalidConnectionParams::class)
    private suspend fun importServerThirdPartyEndpoint(
        @RawRes connectionParams: Int,
    ): PublicThirdPartyEndpoint {
        return withContext(awalaThreadContext) {
            val endpoint = try {
                PublicThirdPartyEndpoint.import(
                    context.resources.openRawResource(connectionParams).use {
                        it.readBytes()
                    },
                )
            } catch (e: InvalidThirdPartyEndpoint) {
                throw InvalidConnectionParams(e)
            }
            endpoint
        }
    }

    companion object {
        const val TAG = "AwalaManager"
    }
}

internal class InvalidConnectionParams(cause: Throwable) : Exception(cause)