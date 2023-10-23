package tech.relaycorp.letro.conversation.server.processor

import tech.relaycorp.awaladroid.messaging.IncomingMessage
import tech.relaycorp.letro.awala.AwalaManager
import tech.relaycorp.letro.awala.processor.AwalaMessageProcessor
import tech.relaycorp.letro.contacts.storage.dao.ContactsDao
import tech.relaycorp.letro.conversation.attachments.AttachmentsRepository
import tech.relaycorp.letro.conversation.server.dto.NewMessageIncomingMessage
import tech.relaycorp.letro.conversation.server.parser.NewMessageMessageParser
import tech.relaycorp.letro.conversation.storage.dao.ConversationsDao
import tech.relaycorp.letro.conversation.storage.dao.MessagesDao
import tech.relaycorp.letro.conversation.storage.entity.Conversation
import tech.relaycorp.letro.conversation.storage.entity.Message
import tech.relaycorp.letro.push.PushManager
import tech.relaycorp.letro.push.PushNewMessageTextFormatter
import tech.relaycorp.letro.push.model.PushData
import tech.relaycorp.letro.ui.navigation.Action
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject

interface NewMessageProcessor : AwalaMessageProcessor

class NewMessageProcessorImpl @Inject constructor(
    private val pushManager: PushManager,
    private val parser: NewMessageMessageParser,
    private val conversationsDao: ConversationsDao,
    private val messagesDao: MessagesDao,
    private val attachmentsRepository: AttachmentsRepository,
    private val contactsDao: ContactsDao,
    private val messageTextFormatter: PushNewMessageTextFormatter,
) : NewMessageProcessor {

    @Suppress("NAME_SHADOWING")
    override suspend fun process(message: IncomingMessage, awalaManager: AwalaManager) {
        val messageWrapper = (parser.parse(message.content) as NewMessageIncomingMessage).content
        val conversationId = UUID.fromString(messageWrapper.conversationId)
        val conversation = conversationsDao.getConversationById(conversationId)?.apply {
            conversationsDao.update(
                this.copy(
                    isRead = false,
                    isArchived = false,
                ),
            )
        } ?: let {
            val conversation = Conversation(
                conversationId = conversationId,
                ownerVeraId = messageWrapper.recipientVeraId,
                contactVeraId = messageWrapper.senderVeraId,
                isRead = false,
            )
            conversationsDao.createNewConversation(conversation)
            conversation
        }
        val message = Message(
            conversationId = conversationId,
            text = messageWrapper.messageText,
            ownerVeraId = conversation.ownerVeraId,
            recipientVeraId = messageWrapper.recipientVeraId,
            senderVeraId = messageWrapper.senderVeraId,
            sentAt = LocalDateTime.now(),
        )
        val messageId = messagesDao.insert(message)
        attachmentsRepository.saveMessageAttachments(messageId, messageWrapper.attachments)

        val senderAlias = contactsDao.getContact(
            ownerVeraId = conversation.ownerVeraId,
            contactVeraId = conversation.contactVeraId,
        )?.alias
        pushManager.showPush(
            PushData(
                title = senderAlias ?: message.senderVeraId,
                text = messageTextFormatter.getText(conversation.subject, message.text, messageWrapper.attachments.map { it.fileName }),
                action = Action.OpenConversation(
                    conversationId = messageWrapper.conversationId,
                    accountId = conversation.ownerVeraId,
                ),
                recipientAccountId = conversation.ownerVeraId,
                notificationId = messageId.toInt(),
            ),
        )
    }

    override suspend fun isFromExpectedSender(
        message: IncomingMessage,
        awalaManager: AwalaManager,
    ): Boolean {
        val messageWrapper = (parser.parse(message.content) as NewMessageIncomingMessage).content
        val contact = contactsDao.getContact(
            ownerVeraId = messageWrapper.recipientVeraId,
            contactVeraId = messageWrapper.senderVeraId,
        )
        return message.senderEndpoint.nodeId == contact?.contactEndpointId
    }
}
