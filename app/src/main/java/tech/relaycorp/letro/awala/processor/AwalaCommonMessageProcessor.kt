package tech.relaycorp.letro.awala.processor

import tech.relaycorp.awaladroid.messaging.IncomingMessage
import tech.relaycorp.letro.awala.AwalaManager
import tech.relaycorp.letro.awala.message.AwalaIncomingMessageContent
import tech.relaycorp.letro.awala.message.MessageType
import tech.relaycorp.letro.utils.Logger

interface AwalaCommonMessageProcessor {
    suspend fun process(message: IncomingMessage, awalaManager: AwalaManager)
}

class AwalaCommonMessageProcessorImpl constructor(
    private val processors: Map<MessageType, AwalaMessageProcessor<out AwalaIncomingMessageContent>>,
    private val logger: Logger,
) : AwalaCommonMessageProcessor {

    override suspend fun process(message: IncomingMessage, awalaManager: AwalaManager) {
        val processor = processors[MessageType.from(message.type)]
        if (processor != null) {
            processor.process(message, awalaManager)
        } else {
            logger.w(TAG, "There is no processor to process message ${MessageType.from(message.type)}")
        }
    }
}

private const val TAG = "AwalaCommonMessageProcessor"
