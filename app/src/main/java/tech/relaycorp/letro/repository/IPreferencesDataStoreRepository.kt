package tech.relaycorp.letro.repository

import kotlinx.coroutines.flow.StateFlow

interface IPreferencesDataStoreRepository {
    val serverFirstPartyEndpointNodeId: StateFlow<String?>
    val serverThirdPartyEndpointNodeId: StateFlow<String?>
    val isGatewayAuthorizedToReceiveMessagesFromServer: StateFlow<Boolean?>

    suspend fun saveServerFirstPartyEndpointNodeId(value: String)
    suspend fun saveServerThirdPartyEndpointNodeId(value: String)
    suspend fun saveAuthorizedReceivingMessagesFromServer(value: Boolean)
    suspend fun clear()
}
