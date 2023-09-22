package tech.relaycorp.letro.messages.parser

import com.google.gson.Gson
import tech.relaycorp.letro.awala.parser.AwalaMessageParser
import tech.relaycorp.letro.messages.dto.NewMessageIncomingMessage
import tech.relaycorp.letro.messages.model.MessageAwalaWrapper
import javax.inject.Inject

interface NewMessageMessageParser : AwalaMessageParser

class NewMessageMessageParserImpl @Inject constructor() : NewMessageMessageParser {

    override fun parse(content: ByteArray): NewMessageIncomingMessage {
        return NewMessageIncomingMessage(
            content = Gson().fromJson(content.decodeToString(), MessageAwalaWrapper::class.java),
        )
    }
}
