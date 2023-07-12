package tech.relaycorp.letro.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import tech.relaycorp.awaladroid.GatewayClient
import tech.relaycorp.awaladroid.endpoint.FirstPartyEndpoint
import tech.relaycorp.awaladroid.endpoint.PublicThirdPartyEndpoint
import tech.relaycorp.awaladroid.messaging.OutgoingMessage
import tech.relaycorp.letro.data.ContentType
import tech.relaycorp.letro.data.dao.AccountDao
import tech.relaycorp.letro.data.entity.AccountDataModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountRepository @Inject constructor(
    private val accountDao: AccountDao,
    private val preferencesDataStoreRepository: PreferencesDataStoreRepository,
    private val gatewayRepository: GatewayRepository,
) {
    private val preferencesScope: CoroutineScope = CoroutineScope(Job())
    private val databaseScope: CoroutineScope = CoroutineScope(Dispatchers.IO)

    private val _allAccountsDataFlow: MutableStateFlow<List<AccountDataModel>> =
        MutableStateFlow(emptyList())
    val allAccountsDataFlow: StateFlow<List<AccountDataModel>> get() = _allAccountsDataFlow

    private val accounts: Flow<List<AccountDataModel>> = accountDao.getAll()

    private val _currentAccountDataFlow: MutableStateFlow<AccountDataModel?> =
        MutableStateFlow(null)
    val currentAccountDataFlow: StateFlow<AccountDataModel?> get() = _currentAccountDataFlow

    private val _serverFirstPartyEndpointNodeId: MutableStateFlow<String> = MutableStateFlow("")
    private val _serverThirdPartyEndpointNodeId: MutableStateFlow<String> = MutableStateFlow("")

    init {
        preferencesScope.launch {
            preferencesDataStoreRepository.currentAccountId.collect {
                if (it != null) {
                    _currentAccountDataFlow.emit(accountDao.getById(it))
                }
            }
        }

        databaseScope.launch {
            gatewayRepository.accountCreatedConfirmationReceived.collect {
                accountDao.update(_currentAccountDataFlow.value!!.copy(isCreationConfirmed = true))
            }
        }

        databaseScope.launch {
            accounts.collect {
                _allAccountsDataFlow.emit(it)
            }
        }

        preferencesScope.launch {
            preferencesDataStoreRepository.serverFirstPartyEndpointNodeId.collect {
                if (it != null) {
                    _serverFirstPartyEndpointNodeId.emit(it)
                }
            }
        }

        preferencesScope.launch {
            preferencesDataStoreRepository.serverThirdPartyEndpointNodeId.collect {
                if (it != null) {
                    _serverThirdPartyEndpointNodeId.emit(it)
                }
            }
        }
    }

    fun createNewAccount(address: String) {
        if (_serverFirstPartyEndpointNodeId.value.isEmpty() || _serverThirdPartyEndpointNodeId.value.isEmpty()) {
            return // TODO Show error
        }

        val account = AccountDataModel(address = address)
        databaseScope.launch {
            insertNewAccountToDatabase(account)
            sendCreateAccountRequest(address)
        }
    }

    private suspend fun sendCreateAccountRequest(address: String) {
        val firstPartyEndpoint = FirstPartyEndpoint.load(_serverFirstPartyEndpointNodeId.value)
        val thirdPartyEndpoint =
            PublicThirdPartyEndpoint.load(_serverThirdPartyEndpointNodeId.value)

        if (firstPartyEndpoint == null || thirdPartyEndpoint == null) {
            return
        }

        val message = OutgoingMessage.build(
            type = ContentType.AccountCreationRequest.value,
            content = address.toByteArray(),
            senderEndpoint = firstPartyEndpoint,
            recipientEndpoint = thirdPartyEndpoint,
        )

        GatewayClient.sendMessage(message)
    }

    private fun insertNewAccountToDatabase(dataModel: AccountDataModel) {
        databaseScope.launch {
            val id = accountDao.insert(dataModel)
            preferencesDataStoreRepository.saveCurrentAccountId(id)
        }
    }
}
