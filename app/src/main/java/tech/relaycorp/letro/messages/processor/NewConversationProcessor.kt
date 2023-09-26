package tech.relaycorp.letro.messages.processor

import tech.relaycorp.awaladroid.messaging.IncomingMessage
import tech.relaycorp.letro.awala.AwalaManager
import tech.relaycorp.letro.awala.processor.AwalaMessageProcessor
import tech.relaycorp.letro.messages.dto.NewConversationIncomingMessage
import tech.relaycorp.letro.messages.model.ConversationAwalaWrapper
import tech.relaycorp.letro.messages.parser.NewConversationMessageParser
import tech.relaycorp.letro.messages.storage.ConversationsDao
import tech.relaycorp.letro.messages.storage.MessagesDao
import tech.relaycorp.letro.messages.storage.entity.Conversation
import tech.relaycorp.letro.messages.storage.entity.Message
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject

interface NewConversationProcessor : AwalaMessageProcessor

class NewConversationProcessorImpl @Inject constructor(
    private val newConversationMessageParser: NewConversationMessageParser,
    private val conversationsDao: ConversationsDao,
    private val messagesDao: MessagesDao,
) : NewConversationProcessor {

    override suspend fun process(message: IncomingMessage, awalaManager: AwalaManager) {
        val content = (newConversationMessageParser.parse(message.content) as NewConversationIncomingMessage).content
        createLocalConversation(content.conversation)
    }

    private suspend fun createLocalConversation(conversationWrapper: ConversationAwalaWrapper) {
        val conversation = Conversation(
            conversationId = UUID.fromString(conversationWrapper.conversationId),
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
        messagesDao.insert(message)
    }
}