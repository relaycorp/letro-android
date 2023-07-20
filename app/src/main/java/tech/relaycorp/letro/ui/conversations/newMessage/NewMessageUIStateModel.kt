package tech.relaycorp.letro.ui.conversations.newMessage

data class NewMessageUIStateModel(
    val senderAddress: String = "",
    val recipientAddress: String = "",
    val subject: String = "",
    val body: String = "",
)
