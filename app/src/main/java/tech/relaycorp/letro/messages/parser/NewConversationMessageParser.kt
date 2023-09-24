package tech.relaycorp.letro.messages.parser

import com.google.gson.Gson
import tech.relaycorp.letro.awala.parser.AwalaMessageParser
import tech.relaycorp.letro.messages.dto.NewConversationIncomingMessage
import tech.relaycorp.letro.messages.dto.NewConversationIncomingMessageContent
import tech.relaycorp.letro.messages.model.ConversationAwalaWrapper
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
