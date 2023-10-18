package tech.relaycorp.letro.conversation.compose

import android.net.Uri
import android.util.Log
import androidx.annotation.IntDef
import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tech.relaycorp.awaladroid.AwaladroidException
import tech.relaycorp.letro.R
import tech.relaycorp.letro.account.BaseViewModel
import tech.relaycorp.letro.account.storage.repository.AccountRepository
import tech.relaycorp.letro.contacts.model.Contact
import tech.relaycorp.letro.contacts.model.ContactPairingStatus
import tech.relaycorp.letro.contacts.storage.repository.ContactsRepository
import tech.relaycorp.letro.conversation.attachments.filepicker.FileConverter
import tech.relaycorp.letro.conversation.attachments.filepicker.model.File
import tech.relaycorp.letro.conversation.attachments.ui.AttachmentInfo
import tech.relaycorp.letro.conversation.attachments.utils.AttachmentInfoConverter
import tech.relaycorp.letro.conversation.compose.ComposeNewMessageViewModel.ScreenType.Companion.NEW_CONVERSATION
import tech.relaycorp.letro.conversation.compose.ComposeNewMessageViewModel.ScreenType.Companion.REPLY_TO_EXISTING_CONVERSATION
import tech.relaycorp.letro.conversation.model.ExtendedConversation
import tech.relaycorp.letro.conversation.storage.repository.ConversationsRepository
import tech.relaycorp.letro.ui.navigation.Route
import tech.relaycorp.letro.ui.utils.SnackbarStringsProvider
import tech.relaycorp.letro.utils.ext.emitOn
import tech.relaycorp.letro.utils.ext.isEmptyOrBlank
import tech.relaycorp.letro.utils.ext.isNotEmptyOrBlank
import tech.relaycorp.letro.utils.files.bytesToKb
import tech.relaycorp.letro.utils.files.bytesToMb
import tech.relaycorp.letro.utils.files.isMoreThanKilobyte
import tech.relaycorp.letro.utils.files.isMoreThanMegabyte
import javax.inject.Inject

