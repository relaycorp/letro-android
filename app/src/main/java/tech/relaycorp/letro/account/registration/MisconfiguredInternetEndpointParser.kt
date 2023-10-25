package tech.relaycorp.letro.account.registration

import tech.relaycorp.letro.awala.message.AwalaIncomingMessageContent
import tech.relaycorp.letro.awala.parser.AwalaMessageParser
import javax.inject.Inject

interface MisconfiguredInternetEndpointParser : AwalaMessageParser<AwalaIncomingMessageContent.MisconfiguredInternetEndpoint>

class MisconfiguredInternetEndpointParserImpl @Inject constructor() : MisconfiguredInternetEndpointParser {

    override suspend fun parse(content: ByteArray): AwalaIncomingMessageContent.MisconfiguredInternetEndpoint? {
        return AwalaIncomingMessageContent.MisconfiguredInternetEndpoint(
            domain = content.decodeToString(),
        )
    }
}
