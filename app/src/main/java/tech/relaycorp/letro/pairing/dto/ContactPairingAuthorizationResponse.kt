package tech.relaycorp.letro.pairing.dto

import tech.relaycorp.letro.awala.message.AwalaIncomingMessage
import tech.relaycorp.letro.awala.message.MessageType

data class ContactPairingAuthorizationResponse(
    val authData: ByteArray,
)

data class ContactPairingAuthorizationIncomingMessage(
    override val content: ContactPairingAuthorizationResponse,
) : AwalaIncomingMessage<ContactPairingAuthorizationResponse> {
    override val type: MessageType
        get() = MessageType.ContactPairingAuthorization
}
