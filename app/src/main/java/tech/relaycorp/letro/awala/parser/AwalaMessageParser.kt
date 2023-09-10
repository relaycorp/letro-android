package tech.relaycorp.letro.awala.parser

import tech.relaycorp.letro.awala.message.AwalaIncomingMessage
import tech.relaycorp.letro.awala.message.MessageType

interface AwalaMessageParser {
    fun parse(type: MessageType, content: ByteArray): AwalaIncomingMessage<*>
}

class AwalaMessageParserImpl constructor(
    private val parsers: Map<MessageType, AwalaMessageParser>
): AwalaMessageParser {
    override fun parse(type: MessageType, content: ByteArray): AwalaIncomingMessage<*> {
        return parsers[type]?.parse(type, content) ?: throw IllegalStateException("No parser for messageType: $type")
    }
}