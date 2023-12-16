package tech.relaycorp.letro.awala

import tech.relaycorp.letro.account.storage.dao.AccountDao
import tech.relaycorp.letro.storage.Preferences
import javax.inject.Inject

interface AwalaRepository {
    fun saveServerThirdPartyEndpointNodeId(nodeId: String)
    fun getServerThirdPartyEndpointNodeId(): String?
    suspend fun hasRegisteredEndpoints(): Boolean
}

class AwalaRepositoryImpl @Inject constructor(
    private val preferences: Preferences,
    private val accountDao: AccountDao,
) : AwalaRepository {

    override fun saveServerThirdPartyEndpointNodeId(nodeId: String) {
        preferences.putString(KEY_THIRD_PARTY_ENDPOINT_NODE_ID, nodeId)
    }

    override fun getServerThirdPartyEndpointNodeId(): String? {
        return preferences.getString(KEY_THIRD_PARTY_ENDPOINT_NODE_ID)
    }

    override suspend fun hasRegisteredEndpoints(): Boolean {
        return accountDao.getAllSync().isNotEmpty()
    }

    private companion object {
        private const val KEY_THIRD_PARTY_ENDPOINT_NODE_ID = "server_third_party_endpoint_node_id"
    }
}
