package tech.relaycorp.letro.awala

import tech.relaycorp.letro.storage.Preferences
import javax.inject.Inject

interface AwalaRepository {
    fun saveServerFirstPartyEndpointNodeId(nodeId: String)
    fun saveServerThirdPartyEndpointNodeId(nodeId: String)
    fun setAuthorizedReceiveMessagesFromServer(isAuthorized: Boolean)
    fun getServerFirstPartyEndpointNodeId(): String?
    fun getServerThirdPartyEndpointNodeId(): String?
    fun isAuthorizedReceiveMessagesFromServer(): Boolean
}

class AwalaRepositoryImpl @Inject constructor(
    private val preferences: Preferences,
): AwalaRepository {

    override fun saveServerFirstPartyEndpointNodeId(nodeId: String) {
        preferences.putString(KEY_FIRST_PARTY_ENDPOINT_NODE_ID, nodeId)
    }

    override fun saveServerThirdPartyEndpointNodeId(nodeId: String) {
        preferences.putString(KEY_THIRD_PARTY_ENDPOINT_NODE_ID, nodeId)
    }

    override fun setAuthorizedReceiveMessagesFromServer(isAuthorized: Boolean) {
        preferences.putBoolean(KEY_IS_AUTHORIZED_RECEIVE_MESSAGES_FROM_SERVER, isAuthorized)
    }

    override fun getServerFirstPartyEndpointNodeId(): String? {
        return preferences.getString(KEY_FIRST_PARTY_ENDPOINT_NODE_ID)
    }

    override fun getServerThirdPartyEndpointNodeId(): String? {
        return preferences.getString(KEY_THIRD_PARTY_ENDPOINT_NODE_ID)
    }

    override fun isAuthorizedReceiveMessagesFromServer(): Boolean {
        return preferences.getBoolean(KEY_IS_AUTHORIZED_RECEIVE_MESSAGES_FROM_SERVER, false)
    }

    private companion object {
        private const val KEY_FIRST_PARTY_ENDPOINT_NODE_ID = "first_party_endpoint_node_id"
        private const val KEY_THIRD_PARTY_ENDPOINT_NODE_ID = "server_third_party_endpoint_node_id"
        private const val KEY_IS_AUTHORIZED_RECEIVE_MESSAGES_FROM_SERVER = "IS_AUTHORIZED_RECEIVE_MESSAGES_FROM_SERVER"
    }
}