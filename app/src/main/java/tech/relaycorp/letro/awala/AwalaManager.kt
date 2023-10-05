package tech.relaycorp.letro.awala

import android.content.Context
import android.util.Base64
import androidx.annotation.IntDef
import androidx.annotation.RawRes
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.withContext
import tech.relaycorp.awaladroid.AwaladroidException
import tech.relaycorp.awaladroid.EncryptionInitializationException
import tech.relaycorp.awaladroid.GatewayBindingException
import tech.relaycorp.awaladroid.endpoint.FirstPartyEndpoint
import tech.relaycorp.awaladroid.endpoint.PrivateThirdPartyEndpoint
import tech.relaycorp.awaladroid.endpoint.PublicThirdPartyEndpoint
import tech.relaycorp.awaladroid.endpoint.ThirdPartyEndpoint
import tech.relaycorp.letro.R
import tech.relaycorp.letro.awala.AwalaInitializationState.Companion.AWALA_NOT_INSTALLED
import tech.relaycorp.letro.awala.AwalaInitializationState.Companion.AWALA_SET_UP
import tech.relaycorp.letro.awala.AwalaInitializationState.Companion.FIRST_PARTY_ENDPOINT_REGISTRED
import tech.relaycorp.letro.awala.AwalaInitializationState.Companion.GATEWAY_CLIENT_BINDED
import tech.relaycorp.letro.awala.AwalaInitializationState.Companion.INITIALIZATION_FATAL_ERROR
import tech.relaycorp.letro.awala.AwalaInitializationState.Companion.INITIALIZATION_NONFATAL_ERROR
import tech.relaycorp.letro.awala.AwalaInitializationState.Companion.INITIALIZED
import tech.relaycorp.letro.awala.AwalaInitializationState.Companion.NOT_INITIALIZED
import tech.relaycorp.letro.awala.di.AwalaThreadContext
import tech.relaycorp.letro.awala.message.AwalaOutgoingMessage
import tech.relaycorp.letro.awala.message.MessageRecipient
import tech.relaycorp.letro.awala.message.MessageType
import tech.relaycorp.letro.awala.processor.AwalaMessageProcessor
import tech.relaycorp.letro.utils.Logger
import tech.relaycorp.letro.utils.di.IODispatcher
import tech.relaycorp.letro.utils.ext.emitOnDelayed
import java.lang.Thread.UncaughtExceptionHandler
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

interface AwalaManager {
    val awalaInitializationState: StateFlow<Int>
    val awalaUnsuccessfulBindings: SharedFlow<Unit>
    suspend fun sendMessage(
        outgoingMessage: AwalaOutgoingMessage,
        recipient: MessageRecipient,
    )
    fun initializeGatewayAsync()

    suspend fun authorizeUsers(
        // TODO: after MVP handle several first party endpoints
        thirdPartyPublicKey: ByteArray,
    )
    suspend fun getFirstPartyPublicKey(): String
    suspend fun importPrivateThirdPartyAuth(auth: ByteArray): String
}

