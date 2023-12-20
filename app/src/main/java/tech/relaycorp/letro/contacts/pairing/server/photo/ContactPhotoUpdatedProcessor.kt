package tech.relaycorp.letro.contacts.pairing.server.photo

import tech.relaycorp.letro.awala.AwalaManager
import tech.relaycorp.letro.awala.message.AwalaIncomingMessageContent
import tech.relaycorp.letro.awala.processor.AwalaMessageProcessor
import tech.relaycorp.letro.contacts.storage.dao.ContactsDao
import tech.relaycorp.letro.conversation.attachments.filepicker.FileManager
import tech.relaycorp.letro.conversation.attachments.filepicker.model.File
import tech.relaycorp.letro.conversation.attachments.filepicker.model.FileExtension
import tech.relaycorp.letro.utils.Logger
import java.util.UUID
import javax.inject.Inject

class ContactPhotoUpdatedProcessor @Inject constructor(
    private val contactsDao: ContactsDao,
    private val fileManager: FileManager,
    parser: ContactPhotoUpdatedParser,
    logger: Logger,
) : AwalaMessageProcessor<AwalaIncomingMessageContent.ContactPhotoUpdated>(parser, logger) {

    override suspend fun handleMessage(
        content: AwalaIncomingMessageContent.ContactPhotoUpdated,
        senderNodeId: String,
        awalaManager: AwalaManager,
    ) {
        val contacts = contactsDao.getContactsByContactEndpointId(
            contactEndpointId = senderNodeId,
        )
        if (contacts.isEmpty()) {
            logger.w(TAG, "Contact $senderNodeId not found")
            return
        }

        val filePath = when (content) {
            is AwalaIncomingMessageContent.ContactPhotoDeleted -> null
            else -> fileManager.save(
                File.FileWithContent(
                    id = UUID.randomUUID(),
                    name = contacts.first().contactVeraId,
                    extension = FileExtension.Image(),
                    size = content.photo.size.toLong(),
                    content = content.photo,
                ),
            )
        }
        contacts.forEach {
            val previousAvatar = it.avatarFilePath
            contactsDao.update(
                it.copy(
                    avatarFilePath = filePath,
                ),
            )
            previousAvatar?.let {
                fileManager.delete(previousAvatar)
            }
        }
    }

    override suspend fun isFromExpectedSender(
        content: AwalaIncomingMessageContent.ContactPhotoUpdated,
        senderNodeId: String,
        awalaManager: AwalaManager,
    ): Boolean {
        return true
    }

    companion object {
        private const val TAG = "ContactPairingMatchProcessor"
    }
}
