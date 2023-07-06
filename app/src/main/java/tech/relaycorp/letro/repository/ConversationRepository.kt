package tech.relaycorp.letro.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import tech.relaycorp.letro.data.ConversationDataModel
import tech.relaycorp.letro.data.MessageDataModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConversationRepository @Inject constructor() {

    private val databaseScope = CoroutineScope(Dispatchers.IO)

    private val _conversationsDataFlow: MutableStateFlow<List<ConversationDataModel>> =
        MutableStateFlow(emptyList())
    val conversationsDataFlow: StateFlow<List<ConversationDataModel>> get() = _conversationsDataFlow

    private val _messagesDataFlow: MutableStateFlow<List<MessageDataModel>> =
        MutableStateFlow(emptyList())
    val messagesDataFlow: StateFlow<List<MessageDataModel>> get() = _messagesDataFlow

    init {
        // TODO Remove fake data
        val messages = mutableListOf<MessageDataModel>()
        for (i in 1..10) {
            messages.add(
                MessageDataModel(
                    id = i.toString(),
                    sender = "Sender $i",
                    timestamp = System.currentTimeMillis(),
                    body = "Message $i",
                    isDraft = false,
                ),
            )
        }

        val conversations = mutableListOf<ConversationDataModel>()
        for (i in 1..10) {
            conversations.add(
                ConversationDataModel(
                    id = i.toString(),
                    contact = "Contact $i",
                    sender = "Sender $i",
                    recipient = "Recipient $i",
                    subject = "Subject $i",
                    isRead = false,
                    isArchived = false,
                    messages = messages,
                ),
            )
        }

        databaseScope.launch {
            _conversationsDataFlow.emit(conversations)
        }
    }
}
