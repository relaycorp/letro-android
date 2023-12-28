package tech.relaycorp.letro.utils.models.contact

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import tech.relaycorp.letro.account.storage.repository.AccountRepository
import tech.relaycorp.letro.awala.AwalaManager
import tech.relaycorp.letro.contacts.ContactsViewModel
import tech.relaycorp.letro.contacts.model.Contact
import tech.relaycorp.letro.contacts.model.ContactPairingStatus
import tech.relaycorp.letro.contacts.storage.dao.ContactsDao
import tech.relaycorp.letro.contacts.storage.repository.ContactsRepository
import tech.relaycorp.letro.contacts.storage.repository.ContactsRepositoryImpl
import tech.relaycorp.letro.storage.Preferences
import tech.relaycorp.letro.utils.models.account.createAccountRepository
import tech.relaycorp.letro.utils.models.awala.createAwalaManager
import tech.relaycorp.letro.utils.models.utils.createLogger
import tech.relaycorp.letro.utils.models.utils.dispatchers

fun createContact(
    ownerVeraId: String = "account@test.id",
    contactVeraId: String = "contact@test.id",
    isPrivateEndpoint: Boolean = true,
    alias: String? = null,
    @ContactPairingStatus status: Int = ContactPairingStatus.COMPLETED,
) = Contact(
    ownerVeraId = ownerVeraId,
    contactVeraId = contactVeraId,
    alias = alias,
    contactEndpointId = "",
    status = status,
    isPrivateEndpoint = isPrivateEndpoint,
)

@OptIn(ExperimentalCoroutinesApi::class)
fun createContactsViewModel(
    contactsRepository: ContactsRepository = createContactsRepository(),
    accountRepository: AccountRepository = createAccountRepository(),
) = ContactsViewModel(
    contactsRepository = contactsRepository,
    accountRepository = accountRepository,
    dispatchers = dispatchers(),
)

@ExperimentalCoroutinesApi
fun createContactsRepository(
    contacts: List<Contact> = emptyList(),
    isSentPairRequestOnce: Boolean = contacts.isNotEmpty(),
    accountRepository: AccountRepository = createAccountRepository(emptyList()),
    awalaManager: AwalaManager = createAwalaManager(),
    ioDispatcher: TestDispatcher = UnconfinedTestDispatcher(),
) = ContactsRepositoryImpl(
    contactsDao = mockk<ContactsDao>().also {
        every { it.getAll() } returns flowOf(contacts)
    },
    accountRepository = accountRepository,
    awalaManager = awalaManager,
    preferences = mockk<Preferences>(relaxed = true).also {
        every { it.getBoolean(any(), any()) } returns isSentPairRequestOnce
    },
    logger = createLogger(),
    ioDispatcher = ioDispatcher,
)
