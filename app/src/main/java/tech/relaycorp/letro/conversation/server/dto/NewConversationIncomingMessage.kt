package tech.relaycorp.letro.conversation.server.dto

import tech.relaycorp.letro.awala.message.AwalaIncomingMessage
import tech.relaycorp.letro.awala.message.MessageType

data class NewConversationIncomingMessageContent(
    val conversation: ConversationAwalaWrapper,
)

data class NewConversationIncomingMessage(
    override val content: NewConversationIncomingMessageContent,
) : AwalaIncomingMessage<NewConversationIncomingMessageContent> {
    override val type: MessageType
        get() = MessageType.NewConversation
}
