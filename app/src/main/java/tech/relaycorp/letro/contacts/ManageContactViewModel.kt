package tech.relaycorp.letro.contacts

import android.util.Log
import androidx.annotation.IntDef
import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tech.relaycorp.awaladroid.AwaladroidException
import tech.relaycorp.letro.R
import tech.relaycorp.letro.account.storage.repository.AccountRepository
import tech.relaycorp.letro.base.BaseViewModel
import tech.relaycorp.letro.base.utils.SnackbarString
import tech.relaycorp.letro.contacts.ManageContactScreenContent.Companion.REQUEST_SENT
import tech.relaycorp.letro.contacts.ManageContactViewModel.Type.Companion.EDIT_CONTACT
import tech.relaycorp.letro.contacts.ManageContactViewModel.Type.Companion.NEW_CONTACT
import tech.relaycorp.letro.contacts.model.Contact
import tech.relaycorp.letro.contacts.model.ContactPairingStatus
import tech.relaycorp.letro.contacts.storage.repository.ContactsRepository
import tech.relaycorp.letro.ui.navigation.Route
import tech.relaycorp.letro.ui.utils.SnackbarStringsProvider
import tech.relaycorp.letro.utils.coroutines.Dispatchers
import tech.relaycorp.letro.utils.ext.decodeFromUTF
import tech.relaycorp.letro.utils.ext.emitOn
import tech.relaycorp.letro.utils.ext.nullIfBlankOrEmpty
import javax.inject.Inject

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class ManageContactViewModel @Inject constructor(
    private val contactsRepository: ContactsRepository,
    private val accountRepository: AccountRepository,
    savedStateHandle: SavedStateHandle,
    dispatchers: Dispatchers,
) : BaseViewModel(dispatchers) {

    @Type
    private val screenType: Int = savedStateHandle[Route.ManageContact.KEY_SCREEN_TYPE]!!
    private val contactIdToEdit: Long? = (savedStateHandle.get(Route.ManageContact.KEY_CONTACT_ID_TO_EDIT) as? Long)?.takeIf { it != Route.ManageContact.NO_ID }
    private val prefilledContactAccountId: String? = (savedStateHandle.get(Route.ManageContact.KEY_PREFILLED_ACCOUNT_ID_ENCODED) as? String)?.decodeFromUTF()

    private val _uiState = MutableStateFlow(
        PairWithOthersUiState(
            manageContactTexts = when (screenType) {
                NEW_CONTACT -> ManageContactTexts.PairWithOthers()
                EDIT_CONTACT -> ManageContactTexts.EditContact()
                else -> throw IllegalStateException("Unknown screen type: $screenType")
            },
            isActionButtonEnabled = screenType == EDIT_CONTACT,
        ),
    )
    val uiState: StateFlow<PairWithOthersUiState>
        get() = _uiState

    private val checkActionButtonAvailabilityFlow = MutableSharedFlow<String>()

    private val _onEditContactCompleted = MutableSharedFlow<String>()
    val onEditContactCompleted: SharedFlow<String>
        get() = _onEditContactCompleted

    private val _goBackSignal = MutableSharedFlow<Unit>()
    val goBackSignal: SharedFlow<Unit>
        get() = _goBackSignal

    private val _showPermissionGoToSettingsSignal = MutableSharedFlow<Unit>()
    val showPermissionGoToSettingsSignal: SharedFlow<Unit>
        get() = _showPermissionGoToSettingsSignal

    private var currentAccountId: String? = null
    private val contacts: HashSet<Contact> = hashSetOf()

    private var editingContact: Contact? = null

    init {
        viewModelScope.launch {
            checkActionButtonAvailabilityFlow
                .debounce(CHECK_ID_DEBOUNCE_DELAY_MS)
                .collect {
                    checkIfIdIsCorrect(it)
                }
        }
        viewModelScope.launch {
            accountRepository.currentAccount
                .flatMapLatest {
                    currentAccountId = it?.accountId
                    if (it != null) {
                        contactsRepository.getContacts(it.accountId)
                    } else {
                        emptyFlow()
                    }
                }
                .collect {
                    contacts.clear()
                    contacts.addAll(it)
                }
        }
        viewModelScope.launch {
            when {
                contactIdToEdit != null -> {
                    contactsRepository.getContactById(contactIdToEdit)?.let { contactToEdit ->
                        editingContact = contactToEdit
                        _uiState.update {
                            it.copy(
                                accountId = contactToEdit.contactVeraId,
                                alias = contactToEdit.alias,
                                isVeraIdInputEnabled = false,
                            )
                        }
                    }
                }
                prefilledContactAccountId != null -> {
                    onIdChanged(prefilledContactAccountId)
                }
            }
        }
    }

    fun onIdChanged(id: String) {
        viewModelScope.launch {
            val trimmedId = id.trim()
            _uiState.update {
                it.copy(
                    accountId = trimmedId,
                    isSentRequestAgainHintVisible = contacts.any { it.contactVeraId == trimmedId && it.status == ContactPairingStatus.REQUEST_SENT },
                )
            }
            checkActionButtonAvailabilityFlow.emit(trimmedId)
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

    fun onUpdateContactButtonClick() {
        if (_uiState.value.isSendingMessage) {
            return
        }
        viewModelScope.launch(dispatchers.IO) {
            _uiState.update { it.copy(isSendingMessage = true) }
            when (screenType) {
                NEW_CONTACT -> {
                    try {
                        sendNewContactRequest()
                        _uiState.update {
                            it.copy(
                                content = REQUEST_SENT,
                            )
                        }
                    } catch (e: AwaladroidException) {
                        Log.w(TAG, e)
                        showSnackbarDebounced.emit(
                            SnackbarString(SnackbarStringsProvider.Type.SEND_MESSAGE_ERROR),
                        )
                    } finally {
                        _uiState.update { it.copy(isSendingMessage = false) }
                    }
                }
                EDIT_CONTACT -> {
                    if (!contacts.any { it.id == contactIdToEdit }) {
                        Log.w(TAG, IllegalStateException("You cannot edit this contact. Contact belongs to ${contacts.firstOrNull()?.ownerVeraId}, but yours is $currentAccountId")) // TODO: log?
                        return@launch
                    }
                    updateContact()
                    _uiState.update { it.copy(isSendingMessage = false) }
                    _onEditContactCompleted.emit(uiState.value.accountId)
                }
                else -> throw IllegalStateException("Unknown screen type: $screenType")
            }
        }
    }

    fun onBackPressed() {
        when (uiState.value.content) {
            REQUEST_SENT -> onGotItClick()
            else -> _goBackSignal.emitOn(Unit, viewModelScope)
        }
    }

    fun onGotItClick() {
        viewModelScope.launch {
            contactsRepository.saveRequestWasOnceSent()
            _goBackSignal.emit(Unit)
        }
    }

    fun onNotificationPermissionResult(isGranted: Boolean) {
        viewModelScope.launch {
            if (!isGranted) {
                _showPermissionGoToSettingsSignal.emit(Unit)
            }
            onGotItClick()
        }
    }

    private fun checkIfIdIsCorrect(id: String) {
        viewModelScope.launch {
            val isValidId = id.matches(CORRECT_ID_REGEX)
            val errorMessage = getPairingErrorMessage(id, isValidId)
            _uiState.update {
                it.copy(
                    isActionButtonEnabled = isValidId && errorMessage == null,
                    pairingErrorCaption = errorMessage,
                )
            }
        }
    }

    private suspend fun updateContact() {
        editingContact?.let { editingContact ->
            contactsRepository.updateContact(
                editingContact.copy(
                    alias = uiState.value.alias?.nullIfBlankOrEmpty(),
                ),
            )
        }
    }

    @Throws(AwaladroidException::class)
    private suspend fun sendNewContactRequest() {
        currentAccountId?.let { currentAccountId ->
            contactsRepository.addNewContact(
                contact = Contact(
                    ownerVeraId = currentAccountId,
                    contactVeraId = uiState.value.accountId,
                    alias = uiState.value.alias?.nullIfBlankOrEmpty(),
                    status = ContactPairingStatus.REQUEST_SENT,
                    isPrivateEndpoint = uiState.value.accountId.contains("@"),
                ),
            )
        }
    }

    private fun getPairingErrorMessage(contactId: String, isValidId: Boolean): PairingErrorCaption? {
        val contact = contacts.find { it.contactVeraId == contactId }
        return when {
            !isValidId -> PairingErrorCaption(R.string.pair_request_invalid_id)
            contact == null -> null
            contact.status == ContactPairingStatus.COMPLETED -> PairingErrorCaption(R.string.pair_request_already_paired)
            contact.status >= ContactPairingStatus.MATCH -> PairingErrorCaption(R.string.pair_request_already_in_progress)
            else -> null
        }
    }

    private companion object {
        private const val TAG = "ManageContactViewModel"
        private const val CHECK_ID_DEBOUNCE_DELAY_MS = 1_500L
        private val CORRECT_ID_REGEX = """^([^@]+@)?\p{L}{1,63}(\.\p{L}{1,63})+$""".toRegex()
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
    val accountId: String = "",
    val alias: String? = null,
    val isActionButtonEnabled: Boolean = false,
    val isSentRequestAgainHintVisible: Boolean = false,
    val isVeraIdInputEnabled: Boolean = true,
    val pairingErrorCaption: PairingErrorCaption? = null,
    val showNotificationPermissionRequestIfNoPermission: Boolean = true,
    val isSendingMessage: Boolean = false,
    @ManageContactScreenContent val content: Int = ManageContactScreenContent.MANAGE_CONTACT,
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

annotation class ManageContactScreenContent {

    companion object {
        const val MANAGE_CONTACT = 0
        const val REQUEST_SENT = 1
    }
}
