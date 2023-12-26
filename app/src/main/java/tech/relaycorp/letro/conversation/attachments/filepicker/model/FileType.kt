package tech.relaycorp.letro.conversation.attachments.filepicker.model

sealed class FileType(
    val extension: String,
    val mimeType: String,
) {
    class Pdf(
        extension: String,
        mimeType: String = PDF,
    ) : FileType(extension = extension, mimeType = mimeType)

    class Image(
        extension: String,
        mimeType: String = IMAGE,
    ) : FileType(extension = extension, mimeType = mimeType)

    class Video(
        extension: String,
        mimeType: String = VIDEO,
    ) : FileType(extension = extension, mimeType = mimeType)

    class Audio(
        extension: String,
        mimeType: String = AUDIO,
    ) : FileType(extension = extension, mimeType = mimeType)

    class Other(
        extension: String,
        mimeType: String = OTHER,
    ) : FileType(extension = extension, mimeType = mimeType)

    companion object {
        private const val PDF = "application/pdf"
        private const val IMAGE = "image/*"
        private const val VIDEO = "video/*"
        private const val AUDIO = "audio/*"
        private const val OTHER = "*/*"

        fun fromMimeType(extension: String, mimeType: String?) = when {
            mimeType == null -> Other(extension = extension)
            mimeType.startsWith("application/pdf") -> Pdf(extension = extension, mimeType = mimeType)
            mimeType.startsWith("image/") -> Image(extension = extension, mimeType = mimeType)
            mimeType.startsWith("video/") -> Video(extension = extension, mimeType = mimeType)
            mimeType.startsWith("audio/") -> Audio(extension = extension, mimeType = mimeType)
            else -> Other(extension = extension, mimeType = mimeType)
        }
    }
}
