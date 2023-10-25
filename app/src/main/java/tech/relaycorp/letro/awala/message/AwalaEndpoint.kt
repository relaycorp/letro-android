package tech.relaycorp.letro.awala.message

sealed interface AwalaEndpoint {

    data class Private(
        val nodeId: String,
    ) : AwalaEndpoint

    /**
     * Public endpoint. If nodeId is null, then the recipient is the Letro server
     */
    data class Public(
        val nodeId: String? = null,
    ) : AwalaEndpoint
}
