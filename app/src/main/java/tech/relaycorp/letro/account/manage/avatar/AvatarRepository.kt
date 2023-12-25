package tech.relaycorp.letro.account.manage.avatar

import tech.relaycorp.letro.account.di.AvatarFileConverterAnnotation
import tech.relaycorp.letro.account.model.Account
import tech.relaycorp.letro.account.storage.repository.AccountRepository
import tech.relaycorp.letro.awala.AwalaManager
import tech.relaycorp.letro.awala.message.AwalaEndpoint
import tech.relaycorp.letro.awala.message.AwalaOutgoingMessage
import tech.relaycorp.letro.awala.message.MessageType
import tech.relaycorp.letro.contacts.storage.repository.ContactsRepository
import tech.relaycorp.letro.conversation.attachments.filepicker.FileConverter
import tech.relaycorp.letro.conversation.attachments.filepicker.FileManager
import tech.relaycorp.letro.conversation.attachments.filepicker.FileSizeExceedsLimitException
import tech.relaycorp.letro.conversation.attachments.filepicker.model.File
import java.util.UUID
import javax.inject.Inject

interface AvatarRepository {
    suspend fun saveAvatar(
        account: Account,
        avatarUri: String,
    )

    suspend fun deleteAvatar(account: Account)
}

class AvatarRepositoryImpl @Inject constructor(
    private val accountRepository: AccountRepository,
    private val contactsRepository: ContactsRepository,
    private val awalaManager: AwalaManager,
    @AvatarFileConverterAnnotation private val fileConverter: FileConverter,
    private val fileManager: FileManager,
) : AvatarRepository {

    @Throws(FileSizeExceedsLimitException::class)
    override suspend fun saveAvatar(account: Account, avatarUri: String) {
        val file = fileConverter.getFile(avatarUri) ?: return
        val filePath = fileManager.save(
            File.FileWithContent(
                id = UUID.randomUUID(),
                name = account.accountId,
                extension = file.extension,
                size = file.content.size.toLong(),
                content = file.content,
            ),
        )

        updateAccountAndNotifyContacts(
            account = account,
            avatarFilePath = filePath,
            avatarContent = file.content,
        )
    }

    override suspend fun deleteAvatar(account: Account) {
        val avatar = account.avatarPath ?: return
        fileManager.delete(avatar)
        updateAccountAndNotifyContacts(
            account = account,
            avatarFilePath = null,
            avatarContent = ByteArray(0),
        )
    }

    private suspend fun updateAccountAndNotifyContacts(
        account: Account,
        avatarContent: ByteArray,
        avatarFilePath: String?,
    ) {
        // Set new avatar locally
        accountRepository.updateAccount(
            account.copy(
                avatarPath = avatarFilePath,
            ),
        )

        // Delete old avatar
        account.avatarPath?.let { oldAvatar ->
            fileManager.delete(oldAvatar)
        }

        // Notify contacts
        contactsRepository.getContactsSync(account.accountId)
            .forEach {
                it.contactEndpointId ?: return@forEach
                awalaManager.sendMessage(
                    outgoingMessage = AwalaOutgoingMessage(
                        type = MessageType.ContactPhotoUpdated,
                        content = avatarContent,
                    ),
                    recipient = AwalaEndpoint.Private(
                        nodeId = it.contactEndpointId,
                    ),
                    senderAccount = account,
                )
            }
    }
}
