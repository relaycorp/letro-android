package tech.relaycorp.letro.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import tech.relaycorp.awaladroid.Awala
import tech.relaycorp.awaladroid.GatewayBindingException
import tech.relaycorp.awaladroid.GatewayClient
import tech.relaycorp.awaladroid.endpoint.FirstPartyEndpoint
import tech.relaycorp.awaladroid.endpoint.InvalidThirdPartyEndpoint
import tech.relaycorp.awaladroid.endpoint.PublicThirdPartyEndpoint
import tech.relaycorp.awaladroid.messaging.OutgoingMessage
import tech.relaycorp.letro.R
import tech.relaycorp.letro.data.ContentType
import tech.relaycorp.letro.data.EndpointPairDataModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GatewayRepository @Inject constructor(
    @ApplicationContext var context: Context,
    private val preferencesDataStoreRepository: PreferencesDataStoreRepository,
) {

    private val gatewayScope = CoroutineScope(Dispatchers.IO)

    private val _isGatewayAvailable: MutableStateFlow<Boolean?> = MutableStateFlow(null)
    val isGatewayAvailable: StateFlow<Boolean?> get() = _isGatewayAvailable

    private val _isGatewayFullySetup: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isGatewayFullySetup: StateFlow<Boolean> get() = _isGatewayFullySetup

    private val _accountCreatedConfirmationReceived: MutableSharedFlow<Unit> = MutableSharedFlow()
    val accountCreatedConfirmationReceived: SharedFlow<Unit> get() = _accountCreatedConfirmationReceived

    init {
        checkIfGatewayIsAvailable()

        gatewayScope.launch {
            _isGatewayAvailable.collect {
                if (it == true) {
                    registerServerFirstPartyEndpointIfNeeded()
                    importServerThirdPartyEndpointIfNeeded()
                    collectToUpdateIsGatewayAuthorizedReceivingMessagesFromServer()
                    updateIsGatewayFullySetup()
                    startReceivingMessages()
                }
            }
        }
    }

    // TODO Maybe use something else than combine
    private fun collectToUpdateIsGatewayAuthorizedReceivingMessagesFromServer() {
        gatewayScope.launch {
            combine(
                preferencesDataStoreRepository.serverFirstPartyEndpointNodeId,
                preferencesDataStoreRepository.serverThirdPartyEndpointNodeId,
                preferencesDataStoreRepository.isGatewayAuthorizedToReceiveMessagesFromServer,
            ) {
                    firstPartyEndpointNodeId,
                    thirdPartyEndpointNodeId,
                    authorizedReceivingMessagesFromServer,
                ->

                if (shouldAuthorizeReceivingMessages(
                        firstPartyEndpointNodeId,
                        thirdPartyEndpointNodeId,
                        authorizedReceivingMessagesFromServer,
                    )
                ) {
                    EndpointPairDataModel(
                        firstPartyEndpointNodeId = firstPartyEndpointNodeId!!,
                        thirdPartyEndpointNodeId = thirdPartyEndpointNodeId!!,
                    )
                } else {
                    null
                }
            }.collect { endpointPair ->
                endpointPair?.let {
                    authoriseReceivingMessagesFromThirdPartyEndpoint(
                        it.firstPartyEndpointNodeId,
                        it.thirdPartyEndpointNodeId,
                    )
                }
            }
        }
    }

    private fun updateIsGatewayFullySetup() {
        gatewayScope.launch {
            preferencesDataStoreRepository.isGatewayAuthorizedToReceiveMessagesFromServer.collect { isAuthorized ->
                _isGatewayFullySetup.emit(isAuthorized == true)
            }
        }
    }

    private fun importServerThirdPartyEndpointIfNeeded() {
        gatewayScope.launch {
            preferencesDataStoreRepository.serverThirdPartyEndpointNodeId.collect {
                if (it == null) {
                    importPublicThirdPartyEndpoint()
                }
            }
        }
    }

    private fun registerServerFirstPartyEndpointIfNeeded() {
        gatewayScope.launch {
            preferencesDataStoreRepository.serverFirstPartyEndpointNodeId.collect {
                if (it == null) {
                    registerFirstPartyEndpoint()
                }
            }
        }
    }

    private fun startReceivingMessages() {
        gatewayScope.launch {
            preferencesDataStoreRepository.isGatewayAuthorizedToReceiveMessagesFromServer.collect { authorized ->
                if (authorized == true) {
                    GatewayClient.receiveMessages().collect { message ->
                        if (message.type == ContentType.AccountCreationCompleted.value) {
                            _accountCreatedConfirmationReceived.emit(Unit)
                            message.ack()
                        }
                    }
                }
            }
        }
    }

    private fun shouldAuthorizeReceivingMessages(
        firstPartyEndpointNodeId: String?,
        thirdPartyEndpointNodeId: String?,
        authorizedReceivingMessagesFromServer: Boolean?,
    ) = authorizedReceivingMessagesFromServer != true &&
        firstPartyEndpointNodeId != null &&
        thirdPartyEndpointNodeId != null

    fun checkIfGatewayIsAvailable() {
        gatewayScope.launch {
            Awala.setUp(context)
            try {
                GatewayClient.bind()
                _isGatewayAvailable.emit(true)
            } catch (exp: GatewayBindingException) {
                _isGatewayAvailable.emit(false)
            }
        }
    }

    private suspend fun registerFirstPartyEndpoint() {
        val endpoint = FirstPartyEndpoint.register()
        preferencesDataStoreRepository.saveServerFirstPartyEndpointNodeId(endpoint.nodeId)
    }

    private suspend fun importPublicThirdPartyEndpoint() {
        val endpoint = importPublicThirdPartyEndpoint(
            context.resources.openRawResource(R.raw.server_connection_params).use {
                it.readBytes()
            },
        )

        preferencesDataStoreRepository.saveServerThirdPartyEndpointNodeId(endpoint.nodeId)
    }

    private suspend fun authoriseReceivingMessagesFromThirdPartyEndpoint(
        firstPartyEndpointNodeId: String,
        thirdPartyEndpointNodeId: String,
    ) {
        val firstPartyEndpoint = FirstPartyEndpoint.load(firstPartyEndpointNodeId)
        val thirdPartyEndpoint = PublicThirdPartyEndpoint.load(thirdPartyEndpointNodeId)

        if (firstPartyEndpoint == null || thirdPartyEndpoint == null) {
            return
        }

        // Create the Parcel Delivery Authorisation (PDA)
        val auth = firstPartyEndpoint.authorizeIndefinitely(thirdPartyEndpoint)

        // Send it to the server
        val authMessage = OutgoingMessage.build(
            ContentType.AuthorizeReceivingFromServer.value,
            auth,
            firstPartyEndpoint,
            thirdPartyEndpoint,
        )

        GatewayClient.sendMessage(authMessage)

        preferencesDataStoreRepository.saveAuthorizedReceivingMessagesFromServer(true)
    }

    @Throws(InvalidConnectionParams::class)
    private suspend fun importPublicThirdPartyEndpoint(connectionParams: ByteArray): PublicThirdPartyEndpoint {
        val endpoint = try {
            PublicThirdPartyEndpoint.import(connectionParams)
        } catch (e: InvalidThirdPartyEndpoint) {
            throw InvalidConnectionParams(e)
        }
        return endpoint
    }
}

internal class InvalidConnectionParams(cause: Throwable) : Exception(cause)
