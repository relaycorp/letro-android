package tech.relaycorp.letro.account.registration.server

import tech.relaycorp.awaladroid.messaging.IncomingMessage
import tech.relaycorp.letro.account.storage.repository.AccountRepository
import tech.relaycorp.letro.awala.AwalaManager
import tech.relaycorp.letro.awala.processor.AwalaMessageProcessor
import tech.relaycorp.letro.server.messages.InvalidAccountCreationException
import tech.relaycorp.letro.utils.Logger
import tech.relaycorp.letro.utils.crypto.deserialiseKeyPair
import tech.relaycorp.letro.utils.member.verifyBundle
import tech.relaycorp.veraid.pki.MemberIdBundle
import tech.relaycorp.veraid.pki.PkiException
import javax.inject.Inject

interface VeraIdMemberBundleProcessor : AwalaMessageProcessor

class VeraIdMemberBundleProcessorImpl @Inject constructor(
    private val accountRepository: AccountRepository,
    private val logger: Logger,
) : VeraIdMemberBundleProcessor {

    override suspend fun process(message: IncomingMessage, awalaManager: AwalaManager) {
        val memberIdBundle = try {
            MemberIdBundle.deserialise(message.content)
        } catch (e: PkiException) {
            logger.w(TAG, e)
            return
        }
        val memberId = try {
            verifyBundle(message.content)
        } catch (exc: InvalidAccountCreationException) {
            logger.w(TAG, "Invalid member id bundle", exc)
            return
        }
        accountRepository.allAccounts.value
            .filter { it.veraidPrivateKey.deserialiseKeyPair().public == memberIdBundle.memberPublicKey && it.domain == memberId.second.orgName }
            .forEach { account ->
                accountRepository.updateAccount(
                    account = account,
                    accountId = if (memberId.second.userName.isNullOrEmpty()) memberId.second.orgName else "${memberId.second.userName}@${memberId.second.orgName}",
                    veraidBundle = message.content,
                )
            }
    }
}

private const val TAG = "VeraIdMemberBundleProcessorImpl"
