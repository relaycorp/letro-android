package tech.relaycorp.letro.awala.processor

import tech.relaycorp.awaladroid.messaging.IncomingMessage
import tech.relaycorp.letro.awala.AwalaManager
import tech.relaycorp.letro.awala.message.MessageType
import tech.relaycorp.letro.utils.Logger

interface AwalaMessageProcessor {
    suspend fun isFromExpectedSender(
        message: IncomingMessage,
        awalaManager: AwalaManager,
    ): Boolean
    suspend fun process(message: IncomingMessage, awalaManager: AwalaManager)
}

class AwalaMessageProcessorImpl constructor(
    private val processors: Map<MessageType, AwalaMessageProcessor>,
    private val logger: Logger,
) : AwalaMessageProcessor {

    override suspend fun process(message: IncomingMessage, awalaManager: AwalaManager) {
        val type = MessageType.from(message.type)
        val processor = processors[type]!!
        if (processor.isFromExpectedSender(message, awalaManager)) {
            processors[type]!!.process(message, awalaManager)
        } else {
            logger.w(TAG, IllegalStateException("There is a message processor to process the message $type, but it came from unexpected sender"))
        }
    }

    override suspend fun isFromExpectedSender(
        message: IncomingMessage,
        awalaManager: AwalaManager,
    ): Boolean {
        return true
    }
}

private const val TAG = "AwalaMessageProcessor"
