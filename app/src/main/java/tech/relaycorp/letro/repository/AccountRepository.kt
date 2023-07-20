package tech.relaycorp.letro.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import tech.relaycorp.letro.data.AccountCreatedDataModel
import tech.relaycorp.letro.data.dao.AccountDao
import tech.relaycorp.letro.data.entity.AccountDataModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountRepository @Inject constructor(
    private val accountDao: AccountDao,
    private val gatewayRepository: GatewayRepository,
) {
    private val databaseScope: CoroutineScope = CoroutineScope(Dispatchers.IO)

    private val _allAccountsDataFlow: MutableStateFlow<List<AccountDataModel>> =
        MutableStateFlow(emptyList())
    val allAccountsDataFlow: StateFlow<List<AccountDataModel>> get() = _allAccountsDataFlow

    private val _currentAccountDataFlow: MutableStateFlow<AccountDataModel?> =
        MutableStateFlow(null)
    val currentAccountDataFlow: StateFlow<AccountDataModel?> get() = _currentAccountDataFlow

    init {
        databaseScope.launch {
            accountDao.getAll().collect {
                _allAccountsDataFlow.emit(it)
            }
        }

        databaseScope.launch {
            allAccountsDataFlow.collect { list ->
                list.firstOrNull { it.isCurrent }?.let {
                    _currentAccountDataFlow.emit(it)
                }
            }
        }

        databaseScope.launch {
            gatewayRepository.accountCreationConfirmationReceivedFromServer.collect { dataModel: AccountCreatedDataModel ->
                databaseScope.launch {
                    val account = accountDao.getByAddress(dataModel.requestedAddress)
                    if (account != null) {
                        accountDao.updateAddress(account.id, dataModel.assignedAddress)
                        accountDao.setAccountCreationConfirmed(dataModel.assignedAddress)
                    }
                }
            }
        }
    }

    fun startCreatingNewAccount(address: String) {
        val account = AccountDataModel(address = address)
        databaseScope.launch {
            insertNewAccountIntoDatabase(account)
            gatewayRepository.sendCreateAccountRequest(address)
        }
    }

    private suspend fun insertNewAccountIntoDatabase(dataModel: AccountDataModel) {
        accountDao.insert(dataModel)
        accountDao.setCurrentAccount(dataModel.address)
    }
}
