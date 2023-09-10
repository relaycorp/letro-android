package tech.relaycorp.letro.onboarding.registration

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch
import tech.relaycorp.letro.account.storage.AccountRepository
import tech.relaycorp.letro.awala.AwalaManager
import tech.relaycorp.letro.awala.message.AwalaOutgoingMessage
import tech.relaycorp.letro.awala.message.MessageRecipient
import tech.relaycorp.letro.awala.message.MessageType
import tech.relaycorp.letro.onboarding.registration.dto.RegistrationResponseIncomingMessage
import javax.inject.Inject

interface RegistrationRepository {
    suspend fun createNewAccount(id: String)
}

class RegistrationRepositoryImpl @Inject constructor(
    private val awalaManager: AwalaManager,
    private val accountRepository: AccountRepository,
) : RegistrationRepository {

    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        scope.launch {
            awalaManager.incomingMessages
                .filterIsInstance(RegistrationResponseIncomingMessage::class)
                .collect {
                    accountRepository.updateAccountId(it.content.requestedVeraId, it.content.assignedVeraId)
                }
        }
    }

    override suspend fun createNewAccount(id: String) {
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
