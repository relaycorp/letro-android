package tech.relaycorp.letro.contacts.pairing.parser

import tech.relaycorp.letro.awala.parser.AwalaMessageParser
import tech.relaycorp.letro.contacts.pairing.dto.ContactPairingAuthorizationIncomingMessage
import tech.relaycorp.letro.contacts.pairing.dto.ContactPairingAuthorizationResponse
import javax.inject.Inject

interface ContactPairingAuthorizationParser : AwalaMessageParser

class ContactPairingAuthorizationParserImpl @Inject constructor() :
    ContactPairingAuthorizationParser {

    override fun parse(content: ByteArray): ContactPairingAuthorizationIncomingMessage {
        return ContactPairingAuthorizationIncomingMessage(
            content = ContactPairingAuthorizationResponse(
                authData = content,
            ),
        )
    }
}
