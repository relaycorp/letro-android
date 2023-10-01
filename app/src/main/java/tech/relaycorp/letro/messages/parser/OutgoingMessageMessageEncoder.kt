package tech.relaycorp.letro.messages.parser

import com.google.gson.Gson
import tech.relaycorp.letro.messages.filepicker.model.File
import tech.relaycorp.letro.messages.model.AttachmentAwalaWrapper
import tech.relaycorp.letro.messages.model.ConversationAwalaWrapper
import tech.relaycorp.letro.messages.model.MessageAwalaWrapper
import tech.relaycorp.letro.messages.storage.entity.Conversation
import tech.relaycorp.letro.messages.storage.entity.Message
import javax.inject.Inject

interface OutgoingMessageMessageEncoder {
    fun encodeNewConversationContent(
        conversation: Conversation,
        messageText: String,
        attachments: List<File.FileWithContent>,
    ): ByteArray
    fun encodeNewMessageContent(
        message: Message,
        attachments: List<File.FileWithContent>,
    ): ByteArray
}

class OutgoingMessageMessageEncoderImpl @Inject constructor() : OutgoingMessageMessageEncoder {

    override fun encodeNewConversationContent(
        conversation: Conversation,
        messageText: String,
        attachments: List<File.FileWithContent>,
    ): ByteArray {
        val json = Gson().toJson(
            ConversationAwalaWrapper(
                conversationId = conversation.conversationId.toString(),
                messageText = messageText,
                senderVeraId = conversation.ownerVeraId,
                recipientVeraId = conversation.contactVeraId,
                subject = conversation.subject,
                attachments = attachments.map {
                    AttachmentAwalaWrapper(
                        fileName = it.name,
                        content = it.content,
                        mimeType = it.extension.mimeType,
                    )
                },
            ),
        )
        return json.toByteArray()
    }

    override fun encodeNewMessageContent(
        message: Message,
        attachments: List<File.FileWithContent>,
    ): ByteArray {
        val json = Gson().toJson(
            MessageAwalaWrapper(
                conversationId = message.conversationId.toString(),
                messageText = message.text,
                senderVeraId = message.senderVeraId,
                recipientVeraId = message.recipientVeraId,
                attachments = attachments.map {
                    AttachmentAwalaWrapper(
                        fileName = it.name,
                        content = it.content,
                        mimeType = it.extension.mimeType,
                    )
                },
            ),
        )
        return json.toByteArray()
    }
}
