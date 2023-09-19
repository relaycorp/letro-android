package tech.relaycorp.letro.messages.processor

import tech.relaycorp.awaladroid.messaging.IncomingMessage
import tech.relaycorp.letro.awala.AwalaManager
import tech.relaycorp.letro.awala.processor.AwalaMessageProcessor
import tech.relaycorp.letro.messages.dto.NewMessageIncomingMessage
import tech.relaycorp.letro.messages.parser.NewMessageMessageParser
import tech.relaycorp.letro.messages.storage.MessagesDao
import javax.inject.Inject

interface NewMessageProcessor : AwalaMessageProcessor

class NewMessageProcessorImpl @Inject constructor(
    private val parser: NewMessageMessageParser,
    private val messagesDao: MessagesDao,
) : NewMessageProcessor {
    override suspend fun process(message: IncomingMessage, awalaManager: AwalaManager) {
        val parsedMessage = (parser.parse(message.content) as NewMessageIncomingMessage).content
        messagesDao.insert(parsedMessage)
    }
}
