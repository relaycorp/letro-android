package tech.relaycorp.letro.conversation.server.dto

data class ConversationAwalaWrapper(
    val conversationId: String,
    val senderVeraId: String,
    val recipientVeraId: String,
    val subject: String?,
    val messageText: String,
    val attachments: List<AttachmentAwalaWrapper>,
)

data class MessageAwalaWrapper(
    val conversationId: String,
    val messageText: String,
    val senderVeraId: String,
    val recipientVeraId: String,
    val attachments: List<AttachmentAwalaWrapper>,
)

data class AttachmentAwalaWrapper(
    val fileName: String,
    val content: ByteArray,
    val extension: String,
    val mimeType: String,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AttachmentAwalaWrapper

        if (fileName != other.fileName) return false
        if (!content.contentEquals(other.content)) return false
        if (extension != other.extension) return false
        if (mimeType != other.mimeType) return false

        return true
    }

    override fun hashCode(): Int {
        var result = fileName.hashCode()
        result = 31 * result + content.contentHashCode()
        result = 31 * result + mimeType.hashCode()
        result = 31 * result + extension.hashCode()
        return result
    }
}

data class PhotoAwalaWrapper(
    val photo: ByteArray,
    val extension: String,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PhotoAwalaWrapper

        if (!photo.contentEquals(other.photo)) return false
        return extension == other.extension
    }

    override fun hashCode(): Int {
        var result = photo.contentHashCode()
        result = 31 * result + extension.hashCode()
        return result
    }
}
