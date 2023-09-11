package tech.relaycorp.letro.awala.message

interface AwalaIncomingMessage<T> {
    val type: MessageType
    val content: T
}
