package tech.relaycorp.letro.awala

import android.content.Context
import android.util.Log
import androidx.annotation.RawRes
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
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
import tech.relaycorp.letro.awala.message.Message
import tech.relaycorp.letro.awala.message.MessageRecipient
import tech.relaycorp.letro.awala.message.MessageType
import tech.relaycorp.letro.ui.navigation.Route
import tech.relaycorp.letro.utils.awala.loadNonNullFirstPartyEndpoint
import tech.relaycorp.letro.utils.awala.loadNonNullThirdPartyEndpoint
import javax.inject.Inject

interface AwalaManager {
    val messages: Flow<Message>
    suspend fun sendMessage(
        message: Message,
        recipient: MessageRecipient,
    )
    suspend fun isAwalaInstalled(currentScreen: Route): Boolean
}

class AwalaManagerImpl @Inject constructor(
    private val awalaRepository: AwalaRepository,
    @ApplicationContext private val context: Context,
): AwalaManager {

    private val awalaScope = CoroutineScope(Dispatchers.IO)

    private val _messages = MutableSharedFlow<Message>()
    override val messages: Flow<Message>
        get() = _messages

    @Volatile
    private var isAwalaSetUp = false
    private var awalaSetupJob: Job? = null

    @Volatile
    private var isAwalaInstalledOnDevice: Boolean? = null

    @Volatile
    private var isReceivingMessages = false

    init {
        awalaSetupJob = awalaScope.launch {
            Awala.setUp(context)
            checkIfAwalaAppInstalled()
            isAwalaSetUp = true
            awalaSetupJob = null
        }
    }

    override suspend fun sendMessage(
        message: Message,
        recipient: MessageRecipient,
    ) {
        val firstPartyEndpointNodeId = awalaRepository.getServerFirstPartyEndpointNodeId()
            ?: registerFirstPartyEndpointIfNeeded()?.nodeId
            ?: ""

        val thirdPartyEndpointNodeId = when (recipient) {
            is MessageRecipient.Server -> {
                recipient.nodeId
                    ?: awalaRepository.getServerThirdPartyEndpointNodeId()
                    ?: importServerThirdPartyEndpointIfNeeded()?.nodeId
                    ?: ""
            }
            else -> {
                throw IllegalStateException("User messages are not supported yet!")
            }
        }

        if (firstPartyEndpointNodeId.isEmpty() || thirdPartyEndpointNodeId.isEmpty()) {
            throw IllegalStateException("some of ids is empty: $firstPartyEndpointNodeId ; $thirdPartyEndpointNodeId")
        }

        Log.d(TAG, "sendMessage() from $firstPartyEndpointNodeId to $thirdPartyEndpointNodeId: ${message})")

        val firstPartyEndpoint = loadNonNullFirstPartyEndpoint(firstPartyEndpointNodeId)
        val thirdPartyEndpoint = loadNonNullThirdPartyEndpoint(thirdPartyEndpointNodeId)

        GatewayClient.sendMessage(
            OutgoingMessage.build(
                type = message.type.value,
                content = message.content,
                senderEndpoint = firstPartyEndpoint,
                recipientEndpoint = thirdPartyEndpoint,
            )
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

    private suspend fun startReceivingMessages() {
        if (isReceivingMessages) {
            return
        }

        awalaScope.launch {
            Log.d(TAG, "start receiving messages...")
            GatewayClient.receiveMessages().collect { message ->
                // TODO Remove first message.ack() before publishing the app.
                // It's here to avoid the server getting stuck with messages that can't be processed.
                message.ack()
                _messages.emit(
                    Message(
                        type = MessageType.from(message.type),
                        content = message.content,
                    ).also { Log.d(TAG, "Receive message: ($it)") }
                )
                message.ack()
            }
        }

        isReceivingMessages = true
    }

    private suspend fun configureAwala() {
        registerFirstPartyEndpointIfNeeded()
        importServerThirdPartyEndpointIfNeeded()
        startReceivingMessages()
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
            return null
        }
        val firstPartyEndpoint = FirstPartyEndpoint.register()
        awalaRepository.saveServerFirstPartyEndpointNodeId(firstPartyEndpoint.nodeId)
        return firstPartyEndpoint
    }

    private suspend fun importServerThirdPartyEndpointIfNeeded(): ThirdPartyEndpoint? {
        if (awalaRepository.getServerThirdPartyEndpointNodeId() != null) {
            return null
        }

        val firstPartyEndpointNodeId = awalaRepository.getServerFirstPartyEndpointNodeId()
            ?: registerFirstPartyEndpointIfNeeded()?.nodeId
            ?: ""

        if (firstPartyEndpointNodeId.isEmpty()) {
            throw IllegalStateException("You should register first party endpoint first!")
        }

        val thirdPartyEndpoint = importServerThirdPartyEndpoint(
            connectionParams = R.raw.server_connection_params
        )

        val firstPartyEndpoint = loadNonNullFirstPartyEndpoint(firstPartyEndpointNodeId)

        // Create the Parcel Delivery Authorisation (PDA)
        val auth = firstPartyEndpoint.authorizeIndefinitely(thirdPartyEndpoint)
        sendMessage(
            message = Message(
                type = MessageType.AuthorizeReceivingFromServer,
                content = auth,
            ),
            recipient = MessageRecipient.Server(
                nodeId = thirdPartyEndpoint.nodeId
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
                }
            )
        } catch (e: InvalidThirdPartyEndpoint) {
            throw InvalidConnectionParams(e)
        }
        return endpoint
    }

    private companion object {
        private const val TAG = "AwalaManager"
        private const val SERVER = "ServerUniqueRecipientId1241242"
    }

}

internal class InvalidConnectionParams(cause: Throwable) : Exception(cause)