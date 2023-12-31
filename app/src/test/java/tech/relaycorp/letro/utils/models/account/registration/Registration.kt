package tech.relaycorp.letro.utils.models.account.registration

import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import tech.relaycorp.letro.account.registration.RegistrationViewModel
import tech.relaycorp.letro.account.registration.storage.RegistrationRepository
import tech.relaycorp.letro.account.registration.storage.RegistrationRepositoryImpl
import tech.relaycorp.letro.account.registration.utils.AccountIdBuilder
import tech.relaycorp.letro.account.registration.utils.RegistrationDomainProvider
import tech.relaycorp.letro.account.storage.repository.AccountRepository
import tech.relaycorp.letro.awala.AwalaManager
import tech.relaycorp.letro.utils.coroutines.Dispatchers
import tech.relaycorp.letro.utils.models.account.createAccountIdBuilder
import tech.relaycorp.letro.utils.models.account.createAccountRepository
import tech.relaycorp.letro.utils.models.awala.createAwalaManager
import tech.relaycorp.letro.utils.models.utils.dispatchers
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
fun createRegistrationViewModel(
    registrationRepository: RegistrationRepository = createRegistrationRepository(),
    domainProvider: RegistrationDomainProvider = createRegistrationDomainProvider(),
    dispatchers: Dispatchers = dispatchers(),
) = RegistrationViewModel(
    registrationRepository = registrationRepository,
    domainProvider = domainProvider,
    dispatchers = dispatchers,
    logger = mockk(relaxed = true),
)

@OptIn(ExperimentalCoroutinesApi::class)
fun createRegistrationRepository(
    accountRepository: AccountRepository = createAccountRepository(),
    awalaManager: AwalaManager = createAwalaManager(),
    accountIdBuilder: AccountIdBuilder = createAccountIdBuilder(),
) = RegistrationRepositoryImpl(
    awalaManager = awalaManager,
    accountRepository = accountRepository,
    accountIdBuilder = accountIdBuilder,
)

fun createRegistrationDomainProvider(
    domain: String = "chores.fans",
) = object : RegistrationDomainProvider {
    override fun getDomain(): String {
        return domain
    }

    override fun getDomainLocale(): Locale {
        return Locale.getDefault()
    }
}
