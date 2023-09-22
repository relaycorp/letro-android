package tech.relaycorp.letro.messages.model

data class ConversationAwalaWrapper(
    val conversationId: String,
    val senderVeraId: String,
    val recipientVeraId: String,
    val subject: String?,
    val messageText: String,
)

data class MessageAwalaWrapper(
    val conversationId: String,
    val messageText: String,
    val senderVeraId: String,
    val recipientVeraId: String,
)
