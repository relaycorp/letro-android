package tech.relaycorp.letro.contacts.pairing.dto

import tech.relaycorp.letro.awala.message.AwalaIncomingMessage
import tech.relaycorp.letro.awala.message.MessageType

data class ContactPairingMatchResponse(
    val ownerVeraId: String,
    val contactVeraId: String,
    val contactEndpointId: String,
    val contactEndpointPublicKey: ByteArray,
)

data class ContactPairingMatchIncomingMessage(
    override val content: ContactPairingMatchResponse,
) : AwalaIncomingMessage<ContactPairingMatchResponse> {
    override val type: MessageType
        get() = MessageType.ContactPairingMatch
}
