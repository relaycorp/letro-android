package tech.relaycorp.letro.messages.parser

import com.google.gson.Gson
import tech.relaycorp.letro.messages.model.ConversationAwalaWrapper
import tech.relaycorp.letro.messages.model.MessageAwalaWrapper
import tech.relaycorp.letro.messages.storage.entity.Conversation
import tech.relaycorp.letro.messages.storage.entity.Message
import javax.inject.Inject

interface OutgoingMessageMessageEncoder {
    fun encodeNewConversationContent(conversation: Conversation, messageText: String): ByteArray
    fun encodeNewMessageContent(message: Message): ByteArray
}

class OutgoingMessageMessageEncoderImpl @Inject constructor() : OutgoingMessageMessageEncoder {

    override fun encodeNewConversationContent(conversation: Conversation, messageText: String): ByteArray {
        val json = Gson().toJson(
            ConversationAwalaWrapper(
                conversationId = conversation.conversationId.toString(),
                messageText = messageText,
                senderVeraId = conversation.ownerVeraId,
                recipientVeraId = conversation.contactVeraId,
                subject = conversation.subject,
            ),
        )
        return json.toByteArray()
    }

    override fun encodeNewMessageContent(message: Message): ByteArray {
        val json = Gson().toJson(
            MessageAwalaWrapper(
                conversationId = message.conversationId.toString(),
                messageText = message.text,
                senderVeraId = message.senderVeraId,
                recipientVeraId = message.recipientVeraId,
            ),
        )
        return json.toByteArray()
    }
}
