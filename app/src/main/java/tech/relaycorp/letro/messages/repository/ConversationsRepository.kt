package tech.relaycorp.letro.messages.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import tech.relaycorp.letro.account.model.Account
import tech.relaycorp.letro.account.storage.AccountRepository
import tech.relaycorp.letro.contacts.model.Contact
import tech.relaycorp.letro.contacts.storage.ContactsRepository
import tech.relaycorp.letro.messages.converter.ExtendedConversationConverter
import tech.relaycorp.letro.messages.model.ExtendedConversation
import tech.relaycorp.letro.messages.storage.ConversationsDao
import tech.relaycorp.letro.messages.storage.MessagesDao
import tech.relaycorp.letro.messages.storage.entity.Conversation
import tech.relaycorp.letro.messages.storage.entity.Message
import java.time.LocalDateTime
import javax.inject.Inject

interface ConversationsRepository {
    val conversations: Flow<List<ExtendedConversation>>
    fun createNewConversation(
        ownerVeraId: String,
        recipientVeraId: String,
        messageText: String,
        subject: String? = null,
    )
}

class ConversationsRepositoryImpl @Inject constructor(
    private val conversationsDao: ConversationsDao,
    private val messagesDao: MessagesDao,
    private val contactsRepository: ContactsRepository,
    private val accountRepository: AccountRepository,
    private val conversationsConverter: ExtendedConversationConverter,
) : ConversationsRepository {

    private val scope = CoroutineScope(Dispatchers.IO)

    private val _conversations = MutableStateFlow<List<ExtendedConversation>>(emptyList())
    override val conversations: Flow<List<ExtendedConversation>>
        get() = _conversations

    private val contacts: MutableStateFlow<List<Contact>> = MutableStateFlow(emptyList())

    private var conversationsCollectionJob: Job? = null
    private var contactsCollectionJob: Job? = null

    init {
        scope.launch {
            accountRepository.currentAccount.collect {
                if (it != null) {
                    startCollectContacts(it)
                    startCollectConversations(it)
                } else {
                    conversationsCollectionJob?.cancel()
                    conversationsCollectionJob = null
                    contactsCollectionJob?.cancel()
                    contactsCollectionJob = null
                    _conversations.emit(emptyList())
                    contacts.emit(emptyList())
                }
            }
        }
    }

    override fun createNewConversation(
        ownerVeraId: String,
        recipientVeraId: String,
        messageText: String,
        subject: String?,
    ) {
        scope.launch {
            val conversation = Conversation(
                ownerVeraId = ownerVeraId,
                contactVeraId = recipientVeraId,
                subject = if (subject.isNullOrEmpty()) null else subject,
            )
            val message = Message(
                text = messageText,
                conversationId = conversation.conversationId,
                ownerVeraId = ownerVeraId,
                senderVeraId = ownerVeraId,
                recipientVeraId = recipientVeraId,
                sentAt = LocalDateTime.now(),
            )
            messagesDao.insert(message)
            conversationsDao.createNewConversation(conversation)
        }
    }

    private fun startCollectContacts(account: Account) {
        contactsCollectionJob = scope.launch {
            contactsRepository.getContacts(account.veraId).collect {
                contacts.emit(it)
            }
        }
    }

    private fun startCollectConversations(
        account: Account,
    ) {
        conversationsCollectionJob = scope.launch {
            combine(
                conversationsDao.getAll(),
                messagesDao.getAll(),
                contacts,
            ) { conversations, messages, contacts ->
                _conversations.emit(
                    conversationsConverter.convert(
                        conversations = conversations.filter { it.ownerVeraId == account.veraId },
                        messages = messages.filter { it.ownerVeraId == account.veraId },
                        contacts = contacts.filter { it.ownerVeraId == account.veraId },
                    ),
                )
            }.collect()
        }
    }
}
