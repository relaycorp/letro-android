package tech.relaycorp.letro.conversation.server.parser

import com.google.gson.Gson
import tech.relaycorp.letro.conversation.attachments.filepicker.model.File
import tech.relaycorp.letro.conversation.server.dto.AttachmentAwalaWrapper
import tech.relaycorp.letro.conversation.server.dto.ConversationAwalaWrapper
import tech.relaycorp.letro.conversation.server.dto.MessageAwalaWrapper
import tech.relaycorp.letro.conversation.storage.entity.Conversation
import tech.relaycorp.letro.conversation.storage.entity.Message
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
