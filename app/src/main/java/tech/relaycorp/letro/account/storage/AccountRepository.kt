package tech.relaycorp.letro.account.storage

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import tech.relaycorp.letro.account.model.Account
import tech.relaycorp.letro.main.MainViewModel
import tech.relaycorp.letro.utils.i18n.normaliseString
import java.util.Locale
import javax.inject.Inject

interface AccountRepository {
    val currentAccount: Flow<Account?>
    suspend fun createAccount(requestedUserName: String, domainName: String, locale: Locale)

    suspend fun updateAccountId(id: String, newId: String)
}

class AccountRepositoryImpl @Inject constructor(
    private val accountDao: AccountDao,
) : AccountRepository {

    private val databaseScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private val _allAccounts = MutableSharedFlow<List<Account>>()
    private val _currentAccount = MutableStateFlow<Account?>(null)
    override val currentAccount: Flow<Account?>
        get() = _currentAccount

    init {
        databaseScope.launch {
            accountDao.getAll().collect {
                _allAccounts.emit(it)
            }
        }
        databaseScope.launch {
            _allAccounts.collect { list ->
                Log.d(MainViewModel.TAG, "AccountRepository.emit(currentAccount)")
                _currentAccount.emit(
                    list.firstOrNull { it.isCurrent },
                )
            }
        }
    }

    override suspend fun createAccount(
        requestedUserName: String,
        domainName: String,
        locale: Locale,
    ) {
        accountDao.insert(
            Account(
                veraId = "$requestedUserName@$domainName",
                requestedUserName = requestedUserName,
                locale = locale.normaliseString(),
                isCurrent = true,
            ),
        )
    }

    override suspend fun updateAccountId(id: String, newId: String) {
        accountDao.getByVeraId(id)?.let {
            accountDao.update(
                it.copy(
                    veraId = newId,
                    isCreated = true,
                ),
            )
        }
    }
}
