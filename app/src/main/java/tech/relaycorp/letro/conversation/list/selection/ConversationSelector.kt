package tech.relaycorp.letro.conversation.list.selection

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID
import javax.inject.Inject

interface ConversationSelector {
    val selectedConversations: StateFlow<Set<UUID>>
    fun selectConversation(conversationId: UUID)
    fun unselectConversation(conversationId: UUID)
    fun unselectAll()
}

class ConversationSelectorImpl @Inject constructor() : ConversationSelector {

    private val _selectedConversations = MutableStateFlow<Set<UUID>>(emptySet())
    override val selectedConversations: StateFlow<Set<UUID>>
        get() = _selectedConversations

    override fun selectConversation(conversationId: UUID) {
        _selectedConversations.value = HashSet(_selectedConversations.value).apply {
            add(conversationId)
        }
    }

    override fun unselectConversation(conversationId: UUID) {
        _selectedConversations.value = HashSet(_selectedConversations.value).apply {
            remove(conversationId)
        }
    }

    override fun unselectAll() {
        _selectedConversations.value = emptySet()
    }
}
