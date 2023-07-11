package tech.relaycorp.letro.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import tech.relaycorp.letro.data.entity.AccountDataModel
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import tech.relaycorp.letro.data.dao.AccountDao

@Singleton
class AccountRepository @Inject constructor(
    private val accountDao: AccountDao,
) {

    private val databaseScope: CoroutineScope = CoroutineScope(Dispatchers.IO)

    private val _allAccountsDataFlow: MutableStateFlow<List<AccountDataModel>> =
        MutableStateFlow(emptyList())
    val allAccountsDataFlow: StateFlow<List<AccountDataModel>> get() = _allAccountsDataFlow

    private val account: Flow<List<AccountDataModel>> = accountDao.getAll()

    private val _currentAccountDataFlow: MutableStateFlow<AccountDataModel?> = MutableStateFlow(null)
    val currentAccountDataFlow: StateFlow<AccountDataModel?> get() = _currentAccountDataFlow

    init {
        databaseScope.launch {
            _allAccountsDataFlow.collect {
                _currentAccountDataFlow.emit(it.firstOrNull())
            }
        }
    }

    fun createNewAccount(username: String) {
        databaseScope.launch {

        }
    }

    private fun insertNewAccountToDatabase(dataModel: AccountDataModel) {
        databaseScope.launch {
            accountDao.insert(dataModel)
            _currentAccountDataFlow.emit(dataModel)
        }
    }
}
