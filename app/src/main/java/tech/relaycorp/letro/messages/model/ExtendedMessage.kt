package tech.relaycorp.letro.messages.model

import java.util.UUID

data class ExtendedMessage(
    val conversationId: UUID,
    val senderVeraId: String,
    val recipientVeraId: String,
    val isOutgoing: Boolean,
    val contactDisplayName: String,
    val text: String,
    val sentAtFormatted: String,
)
