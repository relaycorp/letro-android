package tech.relaycorp.letro.utils.models.main

import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import tech.relaycorp.letro.account.storage.repository.AccountRepository
import tech.relaycorp.letro.awala.AwalaManager
import tech.relaycorp.letro.contacts.storage.repository.ContactsRepository
import tech.relaycorp.letro.conversation.storage.entity.Conversation
import tech.relaycorp.letro.conversation.storage.entity.Message
import tech.relaycorp.letro.main.MainViewModel
import tech.relaycorp.letro.utils.coroutines.Dispatchers
import tech.relaycorp.letro.utils.models.account.createAccountRepository
import tech.relaycorp.letro.utils.models.awala.createAwalaManager
import tech.relaycorp.letro.utils.models.contact.createContactsRepository
import tech.relaycorp.letro.utils.models.conversation.createConversationsRepository
import tech.relaycorp.letro.utils.models.utils.createLogger
import tech.relaycorp.letro.utils.models.utils.dispatchers

@OptIn(ExperimentalCoroutinesApi::class)
fun createMainViewModel(
    awalaManager: AwalaManager = createAwalaManager(),
    accountRepository: AccountRepository = createAccountRepository(),
    contactsRepository: ContactsRepository = createContactsRepository(),
    dispatchers: Dispatchers = dispatchers(),
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
    logger = createLogger(),
    uriToActionConverter = mockk(),
    rootNavigationDebounceMs = 0L,
    dispatchers = dispatchers,
    actionProcessorThread = UnconfinedTestDispatcher(),
)
