package tech.relaycorp.letro.conversation.attachments.dto

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

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
