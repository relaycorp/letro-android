package tech.relaycorp.letro.contacts.pairing.parser

import android.util.Base64
import tech.relaycorp.letro.awala.parser.AwalaMessageParser
import tech.relaycorp.letro.contacts.pairing.dto.ContactPairingMatchIncomingMessage
import tech.relaycorp.letro.contacts.pairing.dto.ContactPairingMatchResponse
import java.nio.charset.Charset
import javax.inject.Inject

interface ContactPairingMatchParser : AwalaMessageParser

class ContactPairingMatchParserImpl @Inject constructor() : ContactPairingMatchParser {

    override fun parse(content: ByteArray): ContactPairingMatchIncomingMessage {
        val contentString = content.toString(Charset.defaultCharset())
        val parts = contentString.split(",")
        return ContactPairingMatchIncomingMessage(
            content = ContactPairingMatchResponse(
                ownerVeraId = parts[0],
                contactVeraId = parts[1],
                contactEndpointId = parts[2],
                contactEndpointPublicKey = Base64.decode(parts[3], Base64.NO_WRAP),
            ),
        )
    }
}
