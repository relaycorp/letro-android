package tech.relaycorp.letro.repository

import android.content.Context
import android.content.res.Resources
import dagger.hilt.android.qualifiers.ApplicationContext
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
import tech.relaycorp.awaladroid.endpoint.FirstPartyEndpoint
import tech.relaycorp.awaladroid.endpoint.InvalidThirdPartyEndpoint
import tech.relaycorp.awaladroid.endpoint.PublicThirdPartyEndpoint
import tech.relaycorp.letro.R

@Singleton
class GatewayRepository @Inject constructor(
    @ApplicationContext var context: Context,
    private val preferencesDataStoreRepository: PreferencesDataStoreRepository,
) {

    private val gatewayScope = CoroutineScope(Dispatchers.IO)

    private val _gatewayAvailabilityDataModel: MutableStateFlow<GatewayAvailabilityDataModel> =
        MutableStateFlow(GatewayAvailabilityDataModel.Unknown)
    val gatewayAvailabilityDataModel: StateFlow<GatewayAvailabilityDataModel> get() = _gatewayAvailabilityDataModel

    private val _firstPartyEndpointNodeId: Flow<String?> = preferencesDataStoreRepository.getFirstPartyEndpoint()
    private val _thirdPartyEndpointNodeId: Flow<String?> = preferencesDataStoreRepository.getThirdPartyEndpoint()

    init {
        checkIfGatewayIsAvailable()
    }

    fun checkIfGatewayIsAvailable() {
        gatewayScope.launch {
            Awala.setUp(context)
            try {
                GatewayClient.bind()
            } catch (exp: GatewayBindingException) {
                _gatewayAvailabilityDataModel.emit(GatewayAvailabilityDataModel.Unavailable)
                return@launch
            } finally {
                registerFirstPartyEndpointIfNeeded()
                importThirdPartyEndpointIfNeeded()
                _gatewayAvailabilityDataModel.emit(GatewayAvailabilityDataModel.Available)
            }
        }
    }

    private suspend fun registerFirstPartyEndpointIfNeeded() {
        if (_firstPartyEndpointNodeId != null) return

        val endpoint = FirstPartyEndpoint.register()
        preferencesDataStoreRepository.setFirstPartyEndpointAddress(endpoint.nodeId)
    }

    private suspend fun importThirdPartyEndpointIfNeeded() {
        if (preferencesDataStoreRepository.thirdPartyEndpointAddress() != null) return

        val endpoint = importThirdPartyEndpoint(
            Resources.getSystem().openRawResource(R.raw.server_connection_params).use {
                it.readBytes()
            }
        )

        preferencesDataStoreRepository.setThirdPartyEndpointAddress(endpoint.nodeId)
    }

    @Throws(InvalidConnectionParams::class)
    private suspend fun importThirdPartyEndpoint(connectionParams: ByteArray): PublicThirdPartyEndpoint {
        val endpoint = try {
            PublicThirdPartyEndpoint.import(connectionParams)
        } catch (e: InvalidThirdPartyEndpoint) {
            throw InvalidConnectionParams(e)
        }
        return endpoint
    }
}

internal class InvalidConnectionParams(cause: Throwable) : Exception(cause)
