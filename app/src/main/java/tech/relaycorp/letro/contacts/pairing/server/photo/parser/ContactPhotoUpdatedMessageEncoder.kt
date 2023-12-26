package tech.relaycorp.letro.contacts.pairing.server.photo.parser

import com.google.gson.Gson
import tech.relaycorp.letro.conversation.server.dto.PhotoAwalaWrapper
import javax.inject.Inject

interface ContactPhotoUpdatedMessageEncoder {
    fun encode(
        photo: ByteArray?,
        extension: String?,
    ): ByteArray
}

class ContactPhotoUpdatedMessageEncoderImpl @Inject constructor() : ContactPhotoUpdatedMessageEncoder {

    override fun encode(photo: ByteArray?, extension: String?): ByteArray {
        val json = Gson().toJson(
            PhotoAwalaWrapper(
                photo = photo ?: ByteArray(0),
                extension = extension ?: "",
            ),
        )
        return json.toByteArray()
    }
}
