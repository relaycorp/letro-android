package tech.relaycorp.letro.conversation.storage.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import tech.relaycorp.letro.account.model.Account
import tech.relaycorp.letro.account.storage.repository.AccountRepository
import tech.relaycorp.letro.awala.AwalaManager
import tech.relaycorp.letro.awala.message.AwalaOutgoingMessage
import tech.relaycorp.letro.awala.message.MessageRecipient
import tech.relaycorp.letro.awala.message.MessageType
import tech.relaycorp.letro.contacts.model.Contact
import tech.relaycorp.letro.contacts.storage.repository.ContactsRepository
import tech.relaycorp.letro.conversation.attachments.AttachmentsRepository
import tech.relaycorp.letro.conversation.attachments.filepicker.model.File
import tech.relaycorp.letro.conversation.model.ExtendedConversation
import tech.relaycorp.letro.conversation.server.parser.OutgoingMessageMessageEncoder
import tech.relaycorp.letro.conversation.storage.converter.ExtendedConversationConverter
import tech.relaycorp.letro.conversation.storage.dao.ConversationsDao
import tech.relaycorp.letro.conversation.storage.dao.MessagesDao
import tech.relaycorp.letro.conversation.storage.entity.Conversation
import tech.relaycorp.letro.conversation.storage.entity.Message
import tech.relaycorp.letro.utils.Logger
import tech.relaycorp.letro.utils.di.IODispatcher
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject

interface ConversationsRepository {
    val conversations: StateFlow<List<ExtendedConversation>>
    suspend fun createNewConversation(
        ownerVeraId: String,
        recipient: Contact,
        messageText: String,
        subject: String? = null,
        attachments: List<File.FileWithContent> = emptyList(),
    )
    suspend fun reply(
        conversationId: UUID,
        messageText: String,
        attachments: List<File.FileWithContent> = emptyList(),
    )
    fun getConversation(id: String): ExtendedConversation?
    fun getConversationFlow(scope: CoroutineScope, id: String): StateFlow<ExtendedConversation?>
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
    private val attachmentsRepository: AttachmentsRepository,
    private val contactsRepository: ContactsRepository,
    private val accountRepository: AccountRepository,
    private val conversationsConverter: ExtendedConversationConverter,
    private val awalaManager: AwalaManager,
    private val outgoingMessageMessageEncoder: OutgoingMessageMessageEncoder,
    private val logger: Logger,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
) : ConversationsRepository {

    private val scope = CoroutineScope(ioDispatcher)

    private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    private val _extendedConversations = MutableStateFlow<List<ExtendedConversation>>(emptyList())
    override val conversations: StateFlow<List<ExtendedConversation>>
        get() = _extendedConversations

    private val contacts: MutableStateFlow<List<Contact>> = MutableStateFlow(emptyList())

    private var conversationsCollectionJob: Job? = null
    private var contactsCollectionJob: Job? = null

    init {
        scope.launch {
            accountRepository.allAccounts.collect {
                val currentAccount = it.firstOrNull { it.isCurrent }
                conversationsCollectionJob?.cancel()
                conversationsCollectionJob = null
                contactsCollectionJob?.cancel()
                contactsCollectionJob = null
                if (currentAccount != null) {
                    startCollectContacts(currentAccount)
                    startCollectConversations(currentAccount)
                } else {
                    logger.d(TAG, "Current account is null. Emitting empty conversations.")
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

    override fun getConversationFlow(scope: CoroutineScope, id: String): StateFlow<ExtendedConversation?> {
        val uuid = UUID.fromString(id)
        return conversations
            .map { it.find { it.conversationId == uuid } }
            .stateIn(scope, SharingStarted.Eagerly, getConversation(id))
    }

    override suspend fun createNewConversation(
        ownerVeraId: String,
        recipient: Contact,
        messageText: String,
        subject: String?,
        attachments: List<File.FileWithContent>,
    ) {
        val recipientNodeId = recipient.contactEndpointId ?: run {
            logger.w(TAG, "Recipient endpoint id is null")
            return
        }
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
        awalaManager.sendMessage(
            outgoingMessage = AwalaOutgoingMessage(
                type = MessageType.NewConversation,
                content = outgoingMessageMessageEncoder.encodeNewConversationContent(
                    conversation = conversation,
                    messageText = message.text,
                    attachments = attachments,
                ),
            ),
            recipient = if (recipient.isPrivateEndpoint) {
                MessageRecipient.PrivateEndpoint(
                    nodeId = recipientNodeId,
                )
            } else {
                MessageRecipient.PublicEndpoint(
                    nodeId = recipientNodeId,
                )
            },
        )
        conversationsDao.createNewConversation(conversation)
        val messageId = messagesDao.insert(message)

        if (attachments.isNotEmpty()) {
            attachmentsRepository.saveAttachments(messageId, attachments)
        }
    }

    override suspend fun reply(
        conversationId: UUID,
        messageText: String,
        attachments: List<File.FileWithContent>,
    ) {
        val conversation = _conversations.value
            .find { it.conversationId == conversationId } ?: return
        val recipient = contacts.value
            .find { it.contactVeraId == conversation.contactVeraId && it.ownerVeraId == conversation.ownerVeraId }

        val recipientNodeId = recipient?.contactEndpointId ?: return
        val message = Message(
            conversationId = conversationId,
            text = messageText,
            ownerVeraId = conversation.ownerVeraId,
            senderVeraId = conversation.ownerVeraId,
            recipientVeraId = conversation.contactVeraId,
            sentAt = LocalDateTime.now(),
        )

        awalaManager.sendMessage(
            outgoingMessage = AwalaOutgoingMessage(
                type = MessageType.NewMessage,
                content = outgoingMessageMessageEncoder.encodeNewMessageContent(
                    message = message,
                    attachments = attachments,
                ),
            ),
            recipient = if (recipient.isPrivateEndpoint) {
                MessageRecipient.PrivateEndpoint(
                    nodeId = recipientNodeId,
                )
            } else {
                MessageRecipient.PublicEndpoint(
                    nodeId = recipientNodeId,
                )
            },
        )

        val messageId = messagesDao.insert(message)
        if (attachments.isNotEmpty()) {
            attachmentsRepository.saveAttachments(messageId, attachments)
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
            contactsRepository.getContacts(account.accountId).collect {
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
                attachmentsRepository.attachments,
                contacts,
            ) { conversations, messages, attachments, contacts ->

                _conversations.emit(conversations)

                val messagesOfCurrentAccount = messages.filter { it.ownerVeraId == account.accountId }
                val messageIdsOfCurrentAccount = messagesOfCurrentAccount.map { it.id }.toSet()

                logger.d(TAG, "Emitting conversations before formatting with size: ${conversations.size}")
                _extendedConversations.emit(
                    conversationsConverter.convert(
                        conversations = conversations.filter { it.ownerVeraId == account.accountId },
                        messages = messagesOfCurrentAccount,
                        contacts = contacts.filter { it.ownerVeraId == account.accountId },
                        attachments = attachments.filter { messageIdsOfCurrentAccount.contains(it.messageId) },
                        ownerVeraId = account.accountId,
                    ).also { logger.d(TAG, "Emitting Extended conversations after formatting with size: ${it.size}") },
                )
            }.collect()
        }
    }

    companion object {
        const val TAG = "ConversationsRepository"
    }
}
