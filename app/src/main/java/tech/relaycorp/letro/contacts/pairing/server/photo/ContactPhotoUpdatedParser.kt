package tech.relaycorp.letro.contacts.pairing.server.photo

import com.google.gson.Gson
import tech.relaycorp.letro.awala.message.AwalaIncomingMessageContent
import tech.relaycorp.letro.awala.parser.AwalaMessageParser
import tech.relaycorp.letro.conversation.server.dto.PhotoAwalaWrapper
import javax.inject.Inject

interface ContactPhotoUpdatedParser :
    AwalaMessageParser<AwalaIncomingMessageContent.ContactPhotoUpdated>

class ContactPhotoUpdatedParserImpl @Inject constructor() : ContactPhotoUpdatedParser {

    override suspend fun parse(content: ByteArray): AwalaIncomingMessageContent.ContactPhotoUpdated {
        val photo = Gson().fromJson(content.decodeToString(), PhotoAwalaWrapper::class.java)
        return if (photo.photo.isEmpty()) {
            AwalaIncomingMessageContent.ContactPhotoDeleted()
        } else {
            AwalaIncomingMessageContent.ContactPhotoUpdated(
                photo = photo,
            )
        }
    }
}
