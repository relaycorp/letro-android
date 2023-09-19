package tech.relaycorp.letro.messages.parser

import tech.relaycorp.letro.messages.storage.entity.Conversation
import javax.inject.Inject

interface OutgoingConversationMessageEncoder {
    fun encode(conversation: Conversation): ByteArray
}

class OutgoingConversationMessageEncoderImpl @Inject constructor() : OutgoingConversationMessageEncoder {

    override fun encode(conversation: Conversation): ByteArray {
        return ByteArray(0) // TODO: encode conversation to ByteArray
    }
}
