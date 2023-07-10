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
    private val userDao: AccountDao,
) {

    private val databaseScope: CoroutineScope = CoroutineScope(Dispatchers.IO)

    private val _allUsersDataFlow: MutableStateFlow<List<AccountDataModel>> =
        MutableStateFlow(emptyList())
    val allUsersDataFlow: StateFlow<List<AccountDataModel>> get() = _allUsersDataFlow

    private val user: Flow<List<AccountDataModel>> = userDao.getAll()

    private val _currentUserDataFlow: MutableStateFlow<AccountDataModel?> = MutableStateFlow(null)
    val currentUserDataFlow: StateFlow<AccountDataModel?> get() = _currentUserDataFlow

    init {
        databaseScope.launch {
            _allUsersDataFlow.collect {
                _currentUserDataFlow.emit(it.firstOrNull())
            }
        }
    }

    fun createNewAccount(username: String) {
        databaseScope.launch {

        }
    }

    private fun insertNewAccountToDatabase(dataModel: AccountDataModel) {
        databaseScope.launch {
            userDao.insert(dataModel)
            _currentUserDataFlow.emit(dataModel)
        }
    }
}
