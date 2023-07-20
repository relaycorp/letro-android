package tech.relaycorp.letro.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

const val MESSAGE_TABLE_NAME = "message"

@Entity(
    tableName = MESSAGE_TABLE_NAME,
    foreignKeys = [
        ForeignKey(
            entity = ConversationDataModel::class,
            parentColumns = ["id"],
            childColumns = ["conversationId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("conversationId")],
)
data class MessageDataModel(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val conversationId: Long,
    val senderId: Long,
    val recipientId: Long,
    val body: String,
    val timestamp: Long,
    val isDraft: Boolean,
)

fun createNewMessage(
    conversationId: Long,
    senderId: Long,
) = MessageDataModel(
    conversationId = conversationId,
    senderId = senderId,
    recipientId = 0L, // TODO This might be a problem
    body = "",
    timestamp = System.currentTimeMillis(),
    isDraft = true,
)
