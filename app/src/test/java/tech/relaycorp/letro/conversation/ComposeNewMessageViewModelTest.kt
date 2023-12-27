package tech.relaycorp.letro.conversation

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import tech.relaycorp.letro.account.model.Account
import tech.relaycorp.letro.account.model.AccountStatus
import tech.relaycorp.letro.account.model.AccountType
import tech.relaycorp.letro.contacts.model.Contact
import tech.relaycorp.letro.contacts.model.ContactPairingStatus
import tech.relaycorp.letro.conversation.attachments.filepicker.model.File
import tech.relaycorp.letro.conversation.attachments.filepicker.model.FileType
import tech.relaycorp.letro.conversation.compose.ComposeNewMessageViewModel
import tech.relaycorp.letro.utils.models.account.createAccountRepository
import tech.relaycorp.letro.utils.models.contact.createContactsRepository
import tech.relaycorp.letro.utils.models.conversation.createComposeNewMessageViewModel
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class ComposeNewMessageViewModelTest {

    private val contactId = "contact@vera.id"
    private lateinit var viewModel: ComposeNewMessageViewModel

    @BeforeEach
    fun setUp() {
        viewModel = createComposeNewMessageViewModel(
            accountRepository = createAccountRepository(
                accounts = listOf(
                    Account(
                        accountId = "owner@vera.id",
                        requestedUserName = "owner",
                        normalisedLocale = "en_us",
                        domain = "vera.id",
                        isCurrent = true,
                        veraidPrivateKey = ByteArray(0),
                        status = AccountStatus.CREATED,
                        accountType = AccountType.CREATED_FROM_SCRATCH,
                        firstPartyEndpointNodeId = "",
                        thirdPartyServerEndpointNodeId = "",
                    ),
                ),
            ),
            contactsRepository = createContactsRepository(
                contacts = listOf(
                    Contact(
                        ownerVeraId = "owner@vera.id",
                        contactVeraId = contactId,
                        isPrivateEndpoint = true,
                        status = ContactPairingStatus.COMPLETED,
                    ),
                ),
            ),
            fileConverter = mockk() {
                coEvery {
                    getFile(any<String>())
                } answers {
                    File.FileWithContent(
                        id = UUID.randomUUID(),
                        name = "file_name",
                        type = FileType.Image(""),
                        size = 6_000_000L,
                        content = ByteArray(0),
                    )
                }
            },
        )
    }

    @Test
    fun `Test that file is being attached`() {
        viewModel.attachments.value.size shouldBe 0

        viewModel.onFilePickerResult("uri")
        viewModel.attachments.value.size shouldBe 1
    }

    @Test
    fun `Test that if file is attached, then message can not be sent, because there are no recipient`() {
        viewModel.onFilePickerResult("uri")

        viewModel.attachments.value.size shouldBe 1
        viewModel.uiState.value.isSendButtonEnabled shouldBe false
    }

    @Test
    fun `Test that if file is attached, and there is a recipient, send button is enabled`() {
        viewModel.onFilePickerResult("file://file.txt")

        viewModel.attachments.value.size shouldBe 1
        viewModel.uiState.value.isSendButtonEnabled shouldBe false

        viewModel.onRecipientTextChanged(contactId)
        viewModel.uiState.value.isSendButtonEnabled shouldBe true
    }

    @Test
    fun `Test that send button is not enabled if message size exceeds limit`() {
        viewModel.onFilePickerResult("file://file.txt")

        viewModel.attachments.value.size shouldBe 1
        viewModel.uiState.value.isSendButtonEnabled shouldBe false

        viewModel.onRecipientTextChanged(contactId)
        viewModel.uiState.value.isSendButtonEnabled shouldBe true

        viewModel.onFilePickerResult("file://file2.txt")
        viewModel.attachments.value.size shouldBe 2
        viewModel.uiState.value.isSendButtonEnabled shouldBe false
    }
}
