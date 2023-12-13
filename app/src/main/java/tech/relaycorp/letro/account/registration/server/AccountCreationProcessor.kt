package tech.relaycorp.letro.account.registration.server

import tech.relaycorp.letro.account.model.AccountStatus
import tech.relaycorp.letro.account.storage.repository.AccountRepository
import tech.relaycorp.letro.awala.AwalaManager
import tech.relaycorp.letro.awala.message.AwalaIncomingMessageContent
import tech.relaycorp.letro.awala.processor.ServerMessageProcessor
import tech.relaycorp.letro.server.messages.InvalidAccountCreationException
import tech.relaycorp.letro.utils.Logger
import tech.relaycorp.letro.utils.crypto.deserialiseKeyPair
import javax.inject.Inject

class AccountCreationProcessor @Inject constructor(
    private val accountRepository: AccountRepository,
    parser: AccountCreationParser,
    logger: Logger,
) : ServerMessageProcessor<AwalaIncomingMessageContent.AccountCreation>(parser, logger) {

    override suspend fun handleMessage(
        content: AwalaIncomingMessageContent.AccountCreation,
        awalaManager: AwalaManager,
    ) {
        val veraidKeyPair = content.account.veraidPrivateKey.deserialiseKeyPair()
        try {
            content.accountCreation.validate(veraidKeyPair.public)
        } catch (exc: InvalidAccountCreationException) {
            logger.w(TAG, "Invalid account creation (${content.accountCreation})", exc)
            accountRepository.updateAccount(
                account = content.account,
                status = AccountStatus.ERROR_CREATION,
            )
            return
        }

        accountRepository.updateAccount(
            content.account,
            content.accountCreation.assignedUserId,
            content.accountCreation.veraidBundle,
        )
        logger.i(TAG, "Completed account creation (${content.accountCreation})")
    }
}

private const val TAG = "AccountCreationProcessor"
