package tech.relaycorp.letro.awala.processor

import tech.relaycorp.awaladroid.messaging.IncomingMessage
import tech.relaycorp.letro.awala.AwalaManager
import tech.relaycorp.letro.awala.message.MessageType

interface AwalaMessageProcessor {
    suspend fun process(message: IncomingMessage, awalaManager: AwalaManager)
}

class AwalaMessageProcessorImpl constructor(
    private val processors: Map<MessageType, AwalaMessageProcessor>,
) : AwalaMessageProcessor {

    override suspend fun process(message: IncomingMessage, awalaManager: AwalaManager) {
        val type = MessageType.from(message.type)
        processors[type]!!.process(message, awalaManager)
    }
}
