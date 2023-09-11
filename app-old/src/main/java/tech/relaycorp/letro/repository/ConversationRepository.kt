package tech.relaycorp.letro.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import tech.relaycorp.letro.data.entity.ConversationDataModel
import tech.relaycorp.letro.data.entity.MessageDataModel
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
}
