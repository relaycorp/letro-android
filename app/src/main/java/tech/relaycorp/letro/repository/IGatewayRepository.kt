package tech.relaycorp.letro.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import tech.relaycorp.letro.data.AccountCreatedDataModel

interface IGatewayRepository {
    val isGatewayAvailable: StateFlow<Boolean?>
    val isGatewayFullySetup: StateFlow<Boolean>
    val accountCreatedConfirmationReceived: SharedFlow<AccountCreatedDataModel>
    val serverFirstPartyEndpointNodeId: Flow<String?>
    val serverThirdPartyEndpointNodeId: Flow<String?>

    fun checkIfGatewayIsAvailable()
}
