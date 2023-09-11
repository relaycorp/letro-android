package tech.relaycorp.letro.pairing

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

    fun onIdChanged(id: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    id = id,
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
        viewModelScope.launch(Dispatchers.IO) {
            currentAccountId?.let { currentAccountId ->
                contactsRepository.addNewContact(
                    contact = Contact(
                        ownerVeraId = currentAccountId,
                        contactVeraId = uiState.value.id,
                        alias = uiState.value.alias,
                        status = ContactPairingStatus.RequestSent,
                    ),
                )
            }
            _backSignal.emit(Unit)
        }
    }

    companion object {
        const val KEY_CURRENT_ACCOUNT_ID = "current_account_id"
    }
}

data class PairWithOthersUiState(
    val id: String = "",
    val alias: String = "",
)
