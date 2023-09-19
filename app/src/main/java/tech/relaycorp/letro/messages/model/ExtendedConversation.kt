package tech.relaycorp.letro.messages.model

import java.util.UUID

data class ExtendedConversation(
    val conversationId: UUID,
    val ownerVeraId: String,
    val recipientVeraId: String,
    val recipientAlias: String?,
    val subject: String?,
    val lastMessageTimestamp: Long,
    val lastMessageFormattedTimestamp: String,
    val messages: List<ExtendedMessage>,
)
