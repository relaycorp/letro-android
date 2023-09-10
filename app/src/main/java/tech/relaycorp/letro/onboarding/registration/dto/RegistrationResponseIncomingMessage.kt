package tech.relaycorp.letro.onboarding.registration.dto

import tech.relaycorp.letro.awala.message.AwalaIncomingMessage
import tech.relaycorp.letro.awala.message.MessageType

data class RegistrationResponseIncomingMessage(
    override val content: RegistrationResponse,
) : AwalaIncomingMessage<RegistrationResponse> {
    override val type: MessageType
        get() = MessageType.AccountCreationCompleted
}
