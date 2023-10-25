package tech.relaycorp.letro.account.registration.server

import tech.relaycorp.letro.awala.message.AwalaIncomingMessageContent
import tech.relaycorp.letro.awala.parser.AwalaMessageParser
import javax.inject.Inject

interface ConnectionParamsParser : AwalaMessageParser<AwalaIncomingMessageContent.ConnectionParams>

class ConnectionParamsParserImpl @Inject constructor() : ConnectionParamsParser {

    override suspend fun parse(content: ByteArray): AwalaIncomingMessageContent.ConnectionParams? {
        return AwalaIncomingMessageContent.ConnectionParams(
            connectionParams = content,
        )
    }
}
