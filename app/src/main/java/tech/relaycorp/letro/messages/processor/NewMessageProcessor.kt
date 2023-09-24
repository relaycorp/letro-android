package tech.relaycorp.letro.messages.processor

import tech.relaycorp.awaladroid.messaging.IncomingMessage
import tech.relaycorp.letro.awala.AwalaManager
import tech.relaycorp.letro.awala.processor.AwalaMessageProcessor
import tech.relaycorp.letro.messages.dto.NewMessageIncomingMessage
import tech.relaycorp.letro.messages.parser.NewMessageMessageParser
import tech.relaycorp.letro.messages.storage.ConversationsDao
import tech.relaycorp.letro.messages.storage.MessagesDao
import tech.relaycorp.letro.messages.storage.entity.Conversation
import tech.relaycorp.letro.messages.storage.entity.Message
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject

interface NewMessageProcessor : AwalaMessageProcessor

class NewMessageProcessorImpl @Inject constructor(
    private val parser: NewMessageMessageParser,
    private val conversationsDao: ConversationsDao,
    private val messagesDao: MessagesDao,
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
        messagesDao.insert(message)
    }
}
