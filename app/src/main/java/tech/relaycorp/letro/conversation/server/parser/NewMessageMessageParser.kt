package tech.relaycorp.letro.conversation.server.parser

import com.google.gson.Gson
import tech.relaycorp.letro.awala.parser.AwalaMessageParser
import tech.relaycorp.letro.conversation.server.dto.MessageAwalaWrapper
import tech.relaycorp.letro.conversation.server.dto.NewMessageIncomingMessage
import javax.inject.Inject

interface NewMessageMessageParser : AwalaMessageParser

class NewMessageMessageParserImpl @Inject constructor() : NewMessageMessageParser {

    override fun parse(content: ByteArray): NewMessageIncomingMessage {
        return NewMessageIncomingMessage(
            content = Gson().fromJson(content.decodeToString(), MessageAwalaWrapper::class.java),
        )
    }
}
