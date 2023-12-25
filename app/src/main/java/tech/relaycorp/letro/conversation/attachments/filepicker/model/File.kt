package tech.relaycorp.letro.conversation.attachments.filepicker.model

import java.util.UUID

sealed class File(
    val id: UUID,
    val name: String,
    val type: FileType,
    val size: Long,
) {

    class FileWithContent(
        id: UUID,
        name: String,
        type: FileType,
        size: Long,
        val content: ByteArray,
    ) : File(id, name, type, size) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as FileWithContent

            if (id != other.id) return false
            if (name != other.name) return false
            if (type != other.type) return false
            if (size != other.size) return false
            if (!content.contentEquals(other.content)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = id.hashCode()
            result = 31 * result + name.hashCode()
            result = 31 * result + type.hashCode()
            result = 31 * result + size.hashCode()
            result = 31 * result + content.contentHashCode()
            return result
        }
    }

    class FileWithoutContent(
        id: UUID,
        name: String,
        extension: FileType,
        size: Long,
        val path: String,
    ) : File(id, name, extension, size) {

        fun exists() = toFile().exists()
        fun toFile() = java.io.File(path)

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as FileWithoutContent

            if (id != other.id) return false
            if (name != other.name) return false
            if (type != other.type) return false
            if (size != other.size) return false
            if (path != other.path) return false

            return true
        }

        override fun hashCode(): Int {
            var result = id.hashCode()
            result = 31 * result + name.hashCode()
            result = 31 * result + type.hashCode()
            result = 31 * result + size.hashCode()
            result = 31 * result + path.hashCode()
            return result
        }
    }
}
