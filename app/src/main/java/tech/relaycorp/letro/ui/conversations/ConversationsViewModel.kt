package tech.relaycorp.letro.ui.conversations

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import tech.relaycorp.letro.data.entity.ConversationDataModel
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
}
