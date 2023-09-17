package tech.relaycorp.letro.messages.storage.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

const val TABLE_NAME_CONVERSATIONS = "conversations"

@Entity(
    tableName = TABLE_NAME_CONVERSATIONS,
)
data class Conversation(
    @PrimaryKey(autoGenerate = true)
    val keyId: Long = 0L,
    val conversationId: UUID = UUID.randomUUID(),
    val ownerVeraId: String,
    val contactVeraId: String,
    val subject: String? = null,
)
