package tech.relaycorp.letro.awala.message

sealed interface MessageRecipient {

    data class PrivateEndpoint(
        val nodeId: String,
    ) : MessageRecipient

    /**
     * Public endpoint. If nodeId is null, then the recipient is the Letro server
     */
    data class PublicEndpoint(
        val nodeId: String? = null,
    ) : MessageRecipient
}
