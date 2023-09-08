package tech.relaycorp.letro.data

sealed class ContentType(val value: String) {
    object AccountCreationRequest : ContentType("application/vnd.relaycorp.letro.account-creation-request")
    object AccountCreationCompleted : ContentType("application/vnd.relaycorp.letro.account-creation-completed-tmp")
    object AuthorizeReceivingFromServer : ContentType("application/vnd+relaycorp.awala.pda-path")
    object ContactPairingRequest : ContentType("application/vnd.relaycorp.letro.pairing-request-tmp")
    object ContactPairingMatch : ContentType("application/vnd.relaycorp.letro.pairing-match-tmp")
    object ContactPairingAuthorization : ContentType("application/vnd.relaycorp.letro.pairing-auth")
}
