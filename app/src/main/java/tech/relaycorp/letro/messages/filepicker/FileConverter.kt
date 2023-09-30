package tech.relaycorp.letro.messages.filepicker

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import androidx.core.net.toUri
import tech.relaycorp.letro.messages.filepicker.model.File
import tech.relaycorp.letro.messages.filepicker.model.FileExtension
import tech.relaycorp.letro.messages.storage.entity.Attachment
import java.util.UUID
import javax.inject.Inject

interface FileConverter {
    suspend fun getFile(uri: Uri): File.FileWithContent?
    suspend fun getFile(attachment: Attachment): File.FileWithoutContent?
}

class FileConverterImpl @Inject constructor(
    private val contentResolver: ContentResolver,
) : FileConverter {

    override suspend fun getFile(uri: Uri): File.FileWithContent? {
        contentResolver.openInputStream(uri).use {
            it ?: return null // TODO: log error?
            val bytes = it.readBytes()
            val extension = getFileExtension(uri)
            val fileName = getFileName(uri) ?: UNKNOWN_FILE_NAME
            return File.FileWithContent(
                id = UUID.randomUUID(),
                name = fileName,
                extension = extension,
                size = bytes.size.toLong(),
                content = bytes,
            )
        }
    }

    override suspend fun getFile(attachment: Attachment): File.FileWithoutContent? {
        val file = java.io.File(attachment.path)
        if (!file.exists()) {
            return null
        }
        return File.FileWithoutContent(
            id = attachment.fileId,
            name = file.name,
            extension = getFileExtension(file.toUri()),
            size = file.length(),
            path = file.absolutePath,
        )
    }

    private suspend fun getFileName(uri: Uri): String? {
        if (uri.scheme.equals("content")) {
            contentResolver.query(uri, null, null, null, null)?.use {
                val nameColumnIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (it.moveToFirst()) {
                    return it.getString(nameColumnIndex)
                }
            }
        }
        // TODO: log error?
        return null
    }

    private fun getFileExtension(uri: Uri): FileExtension {
        return when (MimeTypeMap.getSingleton().getExtensionFromMimeType(contentResolver.getType(uri))) {
            "pdf" -> FileExtension.Pdf()
            "png", "jpg", "jpeg", "webp", "gif" -> FileExtension.Image()
            "mp3", "wav", "aac", "pcm" -> FileExtension.Audio()
            "mp4", "mov", "wmv", "avi" -> FileExtension.Video()
            else -> {
                val extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
                FileExtension.fromMimeType(MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension))
            }
        }
    }

    private companion object {
        private const val UNKNOWN_FILE_NAME = "Unknown"
    }
}
