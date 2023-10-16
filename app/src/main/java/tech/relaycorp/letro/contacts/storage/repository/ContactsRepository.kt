package tech.relaycorp.letro.contacts.storage.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import tech.relaycorp.letro.account.model.Account
import tech.relaycorp.letro.account.storage.repository.AccountRepository
import tech.relaycorp.letro.awala.AwalaManager
import tech.relaycorp.letro.awala.message.AwalaOutgoingMessage
import tech.relaycorp.letro.awala.message.MessageRecipient
import tech.relaycorp.letro.awala.message.MessageType
import tech.relaycorp.letro.contacts.model.Contact
import tech.relaycorp.letro.contacts.model.ContactPairingStatus
import tech.relaycorp.letro.contacts.storage.dao.ContactsDao
import tech.relaycorp.letro.main.MainViewModel
import tech.relaycorp.letro.storage.Preferences
import tech.relaycorp.letro.utils.Logger
import tech.relaycorp.letro.utils.di.IODispatcher
import javax.inject.Inject

interface ContactsRepository {
    val contactsState: StateFlow<ContactsState>
    fun getContacts(ownerVeraId: String): Flow<List<Contact>>
    fun getContactById(id: Long): Contact?

    suspend fun deleteContact(contact: Contact)
    suspend fun addNewContact(contact: Contact)
    suspend fun updateContact(contact: Contact)
    fun saveRequestWasOnceSent()
}

class ContactsRepositoryImpl @Inject constructor(
    private val contactsDao: ContactsDao,
    private val accountRepository: AccountRepository,
    private val awalaManager: AwalaManager,
    private val preferences: Preferences,
    private val logger: Logger,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
) : ContactsRepository {

    private val scope = CoroutineScope(ioDispatcher)
    private val contacts = MutableStateFlow<List<Contact>>(emptyList())

    private var currentAccount: Account? = null
    private val _contactsState: MutableStateFlow<ContactsState> = MutableStateFlow(ContactsState())
    override val contactsState: StateFlow<ContactsState>
        get() = _contactsState

    init {
        scope.launch {
            contactsDao.getAll().collect {
                contacts.emit(it)
                startCollectAccountFlow()
                if (currentAccount != null) {
                    updateContactsState(currentAccount)
                }
            }
        }
    }

    override fun getContacts(ownerVeraId: String): Flow<List<Contact>> {
        return contacts
            .map { it.filter { it.ownerVeraId == ownerVeraId } }
    }

    override fun getContactById(id: Long): Contact? {
        return contacts.value.find { it.id == id }
    }

    override suspend fun addNewContact(contact: Contact) {
        val existingContact = contactsDao.getContact(
            ownerVeraId = contact.ownerVeraId,
            contactVeraId = contact.contactVeraId,
        )

        if (existingContact == null || existingContact.status <= ContactPairingStatus.REQUEST_SENT) {
            if (contact.isPrivateEndpoint) {
                awalaManager.sendMessage(
                    outgoingMessage = AwalaOutgoingMessage(
                        type = MessageType.ContactPairingRequest,
                        content = "${contact.ownerVeraId},${contact.contactVeraId},${awalaManager.getFirstPartyPublicKey()}".toByteArray(),
                    ),
                    recipient = MessageRecipient.Server(),
                )
            } else {
                awalaManager.sendMessage(
                    outgoingMessage = AwalaOutgoingMessage(
                        type = MessageType.ConnectionParamsRequest,
                        content = contact.contactVeraId.toByteArray(),
                    ),
                    recipient = MessageRecipient.Server(),
                )
            }
            if (existingContact == null) {
                contactsDao.insert(
                    contact.copy(
                        status = ContactPairingStatus.REQUEST_SENT,
                    ),
                )
            } else {
                contactsDao.update(
                    contact.copy(
                        status = ContactPairingStatus.REQUEST_SENT,
                    ),
                )
            }
        }
    }

    override suspend fun deleteContact(contact: Contact) {
        contact.contactEndpointId?.let {
            awalaManager.revokeAuthorization(
                MessageRecipient.User(contact.contactEndpointId),
            )
        }
        contactsDao.deleteContact(contact)
    }

    override suspend fun updateContact(contact: Contact) {
        contactsDao.update(contact)
    }

    override fun saveRequestWasOnceSent() {
        val currentAccount = currentAccount ?: return
        scope.launch {
            preferences.putBoolean(getContactRequestHasEverBeenSentKey(currentAccount.accountId), true)
            updateContactsState(currentAccount)
        }
    }

    private fun startCollectAccountFlow() {
        scope.launch {
            accountRepository.currentAccount.collect {
                currentAccount = it
                updateContactsState(it)
            }
        }
    }

    private suspend fun updateContactsState(account: Account?) {
        logger.d(MainViewModel.TAG, "ContactsRepository.emit(pairedContactExist)")
        account ?: run {
            _contactsState.emit(ContactsState())
            return
        }
        val isPairedContactExist = contacts
            .value
            .any {
                it.ownerVeraId == account.accountId && it.status == ContactPairingStatus.COMPLETED
            }
        val isPairRequestWasEverSent = preferences.getBoolean(getContactRequestHasEverBeenSentKey(account.accountId), false)
        _contactsState.emit(
            ContactsState(
                isPairedContactExist = isPairedContactExist,
                isPairRequestWasEverSent = isPairRequestWasEverSent,
            ),
        )
    }

    private fun getContactRequestHasEverBeenSentKey(
        accountId: String,
    ) = "$KEY_CONTACT_REQUEST_HAS_EVER_BEEN_SENT_PREFIX$accountId"

    private companion object {
        private const val TAG = "ContactsRepository"
        private const val KEY_CONTACT_REQUEST_HAS_EVER_BEEN_SENT_PREFIX = "contact_request_has_ever_been_sent_"
    }
}

data class ContactsState(
    val isPairedContactExist: Boolean = false,
    val isPairRequestWasEverSent: Boolean = false,
)
