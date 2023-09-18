package tech.relaycorp.letro.contacts.storage

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import tech.relaycorp.letro.account.model.Account
import tech.relaycorp.letro.account.storage.AccountRepository
import tech.relaycorp.letro.awala.AwalaManager
import tech.relaycorp.letro.awala.message.AwalaOutgoingMessage
import tech.relaycorp.letro.awala.message.MessageRecipient
import tech.relaycorp.letro.awala.message.MessageType
import tech.relaycorp.letro.contacts.model.Contact
import tech.relaycorp.letro.contacts.model.ContactPairingStatus
import tech.relaycorp.letro.main.MainViewModel
import tech.relaycorp.letro.storage.Preferences
import javax.inject.Inject

interface ContactsRepository {
    val contactsState: StateFlow<ContactsState>
    fun getContacts(ownerVeraId: String): Flow<List<Contact>>
    fun getContactById(id: Long): Contact?

    fun addNewContact(contact: Contact)
    fun deleteContact(contact: Contact)
    fun updateContact(contact: Contact)
}

class ContactsRepositoryImpl @Inject constructor(
    private val contactsDao: ContactsDao,
    private val accountRepository: AccountRepository,
    private val awalaManager: AwalaManager,
    private val preferences: Preferences,
) : ContactsRepository {

    private val scope = CoroutineScope(Dispatchers.IO)
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

    override fun addNewContact(contact: Contact) {
        scope.launch {
            val existingContact = contactsDao.getContact(
                ownerVeraId = contact.ownerVeraId,
                contactVeraId = contact.contactVeraId,
            )

            if (existingContact == null || existingContact.status <= ContactPairingStatus.REQUEST_SENT) {
                preferences.putBoolean(getContactRequestHasEverBeenSentKey(contact.ownerVeraId), true)
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
                awalaManager.sendMessage(
                    outgoingMessage = AwalaOutgoingMessage(
                        type = MessageType.ContactPairingRequest,
                        content = "${contact.ownerVeraId},${contact.contactVeraId},${awalaManager.getFirstPartyPublicKey()}".toByteArray(),
                    ),
                    recipient = MessageRecipient.Server(),
                )
            }
        }
    }

    override fun deleteContact(contact: Contact) {
        scope.launch {
            contactsDao.deleteContact(contact)
        }
    }

    override fun updateContact(contact: Contact) {
        scope.launch {
            contactsDao.update(contact)
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
        Log.d(MainViewModel.TAG, "ContactsRepository.emit(pairedContactExist)")
        account ?: run {
            _contactsState.emit(ContactsState())
            return
        }
        val isPairedContactExist = contacts
            .value
            .any {
                it.ownerVeraId == account.veraId && it.status == ContactPairingStatus.COMPLETED
            }
        val isPairRequestWasEverSent = preferences.getBoolean(getContactRequestHasEverBeenSentKey(account.veraId), false)
        _contactsState.emit(
            ContactsState(
                isPairedContactExist = isPairedContactExist,
                isPairRequestWasEverSent = isPairRequestWasEverSent,
            )

        )
    }

    private fun getContactRequestHasEverBeenSentKey(
        veraId: String
    ) = "${KEY_CONTACT_REQUEST_HAS_EVER_BEEN_SENT_PREFIX}${veraId}"

    private companion object {
        private const val TAG = "ContactsRepository"
        private const val KEY_CONTACT_REQUEST_HAS_EVER_BEEN_SENT_PREFIX = "contact_request_has_ever_been_sent_"
    }
}

data class ContactsState(
    val isPairedContactExist: Boolean = false,
    val isPairRequestWasEverSent: Boolean = false,
)
