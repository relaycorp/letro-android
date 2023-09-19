package tech.relaycorp.letro.messages.viewing

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import tech.relaycorp.letro.messages.model.ExtendedConversation
import tech.relaycorp.letro.messages.repository.ConversationsRepository
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

    init {
        conversationsRepository.markConversationAsRead(conversationId)
    }
}
