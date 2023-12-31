package tech.relaycorp.letro.conversation.attachments.utils

import android.content.Context
import androidx.annotation.DrawableRes
import dagger.hilt.android.qualifiers.ApplicationContext
import tech.relaycorp.letro.R
import tech.relaycorp.letro.conversation.attachments.filepicker.model.File
import tech.relaycorp.letro.conversation.attachments.filepicker.model.FileType
import tech.relaycorp.letro.conversation.attachments.ui.AttachmentInfo
import tech.relaycorp.letro.utils.files.bytesToKb
import tech.relaycorp.letro.utils.files.bytesToMb
import tech.relaycorp.letro.utils.files.isMoreThanKilobyte
import tech.relaycorp.letro.utils.files.isMoreThanMegabyte
import javax.inject.Inject

interface AttachmentInfoConverter {
    fun convert(file: File): AttachmentInfo
}

class AttachmentInfoConverterImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : AttachmentInfoConverter {

    override fun convert(file: File): AttachmentInfo {
        return AttachmentInfo(
            fileId = file.id,
            name = file.name,
            size = getDisplayedSize(file),
            icon = getIcon(file),
        )
    }

    private fun getDisplayedSize(file: File): String {
        return when {
            file.size.isMoreThanMegabyte() -> {
                val size = file.size.bytesToMb()
                context.getString(R.string.file_size_megabytes, String.format("%.2f", size))
            }

            file.size.isMoreThanKilobyte() -> {
                val size = file.size.bytesToKb()
                context.getString(R.string.file_size_kilobytes, String.format("%.2f", size))
            }

            else -> {
                context.getString(R.string.file_size_bytes, file.size.toString())
            }
        }
    }

    @DrawableRes
    private fun getIcon(file: File): Int = when (file.type) {
        is FileType.Pdf -> R.drawable.attachment_pdf
        is FileType.Image -> R.drawable.attachment_image
        is FileType.Video -> R.drawable.attachment_video
        is FileType.Audio -> R.drawable.attachment_audio
        is FileType.Other -> R.drawable.attachment_default
    }
}
