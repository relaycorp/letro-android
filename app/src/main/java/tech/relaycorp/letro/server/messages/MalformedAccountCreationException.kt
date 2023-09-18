package tech.relaycorp.letro.server.messages

class MalformedAccountCreationException(message: String?, cause: Throwable? = null) :
    Exception(message, cause)
