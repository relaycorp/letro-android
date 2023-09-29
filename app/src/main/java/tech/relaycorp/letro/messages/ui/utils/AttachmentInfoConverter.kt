package tech.relaycorp.letro.messages.ui.utils

import android.content.Context
import androidx.annotation.DrawableRes
import dagger.hilt.android.qualifiers.ApplicationContext
import tech.relaycorp.letro.R
import tech.relaycorp.letro.messages.filepicker.model.File
import tech.relaycorp.letro.messages.filepicker.model.FileExtension
import tech.relaycorp.letro.messages.ui.AttachmentInfo
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
            file.content.isMoreThanMegabyte() -> {
                val size = file.content.size.bytesToMb()
                context.getString(R.string.file_size_megabytes, String.format("%.2f", size))
            }

            file.content.isMoreThanKilobyte() -> {
                val size = file.content.size.bytesToKb()
                context.getString(R.string.file_size_kilobytes, String.format("%.2f", size))
            }

            else -> {
                context.getString(R.string.file_size_bytes, file.content.size.toString())
            }
        }
    }

    @DrawableRes
    private fun getIcon(file: File): Int = when (file.extension) {
        FileExtension.Pdf -> R.drawable.attachment_pdf
        FileExtension.Image -> R.drawable.attachment_image
        FileExtension.Video -> R.drawable.attachment_video
        FileExtension.Audio -> R.drawable.attachment_audio
        FileExtension.Other -> R.drawable.attachment_default
    }
}
