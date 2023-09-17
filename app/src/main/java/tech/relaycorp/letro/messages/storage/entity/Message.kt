package tech.relaycorp.letro.messages.storage.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime
import java.util.UUID

const val TABLE_NAME_MESSAGES = "messages"

@Entity(
    tableName = TABLE_NAME_MESSAGES,
)
data class Message(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val conversationId: UUID,
    val text: String,
    val ownerVeraId: String,
    val recipientVeraId: String,
    val senderVeraId: String,
    val sentAt: LocalDateTime,
)
