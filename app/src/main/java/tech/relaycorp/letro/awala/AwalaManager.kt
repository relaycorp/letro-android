package tech.relaycorp.letro.awala

import android.content.Context
import android.util.Base64
import android.util.Log
import androidx.annotation.RawRes
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
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
import javax.inject.Inject

interface AwalaManager {
    val incomingMessages: Flow<AwalaIncomingMessage<*>>
    suspend fun sendMessage(
        outgoingMessage: AwalaOutgoingMessage,
        recipient: MessageRecipient,
    )
    suspend fun authorizeUsers(
        // TODO: after MVP handle several first party endpoints
        thirdPartyPublicKey: ByteArray,
    )
    suspend fun isAwalaInstalled(currentScreen: Route): Boolean
    suspend fun getFirstPartyPublicKey(): String
}

class AwalaManagerImpl @Inject constructor(
    private val awalaRepository: AwalaRepository,
    @ApplicationContext private val context: Context,
    private val parser: AwalaMessageParser,
) : AwalaManager {

    private val awalaScope = CoroutineScope(Dispatchers.IO)

    private val _incomingMessages = Channel<AwalaIncomingMessage<*>>()
    override val incomingMessages: Flow<AwalaIncomingMessage<*>>
        get() = _incomingMessages.receiveAsFlow()

    @Volatile
    private var isAwalaSetUp = false
    private var awalaSetupJob: Job? = null

    @Volatile
    private var isAwalaInstalledOnDevice: Boolean? = null

    @Volatile
    private var isReceivingMessages = false

    private var firstPartyEndpoint: FirstPartyEndpoint? = null
    private var thirdPartyServerEndpoint: ThirdPartyEndpoint? = null

    init {
        awalaSetupJob = awalaScope.launch {
            Awala.setUp(context)
            checkIfAwalaAppInstalled()
            isAwalaSetUp = true
            awalaSetupJob = null
        }
    }

    override suspend fun sendMessage(
        outgoingMessage: AwalaOutgoingMessage,
        recipient: MessageRecipient,
    ) {
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

    override suspend fun isAwalaInstalled(currentScreen: Route): Boolean {
        if (!isAwalaSetUp) {
            awalaSetupJob?.join()
        }
        return if (currentScreen == Route.AwalaNotInstalled) {
            checkIfAwalaAppInstalled()
        } else {
            isAwalaInstalledOnDevice ?: checkIfAwalaAppInstalled()
        }
    }

    override suspend fun authorizeUsers(thirdPartyPublicKey: ByteArray) {
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

    override suspend fun getFirstPartyPublicKey(): String {
        val firstPartyEndpoint = loadFirstPartyEndpoint()
        return Base64.encodeToString(firstPartyEndpoint.publicKey.encoded, Base64.NO_WRAP)
    }

    private suspend fun loadFirstPartyEndpoint(): FirstPartyEndpoint {
        val firstPartyEndpointNodeId = awalaRepository.getServerFirstPartyEndpointNodeId()
            ?: registerFirstPartyEndpointIfNeeded()?.nodeId
            ?: throw IllegalStateException("You should register first party endpoint first!")
        return firstPartyEndpoint ?: loadNonNullFirstPartyEndpoint(firstPartyEndpointNodeId)
    }

    private suspend fun loadThirdPartyEndpoint(recipient: MessageRecipient): ThirdPartyEndpoint {
        if (recipient is MessageRecipient.Server) {
            thirdPartyServerEndpoint?.let {
                return it
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
        return loadNonNullThirdPartyEndpoint(thirdPartyEndpointNodeId)
    }

    private suspend fun startReceivingMessages() {
        if (isReceivingMessages) {
            return
        }

        awalaScope.launch {
            Log.i(TAG, "start receiving messages...")
            GatewayClient.receiveMessages().collect { message ->
                val type = MessageType.from(message.type)
                val parsedMessage = parser.parse(type, message.content).also { Log.i(TAG, "Receive message: ($it)") }
                _incomingMessages.send(parsedMessage)
                message.ack()
            }
        }

        isReceivingMessages = true
    }

    private suspend fun configureAwala() {
        registerFirstPartyEndpointIfNeeded()
        importServerThirdPartyEndpointIfNeeded()
    }

    private suspend fun checkIfAwalaAppInstalled(): Boolean {
        try {
            GatewayClient.bind()
            configureAwala()
        } catch (exp: GatewayBindingException) {
            this.isAwalaInstalledOnDevice = false
            return false
        }
        this.isAwalaInstalledOnDevice = true
        return true
    }

    private suspend fun registerFirstPartyEndpointIfNeeded(): FirstPartyEndpoint? {
        if (awalaRepository.getServerFirstPartyEndpointNodeId() != null) {
            startReceivingMessages()
            return null
        }
        val firstPartyEndpoint = FirstPartyEndpoint.register()
        awalaRepository.saveServerFirstPartyEndpointNodeId(firstPartyEndpoint.nodeId)
        Log.i(TAG, "First party endpoint was registred: ${firstPartyEndpoint.nodeId}")
        startReceivingMessages()
        return firstPartyEndpoint
    }

    private suspend fun importServerThirdPartyEndpointIfNeeded(): ThirdPartyEndpoint? {
        if (awalaRepository.getServerThirdPartyEndpointNodeId() != null) {
            return null
        }

        val firstPartyEndpointNodeId = awalaRepository.getServerFirstPartyEndpointNodeId()
            ?: registerFirstPartyEndpointIfNeeded()?.nodeId
            ?: throw IllegalStateException("You should register first party endpoint first!")

        val thirdPartyEndpoint = importServerThirdPartyEndpoint(
            connectionParams = R.raw.server_connection_params,
        )

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
        return thirdPartyEndpoint
    }

    @Throws(InvalidConnectionParams::class)
    private suspend fun importServerThirdPartyEndpoint(
        @RawRes connectionParams: Int,
    ): PublicThirdPartyEndpoint {
        val endpoint = try {
            PublicThirdPartyEndpoint.import(
                context.resources.openRawResource(connectionParams).use {
                    it.readBytes()
                },
            )
        } catch (e: InvalidThirdPartyEndpoint) {
            throw InvalidConnectionParams(e)
        }
        return endpoint
    }

    companion object {
        const val TAG = "AwalaManager"
    }
}

internal class InvalidConnectionParams(cause: Throwable) : Exception(cause)
