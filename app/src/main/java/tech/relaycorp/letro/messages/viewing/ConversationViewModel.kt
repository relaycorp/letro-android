package tech.relaycorp.letro.messages.viewing

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import tech.relaycorp.letro.messages.model.ExtendedConversation
import tech.relaycorp.letro.messages.repository.ConversationsRepository
import tech.relaycorp.letro.messages.storage.entity.Conversation
import tech.relaycorp.letro.ui.navigation.Route
import javax.inject.Inject

@HiltViewModel
class ConversationViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val conversationsRepository: ConversationsRepository,
) : ViewModel() {

    private val conversationId: String = savedStateHandle[Route.Conversation.KEY_CONVERSATION_ID]!!
    private val _conversation = MutableStateFlow(conversationsRepository.getConversation(conversationId))
    val conversation: StateFlow<ExtendedConversation?>
        get() = _conversation

    private val _deleteConversationDialogState = MutableStateFlow(DeleteConversationDialogState())
    val deleteConversationDialogState: StateFlow<DeleteConversationDialogState>
        get() = _deleteConversationDialogState

    init {
        conversationsRepository.markConversationAsRead(conversationId)
    }

    fun onDeleteConversationClick() {
        _deleteConversationDialogState.update {
            it.copy(
                isShown = true,
            )
        }
    }

    fun onConfirmConversationDeletionClick() {
        conversationsRepository.deleteConversation(conversationId)
    }

    fun onDeleteConversationBottomSheetDismissed() {
        _deleteConversationDialogState.update {
            it.copy(
                isShown = false,
            )
        }
    }
}

data class DeleteConversationDialogState(
    val isShown: Boolean = false,
)