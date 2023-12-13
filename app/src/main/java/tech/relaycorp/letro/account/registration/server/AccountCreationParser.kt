package tech.relaycorp.letro.account.registration.server

import tech.relaycorp.letro.account.storage.repository.AccountRepository
import tech.relaycorp.letro.awala.message.AwalaIncomingMessageContent
import tech.relaycorp.letro.awala.parser.AwalaMessageParser
import tech.relaycorp.letro.server.messages.AccountCreation
import tech.relaycorp.letro.server.messages.InvalidAccountCreationException
import tech.relaycorp.letro.utils.Logger
import javax.inject.Inject

interface AccountCreationParser : AwalaMessageParser<AwalaIncomingMessageContent.AccountCreation>

class AccountCreationParserImpl @Inject constructor(
    private val accountRepository: AccountRepository,
    private val logger: Logger,
) : AccountCreationParser {

    override suspend fun parse(content: ByteArray): AwalaIncomingMessageContent.AccountCreation? {
        val accountCreation = try {
            AccountCreation.deserialise(content)
        } catch (exc: InvalidAccountCreationException) {
            logger.w(TAG, "Malformed account creation message", exc)
            return null
        }

        val account = accountRepository.getByRequest(
            accountCreation.requestedUserName,
            accountCreation.locale,
        )
        if (account == null) {
            logger.w(TAG, "No account found for creation message ($accountCreation)")
            return null
        }
        return AwalaIncomingMessageContent.AccountCreation(
            account = account,
            accountCreation = accountCreation,
        )
    }
}

private const val TAG = "AccountCreationParser"
