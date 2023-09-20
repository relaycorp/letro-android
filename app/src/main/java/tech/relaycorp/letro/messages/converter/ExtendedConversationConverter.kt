package tech.relaycorp.letro.messages.converter

import tech.relaycorp.letro.contacts.model.Contact
import tech.relaycorp.letro.messages.model.ExtendedConversation
import tech.relaycorp.letro.messages.model.ExtendedMessage
import tech.relaycorp.letro.messages.storage.entity.Conversation
import tech.relaycorp.letro.messages.storage.entity.Message
import java.sql.Timestamp
import java.time.ZoneOffset
import java.util.UUID
import javax.inject.Inject

interface ExtendedConversationConverter {
    fun convert(
        conversations: List<Conversation>,
        messages: List<Message>,
        contacts: List<Contact>,
        ownerVeraId: String,
    ): List<ExtendedConversation>
}

class ExtendedConversationConverterImpl @Inject constructor(
    private val messageTimestampConverter: MessageTimestampConverter,
) : ExtendedConversationConverter {

    override fun convert(
        conversations: List<Conversation>,
        messages: List<Message>,
        contacts: List<Contact>,
        ownerVeraId: String,
    ): List<ExtendedConversation> {
        val conversationsMap = hashMapOf<UUID, Conversation>()
        conversations.forEach { conversationsMap[it.conversationId] = it }

        val sortedMessages = messages.sortedBy { it.sentAt }

        val messagesToConversation = hashMapOf<UUID, ArrayList<Message>>()
        sortedMessages.forEach { message ->
            val messagesInThisConversation = messagesToConversation[message.conversationId] ?: arrayListOf<Message>().also {
                messagesToConversation[message.conversationId] = it
            }
            messagesInThisConversation.add(message)
        }

        val extendedConversations = arrayListOf<ExtendedConversation>()
        conversations
            .mapNotNull { conversation ->
                if (messagesToConversation[conversation.conversationId].isNullOrEmpty()) {
                    return@mapNotNull null
                }
                val contactDisplayName = contacts.find { it.contactVeraId == conversation.contactVeraId }?.alias ?: conversation.contactVeraId
                val lastMessage = messagesToConversation[conversation.conversationId]!!.last()
                val extendedMessagesList = sortedMessages
                    .filter { it.conversationId == conversation.conversationId }
                    .map {
                        val isOutgoing = ownerVeraId == it.senderVeraId
                        ExtendedMessage(
                            conversationId = conversation.conversationId,
                            senderVeraId = it.senderVeraId,
                            recipientVeraId = it.recipientVeraId,
                            isOutgoing = isOutgoing,
                            contactDisplayName = contactDisplayName,
                            text = it.text,
                            sentAtFormatted = messageTimestampConverter.convert(it.sentAt),
                        )
                    }
                ExtendedConversation(
                    conversationId = conversation.conversationId,
                    ownerVeraId = conversation.ownerVeraId,
                    contactVeraId = conversation.contactVeraId,
                    contactDisplayName = contactDisplayName,
                    subject = conversation.subject,
                    lastMessageTimestamp = Timestamp.from(lastMessage.sentAt.toInstant(ZoneOffset.UTC)).time,
                    lastMessageFormattedTimestamp = messageTimestampConverter.convert(lastMessage.sentAt),
                    messages = extendedMessagesList,
                    lastMessage = extendedMessagesList.last(),
                    isRead = conversation.isRead,
                )
            }
            .forEach { extendedConversations.add(it) }
        return extendedConversations
            .sortedByDescending { it.lastMessageTimestamp }
    }
}
