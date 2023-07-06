package tech.relaycorp.letro.data

data class MessageDataModel(
    val id: String,
    val sender: String,
    val body: String,
    val timestamp: Long,
    val isDraft: Boolean,
)

fun createNewMessage(sender: String) = MessageDataModel(
    id = "0", // TODO Change this
    sender = sender,
    body = "",
    timestamp = System.currentTimeMillis(),
    isDraft = true,
)
