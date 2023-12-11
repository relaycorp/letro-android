package tech.relaycorp.letro.account.registration

import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.jupiter.api.Test
import tech.relaycorp.letro.utils.models.account.createAccountDao
import tech.relaycorp.letro.utils.models.account.createAccountRepository
import tech.relaycorp.letro.utils.models.account.registration.createRegistrationDomainProvider
import tech.relaycorp.letro.utils.models.account.registration.createRegistrationRepository

class RegistrationRepositoryTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testAccountCreationAfterRegistration() {
        val coroutineScope = CoroutineScope(UnconfinedTestDispatcher())
        val domainProvider = createRegistrationDomainProvider("domain")
        val accountDao = createAccountDao(emptyList(), coroutineScope)
        val repository = createRegistrationRepository(
            awalaManager = mockk(relaxed = true),
            accountRepository = createAccountRepository(
                accountDao = accountDao,
            ),
        )
        coroutineScope.launch {
            repository.createNewAccount(
                requestedUserName = "name",
                domainName = "domain",
                domainProvider.getDomainLocale(),
            )
        }
        coVerify {
            accountDao.insert(any())
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testAccountCreationAfterLinkingExistingAccount() {
        val coroutineScope = CoroutineScope(UnconfinedTestDispatcher())
        val accountDao = createAccountDao(emptyList(), coroutineScope)
        val repository = createRegistrationRepository(
            awalaManager = mockk(relaxed = true),
            accountRepository = createAccountRepository(
                accountDao = accountDao,
            ),
        )
        coroutineScope.launch {
            repository.loginToExistingAccount(
                domainName = "domain",
                awalaEndpoint = "",
                token = "",
            )
        }
        coVerify {
            accountDao.insert(any())
        }
    }
}
