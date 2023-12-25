package tech.relaycorp.letro.conversation.server.processor

import tech.relaycorp.letro.awala.AwalaManager
import tech.relaycorp.letro.awala.message.AwalaIncomingMessageContent
import tech.relaycorp.letro.awala.processor.AwalaMessageProcessor
import tech.relaycorp.letro.conversation.attachments.AttachmentsRepository
import tech.relaycorp.letro.conversation.server.parser.NewMessageMessageParserImpl
import tech.relaycorp.letro.conversation.storage.dao.ConversationsDao
import tech.relaycorp.letro.conversation.storage.dao.MessagesDao
import tech.relaycorp.letro.push.PushManager
import tech.relaycorp.letro.push.PushNewMessageTextFormatter
import tech.relaycorp.letro.push.model.LargeIcon
import tech.relaycorp.letro.push.model.PushData
import tech.relaycorp.letro.ui.navigation.Action
import tech.relaycorp.letro.utils.Logger
import javax.inject.Inject

class NewMessageProcessor @Inject constructor(
    private val pushManager: PushManager,
    private val conversationsDao: ConversationsDao,
    private val messagesDao: MessagesDao,
    private val attachmentsRepository: AttachmentsRepository,
    private val messageTextFormatter: PushNewMessageTextFormatter,
    parser: NewMessageMessageParserImpl,
    logger: Logger,
) : AwalaMessageProcessor<AwalaIncomingMessageContent.NewMessage>(parser, logger) {

    override suspend fun handleMessage(
        content: AwalaIncomingMessageContent.NewMessage,
        senderNodeId: String,
        awalaManager: AwalaManager,
    ) {
        if (content.isNewConversation) {
            conversationsDao.createNewConversation(content.conversation)
        } else {
            conversationsDao.update(
                content.conversation.copy(
                    isRead = false,
                    isArchived = false,
                ),
            )
        }
        val messageId = messagesDao.insert(content.message)
        attachmentsRepository.saveMessageAttachments(
            conversationId = content.conversation.conversationId,
            messageId = messageId,
            attachments = content.attachments,
        )

        pushManager.showPush(
            PushData(
                title = content.contact.alias ?: content.message.senderVeraId,
                text = messageTextFormatter.getText(content.conversation.subject, content.message.text, content.attachments.map { it.fileName }),
                action = Action.OpenConversation(
                    conversationId = content.conversation.conversationId.toString(),
                    accountId = content.conversation.ownerVeraId,
                ),
                recipientAccountId = content.conversation.ownerVeraId,
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
