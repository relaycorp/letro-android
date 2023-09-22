package tech.relaycorp.letro.messages.dto

import tech.relaycorp.letro.awala.message.AwalaIncomingMessage
import tech.relaycorp.letro.awala.message.MessageType
import tech.relaycorp.letro.messages.model.MessageAwalaWrapper

data class NewMessageIncomingMessage(
    override val content: MessageAwalaWrapper,
) : AwalaIncomingMessage<MessageAwalaWrapper> {
    override val type: MessageType
        get() = MessageType.NewMessage
}
