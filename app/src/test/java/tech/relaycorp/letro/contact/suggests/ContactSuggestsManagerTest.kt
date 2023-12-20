package tech.relaycorp.letro.contact.suggests

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.Test
import tech.relaycorp.letro.contacts.suggest.ContactSuggestsManagerImpl
import tech.relaycorp.letro.utils.models.account.createAccount
import tech.relaycorp.letro.utils.models.contact.createContact
import tech.relaycorp.letro.utils.models.conversation.createConversation
import tech.relaycorp.letro.utils.models.conversation.createExtendedConversationConverter
import tech.relaycorp.letro.utils.models.conversation.createMessage
import tech.relaycorp.letro.utils.time.nowUTC
import java.util.UUID

class ContactSuggestsManagerTest {

    private val extendedConversationConverter = createExtendedConversationConverter()
    private val contactsSuggestsManager = ContactSuggestsManagerImpl()

    @OptIn(DelicateCoroutinesApi::class)
    @Test
    fun `Test that everything is sorted correctly`() = runBlocking {
        val contactFromLatestConversation = createContact(contactVeraId = "first@contact.id")
        val contactSecond = createContact(contactVeraId = "second@contact.id")
        val contactThird = createContact(contactVeraId = "third@contact.id")
        val contactWithWhomNoConversations = createContact(contactVeraId = "no_conversations@contact.id")
        val contactWithWhomNoConversations2 = createContact(contactVeraId = "no_conversations2@contact.id")
        val unsortedContacts = listOf(
            contactWithWhomNoConversations,
            contactThird,
            contactWithWhomNoConversations2,
            contactFromLatestConversation,
            contactSecond,
        )

        val latestConversation = createConversation(
            id = UUID.randomUUID(),
            contactVeraId = contactFromLatestConversation.contactVeraId,
        )
        val conversationSecond = createConversation(
            id = UUID.randomUUID(),
            contactVeraId = contactSecond.contactVeraId,
        )
        val conversationThird = createConversation(
            id = UUID.randomUUID(),
            contactVeraId = contactThird.contactVeraId,
        )
        val unsortedConversations = listOf(
            conversationThird,
            latestConversation,
            conversationSecond,
        )

        val messageFromLatest = createMessage(
            conversationId = latestConversation.conversationId,
            recipientVeraId = latestConversation.contactVeraId,
            sentAtUtc = nowUTC(),
        )
        val messageFromSecond = createMessage(
            conversationId = conversationSecond.conversationId,
            recipientVeraId = conversationSecond.contactVeraId,
            sentAtUtc = nowUTC().minusDays(1L),
        )
        val messageFromThird = createMessage(
            conversationId = conversationThird.conversationId,
            recipientVeraId = conversationThird.contactVeraId,
            sentAtUtc = nowUTC().minusDays(3L),
        )
        val messages = listOf(messageFromLatest, messageFromSecond, messageFromThird)

        val owner = createAccount(accountId = contactFromLatestConversation.ownerVeraId)
        val sortedContacts = contactsSuggestsManager.orderByRelevance(
            contacts = unsortedContacts,
            conversations = extendedConversationConverter.convert(
                conversations = unsortedConversations,
                messages = messages,
                contacts = unsortedContacts,
                attachments = emptyList(),
                owner = owner,
            ),
        )

        val expectedContactsList = listOf(
            contactFromLatestConversation,
            contactSecond,
            contactThird,
            contactWithWhomNoConversations,
            contactWithWhomNoConversations2,
        )
        // TODO: replace by kotest analog
        assertIterableEquals(expectedContactsList, sortedContacts)
    }
}
