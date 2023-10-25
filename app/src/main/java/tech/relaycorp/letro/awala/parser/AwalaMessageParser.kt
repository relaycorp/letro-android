package tech.relaycorp.letro.awala.parser

import tech.relaycorp.letro.awala.message.AwalaIncomingMessageContent

interface AwalaMessageParser<out T : AwalaIncomingMessageContent> {
    suspend fun parse(content: ByteArray): T?
}
