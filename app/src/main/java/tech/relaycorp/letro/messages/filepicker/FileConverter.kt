package tech.relaycorp.letro.messages.filepicker

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import tech.relaycorp.letro.messages.filepicker.model.File
import tech.relaycorp.letro.messages.filepicker.model.FileExtension
import javax.inject.Inject
import kotlin.random.Random

interface FileConverter {
    suspend fun getFile(uri: Uri): File?
}

class FileConverterImpl @Inject constructor(
    private val contentResolver: ContentResolver,
) : FileConverter {

    override suspend fun getFile(uri: Uri): File? {
        contentResolver.openInputStream(uri).use {
            it ?: return null // TODO: log error?
            val bytes = it.readBytes()
            val extension = getFileExtension(uri)
            val fileName = getFileName(uri) ?: UNKNOWN_FILE_NAME
            return File(
                id = Random.nextInt(),
                name = fileName,
                extension = extension,
                content = bytes,
            )
        }
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

    private suspend fun getFileExtension(uri: Uri): FileExtension =
        when (MimeTypeMap.getSingleton().getExtensionFromMimeType(contentResolver.getType(uri))) {
            "pdf" -> FileExtension.Pdf
            "png", "jpg", "jpeg", "webp", "gif" -> FileExtension.Image
            "mp3", "wav", "aac", "pcm" -> FileExtension.Audio
            "mp4", "mov", "wmv", "avi" -> FileExtension.Video
            else -> FileExtension.Other
        }

    private companion object {
        private const val UNKNOWN_FILE_NAME = "Unknown"
    }
}
