package tech.relaycorp.letro.contacts.suggest

import tech.relaycorp.letro.contacts.model.Contact
import tech.relaycorp.letro.conversation.model.ExtendedConversation
import tech.relaycorp.letro.utils.collections.toMap
import javax.inject.Inject

interface ContactSuggestsManager {
    fun orderByRelevance(
        contacts: List<Contact>,
        conversations: List<ExtendedConversation>,
    ): List<Contact>
}

class ContactSuggestsManagerImpl @Inject constructor() : ContactSuggestsManager {

    override fun orderByRelevance(
        contacts: List<Contact>,
        conversations: List<ExtendedConversation>,
    ): List<Contact> {
        val contactsMap = contacts.toMap(key = { it.contactVeraId })
        val result = arrayListOf<Contact>()
        val conversationsSortedByLastMessageDate = conversations
            .sortedByDescending { it.lastMessageSentAtUtc }

        // First - add to the result contacts from latest conversations
        conversationsSortedByLastMessageDate.forEach { conversation ->
            if (result.any { it.contactVeraId == conversation.contactVeraId }) {
                return@forEach
            }
            contactsMap[conversation.contactVeraId]?.let {
                result.add(it)
            }
        }

        // Then - add everyone else
        val resultSet = result.toSet()
        contacts.forEach {
            if (!resultSet.contains(it)) {
                result.add(it)
            }
        }
        return result
    }
}
