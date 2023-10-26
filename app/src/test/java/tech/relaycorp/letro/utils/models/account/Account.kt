package tech.relaycorp.letro.utils.models.account

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import tech.relaycorp.letro.account.model.Account
import tech.relaycorp.letro.account.model.AccountStatus
import tech.relaycorp.letro.account.registration.utils.AccountIdBuilderImpl
import tech.relaycorp.letro.account.storage.dao.AccountDao
import tech.relaycorp.letro.account.storage.repository.AccountRepositoryImpl
import tech.relaycorp.letro.utils.models.utils.createLogger

fun createAccount(
    accountId: String = "account@test.id",
    requestedUserName: String = "account",
    normalisedLocale: String = "test.id",
    isCurrent: Boolean = true,
    @AccountStatus status: Int = AccountStatus.CREATED,
) = Account(
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
    ioDispatcher: TestDispatcher = UnconfinedTestDispatcher(),
) = AccountRepositoryImpl(
    accountDao = mockk<AccountDao>().also {
        every { it.getAll() } returns flowOf(accounts)
    },
    pushManager = mockk(relaxed = true),
    logger = createLogger(),
    ioDispatcher = ioDispatcher,
    accountIdBuilder = createAccountIdBuilder(),
)

fun createAccountIdBuilder() = AccountIdBuilderImpl()
