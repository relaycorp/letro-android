package tech.relaycorp.letro.ui.conversations.newMessage

data class NewMessageUIStateModel(
    val senderVeraId: String = "",
    val recipientVeraId: String = "",
    val subject: String = "",
    val body: String = "",
)
