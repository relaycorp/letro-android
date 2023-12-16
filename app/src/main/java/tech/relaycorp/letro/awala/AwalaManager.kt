package tech.relaycorp.letro.awala

import androidx.annotation.IntDef
import androidx.annotation.RawRes
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
import tech.relaycorp.letro.account.model.Account
import tech.relaycorp.letro.awala.AwalaInitializationState.Companion.AWALA_NOT_INSTALLED
import tech.relaycorp.letro.awala.AwalaInitializationState.Companion.AWALA_SET_UP
import tech.relaycorp.letro.awala.AwalaInitializationState.Companion.INITIALIZATION_FATAL_ERROR
import tech.relaycorp.letro.awala.AwalaInitializationState.Companion.INITIALIZATION_NONFATAL_ERROR
import tech.relaycorp.letro.awala.AwalaInitializationState.Companion.INITIALIZED
import tech.relaycorp.letro.awala.AwalaInitializationState.Companion.NOT_INITIALIZED
import tech.relaycorp.letro.awala.di.AwalaThreadContext
import tech.relaycorp.letro.awala.message.AwalaEndpoint
import tech.relaycorp.letro.awala.message.AwalaOutgoingMessage
import tech.relaycorp.letro.awala.message.MessageType
import tech.relaycorp.letro.awala.processor.AwalaCommonMessageProcessor
import tech.relaycorp.letro.utils.Logger
import tech.relaycorp.letro.utils.di.IODispatcher
import tech.relaycorp.letro.utils.ext.emitOnDelayed
import java.lang.Thread.UncaughtExceptionHandler
import java.security.PublicKey
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

interface AwalaManager {
    val awalaInitializationState: StateFlow<Int>
    val awalaUnsuccessfulBindings: SharedFlow<Unit>
    val awalaUnsuccessfulConfigurations: SharedFlow<Unit>
    suspend fun sendMessage(
        outgoingMessage: AwalaOutgoingMessage,
        recipient: AwalaEndpoint,
        senderAccount: Account?,
    ): String
    fun initializeGatewayAsync()

    suspend fun authorizeContact(
        ownerAccount: Account,
        thirdPartyPublicKey: ByteArray,
    ): String
    suspend fun authorizePublicThirdPartyEndpoint(
        account: Account,
        thirdPartyEndpoint: PublicThirdPartyEndpoint,
    )
    suspend fun revokeAuthorization(
        ownerAccount: Account,
        user: AwalaEndpoint,
    )
    suspend fun getFirstPartyPublicKey(
        ownerAccount: Account,
    ): PublicKey
    suspend fun importPrivateThirdPartyAuth(auth: ByteArray): String
    suspend fun getServerThirdPartyEndpoint(
        firstPartyEndpointNodeId: String,
    ): ThirdPartyEndpoint?
}

