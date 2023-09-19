package tech.relaycorp.letro.messages.converter

import tech.relaycorp.letro.contacts.model.Contact
import tech.relaycorp.letro.messages.model.ExtendedConversation
import tech.relaycorp.letro.messages.model.ExtendedMessage
import tech.relaycorp.letro.messages.storage.entity.Conversation
import tech.relaycorp.letro.messages.storage.entity.Message
import java.util.UUID
import javax.inject.Inject

interface ExtendedConversationConverter {
    fun convert(conversations: List<Conversation>, messages: List<Message>, contacts: List<Contact>): List<ExtendedConversation>
}

class ExtendedConversationConverterImpl @Inject constructor(
    private val messageTimestampConverter: MessageTimestampConverter,
) : ExtendedConversationConverter {

    override fun convert(
        conversations: List<Conversation>,
        messages: List<Message>,
        contacts: List<Contact>,
    ): List<ExtendedConversation> {
        val conversationsMap = hashMapOf<UUID, Conversation>()
        conversations.forEach { conversationsMap[it.conversationId] = it }

        val sortedMessages = messages.sortedByDescending { it.sentAt }

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
                ExtendedConversation(
                    conversationId = conversation.conversationId,
                    ownerVeraId = conversation.ownerVeraId,
                    recipientVeraId = conversation.contactVeraId,
                    recipientAlias = contacts.find { it.contactVeraId == conversation.contactVeraId }?.alias,
                    subject = conversation.subject,
                    lastMessageFormattedTimestamp = messageTimestampConverter.convert(messagesToConversation[conversation.conversationId]!!.first().sentAt),
                    messages = sortedMessages
                        .filter { it.conversationId == conversation.conversationId }
                        .map {
                            ExtendedMessage(
                                conversationId = conversation.conversationId,
                                senderVeraId = it.senderVeraId,
                                recipientVeraId = it.recipientVeraId,
                                text = it.text,
                            )
                        },
                )
            }
            .forEach { extendedConversations.add(it) }
        return extendedConversations
            .sortedByDescending { it.lastMessageFormattedTimestamp }
    }
}
