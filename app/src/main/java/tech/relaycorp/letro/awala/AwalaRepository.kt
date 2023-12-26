package tech.relaycorp.letro.awala

import tech.relaycorp.letro.account.storage.dao.AccountDao
import javax.inject.Inject

interface AwalaRepository {
    suspend fun getServerThirdPartyEndpointNodeId(firstPartyEndpointNodeId: String): String?
    suspend fun hasRegisteredEndpoints(): Boolean
}

class AwalaRepositoryImpl @Inject constructor(
    private val accountDao: AccountDao,
) : AwalaRepository {

    override suspend fun hasRegisteredEndpoints(): Boolean {
        return accountDao.getAllSync().isNotEmpty()
    }

    override suspend fun getServerThirdPartyEndpointNodeId(firstPartyEndpointNodeId: String): String? {
        return accountDao.getByFirstPartyEndpointNodeId(firstPartyEndpointNodeId)?.thirdPartyServerEndpointNodeId
    }
}
