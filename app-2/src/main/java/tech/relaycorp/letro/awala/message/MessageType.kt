package tech.relaycorp.letro.awala.message

sealed class MessageType(val value: String) {
    object AccountCreationRequest : MessageType("application/vnd.relaycorp.letro.account-creation-request")
    object AccountCreationCompleted : MessageType("application/vnd.relaycorp.letro.account-creation-completed-tmp")
    object AuthorizeReceivingFromServer : MessageType("application/vnd+relaycorp.awala.pda-path")
    object ContactPairingRequest : MessageType("application/vnd.relaycorp.letro.pairing-request-tmp")
    object ContactPairingMatch : MessageType("application/vnd.relaycorp.letro.pairing-match-tmp")
    object ContactPairingAuthorization : MessageType("application/vnd.relaycorp.letro.pairing-auth")
    data class Unknown(val rawType: String): MessageType("unknown")

    companion object {
        fun from(type: String): MessageType {
            return when (type) {
                AccountCreationRequest.value -> AccountCreationRequest
                AccountCreationCompleted.value -> AccountCreationCompleted
                AuthorizeReceivingFromServer.value -> AuthorizeReceivingFromServer
                ContactPairingRequest.value -> ContactPairingRequest
                ContactPairingMatch.value -> ContactPairingMatch
                ContactPairingAuthorization.value -> ContactPairingAuthorization
                else -> Unknown(type)
            }
        }
    }
}
