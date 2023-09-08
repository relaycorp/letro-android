package tech.relaycorp.letro.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

const val CONVERSATION_TABLE_NAME = "conversation"

@Entity(
    tableName = CONVERSATION_TABLE_NAME,
    foreignKeys = [
        ForeignKey(
            entity = ContactDataModel::class,
            parentColumns = ["id"],
            childColumns = ["contactId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("contactId")],
)
data class ConversationDataModel(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val contactId: Long,
    val subject: String,
    val isRead: Boolean,
    val isArchived: Boolean,
)

fun createNewConversation(
    contactId: Long,
) = ConversationDataModel(
    subject = "",
    isRead = true,
    isArchived = false,
    contactId = contactId,
)
