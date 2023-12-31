package tech.relaycorp.letro.account.registration.storage

import tech.relaycorp.awaladroid.AwaladroidException
import tech.relaycorp.letro.account.registration.utils.AccountIdBuilder
import tech.relaycorp.letro.account.storage.repository.AccountRepository
import tech.relaycorp.letro.awala.AwalaManager
import tech.relaycorp.letro.awala.message.AwalaEndpoint
import tech.relaycorp.letro.awala.message.AwalaOutgoingMessage
import tech.relaycorp.letro.awala.message.MessageType
import tech.relaycorp.letro.server.messages.AccountRequest
import tech.relaycorp.letro.utils.ext.isNotEmptyOrBlank
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.util.Locale
import javax.inject.Inject
import kotlin.jvm.Throws

interface RegistrationRepository {
    @Throws(DuplicateAccountIdException::class)
    suspend fun createNewAccount(requestedUserName: String, domainName: String, locale: Locale)

    @Throws(DuplicateAccountIdException::class)
    suspend fun loginToExistingAccount(domainName: String, awalaEndpoint: String, token: String)

    fun isAccountWithThisIdAlreadyExists(requestedUserName: String, domainName: String): Boolean
}

class RegistrationRepositoryImpl @Inject constructor(
    private val awalaManager: AwalaManager,
    private val accountRepository: AccountRepository,
    private val accountIdBuilder: AccountIdBuilder,
) : RegistrationRepository {

    @Throws(DuplicateAccountIdException::class, AwaladroidException::class)
    override suspend fun createNewAccount(requestedUserName: String, domainName: String, locale: Locale) {
        if (isAccountWithThisIdAlreadyExists(requestedUserName, domainName)) {
            throw DuplicateAccountIdException(accountIdBuilder.build(requestedUserName, domainName))
        }
        val keyPair = generateRSAKeyPair()

        val creationRequest = AccountRequest(
            requestedUserName,
            locale,
            keyPair.public,
        )
        val nodeIds = awalaManager
            .sendMessage(
                outgoingMessage = AwalaOutgoingMessage(
                    type = MessageType.AccountCreationRequest,
                    content = creationRequest.serialise(keyPair.private),
                ),
                recipient = AwalaEndpoint.Public(),
                senderAccount = null,
            )
        accountRepository.createAccount(
            requestedUserName = requestedUserName,
            domainName = domainName,
            veraidPrivateKey = keyPair.private,
            firstPartyEndpointNodeId = nodeIds.firstParty,
            thirdPartyEndpointNodeId = nodeIds.thirdParty,
            locale = locale,
        )
    }

    @Throws(DuplicateAccountIdException::class, AwaladroidException::class)
    @Suppress("NAME_SHADOWING")
    override suspend fun loginToExistingAccount(domainName: String, awalaEndpoint: String, token: String) {
        val requestedUserName = "..."
        if (isAccountWithThisIdAlreadyExists(requestedUserName, domainName)) {
            throw DuplicateAccountIdException(accountIdBuilder.build(requestedUserName, domainName))
        }
        val keyPair = generateRSAKeyPair()
        val nodeIds = awalaManager.sendMessage(
            outgoingMessage = AwalaOutgoingMessage(
                type = MessageType.ConnectionParamsRequest,
                content = if (awalaEndpoint.isNotEmptyOrBlank()) awalaEndpoint.toByteArray() else domainName.toByteArray(),
            ),
            recipient = AwalaEndpoint.Public(),
            senderAccount = null,
        )
        accountRepository.createAccount(
            requestedUserName = requestedUserName,
            domainName = domainName,
            veraidPrivateKey = keyPair.private,
            firstPartyEndpointNodeId = nodeIds.firstParty,
            thirdPartyEndpointNodeId = nodeIds.thirdParty,
            awalaEndpoint = if (awalaEndpoint.isNotEmptyOrBlank()) awalaEndpoint else null,
            token = token,
        )
    }

    /**
     * Generate an ephemeral key pair temporarily (we'll persist it once VeraId is integrated).
     */
    private fun generateRSAKeyPair(): KeyPair {
        val keyGen = KeyPairGenerator.getInstance("RSA")
        keyGen.initialize(2048)
        return keyGen.generateKeyPair()
    }

    override fun isAccountWithThisIdAlreadyExists(requestedUserName: String, domainName: String): Boolean {
        val accountId = accountIdBuilder.build(requestedUserName = requestedUserName, domainName = domainName)
        return accountRepository.allAccounts.value.any { it.accountId == accountId }
    }
}

class DuplicateAccountIdException(requestedAccountId: String) : IllegalStateException("Account with id $requestedAccountId already exists")
