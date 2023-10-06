package tech.relaycorp.letro.utils.models

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import tech.relaycorp.letro.account.storage.repository.AccountRepository
import tech.relaycorp.letro.awala.AwalaManager
import tech.relaycorp.letro.contacts.model.Contact
import tech.relaycorp.letro.contacts.model.ContactPairingStatus
import tech.relaycorp.letro.contacts.storage.dao.ContactsDao
import tech.relaycorp.letro.contacts.storage.repository.ContactsRepositoryImpl
import tech.relaycorp.letro.storage.Preferences

fun createContact(
    ownerVeraId: String = "account@test.id",
    contactVeraId: String = "contact@test.id",
    alias: String? = null,
    @ContactPairingStatus status: Int = ContactPairingStatus.COMPLETED,
) = Contact(
    ownerVeraId = ownerVeraId,
    contactVeraId = contactVeraId,
    alias = alias,
    contactEndpointId = "",
    status = status,
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
    preferences = mockk<Preferences>().also {
        every { it.getBoolean(any(), any()) } returns isSentPairRequestOnce
    },
    logger = createLogger(),
    ioDispatcher = ioDispatcher,
)
