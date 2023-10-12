package tech.relaycorp.letro.contacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tech.relaycorp.awaladroid.AwaladroidException
import tech.relaycorp.letro.account.model.Account
import tech.relaycorp.letro.account.storage.repository.AccountRepository
import tech.relaycorp.letro.contacts.model.Contact
import tech.relaycorp.letro.contacts.model.ContactPairingStatus
import tech.relaycorp.letro.contacts.storage.repository.ContactsRepository
import tech.relaycorp.letro.ui.utils.SnackbarStringsProvider
import javax.inject.Inject

@HiltViewModel
class ContactsViewModel @Inject constructor(
    private val contactsRepository: ContactsRepository,
    private val accountRepository: AccountRepository,
) : ViewModel() {

    private val _contacts: MutableStateFlow<List<Contact>> = MutableStateFlow(emptyList())
    val contacts: StateFlow<List<Contact>>
        get() = _contacts

    private val _editContactBottomSheetStateState = MutableStateFlow(EditContactBottomSheetState())
    val editContactBottomSheetState: StateFlow<EditContactBottomSheetState>
        get() = _editContactBottomSheetStateState

    private val _deleteContactDialogStateState = MutableStateFlow(DeleteContactDialogState())
    val deleteContactDialogState: StateFlow<DeleteContactDialogState>
        get() = _deleteContactDialogStateState

    private val _showContactDeletedSnackbarSignal = MutableSharedFlow<Unit>()
    val showContactDeletedSnackbarSignal: SharedFlow<Unit>
        get() = _showContactDeletedSnackbarSignal

    private val _showSnackbar = MutableSharedFlow<Int>()
    val showSnackbar: SharedFlow<Int>
        get() = _showSnackbar

    private var contactsCollectionJob: Job? = null

    init {
        viewModelScope.launch {
            accountRepository.currentAccount.collect {
                observeContacts(it)
            }
        }
    }

    fun onActionsButtonClick(contact: Contact) {
        viewModelScope.launch {
            _editContactBottomSheetStateState.update {
                it.copy(
                    isShown = true,
                    contact = contact,
                )
            }
        }
    }

    fun onEditBottomSheetDismissed() {
        closeEditBottomSheet()
    }

    fun onEditContactClick() {
        closeEditBottomSheet()
    }

    fun onDeleteContactDialogDismissed() {
        closeDeleteContactDialog()
    }

    fun onDeleteContactClick(contact: Contact) {
        closeEditBottomSheet()
        viewModelScope.launch {
            _deleteContactDialogStateState.update {
                it.copy(
                    isShown = true,
                    contact = contact,
                )
            }
        }
    }

    fun onConfirmDeletingContactClick(contact: Contact) {
        viewModelScope.launch(Dispatchers.IO) {
            closeDeleteContactDialog()
            try {
                contactsRepository.deleteContact(contact)
                _showContactDeletedSnackbarSignal.emit(Unit)
            } catch (e: AwaladroidException) {
                _showSnackbar.emit(SnackbarStringsProvider.Type.SEND_MESSAGE_ERROR)
            }
        }
    }

    private fun closeDeleteContactDialog() {
        viewModelScope.launch {
            _deleteContactDialogStateState.update {
                it.copy(
                    isShown = false,
                    contact = null,
                )
            }
        }
    }

    private fun closeEditBottomSheet() {
        viewModelScope.launch {
            _editContactBottomSheetStateState.update {
                it.copy(
                    isShown = false,
                    contact = null,
                )
            }
        }
    }

    private fun observeContacts(account: Account?) {
        contactsCollectionJob?.cancel()
        contactsCollectionJob = null
        if (account != null) {
            contactsCollectionJob = viewModelScope.launch {
                contactsRepository.getContacts(account.accountId).collect {
                    _contacts.emit(it.filter { it.status == ContactPairingStatus.COMPLETED })
                }
            }
        }
    }
}

data class EditContactBottomSheetState(
    val isShown: Boolean = false,
    val contact: Contact? = null,
)

data class DeleteContactDialogState(
    val isShown: Boolean = false,
    val contact: Contact? = null,
)
