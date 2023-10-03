package tech.relaycorp.letro.account.storage.repository

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import tech.relaycorp.letro.account.model.Account
import tech.relaycorp.letro.account.storage.dao.AccountDao
import tech.relaycorp.letro.main.MainViewModel
import tech.relaycorp.letro.push.PushManager
import tech.relaycorp.letro.utils.i18n.normaliseString
import java.security.PrivateKey
import java.util.Locale
import javax.inject.Inject

interface AccountRepository {
    val currentAccount: Flow<Account?>
    val allAccounts: StateFlow<List<Account>>
    suspend fun createAccount(
        requestedUserName: String,
        domainName: String,
        locale: Locale,
        veraidPrivateKey: PrivateKey,
    )

    suspend fun getByRequest(
        requestedUserName: String,
        locale: Locale,
    ): Account?

    suspend fun updateAccount(account: Account, accountId: String, veraidBundle: ByteArray)

    suspend fun deleteAccount(account: Account)
}

class AccountRepositoryImpl @Inject constructor(
    private val accountDao: AccountDao,
    private val pushManager: PushManager,
) : AccountRepository {

    private val databaseScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private val _allAccounts = MutableStateFlow<List<Account>>(emptyList())
    override val allAccounts: StateFlow<List<Account>>
        get() = _allAccounts

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
                pushManager.createNotificationChannelsForAccounts(list.map { it.accountId })
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
        veraidPrivateKey: PrivateKey,
    ) {
        accountDao.insert(
            Account(
                accountId = "$requestedUserName@$domainName",
                requestedUserName = requestedUserName,
                normalisedLocale = locale.normaliseString(),
                veraidPrivateKey = veraidPrivateKey.encoded,
                isCurrent = true,
            ),
        )
    }

    override suspend fun getByRequest(requestedUserName: String, locale: Locale): Account? =
        accountDao.getByRequestParams(
            requestedUserName = requestedUserName,
            locale = locale.normaliseString(),
        )

    override suspend fun deleteAccount(account: Account) {
        accountDao.deleteAccount(account)
    }

    override suspend fun updateAccount(
        account: Account,
        accountId: String,
        veraidBundle: ByteArray,
    ) {
        accountDao.update(
            account.copy(
                accountId = accountId,
                veraidMemberBundle = veraidBundle,
                isCreated = true,
            ),
        )
    }
}
