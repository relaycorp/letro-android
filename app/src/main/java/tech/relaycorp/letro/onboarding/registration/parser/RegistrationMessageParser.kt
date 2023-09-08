package tech.relaycorp.letro.onboarding.registration.parser

import tech.relaycorp.letro.awala.message.Message
import tech.relaycorp.letro.onboarding.registration.dto.RegistrationResponse
import java.nio.charset.Charset
import javax.inject.Inject

interface RegistrationMessageParser {
    fun parse(message: Message): RegistrationResponse
}

class RegistrationMessageParserImpl @Inject constructor(): RegistrationMessageParser {

    override fun parse(message: Message): RegistrationResponse {
        val veraIds =
            message.content.toString(Charset.defaultCharset()).split(",")
        return RegistrationResponse(
            requestedVeraId = veraIds[0],
            assignedVeraId = veraIds[1]
        )
    }
}