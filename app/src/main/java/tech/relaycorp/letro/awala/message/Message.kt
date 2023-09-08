package tech.relaycorp.letro.awala.message

data class Message(
    val type: MessageType,
    val content: ByteArray,
)