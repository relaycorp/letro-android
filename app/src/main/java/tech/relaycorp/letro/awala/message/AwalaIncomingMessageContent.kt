package tech.relaycorp.letro.awala.message

import tech.relaycorp.letro.account.model.Account
import tech.relaycorp.letro.contacts.model.Contact
import tech.relaycorp.letro.conversation.server.dto.AttachmentAwalaWrapper
import tech.relaycorp.letro.conversation.storage.entity.Conversation
import tech.relaycorp.letro.conversation.storage.entity.Message
import tech.relaycorp.letro.server.messages.AccountCreation
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

    data class ConnectionParams(
        val connectionParams: ByteArray,
    ) : AwalaIncomingMessageContent {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as ConnectionParams

            if (!connectionParams.contentEquals(other.connectionParams)) return false

            return true
        }

        override fun hashCode(): Int {
            return connectionParams.contentHashCode()
        }
    }

    data class VeraIdMemberBundle(
        val memberIdBundle: MemberIdBundle,
        val member: Member,
        val veraIdBundle: ByteArray,
    ) : AwalaIncomingMessageContent {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as VeraIdMemberBundle

            if (memberIdBundle != other.memberIdBundle) return false
            if (member != other.member) return false
            if (!veraIdBundle.contentEquals(other.veraIdBundle)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = memberIdBundle.hashCode()
            result = 31 * result + member.hashCode()
            result = 31 * result + veraIdBundle.contentHashCode()
            return result
        }
    }

    data class MisconfiguredInternetEndpoint(
        val domain: String,
    ) : AwalaIncomingMessageContent

    data class ContactPairingMatch(
        val ownerVeraId: String,
        val contactVeraId: String,
        val contactEndpointId: String,
        val contactEndpointPublicKey: ByteArray,
    ) : AwalaIncomingMessageContent {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as ContactPairingMatch

            if (ownerVeraId != other.ownerVeraId) return false
            if (contactVeraId != other.contactVeraId) return false
            if (contactEndpointId != other.contactEndpointId) return false
            if (!contactEndpointPublicKey.contentEquals(other.contactEndpointPublicKey)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = ownerVeraId.hashCode()
            result = 31 * result + contactVeraId.hashCode()
            result = 31 * result + contactEndpointId.hashCode()
            result = 31 * result + contactEndpointPublicKey.contentHashCode()
            return result
        }
    }

    data class ContactPairingAuthorization(
        val authData: ByteArray,
    ) : AwalaIncomingMessageContent {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as ContactPairingAuthorization

            if (!authData.contentEquals(other.authData)) return false

            return true
        }

        override fun hashCode(): Int {
            return authData.contentHashCode()
        }
    }
}
