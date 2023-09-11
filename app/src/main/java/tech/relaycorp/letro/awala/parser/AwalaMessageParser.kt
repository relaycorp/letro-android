package tech.relaycorp.letro.awala.parser

import tech.relaycorp.letro.awala.message.AwalaIncomingMessage

interface AwalaMessageParser {
    fun parse(content: ByteArray): AwalaIncomingMessage<*>
}
