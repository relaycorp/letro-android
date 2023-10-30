package tech.relaycorp.letro.conversation.server.parser

import com.google.gson.Gson
import tech.relaycorp.letro.awala.message.AwalaIncomingMessageContent
import tech.relaycorp.letro.awala.parser.AwalaMessageParser
import tech.relaycorp.letro.contacts.storage.dao.ContactsDao
import tech.relaycorp.letro.conversation.server.dto.MessageAwalaWrapper
import tech.relaycorp.letro.conversation.storage.dao.ConversationsDao
import tech.relaycorp.letro.conversation.storage.entity.Conversation
import tech.relaycorp.letro.conversation.storage.entity.Message
import tech.relaycorp.letro.utils.time.nowUTC
import java.util.UUID
import javax.inject.Inject

interface NewMessageMessageParser : AwalaMessageParser<AwalaIncomingMessageContent.NewMessage>

class NewMessageMessageParserImpl @Inject constructor(
    private val conversationsDao: ConversationsDao,
    private val contactsDao: ContactsDao,
) : NewMessageMessageParser {

    override suspend fun parse(content: ByteArray): AwalaIncomingMessageContent.NewMessage? {
        val messageWrapper = Gson().fromJson(content.decodeToString(), MessageAwalaWrapper::class.java)
        val conversationId = UUID.fromString(messageWrapper.conversationId)
        var isNewConversation = false
        val conversation = conversationsDao.getConversationById(conversationId)?.apply {
            isNewConversation = false
        } ?: let {
            val conversation = Conversation(
                conversationId = conversationId,
                ownerVeraId = messageWrapper.recipientVeraId,
                contactVeraId = messageWrapper.senderVeraId,
                isRead = false,
            )
            isNewConversation = true
            conversation
        }
        val message = Message(
            conversationId = conversationId,
            text = messageWrapper.messageText,
            ownerVeraId = conversation.ownerVeraId,
            recipientVeraId = messageWrapper.recipientVeraId,
            senderVeraId = messageWrapper.senderVeraId,
            sentAtUtc = nowUTC(),
        )

        val contact = contactsDao.getContact(
            ownerVeraId = conversation.ownerVeraId,
            contactVeraId = conversation.contactVeraId,
        ) ?: return null

        return AwalaIncomingMessageContent.NewMessage(
            conversation = conversation,
            message = message,
            attachments = messageWrapper.attachments,
            contact = contact,
            isNewConversation = isNewConversation,
        )
    }
}
