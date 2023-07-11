package tech.relaycorp.letro.repository

import android.content.Context
import android.content.res.Resources
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.ZonedDateTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import tech.relaycorp.awaladroid.Awala
import tech.relaycorp.awaladroid.GatewayBindingException
import tech.relaycorp.awaladroid.GatewayClient
import tech.relaycorp.letro.data.GatewayAvailabilityDataModel
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.combine
import tech.relaycorp.awaladroid.endpoint.FirstPartyEndpoint
import tech.relaycorp.awaladroid.endpoint.InvalidThirdPartyEndpoint
import tech.relaycorp.awaladroid.endpoint.PublicThirdPartyEndpoint
import tech.relaycorp.awaladroid.messaging.IncomingMessage
import tech.relaycorp.awaladroid.messaging.OutgoingMessage
import tech.relaycorp.letro.R
import tech.relaycorp.letro.data.ContentType


@Singleton
class GatewayRepository @Inject constructor(
    @ApplicationContext var context: Context,
    private val preferencesDataStoreRepository: PreferencesDataStoreRepository,
) {

    private val gatewayScope = CoroutineScope(Dispatchers.IO)

    private val _gatewayAvailabilityDataModel: MutableStateFlow<GatewayAvailabilityDataModel> =
        MutableStateFlow(GatewayAvailabilityDataModel.Unknown)
    val gatewayAvailabilityDataModel: StateFlow<GatewayAvailabilityDataModel> get() = _gatewayAvailabilityDataModel

    private val _serverFirstPartyEndpointNodeId: MutableStateFlow<String?> = MutableStateFlow(null)
    private val _serverThirdPartyEndpointNodeId: MutableStateFlow<String?> = MutableStateFlow(null)
    private val _authorizedReceivingMessagesFromServer: MutableStateFlow<Boolean> =
        MutableStateFlow(false)

    private val _incomingMessagesFromServer: MutableSharedFlow<IncomingMessage> = MutableSharedFlow()
    val incomingMessagesFromServer: SharedFlow<IncomingMessage> get() = _incomingMessagesFromServer

    init {
        checkIfGatewayIsAvailable()

        gatewayScope.launch {
            GatewayClient.receiveMessages().collect {
                _incomingMessagesFromServer.emit(it)
            }
        }

        gatewayScope.launch {
            preferencesDataStoreRepository.getServerFirstPartyEndpointNodeId().collect {
                if (it != null) {
                    _serverFirstPartyEndpointNodeId.emit(it)
                } else {
                    registerFirstPartyEndpoint()
                }
            }
        }

        gatewayScope.launch {
            preferencesDataStoreRepository.getServerThirdPartyEndpointNodeId().collect {
                if (it != null) {
                    _serverThirdPartyEndpointNodeId.emit(it)
                } else {
                    importPublicThirdPartyEndpoint()
                }
            }
        }

        gatewayScope.launch {
            preferencesDataStoreRepository.getAuthorizedReceivingMessagesFromServer().collect {
                if (it != null) {
                    _authorizedReceivingMessagesFromServer.emit(it)
                } else {
                    _authorizedReceivingMessagesFromServer.emit(false)
                }
            }
        }

        combine( // TODO: Maybe use something else than combine
            _gatewayAvailabilityDataModel,
            _serverFirstPartyEndpointNodeId,
            _serverThirdPartyEndpointNodeId,
            _authorizedReceivingMessagesFromServer,
        ) { gatewayAvailability,
            firstPartyEndpointNodeId,
            thirdPartyEndpointNodeId,
            authorizedReceivingMessagesFromServer ->

            if (!authorizedReceivingMessagesFromServer
                && gatewayAvailability == GatewayAvailabilityDataModel.Available
                && firstPartyEndpointNodeId != null
                && thirdPartyEndpointNodeId != null
            ) {
                authoriseReceivingMessagesFromThirdPartyEndpoint(
                    firstPartyEndpointNodeId,
                    thirdPartyEndpointNodeId,
                )
            }
        }
    }

    fun checkIfGatewayIsAvailable() {
        gatewayScope.launch {
            Awala.setUp(context)
            try {
                GatewayClient.bind()
                _gatewayAvailabilityDataModel.emit(GatewayAvailabilityDataModel.Available)
            } catch (exp: GatewayBindingException) {
                _gatewayAvailabilityDataModel.emit(GatewayAvailabilityDataModel.Unavailable)
            }
        }
    }

    private suspend fun registerFirstPartyEndpoint() {
        val endpoint = FirstPartyEndpoint.register()
        preferencesDataStoreRepository.saveServerFirstPartyEndpointNodeId(endpoint.nodeId)
    }

    private suspend fun importPublicThirdPartyEndpoint() {
        val endpoint = importPublicThirdPartyEndpoint(
            Resources.getSystem().openRawResource(R.raw.server_connection_params).use {
                it.readBytes()
            }
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
