package tech.relaycorp.letro.messages.model

import tech.relaycorp.letro.messages.ui.AttachmentInfo
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
    val sentAtBriefFormatted: String,
    val sentAtDetailedFormatted: String,
    val attachments: List<AttachmentInfo> = emptyList(),
)
