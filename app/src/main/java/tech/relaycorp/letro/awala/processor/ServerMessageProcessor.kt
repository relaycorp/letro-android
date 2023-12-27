package tech.relaycorp.letro.awala.processor

import tech.relaycorp.awaladroid.AwaladroidException
import tech.relaycorp.letro.awala.AwalaManager
import tech.relaycorp.letro.awala.message.AwalaIncomingMessageContent
import tech.relaycorp.letro.awala.parser.AwalaMessageParser
import tech.relaycorp.letro.utils.Logger

abstract class ServerMessageProcessor<T : AwalaIncomingMessageContent>(
    parser: AwalaMessageParser<T>,
    logger: Logger,
) : AwalaMessageProcessor<T>(parser, logger) {

    override suspend fun isFromExpectedSender(
        content: T,
        recipientNodeId: String,
        senderNodeId: String,
        awalaManager: AwalaManager,
    ): Boolean {
        val thirdPartyEndpointNodeId = try {
            awalaManager.getServerThirdPartyEndpoint(recipientNodeId).nodeId
        } catch (e: AwaladroidException) {
            null
        }
        return senderNodeId == thirdPartyEndpointNodeId
    }
}
