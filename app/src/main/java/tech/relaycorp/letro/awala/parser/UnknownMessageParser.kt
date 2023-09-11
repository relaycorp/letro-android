package tech.relaycorp.letro.awala.parser

import tech.relaycorp.letro.awala.message.AwalaIncomingMessage
import tech.relaycorp.letro.awala.message.MessageType
import javax.inject.Inject

interface UnknownMessageParser : AwalaMessageParser

class UnknownMessageParserImpl @Inject constructor() : UnknownMessageParser {

    override fun parse(type: MessageType, content: ByteArray): AwalaIncomingMessage<*> {
        return UnknownIncomingMessage(
            content = content.decodeToString(),
        )
    }
}

data class UnknownIncomingMessage(
    override val content: String,
) : AwalaIncomingMessage<String> {
    override val type: MessageType = MessageType.Unknown
}
