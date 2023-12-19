package tech.relaycorp.letro.awala.message

import android.util.Log
import tech.relaycorp.letro.awala.AwalaManagerImpl

sealed class MessageType(val value: String) {
    object AccountCreationRequest : MessageType("application/vnd.relaycorp.letro.account-request")
    object ConnectionParamsRequest : MessageType("application/vnd.relaycorp.letro.connection-params-request")
    object ConnectionParams : MessageType("application/vnd.relaycorp.letro.connection-params")
    object MisconfiguredInternetEndpoint : MessageType("application/vnd.relaycorp.letro.misconfigured-internet-endpoint")
    object MemberPublicKeyImport : MessageType("application/vnd.veraid-authority.member-public-key-import")
    object VeraIdMemberBundle : MessageType("application/vnd.veraid.member-bundle")
    object AccountCreation : MessageType("application/vnd.relaycorp.letro.account-creation")
    object AuthorizeReceivingFromServer : MessageType("application/vnd+relaycorp.awala.pda-path")
    object ContactPairingRequest : MessageType("application/vnd.relaycorp.letro.contact-pairing.request")
    object ContactPairingAuthorization : MessageType("application/vnd.relaycorp.letro.pairing-auth")
    object NewConversation : MessageType("application/vnd.letro.conversation")
    object NewMessage : MessageType("application/vnd.letro.message")
    object ContactPhotoUpdated : MessageType("application/vnd.letro.contact-photo-updated")
    object ContactPhotoDeleted : MessageType("application/vnd.letro.contact-photo-deleted")
    object Unknown : MessageType("unknown")

    companion object {
        fun from(type: String): MessageType {
            return when (type) {
                AccountCreationRequest.value -> AccountCreationRequest
                AccountCreation.value -> AccountCreation
                AuthorizeReceivingFromServer.value -> AuthorizeReceivingFromServer
                ConnectionParamsRequest.value -> ConnectionParamsRequest
                ConnectionParams.value -> ConnectionParams
                MisconfiguredInternetEndpoint.value -> MisconfiguredInternetEndpoint
                MemberPublicKeyImport.value -> MemberPublicKeyImport
                VeraIdMemberBundle.value -> VeraIdMemberBundle
                ContactPairingRequest.value -> ContactPairingRequest
                ContactPairingAuthorization.value -> ContactPairingAuthorization
                NewMessage.value -> NewMessage
                NewConversation.value -> NewConversation
                ContactPhotoUpdated.value -> ContactPhotoUpdated
                ContactPhotoDeleted.value -> ContactPhotoDeleted
                else -> {
                    Log.e(AwalaManagerImpl.TAG, "Unknown message type $type")
                    Unknown
                }
            }
        }
    }
}
