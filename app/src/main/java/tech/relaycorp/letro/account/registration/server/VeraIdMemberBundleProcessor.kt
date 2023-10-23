package tech.relaycorp.letro.account.registration.server

import tech.relaycorp.awaladroid.messaging.IncomingMessage
import tech.relaycorp.letro.account.model.Account
import tech.relaycorp.letro.account.storage.repository.AccountRepository
import tech.relaycorp.letro.awala.AwalaManager
import tech.relaycorp.letro.awala.processor.AwalaMessageProcessor
import tech.relaycorp.letro.server.messages.InvalidAccountCreationException
import tech.relaycorp.letro.utils.Logger
import tech.relaycorp.letro.utils.crypto.deserialiseKeyPair
import tech.relaycorp.letro.utils.member.verifyBundle
import tech.relaycorp.veraid.Member
import tech.relaycorp.veraid.pki.MemberIdBundle
import javax.inject.Inject

interface VeraIdMemberBundleProcessor : AwalaMessageProcessor

class VeraIdMemberBundleProcessorImpl @Inject constructor(
    private val accountRepository: AccountRepository,
    private val logger: Logger,
) : VeraIdMemberBundleProcessor {

    override suspend fun process(message: IncomingMessage, awalaManager: AwalaManager) {
        val memberIdInfo = deserialiseMessage(message) ?: return
        val memberIdBundle = memberIdInfo.first
        val member = memberIdInfo.second
        getAccountsToUpdate(memberIdBundle, member)
            .forEach { account ->
                accountRepository.updateAccount(
                    account = account,
                    accountId = if (member.userName.isNullOrEmpty()) member.orgName else "${member.userName}@${member.orgName}",
                    veraidBundle = message.content,
                )
            }
    }

    override suspend fun isFromExpectedSender(
        message: IncomingMessage,
        awalaManager: AwalaManager,
    ): Boolean {
        val memberIdInfo = deserialiseMessage(message) ?: return false
        val memberIdBundle = memberIdInfo.first
        val member = memberIdInfo.second
        return getAccountsToUpdate(memberIdBundle, member)
            .all { it.publicThirdPartyNodeId == message.senderEndpoint.nodeId }
    }

    private fun getAccountsToUpdate(memberIdBundle: MemberIdBundle, member: Member): List<Account> {
        return accountRepository.allAccounts.value
            .filter { it.veraidPrivateKey.deserialiseKeyPair().public == memberIdBundle.memberPublicKey && it.domain == member.orgName }
    }

    private suspend fun deserialiseMessage(message: IncomingMessage): Pair<MemberIdBundle, Member>? {
        return try {
            verifyBundle(message.content)
        } catch (exc: InvalidAccountCreationException) {
            logger.w(TAG, "Invalid member id bundle", exc)
            null
        }
    }
}

private const val TAG = "VeraIdMemberBundleProcessorImpl"
