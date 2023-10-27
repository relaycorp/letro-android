package tech.relaycorp.letro.conversation.server.parser

import com.google.gson.Gson
import tech.relaycorp.letro.awala.message.AwalaIncomingMessageContent
import tech.relaycorp.letro.awala.parser.AwalaMessageParser
import tech.relaycorp.letro.contacts.storage.dao.ContactsDao
import tech.relaycorp.letro.conversation.server.dto.ConversationAwalaWrapper
import tech.relaycorp.letro.conversation.storage.entity.Conversation
import tech.relaycorp.letro.conversation.storage.entity.Message
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject

interface NewConversationMessageParser : AwalaMessageParser<AwalaIncomingMessageContent.NewMessage>

class NewConversationMessageParserImpl @Inject constructor(
    private val contactsDao: ContactsDao,
) : NewConversationMessageParser {

    override suspend fun parse(content: ByteArray): AwalaIncomingMessageContent.NewMessage? {
        val conversationWrapper = Gson().fromJson(content.decodeToString(), ConversationAwalaWrapper::class.java)
        val conversationId = UUID.fromString(conversationWrapper.conversationId)
        val conversation = Conversation(
            conversationId = conversationId,
            ownerVeraId = conversationWrapper.recipientVeraId,
            contactVeraId = conversationWrapper.senderVeraId,
            subject = conversationWrapper.subject,
            isRead = false,
        )
        val message = Message(
            conversationId = conversation.conversationId,
            text = conversationWrapper.messageText,
            ownerVeraId = conversation.ownerVeraId,
            recipientVeraId = conversation.ownerVeraId,
            senderVeraId = conversation.contactVeraId,
            sentAt = LocalDateTime.now(),
        )
        val contact = contactsDao.getContact(
            ownerVeraId = conversation.ownerVeraId,
            contactVeraId = conversation.contactVeraId,
        ) ?: return null
        return AwalaIncomingMessageContent.NewMessage(
            conversation = conversation,
            message = message,
            contact = contact,
            attachments = conversationWrapper.attachments,
            isNewConversation = true,
        )
    }
}
