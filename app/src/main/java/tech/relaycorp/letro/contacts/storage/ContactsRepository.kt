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
import javax.inject.Inject

interface ContactsRepository {
    val isPairedContactsExist: StateFlow<Boolean>
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
) : ContactsRepository {

    private val scope = CoroutineScope(Dispatchers.IO)
    private val contacts = MutableStateFlow<List<Contact>>(emptyList())

    private var currentAccount: Account? = null
    private val _isPairedContactsExist: MutableStateFlow<Boolean> = MutableStateFlow(false)
    override val isPairedContactsExist: StateFlow<Boolean>
        get() = _isPairedContactsExist

    init {
        scope.launch {
            contactsDao.getAll().collect {
                contacts.emit(it)
                startCollectAccountFlow()
                if (currentAccount != null) {
                    updatePairedContactExist(currentAccount)
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
                updatePairedContactExist(it)
            }
        }
    }

    private suspend fun updatePairedContactExist(account: Account?) {
        Log.d(MainViewModel.TAG, "ContactsRepository.emit(pairedContactExist)")
        account ?: run {
            _isPairedContactsExist.emit(false)
            return
        }
        _isPairedContactsExist.emit(
            contacts
                .value
                .any {
                    it.ownerVeraId == account.veraId && it.status == ContactPairingStatus.COMPLETED
                },
        )
    }

    private companion object {
        private const val TAG = "ContactsRepository"
    }
}
