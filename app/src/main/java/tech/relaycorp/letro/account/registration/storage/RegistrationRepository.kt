package tech.relaycorp.letro.account.registration.storage

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import tech.relaycorp.letro.account.storage.repository.AccountRepository
import tech.relaycorp.letro.awala.AwalaManager
import tech.relaycorp.letro.awala.message.AwalaOutgoingMessage
import tech.relaycorp.letro.awala.message.MessageRecipient
import tech.relaycorp.letro.awala.message.MessageType
import tech.relaycorp.letro.server.messages.AccountRequest
import tech.relaycorp.letro.utils.di.IODispatcher
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.util.Locale
import javax.inject.Inject

interface RegistrationRepository {
    fun createNewAccount(requestedUserName: String, domainName: String, locale: Locale)
}

class RegistrationRepositoryImpl @Inject constructor(
    private val awalaManager: AwalaManager,
    private val accountRepository: AccountRepository,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
) : RegistrationRepository {

    private val scope = CoroutineScope(ioDispatcher)

    override fun createNewAccount(requestedUserName: String, domainName: String, locale: Locale) {
        scope.launch {
            val keyPair = generateRSAKeyPair()
            accountRepository.createAccount(requestedUserName, domainName, locale, keyPair.private)

            val creationRequest = AccountRequest(
                requestedUserName,
                locale,
                keyPair.public,
            )
            awalaManager
                .sendMessage(
                    outgoingMessage = AwalaOutgoingMessage(
                        type = MessageType.AccountCreationRequest,
                        content = creationRequest.serialise(keyPair.private),
                    ),
                    recipient = MessageRecipient.Server(),
                )
        }
    }

    /**
     * Generate an ephemeral key pair temporarily (we'll persist it once VeraId is integrated).
     */
    private fun generateRSAKeyPair(): KeyPair {
        val keyGen = KeyPairGenerator.getInstance("RSA")
        keyGen.initialize(2048)
        return keyGen.generateKeyPair()
    }
}
