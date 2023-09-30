package tech.relaycorp.letro.messages.filepicker.model

sealed class FileExtension(
    val mimeType: String,
) {
    class Pdf(
        mimeType: String = PDF,
    ) : FileExtension(mimeType)

    class Image(
        mimeType: String = IMAGE,
    ) : FileExtension(mimeType)

    class Video(
        mimeType: String = VIDEO,
    ) : FileExtension(mimeType)

    class Audio(
        mimeType: String = AUDIO,
    ) : FileExtension(mimeType)

    class Other(
        mimeType: String = OTHER,
    ) : FileExtension(mimeType)

    companion object {
        private const val PDF = "application/pdf"
        private const val IMAGE = "image/*"
        private const val VIDEO = "video/*"
        private const val AUDIO = "audio/*"
        private const val OTHER = "*/*"

        fun fromMimeType(mimeType: String?) = when {
            mimeType == null -> Other()
            mimeType.startsWith("application/pdf") -> Pdf(mimeType)
            mimeType.startsWith("image/") -> Image(mimeType)
            mimeType.startsWith("video/") -> Video(mimeType)
            mimeType.startsWith("audio/") -> Audio(mimeType)
            else -> Other(mimeType)
        }
    }
}
