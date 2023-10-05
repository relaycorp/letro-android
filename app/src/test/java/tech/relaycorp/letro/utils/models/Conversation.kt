package tech.relaycorp.letro.utils.models

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import tech.relaycorp.letro.account.storage.repository.AccountRepository
import tech.relaycorp.letro.awala.AwalaManager
import tech.relaycorp.letro.contacts.storage.repository.ContactsRepository
import tech.relaycorp.letro.conversation.attachments.AttachmentsRepository
import tech.relaycorp.letro.conversation.storage.converter.ExtendedConversationConverterImpl
import tech.relaycorp.letro.conversation.storage.converter.MessageTimestampFormatterImpl
import tech.relaycorp.letro.conversation.storage.dao.ConversationsDao
import tech.relaycorp.letro.conversation.storage.dao.MessagesDao
import tech.relaycorp.letro.conversation.storage.entity.Attachment
import tech.relaycorp.letro.conversation.storage.entity.Conversation
import tech.relaycorp.letro.conversation.storage.entity.Message
import tech.relaycorp.letro.conversation.storage.repository.ConversationsRepositoryImpl
import java.time.LocalDateTime
import java.util.UUID

fun createConversation(
    id: UUID,
    ownerVeraId: String = "account@test.id",
    contactVeraId: String = "contact@test.id",
    isRead: Boolean = true,
    subject: String? = null,
    isArchived: Boolean = false,
) = Conversation(
    conversationId = id,
    ownerVeraId = ownerVeraId,
    contactVeraId = contactVeraId,
    isRead = isRead,
    subject = subject,
    isArchived = isArchived,
)

fun createMessage(
    conversationId: UUID,
    text: String = "Message text",
    ownerVeraId: String = "account@test.id",
    senderVeraId: String = "account@test.id",
    recipientVeraId: String = "contact@test.id",
    sentAt: LocalDateTime = LocalDateTime.now(),
) = Message(
    conversationId = conversationId,
    text = text,
    ownerVeraId = ownerVeraId,
    senderVeraId = senderVeraId,
    recipientVeraId = recipientVeraId,
    sentAt = sentAt,
)

@ExperimentalCoroutinesApi
fun createConversationsRepository(
    conversations: List<Conversation> = emptyList(),
    messages: List<Message> = emptyList(),
    attachments: List<Attachment> = emptyList(),
    awalaManager: AwalaManager = createAwalaManager(),
    contactsRepository: ContactsRepository = createContactsRepository(),
    accountRepository: AccountRepository = createAccountRepository(),
    ioDispatcher: TestDispatcher = UnconfinedTestDispatcher(),
) = ConversationsRepositoryImpl(
    conversationsDao = mockk<ConversationsDao>().also {
        every { it.getAll() } returns flowOf(conversations)
    },
    messagesDao = mockk<MessagesDao>().also {
        every { it.getAll() } returns flowOf(messages)
    },
    attachmentsRepository = mockk<AttachmentsRepository>().also {
        every { it.attachments } returns flowOf(attachments)
    },
    contactsRepository = contactsRepository,
    accountRepository = accountRepository,
    conversationsConverter = ExtendedConversationConverterImpl(
        messageTimestampFormatter = MessageTimestampFormatterImpl(),
        mockk(relaxed = true),
        mockk(relaxed = true),
    ),
    awalaManager = awalaManager,
    outgoingMessageMessageEncoder = mockk(relaxed = true),
    logger = createLogger(),
    ioDispatcher = ioDispatcher,
)
