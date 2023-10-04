package tech.relaycorp.letro.conversation.viewing

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import tech.relaycorp.letro.contacts.storage.repository.ContactsRepository
import tech.relaycorp.letro.conversation.model.ExtendedConversation
import tech.relaycorp.letro.conversation.storage.repository.ConversationsRepository
import tech.relaycorp.letro.ui.navigation.Route
import tech.relaycorp.letro.utils.ext.emitOn
import javax.inject.Inject

@HiltViewModel
class ConversationViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val conversationsRepository: ConversationsRepository,
    private val contactsRepository: ContactsRepository,
) : ViewModel() {

    private val conversationId: String = savedStateHandle[Route.Conversation.KEY_CONVERSATION_ID]!!
    val conversation: StateFlow<ExtendedConversation?>
        get() = conversationsRepository.getConversationFlow(viewModelScope, conversationId)

    private val _deleteConversationDialogState = MutableStateFlow(DeleteConversationDialogState())
    val deleteConversationDialogState: StateFlow<DeleteConversationDialogState>
        get() = _deleteConversationDialogState

    private val _showNoContactsSnackbarSignal: MutableSharedFlow<Unit> = MutableSharedFlow()
    val showNoContactsSnackbarSignal: MutableSharedFlow<Unit>
        get() = _showNoContactsSnackbarSignal

    init {
        conversationsRepository.markConversationAsRead(conversationId)
    }

    fun canReply(): Boolean {
        val canReply = contactsRepository.contactsState.value.isPairedContactExist
        if (!canReply) {
            _showNoContactsSnackbarSignal.emitOn(Unit, viewModelScope)
        }
        return canReply
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
