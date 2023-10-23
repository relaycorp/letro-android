package tech.relaycorp.letro.awala.processor

import tech.relaycorp.awaladroid.messaging.IncomingMessage
import tech.relaycorp.letro.awala.AwalaManager

interface ServerMessageProcessor : AwalaMessageProcessor {

    override suspend fun isFromExpectedSender(
        message: IncomingMessage,
        awalaManager: AwalaManager,
    ): Boolean {
        return message.senderEndpoint.nodeId == awalaManager.getServerThirdPartyEndpoint()?.nodeId
    }
}
