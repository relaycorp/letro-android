package tech.relaycorp.letro.account.registration.storage

import tech.relaycorp.letro.account.storage.repository.AccountRepository
import tech.relaycorp.letro.awala.AwalaManager
import tech.relaycorp.letro.awala.message.AwalaOutgoingMessage
import tech.relaycorp.letro.awala.message.MessageRecipient
import tech.relaycorp.letro.awala.message.MessageType
import tech.relaycorp.letro.server.messages.AccountRequest
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.util.Locale
import javax.inject.Inject

interface RegistrationRepository {
    suspend fun createNewAccount(requestedUserName: String, domainName: String, locale: Locale)
}

class RegistrationRepositoryImpl @Inject constructor(
    private val awalaManager: AwalaManager,
    private val accountRepository: AccountRepository,
) : RegistrationRepository {

    override suspend fun createNewAccount(requestedUserName: String, domainName: String, locale: Locale) {
        val keyPair = generateRSAKeyPair()

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
        accountRepository.createAccount(requestedUserName, domainName, locale, keyPair.private)
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
