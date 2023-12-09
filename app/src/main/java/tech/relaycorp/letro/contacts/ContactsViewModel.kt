package tech.relaycorp.letro.contacts

import android.util.Log
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tech.relaycorp.awaladroid.AwaladroidException
import tech.relaycorp.letro.R
import tech.relaycorp.letro.account.model.Account
import tech.relaycorp.letro.account.storage.repository.AccountRepository
import tech.relaycorp.letro.base.BaseViewModel
import tech.relaycorp.letro.base.utils.SnackbarString
import tech.relaycorp.letro.contacts.model.Contact
import tech.relaycorp.letro.contacts.model.ContactPairingStatus
import tech.relaycorp.letro.contacts.storage.repository.ContactsRepository
import tech.relaycorp.letro.contacts.ui.ContactsListContent
import tech.relaycorp.letro.ui.common.bottomsheet.BottomSheetAction
import tech.relaycorp.letro.ui.utils.SnackbarStringsProvider
import tech.relaycorp.letro.utils.coroutines.Dispatchers
import tech.relaycorp.letro.utils.ext.emitOn
import javax.inject.Inject

@HiltViewModel
class ContactsViewModel @Inject constructor(
    private val contactsRepository: ContactsRepository,
    private val accountRepository: AccountRepository,
    dispatchers: Dispatchers,
) : BaseViewModel(dispatchers) {

    private val _contacts: MutableStateFlow<ContactsListContent> = MutableStateFlow(ContactsListContent.Empty)
    val contacts: StateFlow<ContactsListContent>
        get() = _contacts

    private val _contactActionsBottomSheetStateState = MutableStateFlow(ContactActionsBottomSheetState())
    val contactActionsBottomSheetState: StateFlow<ContactActionsBottomSheetState>
        get() = _contactActionsBottomSheetStateState

    private val _deleteContactDialogStateState = MutableStateFlow(DeleteContactDialogState())
    val deleteContactDialogState: StateFlow<DeleteContactDialogState>
        get() = _deleteContactDialogStateState

    private val _showContactDeletedSnackbarSignal = MutableSharedFlow<Unit>()
    val showContactDeletedSnackbarSignal: SharedFlow<Unit>
        get() = _showContactDeletedSnackbarSignal

    private val _openConversationSignal = MutableSharedFlow<Contact>()
    val openConversationSignal: SharedFlow<Contact>
        get() = _openConversationSignal

    private val _editContactSignal = MutableSharedFlow<Contact>()
    val editContactSignal: SharedFlow<Contact>
        get() = _editContactSignal

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
            val actions = getContactBottomSheetActions(contact)
            _contactActionsBottomSheetStateState.update {
                it.copy(
                    isShown = true,
                    data = ContactActionsBottomSheet(
                        title = contact.alias ?: contact.contactVeraId,
                        actions = actions,
                    ),
                )
            }
        }
    }

    fun onActionsBottomSheetDismissed() {
        closeActionsBottomSheet()
    }

    fun onDeleteContactDialogDismissed() {
        closeDeleteContactDialog()
    }

    fun onConfirmDeletingContactClick(contact: Contact) {
        viewModelScope.launch(dispatchers.IO) {
            closeDeleteContactDialog()
            try {
                contactsRepository.deleteContact(contact)
                _showContactDeletedSnackbarSignal.emit(Unit)
            } catch (e: AwaladroidException) {
                Log.w(TAG, e)
                showSnackbarDebounced.emit(
                    SnackbarString(SnackbarStringsProvider.Type.SEND_MESSAGE_ERROR),
                )
            }
        }
    }

    private fun onEditContactClick(contact: Contact) {
        closeActionsBottomSheet()
        _editContactSignal.emitOn(contact, viewModelScope)
    }

    private fun onDeleteContactClick(contact: Contact) {
        closeActionsBottomSheet()
        viewModelScope.launch {
            _deleteContactDialogStateState.update {
                it.copy(
                    isShown = true,
                    contact = contact,
                )
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

    private fun closeActionsBottomSheet() {
        viewModelScope.launch {
            _contactActionsBottomSheetStateState.update {
                it.copy(
                    isShown = false,
                    data = null,
                )
            }
        }
    }

    private fun openConversation(contact: Contact) {
        closeActionsBottomSheet()
        _openConversationSignal.emitOn(contact, viewModelScope)
    }

    private fun observeContacts(account: Account?) {
        contactsCollectionJob?.cancel()
        contactsCollectionJob = null
        if (account != null) {
            contactsCollectionJob = viewModelScope.launch {
                contactsRepository.getContacts(account.accountId).collect {
                    if (it.isEmpty()) {
                        _contacts.emit(ContactsListContent.Empty)
                    } else {
                        _contacts.emit(ContactsListContent.Contacts(it))
                    }
                }
            }
        }
    }

    private fun getContactBottomSheetActions(contact: Contact) = arrayListOf<BottomSheetAction>().apply {
        if (contact.status == ContactPairingStatus.COMPLETED) {
            add(
                BottomSheetAction(
                    icon = R.drawable.ic_mail_24,
                    title = R.string.start_a_conversation,
                    action = { openConversation(contact) },
                ),
            )
        }
        add(
            BottomSheetAction(
                icon = R.drawable.edit,
                title = R.string.edit,
                action = { onEditContactClick(contact) },
            ),
        )
        add(
            BottomSheetAction(
                icon = R.drawable.ic_delete,
                title = R.string.delete,
                action = { onDeleteContactClick(contact) },
            ),
        )
    }
}

data class ContactActionsBottomSheetState(
    val isShown: Boolean = false,
    val data: ContactActionsBottomSheet? = null,
)

data class ContactActionsBottomSheet(
    val title: String,
    val actions: List<BottomSheetAction>,
)

data class DeleteContactDialogState(
    val isShown: Boolean = false,
    val contact: Contact? = null,
)

private const val TAG = "ContactsViewModel"
