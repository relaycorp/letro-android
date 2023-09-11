package tech.relaycorp.letro.contacts.storage

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import tech.relaycorp.letro.account.model.Account
import tech.relaycorp.letro.account.storage.AccountRepository
import tech.relaycorp.letro.contacts.model.Contact
import tech.relaycorp.letro.contacts.model.ContactPairingStatus
import javax.inject.Inject

interface ContactsRepository {
    val isPairedContactsExist: Flow<Boolean>
    fun getContacts(ownerVeraId: String): Flow<List<Contact>>
}

class ContactsRepositoryImpl @Inject constructor(
    private val contactsDao: ContactsDao,
    private val accountRepository: AccountRepository,
) : ContactsRepository {

    private val scope = CoroutineScope(Dispatchers.IO)
    private val contacts = MutableStateFlow<List<Contact>>(emptyList())

    private val _isPairedContactsExist: MutableStateFlow<Boolean> = MutableStateFlow(false)
    override val isPairedContactsExist: StateFlow<Boolean>
        get() = _isPairedContactsExist

    init {
        scope.launch {
            contactsDao.getAll().collect {
                contacts.emit(it)
                startCollectAccountFlow()
            }
        }
    }

    override fun getContacts(ownerVeraId: String): Flow<List<Contact>> {
        return contacts
            .map { it.filter { it.ownerVeraId == ownerVeraId } }
    }

    private fun startCollectAccountFlow() {
        scope.launch {
            accountRepository.currentAccount.collect {
                updatePairedContactExist(it)
            }
        }
    }

    private suspend fun updatePairedContactExist(account: Account?) {
        account ?: run {
            _isPairedContactsExist.emit(false)
            return
        }
        _isPairedContactsExist.emit(
            contacts
                .value
                .any {
                    it.ownerVeraId == account.veraId &&
                        it.status == ContactPairingStatus.Complete
                },
        )
    }

    private companion object {
        private const val TAG = "ContactsRepository"
    }
}
