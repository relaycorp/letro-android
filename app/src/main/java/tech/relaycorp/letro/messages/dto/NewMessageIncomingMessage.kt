package tech.relaycorp.letro.messages.dto

import tech.relaycorp.letro.awala.message.AwalaIncomingMessage
import tech.relaycorp.letro.awala.message.MessageType
import tech.relaycorp.letro.messages.storage.entity.Message

data class NewMessageIncomingMessage(
    override val content: Message,
) : AwalaIncomingMessage<Message> {
    override val type: MessageType
        get() = MessageType.NewMessage
}
