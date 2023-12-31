package tech.relaycorp.letro.conversation.storage.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import tech.relaycorp.awaladroid.AwaladroidException
import tech.relaycorp.letro.account.model.Account
import tech.relaycorp.letro.account.storage.repository.AccountRepository
import tech.relaycorp.letro.awala.AwalaManager
import tech.relaycorp.letro.awala.message.AwalaEndpoint
import tech.relaycorp.letro.awala.message.AwalaOutgoingMessage
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
import tech.relaycorp.letro.utils.time.DeviceTimeChangedProvider
import tech.relaycorp.letro.utils.time.OnDeviceTimeChangedListener
import tech.relaycorp.letro.utils.time.nowUTC
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
    private val timeChangedProvider: DeviceTimeChangedProvider,
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

    private val timeChangedListener = object : OnDeviceTimeChangedListener {
        override fun onChanged() {
            scope.launch(Dispatchers.IO) {
                _extendedConversations.emit(
                    conversationsConverter.updateTimestamps(_extendedConversations.value),
                )
            }
        }
    }

    init {
        timeChangedProvider.addListener(timeChangedListener)
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

    @Throws(AwaladroidException::class)
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
        val ownerAccount = accountRepository.getByVeraidId(ownerVeraId) ?: run {
            logger.w(TAG, "Account not found for VeraId $ownerVeraId")
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
            sentAtUtc = nowUTC(),
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
                AwalaEndpoint.Private(
                    nodeId = recipientNodeId,
                )
            } else {
                AwalaEndpoint.Public(
                    nodeId = recipientNodeId,
                )
            },
            senderAccount = ownerAccount,
        )
        conversationsDao.createNewConversation(conversation)
        val messageId = messagesDao.insert(message)

        if (attachments.isNotEmpty()) {
            attachmentsRepository.saveAttachments(
                conversationId = conversation.conversationId,
                messageId = messageId,
                attachments = attachments,
            )
        }
    }

    @Throws(AwaladroidException::class)
    override suspend fun reply(
        conversationId: UUID,
        messageText: String,
        attachments: List<File.FileWithContent>,
    ) {
        val conversation = _conversations.value
            .find { it.conversationId == conversationId } ?: return
        val recipient = contacts.value
            .find { it.contactVeraId == conversation.contactVeraId && it.ownerVeraId == conversation.ownerVeraId }

        val ownerAccount = accountRepository.getByVeraidId(conversation.ownerVeraId) ?: run {
            logger.w(TAG, "Account not found for VeraId ${conversation.ownerVeraId}")
            return
        }

        val recipientNodeId = recipient?.contactEndpointId ?: return
        val message = Message(
            conversationId = conversationId,
            text = messageText,
            ownerVeraId = conversation.ownerVeraId,
            senderVeraId = conversation.ownerVeraId,
            recipientVeraId = conversation.contactVeraId,
            sentAtUtc = nowUTC(),
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
                AwalaEndpoint.Private(
                    nodeId = recipientNodeId,
                )
            } else {
                AwalaEndpoint.Public(
                    nodeId = recipientNodeId,
                )
            },
            senderAccount = ownerAccount,
        )

        val messageId = messagesDao.insert(message)
        if (attachments.isNotEmpty()) {
            attachmentsRepository.saveAttachments(
                conversationId = conversation.conversationId,
                messageId = messageId,
                attachments = attachments,
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
            attachmentsRepository.deleteAttachments(conversation.conversationId)
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
                        owner = account,
                    ).also { logger.d(TAG, "Emitting Extended conversations after formatting with size: ${it.size}") },
                )
            }.collect()
        }
    }

    companion object {
        const val TAG = "ConversationsRepository"
    }
}
