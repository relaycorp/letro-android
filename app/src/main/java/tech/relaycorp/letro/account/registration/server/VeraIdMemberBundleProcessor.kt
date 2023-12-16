package tech.relaycorp.letro.account.registration.server

import tech.relaycorp.letro.account.model.Account
import tech.relaycorp.letro.account.storage.repository.AccountRepository
import tech.relaycorp.letro.awala.AwalaManager
import tech.relaycorp.letro.awala.message.AwalaIncomingMessageContent
import tech.relaycorp.letro.awala.processor.AwalaMessageProcessor
import tech.relaycorp.letro.utils.Logger
import tech.relaycorp.letro.utils.crypto.deserialiseKeyPair
import tech.relaycorp.veraid.Member
import tech.relaycorp.veraid.pki.MemberIdBundle
import javax.inject.Inject

class VeraIdMemberBundleProcessor @Inject constructor(
    private val accountRepository: AccountRepository,
    parser: VeraIdMemberBundleParser,
    logger: Logger,
) : AwalaMessageProcessor<AwalaIncomingMessageContent.VeraIdMemberBundle>(parser, logger) {

    override suspend fun handleMessage(
        content: AwalaIncomingMessageContent.VeraIdMemberBundle,
        awalaManager: AwalaManager,
    ) {
        val memberIdBundle = content.bundle
        val member = content.member
        getAccountsToUpdate(memberIdBundle, member)
            .forEach { account ->
                accountRepository.updateAccount(
                    account = account,
                    accountId = if (member.userName.isNullOrEmpty()) member.orgName else "${member.userName}@${member.orgName}",
                    veraidBundle = content.bundleSerialised,
                )
            }
    }

    override suspend fun isFromExpectedSender(
        content: AwalaIncomingMessageContent.VeraIdMemberBundle,
        recipientNodeId: String,
        senderNodeId: String,
        awalaManager: AwalaManager,
    ): Boolean {
        val memberIdBundle = content.bundle
        val member = content.member
        return getAccountsToUpdate(memberIdBundle, member)
            .all { it.veraidAuthEndpointId == senderNodeId }
    }

    private fun getAccountsToUpdate(memberIdBundle: MemberIdBundle, member: Member): List<Account> {
        return accountRepository.allAccounts.value
            .filter { it.veraidPrivateKey.deserialiseKeyPair().public == memberIdBundle.memberPublicKey && it.domain == member.orgName }
    }
}

private const val TAG = "VeraIdMemberBundleProcessorImpl"
