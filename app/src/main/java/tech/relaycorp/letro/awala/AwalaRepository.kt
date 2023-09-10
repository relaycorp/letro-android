package tech.relaycorp.letro.awala

import tech.relaycorp.letro.storage.Preferences
import javax.inject.Inject

interface AwalaRepository {
    fun saveServerFirstPartyEndpointNodeId(nodeId: String)
    fun saveServerThirdPartyEndpointNodeId(nodeId: String)
    fun getServerFirstPartyEndpointNodeId(): String?
    fun getServerThirdPartyEndpointNodeId(): String?
}

class AwalaRepositoryImpl @Inject constructor(
    private val preferences: Preferences,
) : AwalaRepository {

    override fun saveServerFirstPartyEndpointNodeId(nodeId: String) {
        preferences.putString(KEY_FIRST_PARTY_ENDPOINT_NODE_ID, nodeId)
    }

    override fun saveServerThirdPartyEndpointNodeId(nodeId: String) {
        preferences.putString(KEY_THIRD_PARTY_ENDPOINT_NODE_ID, nodeId)
    }

    override fun getServerFirstPartyEndpointNodeId(): String? {
        return preferences.getString(KEY_FIRST_PARTY_ENDPOINT_NODE_ID)
    }

    override fun getServerThirdPartyEndpointNodeId(): String? {
        return preferences.getString(KEY_THIRD_PARTY_ENDPOINT_NODE_ID)
    }

    private companion object {
        private const val KEY_FIRST_PARTY_ENDPOINT_NODE_ID = "first_party_endpoint_node_id"
        private const val KEY_THIRD_PARTY_ENDPOINT_NODE_ID = "server_third_party_endpoint_node_id"
    }
}
