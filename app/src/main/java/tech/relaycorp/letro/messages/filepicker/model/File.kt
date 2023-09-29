package tech.relaycorp.letro.messages.filepicker.model

data class File(
    val id: Int,
    val name: String,
    val extension: FileExtension,
    val content: ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as File

        if (id != other.id) return false
        if (name != other.name) return false
        if (extension != other.extension) return false
        if (!content.contentEquals(other.content)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + name.hashCode()
        result = 31 * result + extension.hashCode()
        result = 31 * result + content.contentHashCode()
        return result
    }
}
