package tech.relaycorp.letro.messages.list

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import tech.relaycorp.letro.messages.model.ExtendedConversation
import tech.relaycorp.letro.messages.repository.ConversationsRepository
import javax.inject.Inject

@HiltViewModel
class ConversationsViewModel @Inject constructor(
    private val conversationsRepository: ConversationsRepository,
) : ViewModel() {

    val conversations: Flow<List<ExtendedConversation>>
        get() = conversationsRepository.conversations
}
