package tech.relaycorp.letro.main

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInfo
import tech.relaycorp.letro.account.model.AccountStatus
import tech.relaycorp.letro.account.storage.repository.AccountRepository
import tech.relaycorp.letro.awala.AwalaManager
import tech.relaycorp.letro.contacts.model.ContactPairingStatus
import tech.relaycorp.letro.contacts.storage.repository.ContactsRepository
import tech.relaycorp.letro.conversation.storage.entity.Conversation
import tech.relaycorp.letro.conversation.storage.entity.Message
import tech.relaycorp.letro.ui.navigation.RootNavigationScreen
import tech.relaycorp.letro.utils.models.AwalaInitializationResult
import tech.relaycorp.letro.utils.models.createAccount
import tech.relaycorp.letro.utils.models.createAccountRepository
import tech.relaycorp.letro.utils.models.createAwalaManager
import tech.relaycorp.letro.utils.models.createContact
import tech.relaycorp.letro.utils.models.createContactsRepository
import tech.relaycorp.letro.utils.models.createConversation
import tech.relaycorp.letro.utils.models.createConversationsRepository
import tech.relaycorp.letro.utils.models.createLogger
import tech.relaycorp.letro.utils.models.createMessage
import java.util.UUID

@ExperimentalCoroutinesApi
class RootNavigationTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    private val dispatcher = UnconfinedTestDispatcher()
    private val logger = createLogger()

    @BeforeEach
    fun setUpEach(testInfo: TestInfo) {
        logger.i(TAG, "\n=========\n\n${testInfo.displayName}:")
    }

    @Test
    fun `Test that Home screen is being opened, if Awala is successfully initialized and contacts is not Empty`() {
        val awalaManager = createAwalaManager(ioDispatcher = dispatcher)
        val accountRepository = createAccountRepository(accounts = listOf(createAccount()))
        val contactsRepository = createContactsRepository(
            contacts = listOf(createContact()),
            isSentPairRequestOnce = true,
            accountRepository = accountRepository,
            awalaManager = awalaManager,
        )
        val viewModel = createViewModel(
            awalaManager = awalaManager,
            accountRepository = accountRepository,
            contactsRepository = contactsRepository,
        )
        viewModel.rootNavigationScreen.value shouldBe RootNavigationScreen.Home
    }

    @Test
    fun `Test that No Contacts screen is being opened, if Awala is successfully initialized and pair request was sent, but not accepted`() {
        val awalaManager = createAwalaManager(ioDispatcher = dispatcher)
        val accountRepository = createAccountRepository(accounts = listOf(createAccount()))
        val contactsRepository = createContactsRepository(
            contacts = listOf(createContact(status = ContactPairingStatus.REQUEST_SENT)),
            isSentPairRequestOnce = true,
            accountRepository = accountRepository,
            awalaManager = awalaManager,
        )
        val viewModel = createViewModel(
            awalaManager = awalaManager,
            accountRepository = accountRepository,
            contactsRepository = contactsRepository,
        )
        viewModel.rootNavigationScreen.value shouldBe RootNavigationScreen.NoContactsScreen
    }

    @Test
    fun `Test that Welcome To Letro screen is being opened, if Awala is successfully initialized, but pair request has not being sent`() {
        val awalaManager = createAwalaManager(ioDispatcher = dispatcher)
        val accountRepository = createAccountRepository(accounts = listOf(createAccount()))
        val contactsRepository = createContactsRepository(
            contacts = emptyList(),
            isSentPairRequestOnce = false,
            accountRepository = accountRepository,
            awalaManager = awalaManager,
        )
        val viewModel = createViewModel(
            awalaManager = awalaManager,
            accountRepository = accountRepository,
            contactsRepository = contactsRepository,
        )
        viewModel.rootNavigationScreen.value shouldBe RootNavigationScreen.WelcomeToLetro
    }

    @Test
    fun `Test that No Contacts screen is being opened, if Awala is successfully initialized, pair request was once sent, but now there are no contacts`() {
        val awalaManager = createAwalaManager(ioDispatcher = dispatcher)
        val accountRepository = createAccountRepository(accounts = listOf(createAccount()))
        val contactsRepository = createContactsRepository(
            contacts = emptyList(),
            isSentPairRequestOnce = true,
            accountRepository = accountRepository,
            awalaManager = awalaManager,
        )
        val viewModel = createViewModel(
            awalaManager = awalaManager,
            accountRepository = accountRepository,
            contactsRepository = contactsRepository,
        )
        viewModel.rootNavigationScreen.value shouldBe RootNavigationScreen.NoContactsScreen
    }

    @Test
    fun `Test that Registration waiting screen is being opened, if Awala is successfully initialized, and account is not created yet (pending creation)`() {
        val awalaManager = createAwalaManager(ioDispatcher = dispatcher)
        val accountRepository = createAccountRepository(
            accounts = listOf(createAccount(status = AccountStatus.CREATION_WAITING)),
        )
        val contactsRepository = createContactsRepository(
            contacts = emptyList(),
            isSentPairRequestOnce = false,
            accountRepository = accountRepository,
            awalaManager = awalaManager,
        )
        val viewModel = createViewModel(
            awalaManager = awalaManager,
            accountRepository = accountRepository,
            contactsRepository = contactsRepository,
        )
        viewModel.rootNavigationScreen.value shouldBe RootNavigationScreen.RegistrationWaiting
    }

    @Test
    fun `Test that Home screen is being opened, if Awala is successfully initialized, contacts is empty, BUT there are some conversations`() {
        val awalaManager = createAwalaManager(ioDispatcher = dispatcher)
        val accountRepository = createAccountRepository(accounts = listOf(createAccount()), ioDispatcher = dispatcher)
        val contactsRepository = createContactsRepository(
            contacts = emptyList(),
            isSentPairRequestOnce = true,
            accountRepository = accountRepository,
            awalaManager = awalaManager,
            ioDispatcher = dispatcher,
        )
        val conversationId = UUID.randomUUID()
        val viewModel = createViewModel(
            awalaManager = awalaManager,
            accountRepository = accountRepository,
            contactsRepository = contactsRepository,
            conversations = listOf(createConversation(conversationId)),
            messages = listOf(createMessage(conversationId)),
        )
        viewModel.rootNavigationScreen.value shouldBe RootNavigationScreen.Home
    }

    @Test
    fun `Test that Awala is not installed screen is being opened, if we couldn't bind gateway`() {
        val awalaManager = createAwalaManager(
            awalaInitializationResult = AwalaInitializationResult.CRASH_ON_GATEAWAY_BINDING,
            ioDispatcher = dispatcher,
        )
        val accountRepository = createAccountRepository()
        val contactsRepository = createContactsRepository(
            accountRepository = accountRepository,
            awalaManager = awalaManager,
        )
        val viewModel = createViewModel(
            awalaManager = awalaManager,
            accountRepository = accountRepository,
            contactsRepository = contactsRepository,
        )
        viewModel.rootNavigationScreen.value shouldBe RootNavigationScreen.AwalaNotInstalled
    }

    @Test
    fun `Test that 'Awala non fatal initialization error' is being opened, if we couldn't register first party endpoint`() {
        val awalaManager = createAwalaManager(
            awalaInitializationResult = AwalaInitializationResult.CRASH_ON_FIRST_PARTY_ENDPOINT_REGISTRATION,
            ioDispatcher = dispatcher,
        )
        val accountRepository = createAccountRepository()
        val contactsRepository = createContactsRepository(
            accountRepository = accountRepository,
            awalaManager = awalaManager,
        )
        val viewModel = createViewModel(
            awalaManager = awalaManager,
            accountRepository = accountRepository,
            contactsRepository = contactsRepository,
        )
        viewModel.rootNavigationScreen.value shouldBe RootNavigationScreen.AwalaInitializationError(isFatal = false)
    }

    @Test
    fun `Test that 'Awala non fatal initialization error' is being opened, if we couldn't register third party endpoint`() {
        val awalaManager = createAwalaManager(
            awalaInitializationResult = AwalaInitializationResult.CRASH_ON_IMPORT_SERVER_THIRD_PARTY_ENDPOINT,
            ioDispatcher = dispatcher,
        )
        val accountRepository = createAccountRepository(ioDispatcher = dispatcher)
        val contactsRepository = createContactsRepository(
            accountRepository = accountRepository,
            awalaManager = awalaManager,
            ioDispatcher = dispatcher,
        )
        val viewModel = createViewModel(
            awalaManager = awalaManager,
            accountRepository = accountRepository,
            contactsRepository = contactsRepository,
        )
        viewModel.rootNavigationScreen.value shouldBe RootNavigationScreen.AwalaInitializationError(isFatal = false)
    }

    @Test
    fun `Test that 'Awala fatal initialization error' is being opened, if we caught android security library exception while setting up Awala`() {
        val awalaManager = createAwalaManager(
            awalaInitializationResult = AwalaInitializationResult.ANDROID_SECURITY_LIBRARY_CRASH,
            ioDispatcher = dispatcher,
        )
        val accountRepository = createAccountRepository()
        val contactsRepository = createContactsRepository(
            accountRepository = accountRepository,
            awalaManager = awalaManager,
        )
        val viewModel = createViewModel(
            awalaManager = awalaManager,
            accountRepository = accountRepository,
            contactsRepository = contactsRepository,
        )
        viewModel.rootNavigationScreen.value shouldBe RootNavigationScreen.AwalaInitializationError(isFatal = true)
    }

    private fun createViewModel(
        awalaManager: AwalaManager,
        accountRepository: AccountRepository,
        contactsRepository: ContactsRepository,
        conversations: List<Conversation> = emptyList(),
        messages: List<Message> = emptyList(),
    ) = MainViewModel(
        awalaManager = awalaManager,
        accountRepository = accountRepository,
        contactsRepository = contactsRepository,
        attachmentsRepository = mockk(),
        fileConverter = mockk(),
        conversationsRepository = createConversationsRepository(
            awalaManager = awalaManager,
            contactsRepository = contactsRepository,
            accountRepository = accountRepository,
            conversations = conversations,
            messages = messages,
        ),
        termsAndConditionsLink = "https://terms_and_conditions",
        logger = mockk(relaxed = true),
    )
    private companion object {
        private const val TAG = "RootNavigationTest"

        @OptIn(ExperimentalCoroutinesApi::class)
        @BeforeAll
        @JvmStatic
        fun setUp() {
            Dispatchers.setMain(UnconfinedTestDispatcher())
        }
    }
}