@HiltViewModel
class ComposeNewMessageViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val contactsRepository: ContactsRepository,
    private val conversationsRepository: ConversationsRepository,
    private val fileConverter: FileConverter,
    private val attachmentInfoConverter: AttachmentInfoConverter,
    private val savedStateHandle: SavedStateHandle,
) : BaseViewModel() {

    @ScreenType
    private val screenType: Int = savedStateHandle[Route.CreateNewMessage.KEY_SCREEN_TYPE]!!
    private val conversation: ExtendedConversation? =
        (savedStateHandle.get(Route.CreateNewMessage.KEY_CONVERSATION_ID) as? String)
            ?.let { conversationsRepository.getConversation(it) }

    private val _uiState = MutableStateFlow(
        NewMessageUiState(
            sender = conversation?.ownerVeraId ?: "",
            recipientDisplayedText = conversation?.contactDisplayName ?: "",
            recipientAccountId = conversation?.contactVeraId ?: "",
            subject = conversation?.subject ?: "",
            showNoSubjectText = conversation != null && conversation.subject.isNullOrEmpty(),
            showRecipientAsChip = conversation != null,
            isOnlyTextEditale = conversation != null,
            isSendButtonEnabled = conversation?.contactVeraId != null,
        ),
    )
    val uiState: StateFlow<NewMessageUiState>
        get() = _uiState

    private val contacts = arrayListOf<Contact>()

    private val _messageSentSignal: MutableSharedFlow<Unit> = MutableSharedFlow()
    val messageSentSignal: SharedFlow<Unit>
        get() = _messageSentSignal

    private val _goBackSignal: MutableSharedFlow<Unit> = MutableSharedFlow()
    val goBackSignal: SharedFlow<Unit>
        get() = _goBackSignal

    private val attachedFiles = arrayListOf<File.FileWithContent>()
    private val _attachments: MutableStateFlow<List<AttachmentInfo>> = MutableStateFlow(emptyList())
    val attachments: StateFlow<List<AttachmentInfo>>
        get() = _attachments

    init {
        viewModelScope.launch {
            accountRepository.currentAccount.collect {
                it?.let { account ->
                    _uiState.update { state ->
                        state.copy(
                            sender = account.accountId,
                        )
                    }
                    startCollectingConnectedContacts(account.accountId)
                }
            }
        }
    }

    fun onFilePickerResult(uri: Uri?) {
        uri ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val file = fileConverter.getFile(uri) ?: return@launch
            attachedFiles.add(file)
            _attachments.update {
                ArrayList(it).apply {
                    add(attachmentInfoConverter.convert(file))
                }
            }
            _uiState.update {
                it.copy(
                    isSendButtonEnabled = isSendButtonEnabled(uiState.value.recipientAccountId, it.messageText),
                    messageExceedsLimitTextError = getMessageExceedsLimitError(it.messageText),
                )
            }
        }
    }

    fun onAttachmentDeleteClick(attachmentInfo: AttachmentInfo) {
        viewModelScope.launch {
            attachedFiles.removeAll { it.id == attachmentInfo.fileId }
            _attachments.update {
                it.filter { it.fileId != attachmentInfo.fileId }
            }
            _uiState.update {
                it.copy(
                    isSendButtonEnabled = isSendButtonEnabled(uiState.value.recipientAccountId, it.messageText),
                    messageExceedsLimitTextError = getMessageExceedsLimitError(it.messageText),
                )
            }
        }
    }

    fun onRecipientTextChanged(text: String) {
        if (text == _uiState.value.recipientDisplayedText) {
            return
        }
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    recipientDisplayedText = text,
                    recipientAccountId = text,
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
                    recipientDisplayedText = contact.alias ?: contact.contactVeraId,
                    recipientAccountId = contact.contactVeraId,
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
                    recipientDisplayedText = "",
                    recipientAccountId = "",
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
            val isFromContacts = contacts.any { it.contactVeraId == uiState.value.recipientAccountId }
            _uiState.update {
                it.copy(
                    subject = text,
                    showRecipientAsChip = isFromContacts,
                )
            }
        }
    }

    fun onMessageTextChanged(text: String) {
        if (text == _uiState.value.messageText) {
            return
        }
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    messageText = text,
                    suggestedContacts = null,
                    isSendButtonEnabled = isSendButtonEnabled(uiState.value.recipientAccountId, text),
                    messageExceedsLimitTextError = getMessageExceedsLimitError(text),
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
        val contact = contacts.find { it.contactVeraId == uiState.value.recipientAccountId } ?: return
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isSendingMessage = true) }
            when (screenType) {
                NEW_CONVERSATION -> {
                    try {
                        conversationsRepository.createNewConversation(
                            ownerVeraId = uiState.value.sender,
                            recipient = contact,
                            messageText = uiState.value.messageText,
                            subject = uiState.value.subject,
                            attachments = attachedFiles,
                        )
                        _messageSentSignal.emitOn(Unit, viewModelScope)
                    } catch (e: AwaladroidException) {
                        Log.w(TAG, e)
                        _showSnackbar.emit(SnackbarStringsProvider.Type.SEND_MESSAGE_ERROR)
                    } finally {
                        _uiState.update { it.copy(isSendingMessage = false) }
                    }
                }
                REPLY_TO_EXISTING_CONVERSATION -> {
                    try {
                        val conversation = conversation ?: return@launch
                        conversationsRepository.reply(
                            conversationId = conversation.conversationId,
                            messageText = uiState.value.messageText,
                            attachments = attachedFiles,
                        )
                        _messageSentSignal.emitOn(Unit, viewModelScope)
                    } catch (e: AwaladroidException) {
                        Log.w(TAG, e)
                        _showSnackbar.emit(SnackbarStringsProvider.Type.SEND_MESSAGE_ERROR)
                    } finally {
                        _uiState.update { it.copy(isSendingMessage = false) }
                    }
                }
                else -> {
                    throw IllegalStateException("Unknown screen type $screenType!")
                }
            }
        }
    }

    fun onBackPressed() {
        val uiState = _uiState.value
        if (uiState.messageText.isNotEmptyOrBlank() || attachedFiles.isNotEmpty()) {
            _uiState.update { it.copy(isConfirmDiscardingDialogVisible = true) }
        } else {
            _goBackSignal.emitOn(Unit, viewModelScope)
        }
    }

    fun onConfirmDiscardingDialogDismissed() {
        _uiState.update { it.copy(isConfirmDiscardingDialogVisible = false) }
    }

    fun onConfirmDiscardingClick() {
        _uiState.update { it.copy(isConfirmDiscardingDialogVisible = false) }
        _goBackSignal.emitOn(Unit, viewModelScope)
    }

    private fun updateRecipientIsNotYourContactError() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    showRecipientIsNotYourContactError = !contacts.any { it.contactVeraId == uiState.value.recipientAccountId } && uiState.value.recipientAccountId.isNotEmptyOrBlank(),
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
        recipientAccountId: String,
        messageText: String,
    ): Boolean {
        val isMessageSizeExceedsLimit = isMessageSizeExceedsLimit(messageText)
        return !isMessageSizeExceedsLimit && contacts.any { recipientAccountId == it.contactVeraId }
    }

    private fun isMessageSizeExceedsLimit(messageText: String): Boolean =
        getMessageSize(messageText) > MESSAGE_SIZE_LIMIT_BYTES

    private fun getMessageExceedsLimitError(messageText: String): MessageExceedsLimitError? {
        val messageExceedsLimitBy = (getMessageSize(messageText) - MESSAGE_SIZE_LIMIT_BYTES).toLong()
        return when {
            !isMessageSizeExceedsLimit(messageText) -> null
            messageExceedsLimitBy <= 0 -> null
            messageExceedsLimitBy.isMoreThanMegabyte() -> MessageExceedsLimitError(String.format("%.2f", messageExceedsLimitBy.bytesToMb()), R.string.message_exceeds_limit_megabytes)
            messageExceedsLimitBy.isMoreThanKilobyte() -> MessageExceedsLimitError(String.format("%.2f", messageExceedsLimitBy.bytesToKb()), R.string.message_exceeds_limit_kilobytes)
            else -> MessageExceedsLimitError(String.format("%.2f", messageExceedsLimitBy), R.string.message_exceeds_limit_bytes)
        }
    }

    private fun getMessageSize(messageText: String) =
        messageText.length * MESSAGE_TEXT_COMPRESSION_RATIO + attachedFiles.sumOf { it.size }

    private companion object {
        private const val MESSAGE_SIZE_LIMIT_BYTES = 8_388_608 // 8MB
        private const val MESSAGE_TEXT_COMPRESSION_RATIO = 0.25
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
    val recipientDisplayedText: String = "",
    val recipientAccountId: String = "",
    val subject: String = "",
    val messageText: String = "",
    val showRecipientIsNotYourContactError: Boolean = false,
    val isSendButtonEnabled: Boolean = false,
    val showRecipientAsChip: Boolean = false,
    val isOnlyTextEditale: Boolean = false,
    val showNoSubjectText: Boolean = false,
    val isSendingMessage: Boolean = false,
    val isConfirmDiscardingDialogVisible: Boolean = false,
    val messageExceedsLimitTextError: MessageExceedsLimitError? = null,
    val suggestedContacts: List<Contact>? = null,
)

data class MessageExceedsLimitError(
    val value: String,
    @StringRes val stringRes: Int,
)

private const val TAG = "ComposeNewMessageViewModel"
