package tech.relaycorp.letro.messages.parser

import tech.relaycorp.letro.awala.parser.AwalaMessageParser
import tech.relaycorp.letro.messages.dto.NewMessageIncomingMessage
import tech.relaycorp.letro.messages.storage.entity.Message
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject

interface NewMessageMessageParser : AwalaMessageParser

class NewMessageMessageParserImpl @Inject constructor() : NewMessageMessageParser {

    override fun parse(content: ByteArray): NewMessageIncomingMessage {
        val message = mockMessage(UUID.randomUUID()) // TODO: parse message here
        return NewMessageIncomingMessage(
            content = message,
        )
    }
}

internal fun mockMessage(conversationId: UUID) = Message(
    conversationId = conversationId,
    text = "Hello, how are you?",
    ownerVeraId = "ff@cuppa.fans",
    recipientVeraId = "ff@cuppa.fans",
    senderVeraId = "ff@applepie.rocks",
    sentAt = LocalDateTime.now(),
)
