package tech.relaycorp.letro.conversation.server.processor

import tech.relaycorp.awaladroid.messaging.IncomingMessage
import tech.relaycorp.letro.awala.AwalaManager
import tech.relaycorp.letro.awala.processor.AwalaMessageProcessor
import tech.relaycorp.letro.contacts.storage.dao.ContactsDao
import tech.relaycorp.letro.conversation.attachments.AttachmentsRepository
import tech.relaycorp.letro.conversation.server.dto.ConversationAwalaWrapper
import tech.relaycorp.letro.conversation.server.dto.NewConversationIncomingMessage
import tech.relaycorp.letro.conversation.server.parser.NewConversationMessageParser
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

interface NewConversationProcessor : AwalaMessageProcessor

class NewConversationProcessorImpl @Inject constructor(
    private val pushManager: PushManager,
    private val newConversationMessageParser: NewConversationMessageParser,
    private val conversationsDao: ConversationsDao,
    private val messagesDao: MessagesDao,
    private val contactsDao: ContactsDao,
    private val attachmentsRepository: AttachmentsRepository,
    private val messageTextFormatter: PushNewMessageTextFormatter,
) : NewConversationProcessor {

    override suspend fun process(message: IncomingMessage, awalaManager: AwalaManager) {
        val content = (newConversationMessageParser.parse(message.content) as NewConversationIncomingMessage).content
        createLocalConversation(content.conversation)
    }

    private suspend fun createLocalConversation(conversationWrapper: ConversationAwalaWrapper) {
        val conversationId = UUID.fromString(conversationWrapper.conversationId)
        val conversation = Conversation(
            conversationId = conversationId,
            ownerVeraId = conversationWrapper.recipientVeraId,
            contactVeraId = conversationWrapper.senderVeraId,
            subject = conversationWrapper.subject,
            isRead = false,
        )
        val message = Message(
            conversationId = conversation.conversationId,
            text = conversationWrapper.messageText,
            ownerVeraId = conversation.ownerVeraId,
            recipientVeraId = conversation.ownerVeraId,
            senderVeraId = conversation.contactVeraId,
            sentAt = LocalDateTime.now(),
        )
        conversationsDao.createNewConversation(conversation)
        val messageId = messagesDao.insert(message)

        attachmentsRepository.saveMessageAttachments(messageId, conversationWrapper.attachments)

        val senderAlias = contactsDao.getContact(
            ownerVeraId = conversation.ownerVeraId,
            contactVeraId = conversation.contactVeraId,
        )?.alias

        pushManager.showPush(
            PushData(
                title = senderAlias ?: conversationWrapper.senderVeraId,
                text = messageTextFormatter.getText(conversation.subject, message.text, conversationWrapper.attachments.map { it.fileName }),
                action = Action.OpenConversation(
                    conversationId = conversationWrapper.conversationId,
                    accountId = conversation.ownerVeraId,
                ),
                recipientAccountId = conversation.ownerVeraId,
                notificationId = messageId.toInt(),
            ),
        )
    }
}
