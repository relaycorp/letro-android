package tech.relaycorp.letro.conversation.viewing

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import tech.relaycorp.letro.conversation.model.ExtendedConversation
import tech.relaycorp.letro.conversation.storage.repository.ConversationsRepository
import tech.relaycorp.letro.ui.navigation.Route
import javax.inject.Inject

@HiltViewModel
class ConversationViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val conversationsRepository: ConversationsRepository,
) : ViewModel() {

    private val conversationId: String = savedStateHandle[Route.Conversation.KEY_CONVERSATION_ID]!!
    val conversation: StateFlow<ExtendedConversation?>
        get() = conversationsRepository.getConversationFlow(viewModelScope, conversationId)

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

    fun onArchiveConversationClicked(): Boolean {
        return conversation.value?.let { conversation ->
            val isArchived = !conversation.isArchived
            conversationsRepository.archiveConversation(conversationId, isArchived)
            isArchived
        } ?: false
    }
}

data class DeleteConversationDialogState(
    val isShown: Boolean = false,
)
