package tech.relaycorp.letro.account.storage.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import tech.relaycorp.letro.account.model.Account
import tech.relaycorp.letro.account.model.AccountStatus
import tech.relaycorp.letro.account.model.AccountType
import tech.relaycorp.letro.account.registration.utils.AccountIdBuilder
import tech.relaycorp.letro.account.storage.dao.AccountDao
import tech.relaycorp.letro.main.MainViewModel
import tech.relaycorp.letro.utils.Logger
import tech.relaycorp.letro.utils.di.IODispatcher
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
        veraidPrivateKey: PrivateKey,
        firstPartyEndpointNodeId: String,
        thirdPartyEndpointNodeId: String,
        awalaEndpoint: String? = null,
        locale: Locale? = null,
        token: String? = null,
    )

    suspend fun getByRequest(
        requestedUserName: String,
        locale: Locale,
    ): Account?

    suspend fun getByVeraidId(
        id: String,
    ): Account?
    suspend fun getByDomain(
        domain: String,
    ): List<Account>

    suspend fun getByAwalaDomain(
        awalaDomain: String,
    ): List<Account>

    suspend fun updateAccount(account: Account)

    suspend fun deleteAccount(account: Account)
    suspend fun switchAccount(newCurrentAccount: Account): Boolean
    suspend fun switchAccount(accountId: String): Boolean
}

class AccountRepositoryImpl @Inject constructor(
    private val accountDao: AccountDao,
    private val accountIdBuilder: AccountIdBuilder,
    private val logger: Logger,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
) : AccountRepository {

    private val databaseScope: CoroutineScope = CoroutineScope(ioDispatcher)
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
                logger.d(MainViewModel.TAG, "AccountRepository.emit(currentAccount)")
                _currentAccount.emit(
                    list.firstOrNull { it.isCurrent },
                )
            }
        }
    }

    override suspend fun switchAccount(accountId: String): Boolean {
        _allAccounts.value.find { it.accountId == accountId }?.let {
            return switchAccount(it)
        }
        return false
    }

    override suspend fun switchAccount(newCurrentAccount: Account): Boolean {
        if (newCurrentAccount.accountId == _currentAccount.value?.accountId) {
            return false
        }
        markAllExistingAccountsAsNonCurrent()
        accountDao.update(
            newCurrentAccount.copy(
                isCurrent = true,
            ),
        )
        return true
    }

    override suspend fun createAccount(
        requestedUserName: String,
        domainName: String,
        veraidPrivateKey: PrivateKey,
        firstPartyEndpointNodeId: String,
        thirdPartyEndpointNodeId: String,
        awalaEndpoint: String?,
        locale: Locale?,
        token: String?,
    ) {
        markAllExistingAccountsAsNonCurrent()
        accountDao.insert(
            Account(
                accountId = accountIdBuilder.build(requestedUserName, domainName),
                requestedUserName = requestedUserName,
                normalisedLocale = locale?.normaliseString(),
                veraidPrivateKey = veraidPrivateKey.encoded,
                domain = domainName,
                awalaEndpointId = awalaEndpoint,
                isCurrent = true,
                token = token,
                status = if (token != null) AccountStatus.LINKING_WAITING else AccountStatus.CREATION_WAITING,
                accountType = if (token != null) AccountType.LINKED_EXISTING else AccountType.CREATED_FROM_SCRATCH,
                firstPartyEndpointNodeId = firstPartyEndpointNodeId,
                thirdPartyServerEndpointNodeId = thirdPartyEndpointNodeId,
            ),
        )
    }

    override suspend fun getByRequest(requestedUserName: String, locale: Locale): Account? =
        accountDao.getByRequestParams(
            requestedUserName = requestedUserName,
            locale = locale.normaliseString(),
        )

    override suspend fun deleteAccount(account: Account) {
        if (account.isCurrent) {
            _allAccounts.value.firstOrNull { !it.isCurrent }?.let {
                accountDao.update(
                    it.copy(
                        isCurrent = true,
                    ),
                )
            }
        }
        accountDao.deleteAccount(account)
    }

    override suspend fun updateAccount(account: Account) {
        accountDao.update(account)
    }

    override suspend fun getByVeraidId(id: String): Account? {
        return accountDao.getByVeraidId(id)
    }
    override suspend fun getByDomain(domain: String): List<Account> {
        return accountDao.getByDomain(domain)
    }

    override suspend fun getByAwalaDomain(awalaDomain: String): List<Account> {
        return accountDao.getByAwalaEndpoint(awalaDomain)
    }

    private suspend fun markAllExistingAccountsAsNonCurrent() {
        val updatedAccounts = _allAccounts.value
            .map { it.copy(isCurrent = false) }
        if (updatedAccounts.isNotEmpty()) {
            accountDao.update(updatedAccounts)
        }
    }
}
