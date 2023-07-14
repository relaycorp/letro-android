package tech.relaycorp.letro.ui.conversations.newMessage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tech.relaycorp.letro.data.entity.ConversationDataModel
import tech.relaycorp.letro.data.entity.createNewConversation
import tech.relaycorp.letro.repository.AccountRepository
import javax.inject.Inject

@HiltViewModel
class NewMessageViewModel @Inject constructor(
    accountRepository: AccountRepository,
// TODO add    conversationRepository: ConversationRepository,
) : ViewModel() {

    private val _currentAccount = accountRepository.currentAccountDataFlow
    private val _currentConversationDataFlow: MutableStateFlow<ConversationDataModel> = MutableStateFlow(
        createNewConversation(
            sender = _currentAccount.value?.address.toString(),
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
