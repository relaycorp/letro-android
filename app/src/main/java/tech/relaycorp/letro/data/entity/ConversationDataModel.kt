package tech.relaycorp.letro.data.entity

import androidx.room.Entity

const val CONVERSATION_TABLE_NAME = "conversation"

@Entity(tableName = CONVERSATION_TABLE_NAME)
data class ConversationDataModel(
    val id: Long = 0L,
    val contact: String,
    val sender: String,
    val recipient: String,
    val subject: String,
    val isRead: Boolean,
    val isArchived: Boolean,
    val messages: List<MessageDataModel>,
)

fun createNewConversation(
    sender: String,
) = ConversationDataModel(
    contact = "",
    sender = sender,
    recipient = "",
    subject = "",
    isRead = true,
    isArchived = false,
    messages = listOf(createNewMessage(sender = sender)),
)
