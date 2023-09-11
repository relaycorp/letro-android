package tech.relaycorp.letro.awala.message

sealed interface MessageRecipient {

    data class User(
        val nodeId: String,
    ) : MessageRecipient

    data class Server(
        val nodeId: String? = null,
    ) : MessageRecipient
}
