package tech.relaycorp.letro.awala.message

import tech.relaycorp.letro.account.model.Account
import tech.relaycorp.letro.contacts.model.Contact
import tech.relaycorp.letro.conversation.server.dto.AttachmentAwalaWrapper
import tech.relaycorp.letro.conversation.storage.entity.Conversation
import tech.relaycorp.letro.conversation.storage.entity.Message
import tech.relaycorp.veraid.Member
import tech.relaycorp.veraid.pki.MemberIdBundle

sealed interface AwalaIncomingMessageContent {
    data class NewMessage(
        val conversation: Conversation,
        val message: Message,
        val contact: Contact,
        val attachments: List<AttachmentAwalaWrapper>,
        val isNewConversation: Boolean,
    ) : AwalaIncomingMessageContent

    data class AccountCreation(
        val account: Account,
        val accountCreation: tech.relaycorp.letro.server.messages.AccountCreation,
    ) : AwalaIncomingMessageContent

    class ConnectionParams(
        val connectionParams: ByteArray,
    ) : AwalaIncomingMessageContent

    class VeraIdMemberBundle(
        val bundle: MemberIdBundle,
        val member: Member,
        val bundleSerialised: ByteArray,
    ) : AwalaIncomingMessageContent

    data class MisconfiguredInternetEndpoint(
        val domain: String,
    ) : AwalaIncomingMessageContent

    class ContactPairingMatch(
        val ownerVeraId: String,
        val contactVeraId: String,
        val contactEndpointId: String,
        val contactEndpointPublicKey: ByteArray,
    ) : AwalaIncomingMessageContent

    class ContactPairingAuthorization(
        val authData: ByteArray,
    ) : AwalaIncomingMessageContent
}
