package tech.relaycorp.letro.onboarding.registration.parser

import tech.relaycorp.letro.awala.message.AwalaIncomingMessage
import tech.relaycorp.letro.awala.message.MessageType
import tech.relaycorp.letro.awala.parser.AwalaMessageParser
import tech.relaycorp.letro.onboarding.registration.dto.RegistrationResponse
import tech.relaycorp.letro.onboarding.registration.dto.RegistrationResponseIncomingMessage
import java.nio.charset.Charset
import javax.inject.Inject

interface RegistrationMessageParser: AwalaMessageParser

class RegistrationMessageParserImpl @Inject constructor(): RegistrationMessageParser {

    override fun parse(type: MessageType, content: ByteArray): AwalaIncomingMessage<*> {
        val veraIds = content.toString(Charset.defaultCharset()).split(",")
        val response = RegistrationResponse(
            requestedVeraId = veraIds[0],
            assignedVeraId = veraIds[1]
        )
        return RegistrationResponseIncomingMessage(
            content = response
        )
    }
}