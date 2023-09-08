package tech.relaycorp.letro.onboarding.registration

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import tech.relaycorp.letro.account.storage.AccountRepository
import tech.relaycorp.letro.awala.AwalaManager
import tech.relaycorp.letro.awala.message.Message
import tech.relaycorp.letro.awala.message.MessageRecipient
import tech.relaycorp.letro.awala.message.MessageType
import tech.relaycorp.letro.onboarding.registration.parser.RegistrationMessageParser
import javax.inject.Inject

interface RegistrationRepository {
    suspend fun createNewAccount(id: String)
}

class RegistrationRepositoryImpl @Inject constructor(
    private val awalaManager: AwalaManager,
    private val accountRepository: AccountRepository,
    private val registrationMessageParser: RegistrationMessageParser,
): RegistrationRepository {

    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        scope.launch {
            awalaManager.messages
                .filter { it.type == MessageType.AccountCreationCompleted }
                .map(registrationMessageParser::parse)
                .collect {
                    accountRepository.updateAccountId(it.requestedVeraId, it.assignedVeraId)
                }
        }
    }

    override suspend fun createNewAccount(id: String) {
        accountRepository.createAccount(id)
        awalaManager
            .sendMessage(
                message = Message(
                    type = MessageType.AccountCreationRequest,
                    content = id.toByteArray(),
                ),
                recipient = MessageRecipient.Server(),
            )
    }

}