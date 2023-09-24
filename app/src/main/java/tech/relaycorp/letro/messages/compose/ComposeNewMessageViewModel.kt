package tech.relaycorp.letro.messages.compose

import androidx.annotation.IntDef
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
import tech.relaycorp.letro.account.storage.AccountRepository
import tech.relaycorp.letro.contacts.model.Contact
import tech.relaycorp.letro.contacts.model.ContactPairingStatus
import tech.relaycorp.letro.contacts.storage.ContactsRepository
import tech.relaycorp.letro.messages.compose.CreateNewMessageViewModel.ScreenType.Companion.NEW_CONVERSATION
import tech.relaycorp.letro.messages.compose.CreateNewMessageViewModel.ScreenType.Companion.REPLY_TO_EXISTING_CONVERSATION
import tech.relaycorp.letro.messages.model.ExtendedConversation
import tech.relaycorp.letro.messages.repository.ConversationsRepository
import tech.relaycorp.letro.ui.navigation.Route
import tech.relaycorp.letro.utils.ext.emitOn
import tech.relaycorp.letro.utils.ext.isEmptyOrBlank
import tech.relaycorp.letro.utils.ext.isNotEmptyOrBlank
import javax.inject.Inject

@HiltViewModel
class CreateNewMessageViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val contactsRepository: ContactsRepository,
    private val conversationsRepository: ConversationsRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    @ScreenType
    private val screenType: Int = savedStateHandle[Route.CreateNewMessage.KEY_SCREEN_TYPE]!!
    private val conversation: ExtendedConversation? =
        (savedStateHandle.get(Route.CreateNewMessage.KEY_CONVERSATION_ID) as? String)
            ?.let { conversationsRepository.getConversation(it) }

    private val _uiState = MutableStateFlow(
        NewMessageUiState(
            sender = conversation?.ownerVeraId ?: "",
            recipient = conversation?.contactVeraId ?: "",
            subject = conversation?.subject ?: "",
            showNoSubjectText = conversation != null && conversation.subject.isNullOrEmpty(),
            showRecipientAsChip = conversation != null,
            isOnlyTextEditale = conversation != null,
        ),
    )
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
        val contact = contacts.find { it.contactVeraId == uiState.value.recipient } ?: return
        when (screenType) {
            NEW_CONVERSATION -> {
                conversationsRepository.createNewConversation(
                    ownerVeraId = uiState.value.sender,
                    recipient = contact,
                    messageText = uiState.value.messageText,
                    subject = uiState.value.subject,
                )
            }
            REPLY_TO_EXISTING_CONVERSATION -> {
                val conversation = conversation ?: return
                conversationsRepository.reply(
                    conversationId = conversation.conversationId,
                    messageText = uiState.value.messageText,
                )
            }
            else -> {
                throw IllegalStateException("Unknown screen type $screenType!")
            }
        }
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

    @IntDef(NEW_CONVERSATION, REPLY_TO_EXISTING_CONVERSATION)
    annotation class ScreenType {
        companion object {
            const val NEW_CONVERSATION = 0
            const val REPLY_TO_EXISTING_CONVERSATION = 1
        }
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
    val isOnlyTextEditale: Boolean = false,
    val showNoSubjectText: Boolean = false,
    val suggestedContacts: List<Contact>? = null,
)
