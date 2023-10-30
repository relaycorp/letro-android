package tech.relaycorp.letro.utils.models.conversation

import android.net.Uri
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
import tech.relaycorp.letro.conversation.attachments.filepicker.FileConverter
import tech.relaycorp.letro.conversation.attachments.filepicker.model.File
import tech.relaycorp.letro.conversation.attachments.ui.AttachmentInfo
import tech.relaycorp.letro.conversation.attachments.utils.AttachmentInfoConverter
import tech.relaycorp.letro.conversation.server.dto.AttachmentAwalaWrapper
import tech.relaycorp.letro.conversation.storage.converter.ExtendedConversationConverterImpl
import tech.relaycorp.letro.conversation.storage.converter.MessageTimestampFormatter
import tech.relaycorp.letro.conversation.storage.converter.MessageTimestampFormatterImpl
import tech.relaycorp.letro.conversation.storage.dao.ConversationsDao
import tech.relaycorp.letro.conversation.storage.dao.MessagesDao
import tech.relaycorp.letro.conversation.storage.entity.Attachment
import tech.relaycorp.letro.conversation.storage.entity.Conversation
import tech.relaycorp.letro.conversation.storage.entity.Message
import tech.relaycorp.letro.conversation.storage.repository.ConversationsRepositoryImpl
import tech.relaycorp.letro.utils.Logger
import tech.relaycorp.letro.utils.models.account.createAccountRepository
import tech.relaycorp.letro.utils.models.awala.createAwalaManager
import tech.relaycorp.letro.utils.models.contact.createContactsRepository
import tech.relaycorp.letro.utils.models.utils.createLogger
import tech.relaycorp.letro.utils.time.nowUTC
import java.time.ZonedDateTime
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
    sentAtUtc: ZonedDateTime = nowUTC(),
) = Message(
    conversationId = conversationId,
    text = text,
    ownerVeraId = ownerVeraId,
    senderVeraId = senderVeraId,
    recipientVeraId = recipientVeraId,
    sentAtUtc = sentAtUtc,
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
    timeChangedProvider = mockk(relaxed = true),
    ioDispatcher = ioDispatcher,
)

fun createExtendedConversationConverter(
    messageTimestampFormatter: MessageTimestampFormatter = MessageTimestampFormatterImpl(),
    fileConverter: FileConverter = createDummyFileConverter(),
    attachmentInfoConverter: AttachmentInfoConverter = createDummyAttachmentInfoConverter(),
) = ExtendedConversationConverterImpl(messageTimestampFormatter, fileConverter, attachmentInfoConverter)

fun createDummyFileConverter(
    logger: Logger = createLogger(),
): FileConverter = object : FileConverter {
    override suspend fun getFile(attachment: Attachment): File.FileWithoutContent? {
        logger.w("FileConverter", "Used dummy file converter")
        return null
    }

    override suspend fun getFile(attachmentAwalaWrapper: AttachmentAwalaWrapper): File.FileWithContent? {
        logger.w("FileConverter", "Used dummy file converter")
        return null
    }

    override suspend fun getFile(uri: Uri): File.FileWithContent? {
        logger.w("FileConverter", "Used dummy file converter")
        return null
    }
}

fun createDummyAttachmentInfoConverter(
    logger: Logger = createLogger(),
): AttachmentInfoConverter = object : AttachmentInfoConverter {
    override fun convert(file: File): AttachmentInfo {
        logger.w("AttachmentInfoConverter", "Used dummy attachment info converter")
        return AttachmentInfo(
            fileId = UUID.randomUUID(),
            name = "mocked_file_name.file",
            size = "mock",
            icon = -1,
        )
    }
}
