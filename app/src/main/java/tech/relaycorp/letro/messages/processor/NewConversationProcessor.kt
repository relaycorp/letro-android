package tech.relaycorp.letro.messages.processor

import tech.relaycorp.awaladroid.messaging.IncomingMessage
import tech.relaycorp.letro.awala.AwalaManager
import tech.relaycorp.letro.awala.processor.AwalaMessageProcessor
import tech.relaycorp.letro.messages.dto.NewConversationIncomingMessage
import tech.relaycorp.letro.messages.parser.NewConversationMessageParser
import tech.relaycorp.letro.messages.storage.ConversationsDao
import tech.relaycorp.letro.messages.storage.MessagesDao
import javax.inject.Inject

interface NewConversationProcessor : AwalaMessageProcessor

class NewConversationProcessorImpl @Inject constructor(
    private val newConversationMessageParser: NewConversationMessageParser,
    private val conversationsDao: ConversationsDao,
    private val messagesDao: MessagesDao,
) : NewConversationProcessor {

    override suspend fun process(message: IncomingMessage, awalaManager: AwalaManager) {
        val content = (newConversationMessageParser.parse(message.content) as NewConversationIncomingMessage).content
        conversationsDao.createNewConversation(content.conversation)
        messagesDao.insert(content.message)
    }
}
