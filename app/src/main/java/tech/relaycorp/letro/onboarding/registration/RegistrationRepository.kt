package tech.relaycorp.letro.onboarding.registration

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tech.relaycorp.letro.account.storage.AccountRepository
import tech.relaycorp.letro.awala.AwalaManager
import tech.relaycorp.letro.awala.message.AwalaOutgoingMessage
import tech.relaycorp.letro.awala.message.MessageRecipient
import tech.relaycorp.letro.awala.message.MessageType
import javax.inject.Inject

interface RegistrationRepository {
    fun createNewAccount(id: String)
}

class RegistrationRepositoryImpl @Inject constructor(
    private val awalaManager: AwalaManager,
    private val accountRepository: AccountRepository,
) : RegistrationRepository {

    private val scope = CoroutineScope(Dispatchers.IO)

    override fun createNewAccount(id: String) {
        scope.launch {
            accountRepository.createAccount(id)
            awalaManager
                .sendMessage(
                    outgoingMessage = AwalaOutgoingMessage(
                        type = MessageType.AccountCreationRequest,
                        content = id.toByteArray(),
                    ),
                    recipient = MessageRecipient.Server(),
                )
        }
    }
}
