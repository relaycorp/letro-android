package tech.relaycorp.letro.conversation.model

import java.time.ZonedDateTime
import java.util.UUID

data class ExtendedConversation(
    val conversationId: UUID,
    val ownerVeraId: String,
    val contactVeraId: String,
    val contactDisplayName: String,
    val subject: String?,
    val lastMessageSentAtUtc: ZonedDateTime,
    val lastMessageFormattedTimestamp: String,
    val lastMessage: ExtendedMessage,
    val isRead: Boolean,
    val isArchived: Boolean,
    val totalMessagesFormattedText: String?,
    val messages: List<ExtendedMessage>,
)
