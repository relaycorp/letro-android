package tech.relaycorp.letro.onboarding.registration

import tech.relaycorp.awaladroid.messaging.IncomingMessage
import tech.relaycorp.letro.account.storage.AccountRepository
import tech.relaycorp.letro.awala.AwalaManager
import tech.relaycorp.letro.awala.processor.AwalaMessageProcessor
import tech.relaycorp.letro.server.messages.AccountCreation
import tech.relaycorp.letro.server.messages.InvalidAccountCreationException
import tech.relaycorp.letro.utils.crypto.deserialiseKeyPair
import java.util.logging.Level
import java.util.logging.Logger.getLogger
import javax.inject.Inject

interface AccountCreationProcessor : AwalaMessageProcessor

class AccountCreationProcessorImpl @Inject constructor(
    private val accountRepository: AccountRepository,
) : AccountCreationProcessor {
    private val logger = getLogger(javaClass.name)

    override suspend fun process(message: IncomingMessage, awalaManager: AwalaManager) {
        val accountCreation = try {
            AccountCreation.deserialise(message.content)
        } catch (exc: InvalidAccountCreationException) {
            logger.log(Level.WARNING, "Malformed account creation message", exc)
            return
        }

        val account = accountRepository.getByRequest(
            accountCreation.requestedUserName,
            accountCreation.locale,
        )
        if (account == null) {
            logger.warning("No account found for creation message ($accountCreation)")
            return
        }

        val veraidKeyPair = account.veraidPrivateKey.deserialiseKeyPair()
        try {
            accountCreation.validate(veraidKeyPair.public)
        } catch (exc: InvalidAccountCreationException) {
            logger.log(Level.WARNING, "Invalid account creation ($accountCreation)", exc)
            return
        }

        accountRepository.completeRegistration(
            account.id,
            accountCreation.assignedUserId,
            accountCreation.veraidBundle,
        )
        logger.info("Completed account creation ($accountCreation)")
    }
}
