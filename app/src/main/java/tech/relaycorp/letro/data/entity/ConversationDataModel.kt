package tech.relaycorp.letro.data.entity

data class ConversationDataModel(
    val id: String,
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
    id = "0", // TODO Change this
    contact = "",
    sender = sender,
    recipient = "",
    subject = "",
    isRead = true,
    isArchived = false,
    messages = listOf(createNewMessage(sender = sender)),
)
