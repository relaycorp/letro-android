package tech.relaycorp.letro.messages.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import tech.relaycorp.letro.account.model.Account
import tech.relaycorp.letro.account.storage.AccountRepository
import tech.relaycorp.letro.awala.AwalaManager
import tech.relaycorp.letro.awala.message.AwalaOutgoingMessage
import tech.relaycorp.letro.awala.message.MessageRecipient
import tech.relaycorp.letro.awala.message.MessageType
import tech.relaycorp.letro.contacts.model.Contact
import tech.relaycorp.letro.contacts.storage.ContactsRepository
import tech.relaycorp.letro.messages.converter.ExtendedConversationConverter
import tech.relaycorp.letro.messages.model.ExtendedConversation
import tech.relaycorp.letro.messages.parser.OutgoingMessageMessageEncoder
import tech.relaycorp.letro.messages.storage.ConversationsDao
import tech.relaycorp.letro.messages.storage.MessagesDao
import tech.relaycorp.letro.messages.storage.entity.Conversation
import tech.relaycorp.letro.messages.storage.entity.Message
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject

interface ConversationsRepository {
    val conversations: StateFlow<List<ExtendedConversation>>
    fun createNewConversation(
        ownerVeraId: String,
        recipient: Contact,
        messageText: String,
        subject: String? = null,
    )
    fun reply(
        conversationId: UUID,
        messageText: String,
    )
    fun getConversation(id: String): ExtendedConversation?
    fun markConversationAsRead(conversationId: String)
    fun deleteConversation(conversationId: String)
    fun archiveConversation(
        conversationId: String,
        isArchived: Boolean,
    )
}

class ConversationsRepositoryImpl @Inject constructor(
    private val conversationsDao: ConversationsDao,
    private val messagesDao: MessagesDao,
    private val contactsRepository: ContactsRepository,
    private val accountRepository: AccountRepository,
    private val conversationsConverter: ExtendedConversationConverter,
    private val awalaManager: AwalaManager,
    private val outgoingMessageMessageEncoder: OutgoingMessageMessageEncoder,
) : ConversationsRepository {

    private val scope = CoroutineScope(Dispatchers.IO)

    private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    private val _extendedConversations = MutableStateFlow<List<ExtendedConversation>>(emptyList())
    override val conversations: StateFlow<List<ExtendedConversation>>
        get() = _extendedConversations

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
                    _extendedConversations.emit(emptyList())
                    contacts.emit(emptyList())
                }
            }
        }
    }

    override fun getConversation(id: String): ExtendedConversation? {
        val uuid = UUID.fromString(id)
        return _extendedConversations.value.find { it.conversationId == uuid }
    }

    override fun createNewConversation(
        ownerVeraId: String,
        recipient: Contact,
        messageText: String,
        subject: String?,
    ) {
        val recipientNodeId = recipient.contactEndpointId ?: return
        scope.launch {
            val conversation = Conversation(
                ownerVeraId = ownerVeraId,
                contactVeraId = recipient.contactVeraId,
                subject = if (subject.isNullOrEmpty()) null else subject,
                isRead = true,
            )
            val message = Message(
                text = messageText,
                conversationId = conversation.conversationId,
                ownerVeraId = ownerVeraId,
                senderVeraId = ownerVeraId,
                recipientVeraId = recipient.contactVeraId,
                sentAt = LocalDateTime.now(),
            )
            conversationsDao.createNewConversation(conversation)
            messagesDao.insert(message)

            awalaManager.sendMessage(
                outgoingMessage = AwalaOutgoingMessage(
                    type = MessageType.NewConversation,
                    content = outgoingMessageMessageEncoder.encodeNewConversationContent(
                        conversation = conversation,
                        messageText = message.text,
                    ),
                ),
                recipient = MessageRecipient.User(
                    nodeId = recipientNodeId,
                ),
            )
        }
    }

    override fun reply(conversationId: UUID, messageText: String) {
        scope.launch {
            val conversation = _conversations.value
                .find { it.conversationId == conversationId } ?: return@launch
            val recipientNodeId = contacts.value
                .find { it.contactVeraId == conversation.contactVeraId && it.ownerVeraId == conversation.ownerVeraId }
                ?.contactEndpointId ?: return@launch
            val message = Message(
                conversationId = conversationId,
                text = messageText,
                ownerVeraId = conversation.ownerVeraId,
                senderVeraId = conversation.ownerVeraId,
                recipientVeraId = conversation.contactVeraId,
                sentAt = LocalDateTime.now(),
            )
            messagesDao.insert(message)
            awalaManager.sendMessage(
                outgoingMessage = AwalaOutgoingMessage(
                    type = MessageType.NewMessage,
                    content = outgoingMessageMessageEncoder.encodeNewMessageContent(message),
                ),
                recipient = MessageRecipient.User(
                    nodeId = recipientNodeId,
                ),
            )
        }
    }

    @Suppress("NAME_SHADOWING")
    override fun markConversationAsRead(conversationId: String) {
        scope.launch {
            val conversationId = UUID.fromString(conversationId)
            val conversation = _conversations.value.find { it.conversationId == conversationId } ?: return@launch
            conversationsDao.update(
                conversation.copy(
                    isRead = true,
                ),
            )
        }
    }

    @Suppress("NAME_SHADOWING")
    override fun deleteConversation(conversationId: String) {
        scope.launch {
            val conversationId = UUID.fromString(conversationId)
            val conversation = _conversations.value.find { it.conversationId == conversationId } ?: return@launch
            conversationsDao.delete(conversation)
        }
    }

    @Suppress("NAME_SHADOWING")
    override fun archiveConversation(conversationId: String, isArchived: Boolean) {
        scope.launch {
            val conversationId = UUID.fromString(conversationId)
            val conversation = _conversations.value.find { it.conversationId == conversationId } ?: return@launch
            conversationsDao.update(
                conversation.copy(
                    isArchived = isArchived,
                ),
            )
        }
    }

    private fun startCollectContacts(account: Account) {
        contactsCollectionJob = scope.launch {
            contactsRepository.getContacts(account.veraidId).collect {
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
                _conversations.emit(conversations)
                _extendedConversations.emit(
                    conversationsConverter.convert(
                        conversations = conversations.filter { it.ownerVeraId == account.veraidId },
                        messages = messages.filter { it.ownerVeraId == account.veraidId },
                        contacts = contacts.filter { it.ownerVeraId == account.veraidId },
                        ownerVeraId = account.veraidId,
                    ),
                )
            }.collect()
        }
    }
}
