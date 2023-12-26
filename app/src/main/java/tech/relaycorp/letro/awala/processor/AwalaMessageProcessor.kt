package tech.relaycorp.letro.awala.processor

import tech.relaycorp.awaladroid.messaging.IncomingMessage
import tech.relaycorp.letro.awala.AwalaManager
import tech.relaycorp.letro.awala.message.AwalaIncomingMessageContent
import tech.relaycorp.letro.awala.parser.AwalaMessageParser
import tech.relaycorp.letro.utils.Logger

abstract class AwalaMessageProcessor<T : AwalaIncomingMessageContent>(
    protected val parser: AwalaMessageParser<T>,
    protected val logger: Logger,
) {

    suspend fun process(message: IncomingMessage, awalaManager: AwalaManager) {
        val content = parser.parse(message.content) ?: kotlin.run {
            logger.w(TAG, "Couldn't parse message ${message.type}")
            return
        }
        if (isFromExpectedSender(content, message.recipientEndpoint.nodeId, message.senderEndpoint.nodeId, awalaManager)) {
            handleMessage(content, message.recipientEndpoint.nodeId, message.senderEndpoint.nodeId, awalaManager)
        } else {
            logger.w(TAG, "There is a message processor to process the message ${message.type}, but it came from unexpected sender")
        }
    }

    protected abstract suspend fun handleMessage(
        content: T,
        recipientNodeId: String,
        senderNodeId: String,
        awalaManager: AwalaManager,
    )
    protected abstract suspend fun isFromExpectedSender(
        content: T,
        recipientNodeId: String,
        senderNodeId: String,
        awalaManager: AwalaManager,
    ): Boolean
}

private const val TAG = "AwalaMessageProcessor"
