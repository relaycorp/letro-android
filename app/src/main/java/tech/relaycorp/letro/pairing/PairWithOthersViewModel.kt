package tech.relaycorp.letro.pairing

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tech.relaycorp.letro.R
import tech.relaycorp.letro.contacts.model.Contact
import tech.relaycorp.letro.contacts.model.ContactPairingStatus
import tech.relaycorp.letro.contacts.storage.ContactsRepository
import javax.inject.Inject

@HiltViewModel
class PairWithOthersViewModel @Inject constructor(
    private val contactsRepository: ContactsRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val currentAccountId: String? = savedStateHandle[KEY_CURRENT_ACCOUNT_ID]

    private val _uiState = MutableStateFlow(PairWithOthersUiState())
    val uiState: StateFlow<PairWithOthersUiState>
        get() = _uiState

    private val _backSignal = MutableSharedFlow<Unit>()
    val backSignal: SharedFlow<Unit>
        get() = _backSignal

    private val contacts: HashSet<Contact> = hashSetOf()

    init {
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
                    id = trimmedId,
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

    fun onPairRequestClick() {
        currentAccountId?.let { currentAccountId ->
            contactsRepository.addNewContact(
                contact = Contact(
                    ownerVeraId = currentAccountId,
                    contactVeraId = uiState.value.id,
                    alias = uiState.value.alias,
                    status = ContactPairingStatus.REQUEST_SENT,
                ),
            )
        }
        viewModelScope.launch(Dispatchers.IO) {
            _backSignal.emit(Unit)
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

    companion object {
        const val KEY_CURRENT_ACCOUNT_ID = "current_account_id"
    }
}

data class PairWithOthersUiState(
    val id: String = "",
    val alias: String = "",
    val isSentRequestAgainHintVisible: Boolean = false,
    val pairingErrorCaption: PairingErrorCaption? = null,
)

data class PairingErrorCaption(
    @StringRes val message: Int,
)
