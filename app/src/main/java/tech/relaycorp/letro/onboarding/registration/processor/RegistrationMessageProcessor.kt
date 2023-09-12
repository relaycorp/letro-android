package tech.relaycorp.letro.onboarding.registration.processor

import tech.relaycorp.awaladroid.messaging.IncomingMessage
import tech.relaycorp.letro.account.storage.AccountRepository
import tech.relaycorp.letro.awala.AwalaManager
import tech.relaycorp.letro.awala.processor.AwalaMessageProcessor
import tech.relaycorp.letro.onboarding.registration.dto.RegistrationResponseIncomingMessage
import tech.relaycorp.letro.onboarding.registration.parser.RegistrationMessageParser
import javax.inject.Inject

interface RegistrationMessageProcessor : AwalaMessageProcessor

class RegistrationMessageProcessorImpl @Inject constructor(
    private val parser: RegistrationMessageParser,
    private val accountRepository: AccountRepository,
) : RegistrationMessageProcessor {

    override suspend fun process(message: IncomingMessage, awalaManager: AwalaManager) {
        val response = parser.parse(message.content) as RegistrationResponseIncomingMessage
        accountRepository.updateAccountId(response.content.requestedVeraId, response.content.assignedVeraId)
    }
}
