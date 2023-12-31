package tech.relaycorp.letro.conversation.server.processor

import tech.relaycorp.letro.awala.AwalaManager
import tech.relaycorp.letro.awala.message.AwalaIncomingMessageContent
import tech.relaycorp.letro.awala.processor.AwalaMessageProcessor
import tech.relaycorp.letro.conversation.attachments.AttachmentsRepository
import tech.relaycorp.letro.conversation.server.parser.NewConversationMessageParser
import tech.relaycorp.letro.conversation.storage.dao.ConversationsDao
import tech.relaycorp.letro.conversation.storage.dao.MessagesDao
import tech.relaycorp.letro.push.PushManager
import tech.relaycorp.letro.push.PushNewMessageTextFormatter
import tech.relaycorp.letro.push.model.LargeIcon
import tech.relaycorp.letro.push.model.PushData
import tech.relaycorp.letro.ui.navigation.Action
import tech.relaycorp.letro.utils.Logger
import javax.inject.Inject

@Suppress("NAME_SHADOWING")
class NewConversationProcessor @Inject constructor(
    private val pushManager: PushManager,
    private val conversationsDao: ConversationsDao,
    private val messagesDao: MessagesDao,
    private val attachmentsRepository: AttachmentsRepository,
    private val messageTextFormatter: PushNewMessageTextFormatter,
    parser: NewConversationMessageParser,
    logger: Logger,
) : AwalaMessageProcessor<AwalaIncomingMessageContent.NewMessage>(parser, logger) {

    override suspend fun handleMessage(
        content: AwalaIncomingMessageContent.NewMessage,
        recipientNodeId: String,
        senderNodeId: String,
        awalaManager: AwalaManager,
    ) {
        val conversation = content.conversation
        val message = content.message
        val contact = content.contact

        conversationsDao.createNewConversation(conversation)
        val messageId = messagesDao.insert(message)

        attachmentsRepository.saveMessageAttachments(
            conversationId = conversation.conversationId,
            messageId = messageId,
            attachments = content.attachments,
        )

        pushManager.showPush(
            PushData(
                title = contact.alias ?: message.senderVeraId,
                text = messageTextFormatter.getText(conversation.subject, message.text, content.attachments.map { it.fileName }),
                action = Action.OpenConversation(
                    conversationId = conversation.conversationId.toString(),
                    accountId = conversation.ownerVeraId,
                ),
                recipientAccountId = conversation.ownerVeraId,
                notificationId = messageId.toInt(),
                largeIcon = if (content.contact.avatarFilePath != null) LargeIcon.File(content.contact.avatarFilePath) else LargeIcon.DefaultAvatar(),
            ),
        )
    }

    override suspend fun isFromExpectedSender(
        content: AwalaIncomingMessageContent.NewMessage,
        recipientNodeId: String,
        senderNodeId: String,
        awalaManager: AwalaManager,
    ): Boolean {
        return senderNodeId == content.contact.contactEndpointId
    }
}
