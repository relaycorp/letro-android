package tech.relaycorp.letro.conversation.storage.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.ZonedDateTime
import java.util.UUID

const val TABLE_NAME_MESSAGES = "messages"

@Entity(
    tableName = TABLE_NAME_MESSAGES,
    foreignKeys = [
        ForeignKey(
            entity = Conversation::class,
            parentColumns = ["conversationId"],
            childColumns = ["conversationId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
/**
 * @param id - primary key for the messages table. Autogenerated.
 * @param conversationId - Unique ID of a conversation, which the message belongs to
 * @param text - text of the message
 * @param ownerVeraId - ID of the account, which this conversation belongs to (for display this message only for a particular account)
 * @param recipientVeraId - the ID of the recipient of the message
 * @param senderVeraId - the ID of the sender of the message
 * @param sentAtUtc - timestamp where this message was sent (or received)
 */
data class Message(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val conversationId: UUID,
    val text: String,
    val ownerVeraId: String,
    val recipientVeraId: String,
    val senderVeraId: String,
    val sentAtUtc: ZonedDateTime,
)
