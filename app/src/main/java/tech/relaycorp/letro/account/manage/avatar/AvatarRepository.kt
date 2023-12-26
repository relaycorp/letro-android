package tech.relaycorp.letro.account.manage.avatar

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import tech.relaycorp.awaladroid.AwaladroidException
import tech.relaycorp.letro.account.di.AvatarFileConverterAnnotation
import tech.relaycorp.letro.account.di.ContactsNewAvatarNotifierThread
import tech.relaycorp.letro.account.model.Account
import tech.relaycorp.letro.account.storage.repository.AccountRepository
import tech.relaycorp.letro.awala.AwalaManager
import tech.relaycorp.letro.awala.message.AwalaEndpoint
import tech.relaycorp.letro.awala.message.AwalaOutgoingMessage
import tech.relaycorp.letro.awala.message.MessageType
import tech.relaycorp.letro.contacts.pairing.server.photo.parser.ContactPhotoUpdatedMessageEncoder
import tech.relaycorp.letro.contacts.storage.repository.ContactsRepository
import tech.relaycorp.letro.conversation.attachments.filepicker.FileConverter
import tech.relaycorp.letro.conversation.attachments.filepicker.FileManager
import tech.relaycorp.letro.conversation.attachments.filepicker.FileSizeExceedsLimitException
import tech.relaycorp.letro.conversation.attachments.filepicker.model.File
import tech.relaycorp.letro.conversation.attachments.filepicker.model.FileType
import java.util.UUID
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

interface AvatarRepository {
    suspend fun saveAvatar(
        account: Account,
        avatarUri: String,
    )

    suspend fun deleteAvatar(account: Account, notifyContacts: Boolean = true)
}

class AvatarRepositoryImpl @Inject constructor(
    private val accountRepository: AccountRepository,
    private val contactsRepository: ContactsRepository,
    private val awalaManager: AwalaManager,
    @AvatarFileConverterAnnotation private val fileConverter: FileConverter,
    private val fileManager: FileManager,
    private val messageEncoder: ContactPhotoUpdatedMessageEncoder,
    @ContactsNewAvatarNotifierThread private val contactsNotifierThread: CoroutineContext,
) : AvatarRepository {

    @Throws(FileSizeExceedsLimitException::class, UnsupportedAvatarFormatException::class, AwaladroidException::class)
    override suspend fun saveAvatar(account: Account, avatarUri: String) {
        val file = fileConverter.getFile(avatarUri) ?: return
        if (file.type !is FileType.Image || file.type.extension.lowercase() !in supportedAvatarTypes) {
            throw UnsupportedAvatarFormatException()
        }
        val fileToSave = File.FileWithContent(
            id = UUID.randomUUID(),
            name = account.accountId,
            type = file.type,
            size = file.content.size.toLong(),
            content = file.content,
        )
        val filePath = fileManager.save(fileToSave)

        updateAccountAndNotifyContacts(
            account = account,
            avatarFilePath = filePath,
            file = fileToSave,
        )
    }

    override suspend fun deleteAvatar(account: Account, notifyContacts: Boolean) {
        val avatar = account.avatarPath ?: return
        fileManager.delete(avatar)
        updateAccountAndNotifyContacts(
            account = account,
            avatarFilePath = null,
            file = null,
        )
    }

    @OptIn(DelicateCoroutinesApi::class)
    private suspend fun updateAccountAndNotifyContacts(
        account: Account,
        file: File.FileWithContent?,
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
        GlobalScope.launch(contactsNotifierThread) {
            contactsRepository.getContactsSync(account.accountId)
                .forEach {
                    it.contactEndpointId ?: return@forEach
                    awalaManager.sendMessage(
                        outgoingMessage = AwalaOutgoingMessage(
                            type = MessageType.ContactPhotoUpdated,
                            content = messageEncoder.encode(
                                photo = file?.content,
                                extension = file?.type?.extension,
                            ),
                    ),
                    recipient = AwalaEndpoint.Private(
                        nodeId = it.contactEndpointId,
                    ),
                    senderAccount = account,
                )
            }
    }
}

    private companion object {
        private val supportedAvatarTypes = ("png, jpeg, jpg, webp")
    }
}

class UnsupportedAvatarFormatException : IllegalStateException("Unsupported avatar type. Supported types: png, jpeg, webp")
