package tech.relaycorp.letro.conversation.server.dto

import tech.relaycorp.letro.awala.message.AwalaIncomingMessage
import tech.relaycorp.letro.awala.message.MessageType

data class NewMessageIncomingMessage(
    override val content: MessageAwalaWrapper,
) : AwalaIncomingMessage<MessageAwalaWrapper> {
    override val type: MessageType
        get() = MessageType.NewMessage
}
