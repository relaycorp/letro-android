package tech.relaycorp.letro.utils.files

import android.content.ClipData
import android.util.Log
import tech.relaycorp.letro.conversation.attachments.dto.AttachmentToShare

fun ClipData.toAttachmentsToShare(): List<AttachmentToShare> {
    val result = arrayListOf<AttachmentToShare>()
    for (i in 0 until itemCount) {
        val item = getItemAt(i)
        when {
            item.uri != null -> result.add(AttachmentToShare.File(item.uri.toString()))
            item.text != null -> result.add(AttachmentToShare.String(item.text.toString()))
            else -> Log.w(TAG, "Unhandled attachment to share type")
        }
    }
    return result
}

private const val TAG = "ClipDataExt"
