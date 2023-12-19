package tech.relaycorp.letro.conversation.storage.converter

import tech.relaycorp.letro.account.model.Account
import tech.relaycorp.letro.contacts.model.Contact
import tech.relaycorp.letro.conversation.attachments.filepicker.FileConverter
import tech.relaycorp.letro.conversation.attachments.utils.AttachmentInfoConverter
import tech.relaycorp.letro.conversation.di.ConversationFileConverterAnnotation
import tech.relaycorp.letro.conversation.model.ExtendedConversation
import tech.relaycorp.letro.conversation.model.ExtendedMessage
import tech.relaycorp.letro.conversation.storage.entity.Attachment
import tech.relaycorp.letro.conversation.storage.entity.Conversation
import tech.relaycorp.letro.conversation.storage.entity.Message
import tech.relaycorp.letro.utils.time.toSystemTimeZone
import java.util.UUID
import javax.inject.Inject

interface ExtendedConversationConverter {
    suspend fun convert(
        conversations: List<Conversation>,
        messages: List<Message>,
        contacts: List<Contact>,
        attachments: List<Attachment>,
        owner: Account,
    ): List<ExtendedConversation>

    suspend fun updateTimestamps(
        conversations: List<ExtendedConversation>,
    ): List<ExtendedConversation>
}

class ExtendedConversationConverterImpl @Inject constructor(
    private val messageTimestampFormatter: MessageTimestampFormatter,
    @ConversationFileConverterAnnotation private val fileConverter: FileConverter,
    private val attachmentInfoConverter: AttachmentInfoConverter,
) : ExtendedConversationConverter {

    override suspend fun convert(
        conversations: List<Conversation>,
        messages: List<Message>,
        contacts: List<Contact>,
        attachments: List<Attachment>,
        owner: Account,
    ): List<ExtendedConversation> {
        val conversationsMap = hashMapOf<UUID, Conversation>()
        conversations.forEach { conversationsMap[it.conversationId] = it }

        val sortedMessages = messages.sortedBy { it.sentAtUtc }

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
                val contact = contacts.find { it.contactVeraId == conversation.contactVeraId }
                val contactDisplayName = contact?.alias ?: conversation.contactVeraId
                val lastMessage = messagesToConversation[conversation.conversationId]!!.last()
                val extendedMessagesList = sortedMessages
                    .filter { it.conversationId == conversation.conversationId }
                    .map { message ->
                        val isOutgoing = owner.accountId == message.senderVeraId
                        ExtendedMessage(
                            conversationId = conversation.conversationId,
                            senderVeraId = message.senderVeraId,
                            recipientVeraId = message.recipientVeraId,
                            senderDisplayName = if (isOutgoing) message.ownerVeraId else contactDisplayName,
                            recipientDisplayName = if (isOutgoing) contactDisplayName else message.ownerVeraId,
                            senderAvatarPath = if (isOutgoing) contact?.avatarFilePath else owner.avatarPath,
                            isOutgoing = isOutgoing,
                            contactDisplayName = contactDisplayName,
                            text = message.text,
                            sentAtUtc = message.sentAtUtc,
                            sentAtBriefFormatted = messageTimestampFormatter.formatBrief(message.sentAtUtc.toSystemTimeZone()),
                            sentAtDetailedFormatted = messageTimestampFormatter.formatDetailed(message.sentAtUtc.toSystemTimeZone()),
                            attachments = attachments.filter { it.messageId == message.id }.mapNotNull { fileConverter.getFile(it)?.let { attachmentInfoConverter.convert(it) } },
                        )
                    }
                ExtendedConversation(
                    conversationId = conversation.conversationId,
                    ownerVeraId = conversation.ownerVeraId,
                    contactVeraId = conversation.contactVeraId,
                    contactDisplayName = contactDisplayName,
                    contactAvatarPath = contact?.avatarFilePath,
                    subject = conversation.subject,
                    lastMessageSentAtUtc = lastMessage.sentAtUtc,
                    lastMessageFormattedTimestamp = messageTimestampFormatter.formatBrief(lastMessage.sentAtUtc.toSystemTimeZone()),
                    messages = extendedMessagesList,
                    lastMessage = extendedMessagesList.last(),
                    isRead = conversation.isRead,
                    isArchived = conversation.isArchived,
                    totalMessagesFormattedText = if (extendedMessagesList.count() <= 1) null else "(${extendedMessagesList.count()})",
                )
            }
            .forEach { extendedConversations.add(it) }
        return extendedConversations
            .sortedByDescending { it.lastMessageSentAtUtc }
    }

    override suspend fun updateTimestamps(conversations: List<ExtendedConversation>): List<ExtendedConversation> {
        return conversations
            .map {
                it.copy(
                    lastMessageFormattedTimestamp = messageTimestampFormatter.formatBrief(it.lastMessageSentAtUtc.toSystemTimeZone()),
                    messages = it.messages.map {
                        it.copy(
                            sentAtBriefFormatted = messageTimestampFormatter.formatBrief(it.sentAtUtc.toSystemTimeZone()),
                            sentAtDetailedFormatted = messageTimestampFormatter.formatDetailed(it.sentAtUtc.toSystemTimeZone()),
                        )
                    },
                )
            }
    }
}
