package tech.relaycorp.letro.ui.conversations.messages

data class MessagesUIStateModel(
    val messages: List<MessageUIModel> = emptyList(),
)

data class MessageUIModel(
    val senderAddress: String = "",
    val recipientAddress: String = "",
    val body: String = "",
    val timestamp: String = "",
)
