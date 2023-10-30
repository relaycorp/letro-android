package tech.relaycorp.letro.contacts.pairing.server.match

import tech.relaycorp.letro.awala.message.AwalaIncomingMessageContent
import tech.relaycorp.letro.awala.message.ContactPairingRequest
import tech.relaycorp.letro.awala.message.InvalidPairingRequestException
import tech.relaycorp.letro.awala.parser.AwalaMessageParser
import tech.relaycorp.letro.utils.Logger
import javax.inject.Inject

interface ContactPairingMatchParser :
    AwalaMessageParser<AwalaIncomingMessageContent.ContactPairingMatch>

class ContactPairingMatchParserImpl @Inject constructor(
    private val logger: Logger,
) : ContactPairingMatchParser {

    override suspend fun parse(content: ByteArray): AwalaIncomingMessageContent.ContactPairingMatch? {
        val (signerVeraidId, request) = try {
            ContactPairingRequest.deserialise(content)
        } catch (exc: InvalidPairingRequestException) {
            logger.w(TAG, "Invalid pairing request match", exc)
            return null
        }
        return AwalaIncomingMessageContent.ContactPairingMatch(
            ownerVeraId = request.contactVeraidId,
            contactVeraId = signerVeraidId,
            contactEndpointPublicKey = request.requesterEndpointPublicKey,
        )
    }

    companion object {
        private const val TAG = "ContactPairingMatchParser"
    }
}
