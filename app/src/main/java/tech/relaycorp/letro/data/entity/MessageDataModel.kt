package tech.relaycorp.letro.data.entity

import androidx.room.Entity

const val MESSAGE_TABLE_NAME = "message"

@Entity(tableName = MESSAGE_TABLE_NAME)
data class MessageDataModel(
    val id: Long = 0L,
    val sender: String,
    val body: String,
    val timestamp: Long,
    val isDraft: Boolean,
)

fun createNewMessage(sender: String) = MessageDataModel(
    sender = sender,
    body = "",
    timestamp = System.currentTimeMillis(),
    isDraft = true,
)
