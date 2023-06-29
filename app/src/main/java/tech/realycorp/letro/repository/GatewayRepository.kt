package tech.realycorp.letro.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import tech.realycorp.letro.data.GatewayAvailabilityDataModel
import tech.relaycorp.awaladroid.Awala
import tech.relaycorp.awaladroid.GatewayBindingException
import tech.relaycorp.awaladroid.GatewayClient
import javax.inject.Inject
import javax.inject.Singleton

// Still not sure should this be called a Repository, Service or Manager but maybe it will be more clear down the line
@Singleton
class GatewayRepository @Inject constructor(@ApplicationContext var context: Context) {

    private val _gatewayAvailabilityDataModel: MutableStateFlow<GatewayAvailabilityDataModel> =
        MutableStateFlow(GatewayAvailabilityDataModel.Unknown)
    val gatewayAvailabilityDataModel: StateFlow<GatewayAvailabilityDataModel> get() = _gatewayAvailabilityDataModel

    private val gatewayScope = CoroutineScope(Dispatchers.IO)

    init {
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
}
