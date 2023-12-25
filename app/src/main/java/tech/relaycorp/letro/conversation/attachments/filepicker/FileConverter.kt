package tech.relaycorp.letro.conversation.attachments.filepicker

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import androidx.core.net.toUri
import tech.relaycorp.letro.conversation.attachments.filepicker.model.File
import tech.relaycorp.letro.conversation.attachments.filepicker.model.FileType
import tech.relaycorp.letro.conversation.server.dto.AttachmentAwalaWrapper
import tech.relaycorp.letro.conversation.storage.entity.Attachment
import java.util.UUID
import kotlin.jvm.Throws

interface FileConverter {
    @Throws(FileSizeExceedsLimitException::class)
    suspend fun getFile(uri: String): File.FileWithContent?
    suspend fun getFile(attachment: Attachment): File.FileWithoutContent?
    suspend fun getFile(attachmentAwalaWrapper: AttachmentAwalaWrapper): File.FileWithContent?
}

@Suppress("NAME_SHADOWING")
abstract class DefaultFileConverter(
    private val contentResolver: ContentResolver,
    private val fileSizeLimitBytes: Int,
) : FileConverter {

    @Throws(FileSizeExceedsLimitException::class)
    override suspend fun getFile(uri: String): File.FileWithContent? {
        val uri = Uri.parse(uri)
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            cursor.moveToFirst()
            val sizeColumnIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (sizeColumnIndex > -1) {
                val fileSize = cursor.getString(sizeColumnIndex)?.toLongOrNull() ?: return null
                if (fileSize >= fileSizeLimitBytes) {
                    throw FileSizeExceedsLimitException(fileSize, fileSizeLimitBytes)
                }
                contentResolver.openInputStream(uri).use {
                    it ?: return null // TODO: log error?
                    val bufferedStream = it.buffered(fileSizeLimitBytes)
                    val bytes = bufferedStream.readBytes()
                    val extension = getFileExtension(uri)
                    val fileName = getFileName(uri) ?: UNKNOWN_FILE_NAME
                    return File.FileWithContent(
                        id = UUID.randomUUID(),
                        name = fileName,
                        type = extension,
                        size = bytes.size.toLong(),
                        content = bytes,
                    )
                }
            }
        }
        return null
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

    override suspend fun getFile(attachmentAwalaWrapper: AttachmentAwalaWrapper): File.FileWithContent? {
        return File.FileWithContent(
            id = UUID.randomUUID(),
            name = attachmentAwalaWrapper.fileName,
            type = FileType.fromMimeType(attachmentAwalaWrapper.extension, attachmentAwalaWrapper.mimeType),
            size = attachmentAwalaWrapper.content.size.toLong(),
            content = attachmentAwalaWrapper.content,
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

    private fun getFileExtension(uri: Uri): FileType {
        return when (val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(contentResolver.getType(uri))) {
            "pdf" -> FileType.Pdf(extension)
            "png", "jpg", "jpeg", "webp", "gif" -> FileType.Image(extension)
            "mp3", "wav", "aac", "pcm" -> FileType.Audio(extension)
            "mp4", "mov", "wmv", "avi" -> FileType.Video(extension)
            else -> {
                val extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
                FileType.fromMimeType(
                    extension = extension,
                    mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension),
                )
            }
        }
    }

    private companion object {
        private const val UNKNOWN_FILE_NAME = "Unknown"
    }
}

class FileSizeExceedsLimitException(fileSize: Long, limit: Int) : IllegalStateException("This file is exceed the limit of $limit bytes, but file size is $fileSize")
