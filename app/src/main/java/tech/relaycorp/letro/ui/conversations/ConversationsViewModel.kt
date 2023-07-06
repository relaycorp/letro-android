package tech.relaycorp.letro.ui.conversations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import tech.relaycorp.letro.data.ConversationDataModel
import tech.relaycorp.letro.repository.ConversationRepository
import javax.inject.Inject

@HiltViewModel
class ConversationsViewModel @Inject constructor(
    conversationRepository: ConversationRepository,
) : ViewModel() {

    private val _conversationsDataFlow: StateFlow<List<ConversationDataModel>> =
        conversationRepository.conversationsDataFlow

    private val _conversationsUIFlow: MutableStateFlow<List<ConversationUIModel>> =
        MutableStateFlow(emptyList())
    val conversationsUIFlow: StateFlow<List<ConversationUIModel>> get() = _conversationsUIFlow

    init {
        viewModelScope.launch {
            _conversationsDataFlow.collect { list ->
                _conversationsUIFlow.emit(
                    list.map { dataModel ->
                        val lastMessage = dataModel.messages.last() // TODO Verify later that the correct message is fetched
                        ConversationUIModel(
                            contact = dataModel.contact,
                            subject = dataModel.subject,
                            lastMessageText = lastMessage.body,
                            lastMessageTime = lastMessage.timestamp.toString(), // TODO Beautify
                            numberOfMessages = dataModel.messages.size,
                            isRead = dataModel.isRead,
                        )
                    },
                )
            }
        }
    }
}
