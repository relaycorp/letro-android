package tech.relaycorp.letro.ui.conversations.newMessage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tech.relaycorp.letro.data.ConversationDataModel
import tech.relaycorp.letro.data.createNewConversation
import tech.relaycorp.letro.repository.UserRepository
import javax.inject.Inject

@HiltViewModel
class NewMessageViewModel @Inject constructor(
    userRepository: UserRepository,
// TODO add    conversationRepository: ConversationRepository,
) : ViewModel() {

    private val _currentUser = userRepository.currentUserDataFlow
    private val _currentConversationDataFlow: MutableStateFlow<ConversationDataModel> = MutableStateFlow(
        createNewConversation(
            sender = _currentUser.value?.username.toString(),
        ),
    )
    val currentConversationDataFlow: MutableStateFlow<ConversationDataModel> get() = _currentConversationDataFlow

    fun onRecipientInput(recipient: String) {
        viewModelScope.launch {
            _currentConversationDataFlow.update {
                it.copy(recipient = recipient)
            }
        }
    }

    fun onContentInput(content: String) {
        viewModelScope.launch {
            _currentConversationDataFlow.update {
                val newMessage = it.messages.last().copy(body = content)
                val newMessages = it.messages.dropLast(1) + newMessage
                it.copy(messages = newMessages)
            }
        }
    }

    fun onSubjectInput(subject: String) {
        viewModelScope.launch {
            _currentConversationDataFlow.update {
                it.copy(subject = subject)
            }
        }
    }

    fun onAttachmentClicked() {
        // TODO
    }

    fun onSendClicked() {
        // TODO
    }
}
