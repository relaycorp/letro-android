package tech.relaycorp.letro.ui.conversations.newMessage

import android.util.Log
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
            contactId = 0L, // TODO Update
        ),
    )

    private val _newMessageUIStateModelFlow: MutableStateFlow<NewMessageUIStateModel> =
        MutableStateFlow(NewMessageUIStateModel())
    val newMessageUIStateModelFlow: MutableStateFlow<NewMessageUIStateModel> get() = _newMessageUIStateModelFlow

    fun onRecipientInput(recipient: String) {
        // TODO
        Log.d("NewMessageViewModel", "onRecipientInput: $recipient")
//        viewModelScope.launch {
//            _currentConversationDataFlow.update {
//                it.copy(contactAddress = recipient)
//            }
//        }
    }

    fun onContentInput(content: String) {
        // TODO
        Log.d("NewMessageViewModel", "onContentInput: $content")
//        viewModelScope.launch {
//            _currentConversationDataFlow.update {dataModel: ConversationDataModel ->
//                val newMessage = dataModel.messages.last().copy(body = content)
//                val newMessages = dataModel.messages.dropLast(1) + newMessage
//                dataModel.copy(messages = newMessages)
//            }
//        }
    }

    fun onSubjectInput(subject: String) {
        // TODO Update
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
