package tech.relaycorp.letro.messages.compose

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tech.relaycorp.letro.account.storage.AccountRepository
import tech.relaycorp.letro.contacts.model.Contact
import tech.relaycorp.letro.contacts.model.ContactPairingStatus
import tech.relaycorp.letro.contacts.storage.ContactsRepository
import tech.relaycorp.letro.messages.repository.ConversationsRepository
import tech.relaycorp.letro.utils.ext.emitOn
import tech.relaycorp.letro.utils.ext.isEmptyOrBlank
import tech.relaycorp.letro.utils.ext.isNotEmptyOrBlank
import javax.inject.Inject

@HiltViewModel
class CreateNewMessageViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val contactsRepository: ContactsRepository,
    private val conversationsRepository: ConversationsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(NewMessageUiState())
    val uiState: StateFlow<NewMessageUiState>
        get() = _uiState

    private val contacts = arrayListOf<Contact>()

    private val _messageSentSignal: MutableSharedFlow<Unit> = MutableSharedFlow()
    val messageSentSignal: SharedFlow<Unit>
        get() = _messageSentSignal

    init {
        viewModelScope.launch {
            accountRepository.currentAccount.collect {
                it?.let { account ->
                    _uiState.update { state ->
                        state.copy(
                            sender = account.veraId,
                        )
                    }
                    startCollectingConnectedContacts(account.veraId)
                }
            }
        }
    }

    fun onRecipientTextChanged(text: String) {
        if (text == _uiState.value.recipient) {
            return
        }
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    recipient = text,
                    suggestedContacts = if (text.isEmptyOrBlank()) null else contacts.filter { it.contactVeraId.lowercase().contains(text.lowercase()) || it.alias?.lowercase()?.contains(text.lowercase()) == true },
                    showRecipientIsNotYourContactError = if (text.isEmptyOrBlank()) false else it.showRecipientIsNotYourContactError,
                    isSendButtonEnabled = isSendButtonEnabled(text, it.messageText),
                )
            }
        }
    }

    fun onSuggestClick(contact: Contact) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    recipient = contact.contactVeraId,
                    suggestedContacts = null,
                    showRecipientIsNotYourContactError = false,
                    showRecipientAsChip = true,
                    isSendButtonEnabled = isSendButtonEnabled(contact.contactVeraId, it.messageText),
                )
            }
        }
    }

    fun onRecipientRemoveClick() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    recipient = "",
                    suggestedContacts = null,
                    showRecipientIsNotYourContactError = false,
                    showRecipientAsChip = false,
                    isSendButtonEnabled = false,
                )
            }
        }
    }

    fun onSubjectTextChanged(text: String) {
        viewModelScope.launch {
            val isFromContacts = contacts.any { it.contactVeraId == uiState.value.recipient }
            _uiState.update {
                it.copy(
                    subject = text,
                    showRecipientAsChip = isFromContacts,
                )
            }
        }
    }

    fun onMessageTextChanged(text: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    messageText = text,
                    suggestedContacts = null,
                    isSendButtonEnabled = isSendButtonEnabled(uiState.value.recipient, text),
                )
            }
        }
    }

    fun onSubjectTextFieldFocused(isFocused: Boolean) {
        if (isFocused) {
            updateRecipientIsNotYourContactError()
        }
    }

    fun onMessageTextFieldFocused(isFocused: Boolean) {
        if (isFocused) {
            updateRecipientIsNotYourContactError()
        }
    }

    fun onSendMessageClick() {
        conversationsRepository.createNewConversation(
            ownerVeraId = uiState.value.sender,
            recipientVeraId = uiState.value.recipient,
            messageText = uiState.value.messageText,
            subject = uiState.value.subject,
        )
        _messageSentSignal.emitOn(Unit, viewModelScope)
    }

    private fun updateRecipientIsNotYourContactError() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    showRecipientIsNotYourContactError = !contacts.any { it.contactVeraId == uiState.value.recipient } && uiState.value.recipient.isNotEmptyOrBlank(),
                )
            }
        }
    }

    private fun startCollectingConnectedContacts(ownerVeraId: String) {
        viewModelScope.launch {
            contactsRepository.getContacts(ownerVeraId).collect {
                contacts.clear()
                contacts.addAll(
                    it.filter { it.status == ContactPairingStatus.COMPLETED }
                        .filter { it.status == ContactPairingStatus.COMPLETED }
                        .sortedBy { it.alias?.lowercase() ?: it.contactVeraId.lowercase() },
                )
            }
        }
    }

    private fun isSendButtonEnabled(
        recipient: String,
        messageText: String,
    ): Boolean {
        return contacts.any { recipient == it.contactVeraId } && messageText.isNotEmptyOrBlank()
    }
}

data class NewMessageUiState(
    val sender: String = "",
    val recipient: String = "",
    val subject: String = "",
    val messageText: String = "",
    val showRecipientIsNotYourContactError: Boolean = false,
    val isSendButtonEnabled: Boolean = false,
    val showRecipientAsChip: Boolean = false,
    val suggestedContacts: List<Contact>? = null,
)