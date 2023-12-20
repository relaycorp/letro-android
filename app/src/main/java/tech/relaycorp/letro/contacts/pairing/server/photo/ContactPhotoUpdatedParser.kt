package tech.relaycorp.letro.contacts.pairing.server.photo

import tech.relaycorp.letro.awala.message.AwalaIncomingMessageContent
import tech.relaycorp.letro.awala.parser.AwalaMessageParser
import javax.inject.Inject

interface ContactPhotoUpdatedParser :
    AwalaMessageParser<AwalaIncomingMessageContent.ContactPhotoUpdated>

class ContactPhotoUpdatedParserImpl @Inject constructor() : ContactPhotoUpdatedParser {

    override suspend fun parse(content: ByteArray): AwalaIncomingMessageContent.ContactPhotoUpdated {
        return if (content.isEmpty()) {
            AwalaIncomingMessageContent.ContactPhotoDeleted()
        } else {
            AwalaIncomingMessageContent.ContactPhotoUpdated(content)
        }
    }
}
