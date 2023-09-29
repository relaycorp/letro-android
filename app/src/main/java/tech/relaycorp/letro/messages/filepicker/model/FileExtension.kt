package tech.relaycorp.letro.messages.filepicker.model

sealed class FileExtension(
    val name: String,
) {
    data object Pdf : FileExtension(PDF)

    data object Image : FileExtension(IMAGE)

    data object Video : FileExtension(VIDEO)

    data object Audio : FileExtension(AUDIO)

    data object Other : FileExtension(OTHER)

    private companion object {
        private const val PDF = "pdf"
        private const val IMAGE = "image"
        private const val VIDEO = "video"
        private const val AUDIO = "audio"
        private const val OTHER = "other"
    }
}
