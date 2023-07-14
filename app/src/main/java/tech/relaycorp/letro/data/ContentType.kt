package tech.relaycorp.letro.data

sealed class ContentType(val value: String) {
    object AccountCreationRequest : ContentType("application/vnd.relaycorp.letro.account-creation-request")
    object AccountCreationCompleted : ContentType("application/vnd.relaycorp.letro.account-creation-completed")
    object AuthorizeReceivingFromServer : ContentType("application/vnd+relaycorp.awala.pda-path")
}
