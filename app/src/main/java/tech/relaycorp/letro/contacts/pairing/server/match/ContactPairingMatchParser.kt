package tech.relaycorp.letro.contacts.pairing.server.match

import android.util.Base64
import tech.relaycorp.letro.awala.message.AwalaIncomingMessageContent
import tech.relaycorp.letro.awala.parser.AwalaMessageParser
import java.nio.charset.Charset
import javax.inject.Inject

interface ContactPairingMatchParser : AwalaMessageParser<AwalaIncomingMessageContent.ContactPairingMatch>

class ContactPairingMatchParserImpl @Inject constructor() : ContactPairingMatchParser {

    override suspend fun parse(content: ByteArray): AwalaIncomingMessageContent.ContactPairingMatch {
        val contentString = content.toString(Charset.defaultCharset())
        val parts = contentString.split(",")
        return AwalaIncomingMessageContent.ContactPairingMatch(
            ownerVeraId = parts[0],
            contactVeraId = parts[1],
            contactEndpointId = parts[2],
            contactEndpointPublicKey = Base64.decode(parts[3], Base64.NO_WRAP),
        )
    }
}
