package tech.relaycorp.letro.awala.message

data class AwalaOutgoingMessage(
    val type: MessageType,
    val content: ByteArray,
)