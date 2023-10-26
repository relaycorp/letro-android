package tech.relaycorp.letro.utils.models.account.registration

import kotlinx.coroutines.ExperimentalCoroutinesApi
import tech.relaycorp.letro.account.registration.storage.RegistrationRepositoryImpl
import tech.relaycorp.letro.account.registration.utils.AccountIdBuilder
import tech.relaycorp.letro.account.registration.utils.RegistrationDomainProvider
import tech.relaycorp.letro.account.storage.repository.AccountRepository
import tech.relaycorp.letro.awala.AwalaManager
import tech.relaycorp.letro.utils.models.account.createAccountIdBuilder
import tech.relaycorp.letro.utils.models.account.createAccountRepository
import tech.relaycorp.letro.utils.models.awala.createAwalaManager
import java.util.Locale

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
