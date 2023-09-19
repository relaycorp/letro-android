package tech.relaycorp.letro.server.messages

class InvalidAccountCreationException(message: String?, cause: Throwable? = null) :
    Exception(message, cause)
