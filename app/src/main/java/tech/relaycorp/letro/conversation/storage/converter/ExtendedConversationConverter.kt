package tech.relaycorp.letro.conversation.storage.converter

import tech.relaycorp.letro.contacts.model.Contact
import tech.relaycorp.letro.conversation.attachments.filepicker.FileConverter
import tech.relaycorp.letro.conversation.attachments.utils.AttachmentInfoConverter
import tech.relaycorp.letro.conversation.model.ExtendedConversation
import tech.relaycorp.letro.conversation.model.ExtendedMessage
import tech.relaycorp.letro.conversation.storage.entity.Attachment
import tech.relaycorp.letro.conversation.storage.entity.Conversation
import tech.relaycorp.letro.conversation.storage.entity.Message
import java.sql.Timestamp
import java.time.ZoneOffset
import java.util.UUID
import javax.inject.Inject

interface ExtendedConversationConverter {
    suspend fun convert(
        conversations: List<Conversation>,
        messages: List<Message>,
        contacts: List<Contact>,
        attachments: List<Attachment>,
        ownerVeraId: String,
    ): List<ExtendedConversation>
}

class ExtendedConversationConverterImpl @Inject constructor(
    private val messageTimestampFormatter: MessageTimestampFormatter,
    private val fileConverter: FileConverter,
    private val attachmentInfoConverter: AttachmentInfoConverter,
) : ExtendedConversationConverter {

    override suspend fun convert(
        conversations: List<Conversation>,
        messages: List<Message>,
        contacts: List<Contact>,
        attachments: List<Attachment>,
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
                    .map { message ->
                        val isOutgoing = ownerVeraId == message.senderVeraId
                        ExtendedMessage(
                            conversationId = conversation.conversationId,
                            senderVeraId = message.senderVeraId,
                            recipientVeraId = message.recipientVeraId,
                            senderDisplayName = if (isOutgoing) message.ownerVeraId else contactDisplayName,
                            recipientDisplayName = if (isOutgoing) contactDisplayName else message.ownerVeraId,
                            isOutgoing = isOutgoing,
                            contactDisplayName = contactDisplayName,
                            text = message.text,
                            sentAt = message.sentAt,
                            sentAtBriefFormatted = messageTimestampFormatter.formatBrief(message.sentAt),
                            sentAtDetailedFormatted = messageTimestampFormatter.formatDetailed(message.sentAt),
                            attachments = attachments.filter { it.messageId == message.id }.mapNotNull { fileConverter.getFile(it)?.let { attachmentInfoConverter.convert(it) } },
                        )
                    }
                ExtendedConversation(
                    conversationId = conversation.conversationId,
                    ownerVeraId = conversation.ownerVeraId,
                    contactVeraId = conversation.contactVeraId,
                    contactDisplayName = contactDisplayName,
                    subject = conversation.subject,
                    lastMessageTimestamp = Timestamp.from(lastMessage.sentAt.toInstant(ZoneOffset.UTC)).time,
                    lastMessageFormattedTimestamp = messageTimestampFormatter.formatBrief(lastMessage.sentAt),
                    messages = extendedMessagesList,
                    lastMessage = extendedMessagesList.last(),
                    isRead = conversation.isRead,
                    isArchived = conversation.isArchived,
                    totalMessagesFormattedText = if (extendedMessagesList.count() <= 1) null else "(${extendedMessagesList.count()})",
                )
            }
            .forEach { extendedConversations.add(it) }
        return extendedConversations
            .sortedByDescending { it.lastMessageTimestamp }
    }
}
