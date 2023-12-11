package tech.relaycorp.letro.utils.models.account

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import tech.relaycorp.letro.account.model.Account
import tech.relaycorp.letro.account.model.AccountStatus
import tech.relaycorp.letro.account.registration.utils.AccountIdBuilderImpl
import tech.relaycorp.letro.account.storage.dao.AccountDao
import tech.relaycorp.letro.account.storage.repository.AccountRepositoryImpl
import tech.relaycorp.letro.utils.models.utils.createLogger
import kotlin.random.Random

fun createAccount(
    accountId: String = "account@test.id",
    requestedUserName: String = "account",
    normalisedLocale: String = "test.id",
    isCurrent: Boolean = true,
    @AccountStatus status: Int = AccountStatus.CREATED,
) = Account(
    id = Random.nextLong(),
    accountId = accountId,
    requestedUserName = requestedUserName,
    normalisedLocale = normalisedLocale,
    domain = "test.id",
    isCurrent = isCurrent,
    veraidPrivateKey = ByteArray(0),
    status = status,
)

@ExperimentalCoroutinesApi
fun createAccountRepository(
    accounts: List<Account> = emptyList(),
    coroutineScope: CoroutineScope = CoroutineScope(UnconfinedTestDispatcher()),
    accountDao: AccountDao = createAccountDao(accounts, coroutineScope),
    ioDispatcher: TestDispatcher = UnconfinedTestDispatcher(),
) = AccountRepositoryImpl(
    accountDao = accountDao,
    pushManager = mockk(relaxed = true),
    logger = createLogger(),
    ioDispatcher = ioDispatcher,
    accountIdBuilder = createAccountIdBuilder(),
)

fun createAccountIdBuilder() = AccountIdBuilderImpl()

fun createAccountDao(
    initialAccounts: List<Account>,
    coroutineScope: CoroutineScope,
) = mockk<AccountDao>().also {
    val accountsFlow = MutableStateFlow(initialAccounts)
    every { it.getAll() } returns accountsFlow

    coEvery { it.update(any<Account>()) } answers {
        coroutineScope.launch {
            val account = args.first() as Account
            val indexOfAccountToChange = accountsFlow.value.indexOfFirst { it.id == account.id }
            if (indexOfAccountToChange != -1) {
                val replacedAccounts = ArrayList(accountsFlow.value).apply {
                    this[indexOfAccountToChange] = account
                }
                accountsFlow.emit(replacedAccounts)
            }
        }
        1
    }

    coEvery { it.update(any<List<Account>>()) } answers {
        coroutineScope.launch {
            val accounts = args.first() as List<*>
            val replacedAccounts = ArrayList(accountsFlow.value)
            accounts.forEach { account ->
                if (account !is Account) return@forEach
                val indexOfAccountToReplace = accountsFlow.value.indexOfFirst { it.id == account.id }
                if (indexOfAccountToReplace != -1) {
                    replacedAccounts.apply {
                        this[indexOfAccountToReplace] = account
                    }
                }
            }
            accountsFlow.emit(replacedAccounts)
        }
    }

    coEvery { it.insert(any<Account>()) } answers {
        coroutineScope.launch {
            val account = args.first() as Account
            val newAccounts = ArrayList(accountsFlow.value).apply {
                add(account)
            }
            accountsFlow.emit(newAccounts)
        }
    }
}
