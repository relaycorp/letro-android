package tech.relaycorp.letro.messages.model

import java.util.UUID

data class ExtendedConversation(
    val conversationId: UUID,
    val ownerVeraId: String,
    val contactVeraId: String,
    val contactDisplayName: String,
    val subject: String?,
    val lastMessageTimestamp: Long,
    val lastMessageFormattedTimestamp: String,
    val lastMessage: ExtendedMessage,
    val messages: List<ExtendedMessage>,
)
