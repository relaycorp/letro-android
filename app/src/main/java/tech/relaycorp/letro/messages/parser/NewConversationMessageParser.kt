package tech.relaycorp.letro.messages.parser

import tech.relaycorp.letro.awala.parser.AwalaMessageParser
import tech.relaycorp.letro.messages.dto.NewConversationIncomingMessage
import tech.relaycorp.letro.messages.dto.NewConversationIncomingMessageContent
import tech.relaycorp.letro.messages.storage.entity.Conversation
import java.util.UUID
import javax.inject.Inject

interface NewConversationMessageParser : AwalaMessageParser

class NewConversationMessageParserImpl @Inject constructor() : NewConversationMessageParser {

    override fun parse(content: ByteArray): NewConversationIncomingMessage {
        val conversation = mockConversation() // TODO: parse conversation
        val message = mockMessage(conversation.conversationId)
        return NewConversationIncomingMessage(
            content = NewConversationIncomingMessageContent(
                conversation = conversation,
                message = message,
            ),
        )
    }

    private fun mockConversation() = Conversation(
        ownerVeraId = "ff@cuppa.fans",
        contactVeraId = "ff@applepie.rocks",
        subject = "Test subject",
        conversationId = MOCK_CONVERSATION_ID,
    )
}

private val MOCK_CONVERSATION_ID = UUID.randomUUID()
