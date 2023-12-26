package tech.relaycorp.letro.account.manage

import io.mockk.coEvery
import io.mockk.coVerifyAll
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.jupiter.api.Test
import tech.relaycorp.letro.account.manage.avatar.AvatarRepositoryImpl
import tech.relaycorp.letro.account.model.Account
import tech.relaycorp.letro.account.storage.repository.AccountRepository
import tech.relaycorp.letro.awala.AwalaManager
import tech.relaycorp.letro.contacts.storage.repository.ContactsRepository
import tech.relaycorp.letro.conversation.attachments.filepicker.FileManager
import tech.relaycorp.letro.conversation.attachments.filepicker.model.File
import tech.relaycorp.letro.conversation.attachments.filepicker.model.FileType
import tech.relaycorp.letro.utils.models.account.createAccount
import tech.relaycorp.letro.utils.models.contact.createContact
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class AvatarRepositoryTest {

    private val account = createAccount(avatarPath = "old_avatar")
    private val newAvatarUri = "uri"
    private val awalaManager: AwalaManager = mockk(relaxed = true)
    private val accountRepository: AccountRepository = mockk(relaxed = true)
    private val contactsRepository: ContactsRepository = mockk(relaxed = true) {
        every { getContactsSync(account.accountId) } returns listOf(createContact())
    }
    private val fileManager: FileManager = mockk(relaxed = true) {
        coEvery { save(any()) } returns "file_path_on_disk"
    }

    private val avatarRepository = AvatarRepositoryImpl(
        accountRepository = accountRepository,
        contactsRepository = contactsRepository,
        awalaManager = awalaManager,
        fileConverter = mockk(relaxed = true) {
            coEvery { getFile(newAvatarUri) } returns File.FileWithContent(UUID.randomUUID(), "file_name", FileType.Image(""), 0L, ByteArray(0))
        },
        fileManager = fileManager,
        messageEncoder = mockk(relaxed = true) {
            coEvery { encode(any(), any()) } returns ByteArray(0)
        },
        contactsNotifierThread = UnconfinedTestDispatcher(),
    )

    private val scope = CoroutineScope(UnconfinedTestDispatcher())

    @Test
    fun `Test contacts are being notified, and photo updates locally, and old avatar is deleted when avatar is changed`() {
        scope.launch {
            avatarRepository.saveAvatar(account, newAvatarUri)
        }

        coVerifyAll {
            accountRepository.updateAccount(any<Account>())
            fileManager.save(any())
            fileManager.delete("old_avatar")
            awalaManager.sendMessage(any(), any(), any())
        }
    }

    @Test
    fun `Test photo is deleted and contacts are being notified when avatar is being deleted`() {
        scope.launch {
            avatarRepository.deleteAvatar(account)
        }

        coVerifyAll {
            accountRepository.updateAccount(any())
            fileManager.delete("old_avatar")
            awalaManager.sendMessage(any(), any(), any())
        }
    }
}
