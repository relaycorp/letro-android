package tech.relaycorp.letro.conversation.attachments.sharing

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

interface ShareAttachmentsRepository {
    fun shareAttachmentsLater(files: List<AttachmentToShare>)

    fun getAttachmentsToShareOnce(): List<AttachmentToShare>
}

/**
 * This class is needed, because Google doesn't allow properly pass Array<String> as argument in navigation,
 * So that's why we need to store the array in a separate instance, to retrieve file URIs from the Compose new conversation screen
 */
class ShareAttachmentsRepositoryImpl @Inject constructor() : ShareAttachmentsRepository {

    private val files = arrayListOf<AttachmentToShare>()

    override fun getAttachmentsToShareOnce(): List<AttachmentToShare> {
        return files.toList().also {
            files.clear()
        }
    }

    override fun shareAttachmentsLater(files: List<AttachmentToShare>) {
        this.files.apply {
            clear()
            addAll(files)
        }
    }
}

sealed class AttachmentToShare : Parcelable {

    @Parcelize
    data class File(
        val uri: kotlin.String,
    ) : AttachmentToShare()

    @Parcelize
    data class String(
        val value: kotlin.String,
    ) : AttachmentToShare()
}
