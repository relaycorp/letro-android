package tech.relaycorp.letro.contacts.pairing.server.auth

import tech.relaycorp.letro.awala.message.AwalaIncomingMessageContent
import tech.relaycorp.letro.awala.parser.AwalaMessageParser
import javax.inject.Inject

interface ContactPairingAuthorizationParser : AwalaMessageParser<AwalaIncomingMessageContent.ContactPairingAuthorization>

class ContactPairingAuthorizationParserImpl @Inject constructor() : ContactPairingAuthorizationParser {

    override suspend fun parse(content: ByteArray): AwalaIncomingMessageContent.ContactPairingAuthorization? {
        return AwalaIncomingMessageContent.ContactPairingAuthorization(content)
    }
}
