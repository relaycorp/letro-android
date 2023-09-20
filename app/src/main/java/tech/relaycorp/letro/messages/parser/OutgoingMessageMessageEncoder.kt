package tech.relaycorp.letro.messages.parser

import tech.relaycorp.letro.messages.storage.entity.Conversation
import tech.relaycorp.letro.messages.storage.entity.Message
import javax.inject.Inject

interface OutgoingMessageMessageEncoder {
    fun encodeNewConversationContent(conversation: Conversation): ByteArray
    fun encodeNewMessageContent(message: Message): ByteArray
}

class OutgoingMessageMessageEncoderImpl @Inject constructor() : OutgoingMessageMessageEncoder {

    override fun encodeNewConversationContent(conversation: Conversation): ByteArray {
        return ByteArray(0) // TODO: encode conversation to ByteArray
    }

    override fun encodeNewMessageContent(message: Message): ByteArray {
        return ByteArray(0) // TODO: encode message to ByteArray
    }
}
