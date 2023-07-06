package tech.relaycorp.letro.ui.conversations.messages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import tech.relaycorp.letro.data.MessageDataModel
import tech.relaycorp.letro.repository.ConversationRepository
import javax.inject.Inject

@HiltViewModel
class MessagesViewModel @Inject constructor(
    conversationRepository: ConversationRepository,
) : ViewModel() {

    private val _messagesDataFlow: MutableStateFlow<List<MessageDataModel>> = MutableStateFlow(emptyList())
    val messagesDataFlow: MutableStateFlow<List<MessageDataModel>> get() = _messagesDataFlow

    init {
        viewModelScope.launch {
            conversationRepository.conversationsDataFlow.collect { conversations ->
                _messagesDataFlow.emit(conversations.first().messages)
            }
        }
    }
}
