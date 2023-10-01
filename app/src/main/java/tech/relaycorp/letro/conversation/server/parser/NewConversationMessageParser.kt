package tech.relaycorp.letro.conversation.server.parser

import com.google.gson.Gson
import tech.relaycorp.letro.awala.parser.AwalaMessageParser
import tech.relaycorp.letro.conversation.server.dto.ConversationAwalaWrapper
import tech.relaycorp.letro.conversation.server.dto.NewConversationIncomingMessage
import tech.relaycorp.letro.conversation.server.dto.NewConversationIncomingMessageContent
import javax.inject.Inject

interface NewConversationMessageParser : AwalaMessageParser

class NewConversationMessageParserImpl @Inject constructor() : NewConversationMessageParser {

    override fun parse(content: ByteArray): NewConversationIncomingMessage {
        return NewConversationIncomingMessage(
            content = NewConversationIncomingMessageContent(
                conversation = Gson().fromJson(content.decodeToString(), ConversationAwalaWrapper::class.java),
            ),
        )
    }
}
