package tech.relaycorp.letro.account.registration.server

import tech.relaycorp.letro.awala.message.AwalaIncomingMessageContent
import tech.relaycorp.letro.awala.parser.AwalaMessageParser
import tech.relaycorp.letro.server.messages.InvalidAccountCreationException
import tech.relaycorp.letro.utils.Logger
import tech.relaycorp.letro.utils.member.verifyBundle
import tech.relaycorp.veraid.Member
import tech.relaycorp.veraid.pki.MemberIdBundle
import javax.inject.Inject

interface VeraIdMemberBundleParser : AwalaMessageParser<AwalaIncomingMessageContent.VeraIdMemberBundle>

class VeraIdMemberBundleParserImpl @Inject constructor(
    private val logger: Logger,
) : VeraIdMemberBundleParser {

    override suspend fun parse(content: ByteArray): AwalaIncomingMessageContent.VeraIdMemberBundle? {
        val memberIdInfo = deserialiseMessage(content) ?: return null
        return AwalaIncomingMessageContent.VeraIdMemberBundle(
            memberIdBundle = memberIdInfo.first,
            member = memberIdInfo.second,
            veraIdBundle = content,
        )
    }

    private suspend fun deserialiseMessage(content: ByteArray): Pair<MemberIdBundle, Member>? {
        return try {
            verifyBundle(content)
        } catch (exc: InvalidAccountCreationException) {
            logger.w(TAG, "Invalid member id bundle", exc)
            null
        }
    }
}

private const val TAG = "VeraIdMemberBundleParser"