@OptIn(ExperimentalCoroutinesApi::class)
class AwalaManagerImpl @Inject constructor(
    private val awala: AwalaWrapper,
    private val awalaRepository: AwalaRepository,
    private val processor: AwalaCommonMessageProcessor,
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

    private val _awalaUnsuccessfulConfigurations = MutableSharedFlow<Unit>()
    override val awalaUnsuccessfulConfigurations: SharedFlow<Unit>
        get() = _awalaUnsuccessfulConfigurations

    private var cachedFirstPartyEndpoints = hashMapOf<Long, FirstPartyEndpoint>()
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

    /**
     * Returns first party node id, from which a message was sent
     */
    @Throws(AwaladroidException::class)
    override suspend fun sendMessage(
        outgoingMessage: AwalaOutgoingMessage,
        recipient: AwalaEndpoint,
        senderAccount: Account?,
    ): String {
        return withContext(awalaThreadContext) {
            if (outgoingMessage.type != MessageType.AuthorizeReceivingFromServer && awalaSetupJob != null) {
                logger.i(TAG, "Awala wasn't initialized while tried to send a message. Wait for completion... $outgoingMessage")
                awalaSetupJob?.join()
                logger.i(TAG, "Awala was initialized, proceed futher...")
            }
            val firstPartyEndpoint = loadFirstPartyEndpoint(senderAccount)
            val thirdPartyEndpoint = loadThirdPartyEndpoint(
                sender = firstPartyEndpoint,
                recipient = recipient,
            )
            if (senderAccount == null) {
                // Create the Parcel Delivery Authorisation (PDA) for newly created first party endpoint
                awala.authorizeIndefinitely(
                    firstPartyEndpoint = firstPartyEndpoint,
                    thirdPartyEndpoint = thirdPartyEndpoint,
                )
            }
            try {
                awala.sendMessage(
                    outgoingMessage = outgoingMessage,
                    firstPartyEndpoint = firstPartyEndpoint,
                    thirdPartyEndpoint = thirdPartyEndpoint,
                )
            } catch (e: EncryptionInitializationException) {
                logger.e(TAG, "Failed to register endpoint due to Android security lib bug", e)
                _awalaInitializationState.emit(INITIALIZATION_FATAL_ERROR)
                throw AwalaException("Failed to register endpoint due to Android security lib bug")
            }
            firstPartyEndpoint.nodeId
        }
    }

    @Throws(AwaladroidException::class)
    override suspend fun authorizePublicThirdPartyEndpoint(
        account: Account,
        thirdPartyEndpoint: PublicThirdPartyEndpoint,
    ) {
        withContext(awalaThreadContext) {
            val firstPartyEndpoint = loadFirstPartyEndpoint(account)
            awala.authorizeIndefinitely(
                firstPartyEndpoint = firstPartyEndpoint,
                thirdPartyEndpoint = thirdPartyEndpoint,
            )
        }
    }

    @Throws(AwaladroidException::class)
    override suspend fun authorizeContact(
        ownerAccount: Account,
        thirdPartyPublicKey: ByteArray,
    ): String {
        return withContext(awalaThreadContext) {
            val firstPartyEndpoint = loadFirstPartyEndpoint(ownerAccount)
            val auth = firstPartyEndpoint.authorizeIndefinitely(thirdPartyPublicKey)
            sendMessage(
                outgoingMessage = AwalaOutgoingMessage(
                    type = MessageType.ContactPairingAuthorization,
                    content = auth.auth,
                ),
                recipient = AwalaEndpoint.Public(),
                senderAccount = ownerAccount,
            )
            return@withContext auth.endpointId
        }
    }

    @Throws(AwaladroidException::class)
    override suspend fun revokeAuthorization(
        ownerAccount: Account,
        user: AwalaEndpoint,
    ) {
        withContext(awalaThreadContext) {
            awala.revokeAuthorization(
                firstPartyEndpoint = loadFirstPartyEndpoint(ownerAccount),
                thirdPartyEndpoint = user,
            )
        }
    }

    @Throws(AwaladroidException::class)
    override suspend fun getFirstPartyPublicKey(
        ownerAccount: Account,
    ): PublicKey {
        return withContext(awalaThreadContext) {
            val firstPartyEndpoint = loadFirstPartyEndpoint(ownerAccount)
            firstPartyEndpoint.publicKey
        }
    }

    override suspend fun importPrivateThirdPartyAuth(auth: ByteArray): String {
        return PrivateThirdPartyEndpoint.import(auth).nodeId
    }

    @Throws(AwaladroidException::class)
    override suspend fun getServerThirdPartyEndpoint(
        firstPartyEndpointNodeId: String,
    ): ThirdPartyEndpoint? {
        thirdPartyServerEndpoint?.let { thirdPartyServerEndpoint ->
            return thirdPartyServerEndpoint
        }
        awalaRepository.getServerThirdPartyEndpointNodeId()?.let { serverNodeId ->
            return awala.loadNonNullPublicThirdPartyEndpoint(serverNodeId).apply {
                thirdPartyServerEndpoint = this
            }
        }
        this.importServerThirdPartyEndpoint()?.let { serverThirdPartyEndpoint ->
            return serverThirdPartyEndpoint.apply {
                thirdPartyServerEndpoint = this
            }
        }
        return null
    }

    @Throws(AwaladroidException::class)
    private suspend fun loadFirstPartyEndpoint(account: Account?): FirstPartyEndpoint {
        return withContext(awalaThreadContext) {
            cachedFirstPartyEndpoints[account?.id] ?: run {
                val firstPartyEndpointNodeId = account?.firstPartyEndpointNodeId
                if (firstPartyEndpointNodeId == null) {
                    registerFirstPartyEndpoint().also { endpoint ->
                        account?.id?.let { id ->
                            cachedFirstPartyEndpoints[id] = endpoint
                        }
                        return@withContext endpoint
                    }
                }
                awala.loadNonNullPublicFirstPartyEndpoint(firstPartyEndpointNodeId).also { endpoint ->
                    account?.id?.let { id ->
                        cachedFirstPartyEndpoints[id] = endpoint
                    }
                }
            }
        }
    }

    @Throws(AwalaException::class)
    private suspend fun loadThirdPartyEndpoint(
        sender: FirstPartyEndpoint,
        recipient: AwalaEndpoint,
    ): ThirdPartyEndpoint {
        return withContext(awalaThreadContext) {
            if (recipient is AwalaEndpoint.Public && recipient.nodeId == null) {
                return@withContext getServerThirdPartyEndpoint(sender.nodeId) ?: throw AwalaException("You should register third party endpoint first!")
            }
            when (recipient) {
                is AwalaEndpoint.Public -> {
                    awala.loadNonNullPublicThirdPartyEndpoint(recipient.nodeId)
                }

                is AwalaEndpoint.Private -> {
                    val senderNodeId = sender.nodeId
                    val recipientNodeId = recipient.nodeId
                    awala.loadNonNullPrivateThirdPartyEndpoint(
                        firstPartyNodeId = senderNodeId,
                        thirdPartyNodeId = recipientNodeId,
                    )
                }
            }
        }
    }

    private suspend fun startReceivingMessages() {
        awalaScope.launch(messageReceivingThreadContext) {
            logger.i(TAG, "start receiving messages...")
            awala.receiveMessages().collect { message ->
                logger.i(TAG, "Receive message: ${message.type}")
                processor.process(message, this@AwalaManagerImpl)
                logger.i(TAG, "Message ${message.type} processed")
                message.ack()
            }
        }
    }

//    private suspend fun configureEndpoints() {
//        withContext(awalaThreadContext) {
//            try {
//                registerFirstPartyEndpoint()
//            } catch (e: EncryptionInitializationException) {
//                logger.e(TAG, "Failed to register endpoint due to Android security lib bug", e)
//                _awalaInitializationState.emit(INITIALIZATION_FATAL_ERROR)
//                return@withContext
//            } catch (e: GatewayUnregisteredException) {
//                logger.e(TAG, "Failed to register endpoint", e)
//                _awalaUnsuccessfulConfigurations.emit(Unit)
//                _awalaInitializationState.emit(COULD_NOT_REGISTER_FIRST_PARTY_ENDPOINT)
//                return@withContext
//            } catch (e: AwaladroidException) {
//                logger.e(TAG, "Failed to register endpoint", e)
//                _awalaInitializationState.emit(INITIALIZATION_NONFATAL_ERROR)
//                return@withContext
//            }
//            _awalaInitializationState.emit(FIRST_PARTY_ENDPOINT_REGISTRED)
//            try {
//                this@AwalaManagerImpl.importServerThirdPartyEndpoint()
//            } catch (e: EncryptionInitializationException) {
//                logger.e(TAG, "Failed to import Letro server endpoint due to Android security lib bug", e)
//                _awalaInitializationState.emit(INITIALIZATION_FATAL_ERROR)
//                return@withContext
//            } catch (e: AwaladroidException) {
//                logger.e(TAG, "Failed to import Letro server endpoint", e)
//                _awalaInitializationState.emit(INITIALIZATION_NONFATAL_ERROR)
//                return@withContext
//            }
//            _awalaInitializationState.emit(INITIALIZED)
//            logger.d(TAG, "Awala is initialized")
//        }
//    }

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
                startReceivingMessages()
                _awalaInitializationState.emit(INITIALIZED)
            } catch (exp: GatewayBindingException) {
                logger.i(TAG, "GatewayClient cannot be bound: $exp")
                _awalaUnsuccessfulBindings.emit(Unit)
                _awalaInitializationState.emit(AWALA_NOT_INSTALLED)
            }
        }
    }

    @Throws(AwaladroidException::class)
    private suspend fun registerFirstPartyEndpoint(): FirstPartyEndpoint {
        return withContext(awalaThreadContext) {
            logger.i(TAG, "Will register first-party endpoint...")
            val firstPartyEndpoint = awala.registerFirstPartyEndpoint()
            logger.i(TAG, "First-party endpoint registered (${firstPartyEndpoint.nodeId})")
            firstPartyEndpoint
        }
    }

    @Throws(AwaladroidException::class)
    private suspend fun importServerThirdPartyEndpoint(): ThirdPartyEndpoint? {
        return withContext(awalaThreadContext) {
            if (awalaRepository.getServerThirdPartyEndpointNodeId() != null) {
                return@withContext null
            }

            val thirdPartyEndpoint = importServerThirdPartyEndpoint(
                connectionParams = R.raw.server_connection_params,
            )
            logger.i(TAG, "Server third party endpoint was imported ${thirdPartyEndpoint.nodeId}")
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
    INITIALIZED,
)
annotation class AwalaInitializationState {
    companion object {
        const val INITIALIZATION_FATAL_ERROR = -999
        const val INITIALIZATION_NONFATAL_ERROR = -2
        const val AWALA_NOT_INSTALLED = -1
        const val NOT_INITIALIZED = 0
        const val AWALA_SET_UP = 1
        const val INITIALIZED = 2
    }
}
