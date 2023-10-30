package tech.relaycorp.letro.conversation.model

import tech.relaycorp.letro.conversation.attachments.ui.AttachmentInfo
import java.time.ZonedDateTime
import java.util.UUID

data class ExtendedMessage(
    val conversationId: UUID,
    val senderVeraId: String,
    val recipientVeraId: String,
    val isOutgoing: Boolean,
    val contactDisplayName: String,
    val senderDisplayName: String,
    val recipientDisplayName: String,
    val text: String,
    val sentAtUtc: ZonedDateTime,
    val sentAtBriefFormatted: String,
    val sentAtDetailedFormatted: String,
    val attachments: List<AttachmentInfo> = emptyList(),
)