@OptIn(ExperimentalCoroutinesApi::class)
class AwalaManagerImpl @Inject constructor(
    private val awala: AwalaWrapper,
    private val awalaRepository: AwalaRepository,
    @ApplicationContext private val context: Context,
    private val processor: AwalaMessageProcessor,
    private val logger: Logger,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
    @AwalaThreadContext private val awalaThreadContext: CoroutineContext,
) : AwalaManager {

    private val awalaScope = CoroutineScope(ioDispatcher)

    @OptIn(DelicateCoroutinesApi::class)
    private val messageReceivingThreadContext = newSingleThreadContext("AwalaManagerMessageReceiverThread")

    private val _awalaInitializationState = MutableStateFlow<Int>(AwalaInitializationState.NOT_INITIALIZED)
    override val awalaInitializationState: StateFlow<Int>
        get() = _awalaInitializationState
    private var awalaSetupJob: Job? = null

    private val _awalaUnsuccessfulBindings = MutableSharedFlow<Unit>()
    override val awalaUnsuccessfulBindings: SharedFlow<Unit>
        get() = _awalaUnsuccessfulBindings

    private var isReceivingMessages = false

    private var firstPartyEndpoint: FirstPartyEndpoint? = null
    private var thirdPartyServerEndpoint: ThirdPartyEndpoint? = null

    private var previousUncaughtExceptionHandler: UncaughtExceptionHandler? = null
    private var uncaughtExceptionHandler = UncaughtExceptionHandler { thread, exception ->
        if (exception is EncryptionInitializationException) {
            logger.e(TAG, "Uncaught exception due to failure to use Android security library", exception)
            _awalaInitializationState.emitOnDelayed(INITIALIZATION_FATAL_ERROR, awalaScope, 1_000L, awalaThreadContext)
        } else {
            previousUncaughtExceptionHandler?.uncaughtException(thread, exception)
        }
    }

    init {
        previousUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler(uncaughtExceptionHandler)
        awalaSetupJob = awalaScope.launch {
            withContext(awalaThreadContext) {
                logger.i(TAG, "Setting up Awala")
                try {
                    awala.setUp()
                } catch (e: EncryptionInitializationException) {
                    logger.e(TAG, "Failed to set up Awala due to Android security lib bug", e)
                    _awalaInitializationState.emit(INITIALIZATION_FATAL_ERROR)
                    awalaSetupJob = null
                    return@withContext
                }
                _awalaInitializationState.emit(AWALA_SET_UP)
                initializeGateway()
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
                logger.i(TAG, "Awala wasn't initialized while tried to send a message. Wait for completion... $outgoingMessage")
                awalaSetupJob?.join()
                logger.i(TAG, "Awala was initialized, proceed futher...")
            }
            val firstPartyEndpoint = loadFirstPartyEndpoint()
            val thirdPartyEndpoint = loadThirdPartyEndpoint(
                sender = firstPartyEndpoint,
                recipient = recipient,
            )
            logger.i(TAG, "sendMessage() from ${firstPartyEndpoint.nodeId} to ${thirdPartyEndpoint.nodeId}: $outgoingMessage)")
            awala.sendMessage(
                outgoingMessage = outgoingMessage,
                firstPartyEndpoint = firstPartyEndpoint,
                thirdPartyEndpoint = thirdPartyEndpoint,
            )
        }
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
            firstPartyEndpoint ?: awala.loadNonNullPublicFirstPartyEndpoint(firstPartyEndpointNodeId)
        }
    }

    private suspend fun loadThirdPartyEndpoint(
        sender: FirstPartyEndpoint,
        recipient: MessageRecipient,
    ): ThirdPartyEndpoint {
        return withContext(awalaThreadContext) {
            if (recipient is MessageRecipient.Server) {
                thirdPartyServerEndpoint?.let {
                    return@withContext it
                }
            }
            when (recipient) {
                is MessageRecipient.Server -> {
                    val nodeId = recipient.nodeId
                        ?: awalaRepository.getServerThirdPartyEndpointNodeId()
                        ?: importServerThirdPartyEndpointIfNeeded()?.nodeId
                        ?: throw IllegalStateException("You should register third party endpoint first!")
                    awala.loadNonNullPublicThirdPartyEndpoint(nodeId)
                }

                is MessageRecipient.User -> {
                    val senderNodeId = sender.nodeId
                    val recipientNodeId = recipient.nodeId
                    awala.loadNonNullPrivateThirdPartyEndpoint(
                        senderNodeId = senderNodeId,
                        recipientNodeId = recipientNodeId,
                    )
                }
            }
        }
    }

    private suspend fun startReceivingMessages() {
        awalaScope.launch(messageReceivingThreadContext) {
            if (isReceivingMessages) {
                return@launch
            }
            isReceivingMessages = true

            logger.i(TAG, "start receiving messages...")
            awala.receiveMessages().collect { message ->
                logger.i(TAG, "Receive message: ${message.type}: ($message)")
                processor.process(message, this@AwalaManagerImpl)
                logger.i(TAG, "Message ${message.type} processed")
                message.ack()
            }
        }
    }

    private suspend fun configureAwala() {
        withContext(awalaThreadContext) {
            try {
                registerFirstPartyEndpointIfNeeded()
            } catch (e: EncryptionInitializationException) {
                logger.e(TAG, "Failed to register endpoint due to Android security lib bug", e)
                _awalaInitializationState.emit(INITIALIZATION_FATAL_ERROR)
                return@withContext
            } catch (e: AwaladroidException) {
                logger.e(TAG, "Failed to register endpoint", e)
                _awalaInitializationState.emit(INITIALIZATION_NONFATAL_ERROR)
                return@withContext
            }
            _awalaInitializationState.emit(FIRST_PARTY_ENDPOINT_REGISTRED)
            try {
                importServerThirdPartyEndpointIfNeeded()
            } catch (e: EncryptionInitializationException) {
                logger.e(TAG, "Failed to import Letro server endpoint due to Android security lib bug", e)
                _awalaInitializationState.emit(INITIALIZATION_FATAL_ERROR)
                return@withContext
            } catch (e: AwaladroidException) {
                logger.e(TAG, "Failed to import Letro server endpoint", e)
                _awalaInitializationState.emit(INITIALIZATION_NONFATAL_ERROR)
                return@withContext
            }
            _awalaInitializationState.emit(INITIALIZED)
            logger.d(TAG, "Awala is initialized")
        }
    }

    override fun initializeGatewayAsync() {
        awalaScope.launch(awalaThreadContext) {
            initializeGateway()
        }
    }

    private suspend fun initializeGateway() {
        withContext(awalaThreadContext) {
            try {
                logger.i(TAG, "GatewayClient binding...")
                awala.bindGateway()
                _awalaInitializationState.emit(GATEWAY_CLIENT_BINDED)
                logger.i(TAG, "GatewayClient bound")
                configureAwala()
            } catch (exp: GatewayBindingException) {
                logger.i(TAG, "GatewayClient cannot be bound: $exp")
                _awalaUnsuccessfulBindings.emit(Unit)
                _awalaInitializationState.emit(AWALA_NOT_INSTALLED)
            }
        }
    }

    private suspend fun registerFirstPartyEndpointIfNeeded(): FirstPartyEndpoint? {
        return withContext(awalaThreadContext) {
            if (awalaRepository.getServerFirstPartyEndpointNodeId() != null) {
                logger.i(TAG, "First party endpoint is already registred ${awalaRepository.getServerFirstPartyEndpointNodeId()}")
                startReceivingMessages()
                return@withContext null
            }
            logger.i(TAG, "Will register first-party endpoint...")
            val firstPartyEndpoint = awala.registerFirstPartyEndpoint()
            logger.i(TAG, "First-party endpoint registered (${firstPartyEndpoint.nodeId})")
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
            logger.i(TAG, "Server third party endpoint was imported ${thirdPartyEndpoint.nodeId}")

            val firstPartyEndpoint = awala.loadNonNullPublicFirstPartyEndpoint(firstPartyEndpointNodeId)

            // Create the Parcel Delivery Authorisation (PDA)
            val auth = awala.authorizeIndefinitely(firstPartyEndpoint, thirdPartyEndpoint)
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
            awala.importServerThirdPartyEndpoint(connectionParams)
        }
    }

    companion object {
        const val TAG = "AwalaManager"
    }
}

internal class InvalidConnectionParams(cause: Throwable) : Exception(cause)

@IntDef(
    INITIALIZATION_FATAL_ERROR,
    INITIALIZATION_NONFATAL_ERROR,
    AWALA_NOT_INSTALLED,
    NOT_INITIALIZED,
    AWALA_SET_UP,
    GATEWAY_CLIENT_BINDED,
    FIRST_PARTY_ENDPOINT_REGISTRED,
    INITIALIZED,
)
annotation class AwalaInitializationState {
    companion object {
        const val INITIALIZATION_FATAL_ERROR = -999
        const val INITIALIZATION_NONFATAL_ERROR = -2
        const val AWALA_NOT_INSTALLED = -1
        const val NOT_INITIALIZED = 0
        const val AWALA_SET_UP = 1
        const val GATEWAY_CLIENT_BINDED = 2
        const val FIRST_PARTY_ENDPOINT_REGISTRED = 3
        const val INITIALIZED = 4
    }
}
