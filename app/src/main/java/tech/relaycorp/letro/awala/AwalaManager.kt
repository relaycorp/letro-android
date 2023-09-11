package tech.relaycorp.letro.awala

import android.content.Context
import android.util.Log
import androidx.annotation.RawRes
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.withContext
import tech.relaycorp.awaladroid.Awala
import tech.relaycorp.awaladroid.GatewayBindingException
import tech.relaycorp.awaladroid.GatewayClient
import tech.relaycorp.awaladroid.endpoint.FirstPartyEndpoint
import tech.relaycorp.awaladroid.endpoint.InvalidThirdPartyEndpoint
import tech.relaycorp.awaladroid.endpoint.PublicThirdPartyEndpoint
import tech.relaycorp.awaladroid.endpoint.ThirdPartyEndpoint
import tech.relaycorp.awaladroid.messaging.OutgoingMessage
import tech.relaycorp.letro.R
import tech.relaycorp.letro.awala.message.AwalaIncomingMessage
import tech.relaycorp.letro.awala.message.AwalaOutgoingMessage
import tech.relaycorp.letro.awala.message.MessageRecipient
import tech.relaycorp.letro.awala.message.MessageType
import tech.relaycorp.letro.awala.parser.AwalaMessageParser
import tech.relaycorp.letro.ui.navigation.Route
import tech.relaycorp.letro.utils.awala.loadNonNullFirstPartyEndpoint
import tech.relaycorp.letro.utils.awala.loadNonNullThirdPartyEndpoint
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

interface AwalaManager {
    val incomingMessages: Flow<AwalaIncomingMessage<*>>
    suspend fun sendMessage(
        outgoingMessage: AwalaOutgoingMessage,
        recipient: MessageRecipient,
    )
    suspend fun isAwalaInstalled(currentScreen: Route): Boolean
}

@OptIn(ExperimentalCoroutinesApi::class)
class AwalaManagerImpl @Inject constructor(
    private val awalaRepository: AwalaRepository,
    @ApplicationContext private val context: Context,
    private val parser: AwalaMessageParser,
) : AwalaManager {

    private val awalaScope = CoroutineScope(Dispatchers.IO)

    @OptIn(DelicateCoroutinesApi::class)
    private val awalaThreadContext = newSingleThreadContext("AwalaManagerThread")

    @OptIn(DelicateCoroutinesApi::class)
    private val messageReceivingThreadContext = newSingleThreadContext("AwalaManagerMessageReceiverThread")

    private val _incomingMessages = Channel<AwalaIncomingMessage<*>>()
    override val incomingMessages: Flow<AwalaIncomingMessage<*>>
        get() = _incomingMessages.receiveAsFlow()

    private var isAwalaSetUp = AtomicBoolean(false)
    private var awalaSetupJob: Job? = null

    @Volatile
    private var isAwalaInstalledOnDevice: Boolean? = null

    private var isReceivingMessages = false

    private var firstPartyEndpoint: FirstPartyEndpoint? = null

    private var thirdPartyServerEndpoint: ThirdPartyEndpoint? = null

    init {
        Log.i(TAG, "initializing")
        awalaSetupJob = awalaScope.launch {
            withContext(awalaThreadContext) {
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
                val type = MessageType.from(message.type)
                val parsedMessage = parser.parse(type, message.content)
                    .also { Log.i(TAG, "Receive message: ($it)") }
                _incomingMessages.send(parsedMessage)
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
                GatewayClient.bind()
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
                startReceivingMessages()
                return@withContext null
            }
            val firstPartyEndpoint = FirstPartyEndpoint.register()
            awalaRepository.saveServerFirstPartyEndpointNodeId(firstPartyEndpoint.nodeId)
            Log.i(TAG, "First party endpoint was registred ${firstPartyEndpoint.nodeId}")
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

    private companion object {
        private const val TAG = "AwalaManager"
    }
}

internal class InvalidConnectionParams(cause: Throwable) : Exception(cause)
