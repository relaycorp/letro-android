package tech.relaycorp.letro.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tech.relaycorp.letro.account.model.Account
import tech.relaycorp.letro.account.storage.repository.AccountRepository
import tech.relaycorp.letro.awala.AwalaInitializationState
import tech.relaycorp.letro.awala.AwalaManager
import tech.relaycorp.letro.contacts.storage.repository.ContactsRepository
import tech.relaycorp.letro.conversation.attachments.AttachmentsRepository
import tech.relaycorp.letro.conversation.attachments.filepicker.FileConverter
import tech.relaycorp.letro.conversation.attachments.filepicker.model.File
import tech.relaycorp.letro.conversation.storage.repository.ConversationsRepository
import tech.relaycorp.letro.main.di.TermsAndConditionsLink
import tech.relaycorp.letro.push.model.PushAction
import tech.relaycorp.letro.ui.navigation.RootNavigationScreen
import tech.relaycorp.letro.utils.Logger
import tech.relaycorp.letro.utils.ext.emitOn
import tech.relaycorp.letro.utils.ext.sendOn
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val awalaManager: AwalaManager,
    private val accountRepository: AccountRepository,
    private val contactsRepository: ContactsRepository,
    private val attachmentsRepository: AttachmentsRepository,
    private val fileConverter: FileConverter,
    private val conversationsRepository: ConversationsRepository,
    @TermsAndConditionsLink private val termsAndConditionsLink: String,
    private val logger: Logger,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState>
        get() = _uiState

    private val _openLinkSignal = MutableSharedFlow<String>()
    val openLinkSignal: SharedFlow<String>
        get() = _openLinkSignal

    private val _joinMeOnLetroSignal = MutableSharedFlow<String>()
    val joinMeOnLetroSignal: SharedFlow<String>
        get() = _joinMeOnLetroSignal

    private val _openFileSignal = MutableSharedFlow<File.FileWithoutContent>()
    val openFileSignal: SharedFlow<File.FileWithoutContent>
        get() = _openFileSignal

    private val _rootNavigationScreen: MutableStateFlow<RootNavigationScreen> =
        MutableStateFlow(RootNavigationScreen.Splash)
    val rootNavigationScreen: StateFlow<RootNavigationScreen> get() = _rootNavigationScreen

    /**
     * Replay = 1, in case that some action will be emitted, but no one handled this event
     */
    private val _pushActions = Channel<PushAction>(UNLIMITED)
    val pushAction: Flow<PushAction>
        get() = _pushActions.consumeAsFlow()

    private var currentAccount: Account? = null

    init {
        viewModelScope.launch {
            accountRepository.currentAccount.collect { account ->
                _uiState.update {
                    if (account != null) {
                        it.copy(
                            currentAccount = account.accountId,
                            isCurrentAccountCreated = account.isCreated,
                        )
                    } else {
                        it
                    }
                }
                currentAccount = account
            }
        }

        viewModelScope.launch {
            combine(
                accountRepository.currentAccount,
                contactsRepository.contactsState,
                awalaManager.awalaInitializationState,
                conversationsRepository.conversations,
            ) { currentAccount, contactsState, awalaInitializationState, conversations ->
                logger.d(TAG, "$currentAccount; $contactsState; $awalaInitializationState; ${conversations.size}")
                when {
                    awalaInitializationState == AwalaInitializationState.AWALA_NOT_INSTALLED -> RootNavigationScreen.AwalaNotInstalled
                    awalaInitializationState == AwalaInitializationState.INITIALIZATION_NONFATAL_ERROR -> RootNavigationScreen.AwalaInitializationError(isFatal = false)
                    awalaInitializationState == AwalaInitializationState.INITIALIZATION_FATAL_ERROR -> RootNavigationScreen.AwalaInitializationError(isFatal = true)
                    awalaInitializationState < AwalaInitializationState.INITIALIZED -> RootNavigationScreen.AwalaInitializing
                    currentAccount == null -> RootNavigationScreen.Registration
                    !currentAccount.isCreated -> RootNavigationScreen.RegistrationWaiting
                    !contactsState.isPairRequestWasEverSent -> RootNavigationScreen.WelcomeToLetro
                    !contactsState.isPairedContactExist && conversations.isEmpty() -> RootNavigationScreen.NoContactsScreen
                    else -> RootNavigationScreen.Home
                }
            }
                .distinctUntilChanged()
                .collect {
                    _rootNavigationScreen.emit(it)
                }
        }
    }

    fun onNewPushAction(pushAction: PushAction?) {
        pushAction ?: return
        _pushActions.sendOn(pushAction, viewModelScope)
    }

    fun onInstallAwalaClick() {
        _openLinkSignal.emitOn(AWALA_GOOGLE_PLAY_LINK, viewModelScope)
    }

    fun onTermsAndConditionsClick() {
        _openLinkSignal.emitOn(termsAndConditionsLink, viewModelScope)
    }

    fun onShareIdClick() {
        currentAccount?.accountId?.let { accountId ->
            _joinMeOnLetroSignal.emitOn(getJoinMeLink(accountId), viewModelScope)
        }
    }

    fun onAttachmentClick(fileId: UUID) {
        viewModelScope.launch(Dispatchers.IO) {
            attachmentsRepository.getById(fileId)?.let { attachment ->
                fileConverter.getFile(attachment)?.let { file ->
                    if (file.exists()) {
                        _openFileSignal.emitOn(file, viewModelScope)
                    }
                }
            }
        }
    }

    private fun getJoinMeLink(accountId: String) = "$JOIN_ME_ON_LETRO_COMMON_PART_OF_LINK$accountId"

    companion object {
        const val TAG = "MainViewModel"
        private const val JOIN_ME_ON_LETRO_COMMON_PART_OF_LINK = "https://letro.app/connect/#u="
        private const val AWALA_GOOGLE_PLAY_LINK = "https://play.google.com/store/apps/details?id=tech.relaycorp.gateway"
    }
}

data class MainUiState(
    val currentAccount: String? = null,
    val isCurrentAccountCreated: Boolean = true,
)
