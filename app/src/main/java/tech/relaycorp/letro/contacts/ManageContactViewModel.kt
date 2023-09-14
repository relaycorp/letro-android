package tech.relaycorp.letro.contacts

import androidx.annotation.IntDef
import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tech.relaycorp.letro.R
import tech.relaycorp.letro.contacts.ManageContactViewModel.Type.Companion.EDIT_CONTACT
import tech.relaycorp.letro.contacts.ManageContactViewModel.Type.Companion.NEW_CONTACT
import tech.relaycorp.letro.contacts.model.Contact
import tech.relaycorp.letro.contacts.model.ContactPairingStatus
import tech.relaycorp.letro.contacts.storage.ContactsRepository
import tech.relaycorp.letro.ui.navigation.Route
import tech.relaycorp.letro.utils.ext.decodeFromUTF
import tech.relaycorp.letro.utils.ext.nullIfBlankOrEmpty
import javax.inject.Inject

@HiltViewModel
class ManageContactViewModel @Inject constructor(
    private val contactsRepository: ContactsRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    @Type
    private val screenType: Int = savedStateHandle[Route.ManageContact.KEY_SCREEN_TYPE]!!
    private val currentAccountId: String? = (savedStateHandle.get(Route.ManageContact.KEY_CURRENT_ACCOUNT_ID_ENCODED) as? String)?.decodeFromUTF() // by default Android decode strings inside navigation library, but just in case we decode it here
    private val contactIdToEdit: Long? = savedStateHandle[Route.ManageContact.KEY_CONTACT_ID_TO_EDIT]

    private val _uiState = MutableStateFlow(
        PairWithOthersUiState(
            manageContactTexts = when (screenType) {
                NEW_CONTACT -> ManageContactTexts.PairWithOthers()
                EDIT_CONTACT -> ManageContactTexts.EditContact()
                else -> throw IllegalStateException("Unknown screen type: $screenType")
            },
        ),
    )
    val uiState: StateFlow<PairWithOthersUiState>
        get() = _uiState

    private val _onActionCompleted = MutableSharedFlow<String>()
    val onActionCompleted: SharedFlow<String>
        get() = _onActionCompleted

    private val contacts: HashSet<Contact> = hashSetOf()

    private var editingContact: Contact? = null

    init {
        viewModelScope.launch {
            contactIdToEdit?.let { id ->
                contactsRepository.getContactById(id)?.let { contactToEdit ->
                    editingContact = contactToEdit
                    _uiState.update {
                        it.copy(
                            veraId = contactToEdit.contactVeraId,
                            alias = contactToEdit.alias,
                            isVeraIdInputEnabled = false,
                        )
                    }
                }
            }
        }
        viewModelScope.launch {
            currentAccountId?.let { currentAccountId ->
                contactsRepository.getContacts(currentAccountId).collect {
                    contacts.clear()
                    contacts.addAll(it)
                }
            }
        }
    }

    fun onIdChanged(id: String) {
        viewModelScope.launch {
            val trimmedId = id.trim()
            _uiState.update {
                it.copy(
                    veraId = trimmedId,
                    isSentRequestAgainHintVisible = contacts.any { it.contactVeraId == trimmedId && it.status == ContactPairingStatus.REQUEST_SENT },
                    pairingErrorCaption = getPairingErrorMessage(trimmedId),
                )
            }
        }
    }

    fun onAliasChanged(alias: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    alias = alias,
                )
            }
        }
    }

    fun onActionButtonClick() {
        when (screenType) {
            NEW_CONTACT -> sendNewContactRequest()
            EDIT_CONTACT -> updateContact()
            else -> throw IllegalStateException("Unknown screen type: $screenType")
        }
        viewModelScope.launch {
            _onActionCompleted.emit(uiState.value.veraId)
        }
    }

    private fun updateContact() {
        editingContact?.let { editingContact ->
            contactsRepository.updateContact(
                editingContact.copy(
                    alias = uiState.value.alias?.nullIfBlankOrEmpty(),
                ),
            )
        }
    }

    private fun sendNewContactRequest() {
        currentAccountId?.let { currentAccountId ->
            contactsRepository.addNewContact(
                contact = Contact(
                    ownerVeraId = currentAccountId,
                    contactVeraId = uiState.value.veraId,
                    alias = uiState.value.alias?.nullIfBlankOrEmpty(),
                    status = ContactPairingStatus.REQUEST_SENT,
                ),
            )
        }
    }

    private fun getPairingErrorMessage(contactId: String): PairingErrorCaption? {
        val contact = contacts.find { it.contactVeraId == contactId }
        return when {
            contact == null -> null
            contact.status == ContactPairingStatus.COMPLETED -> PairingErrorCaption(R.string.pair_request_already_paired)
            contact.status >= ContactPairingStatus.MATCH -> PairingErrorCaption(R.string.pair_request_already_in_progress)
            else -> null
        }
    }

    @IntDef(NEW_CONTACT, EDIT_CONTACT)
    annotation class Type {
        companion object {
            const val NEW_CONTACT = 0
            const val EDIT_CONTACT = 1
        }
    }
}

data class PairWithOthersUiState(
    val manageContactTexts: ManageContactTexts,
    val veraId: String = "",
    val alias: String? = null,
    val isSentRequestAgainHintVisible: Boolean = false,
    val isVeraIdInputEnabled: Boolean = true,
    val pairingErrorCaption: PairingErrorCaption? = null,
)

@Immutable
sealed class ManageContactTexts(
    @StringRes val title: Int,
    @StringRes val button: Int,
) {
    class PairWithOthers : ManageContactTexts(
        title = R.string.general_pair_with_others,
        button = R.string.onboarding_pair_with_people_button,
    )

    class EditContact : ManageContactTexts(
        title = R.string.edit_name,
        button = R.string.save_changes,
    )
}

data class PairingErrorCaption(
    @StringRes val message: Int,
)
