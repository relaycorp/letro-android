package tech.relaycorp.letro.account.registration.server

import tech.relaycorp.awaladroid.messaging.IncomingMessage
import tech.relaycorp.letro.account.model.AccountStatus
import tech.relaycorp.letro.account.storage.repository.AccountRepository
import tech.relaycorp.letro.awala.AwalaManager
import tech.relaycorp.letro.awala.processor.AwalaMessageProcessor
import javax.inject.Inject

interface MisconfiguredInternetEndpointProcessor: AwalaMessageProcessor

class MisconfiguredInternetEndpointProcessorImpl @Inject constructor(
    private val accountRepository: AccountRepository,
): MisconfiguredInternetEndpointProcessor {

    override suspend fun process(message: IncomingMessage, awalaManager: AwalaManager) {
        val domain = message.content.decodeToString()
        accountRepository.getByDomain(domain).forEach {
            accountRepository.updateAccount(
                account = it,
                status = AccountStatus.ERROR,
            )
        }
    }

}