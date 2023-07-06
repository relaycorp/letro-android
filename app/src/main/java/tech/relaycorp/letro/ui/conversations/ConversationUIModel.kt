package tech.relaycorp.letro.ui.conversations

data class ConversationUIModel(
    val contact: String,
    val numberOfMessages: Int,
    val subject: String,
    val lastMessageTime: String,
    val lastMessageText: String,
    val isRead: Boolean,
)
