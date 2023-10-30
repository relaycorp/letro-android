@file:OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)

package tech.relaycorp.letro.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tech.relaycorp.letro.account.model.Account
import tech.relaycorp.letro.account.model.AccountStatus
import tech.relaycorp.letro.account.storage.repository.AccountRepository
import tech.relaycorp.letro.awala.AwalaInitializationState
import tech.relaycorp.letro.awala.AwalaManager
import tech.relaycorp.letro.contacts.storage.repository.ContactsRepository
import tech.relaycorp.letro.conversation.attachments.AttachmentsRepository
import tech.relaycorp.letro.conversation.attachments.filepicker.FileConverter
import tech.relaycorp.letro.conversation.attachments.filepicker.model.File
import tech.relaycorp.letro.conversation.attachments.sharing.AttachmentToShare
import tech.relaycorp.letro.conversation.attachments.sharing.ShareAttachmentsRepository
import tech.relaycorp.letro.conversation.storage.repository.ConversationsRepository
import tech.relaycorp.letro.main.di.MainViewModelActionProcessorThread
import tech.relaycorp.letro.main.di.RootNavigationDebounceMs
import tech.relaycorp.letro.main.di.TermsAndConditionsLink
import tech.relaycorp.letro.ui.navigation.Action
import tech.relaycorp.letro.ui.navigation.RootNavigationScreen
import tech.relaycorp.letro.ui.navigation.Route
import tech.relaycorp.letro.utils.Logger
import tech.relaycorp.letro.utils.di.MainDispatcher
import tech.relaycorp.letro.utils.ext.emitOn
import tech.relaycorp.letro.utils.navigation.UriToActionConverter
import java.util.UUID
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

@OptIn(FlowPreview::class)
@HiltViewModel
class MainViewModel @Inject constructor(
    private val awalaManager: AwalaManager,
    private val accountRepository: AccountRepository,
    private val contactsRepository: ContactsRepository,
    private val attachmentsRepository: AttachmentsRepository,
    private val fileConverter: FileConverter,
    private val conversationsRepository: ConversationsRepository,
    private val uriToActionConverter: UriToActionConverter,
    private val shareAttachmentsRepository: ShareAttachmentsRepository,
    private val logger: Logger,
    @MainDispatcher private val mainDispatcher: CoroutineDispatcher,
    @MainViewModelActionProcessorThread private val actionProcessorThread: CoroutineContext,
    @TermsAndConditionsLink private val termsAndConditionsLink: String,
    @RootNavigationDebounceMs private val rootNavigationDebounceMs: Long,
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
        MutableStateFlow(RootNavigationScreen.AwalaInitializing)
    val rootNavigationScreen: StateFlow<RootNavigationScreen> get() = _rootNavigationScreen

    private val isAwalaInitialised = MutableStateFlow(false)
    private val accountIsReadyToProcessActions = MutableStateFlow<Boolean>(false)

    private val _clearBackstackSignal = MutableSharedFlow<RootNavigationScreen>()
    val clearBackstackSignal: MutableSharedFlow<RootNavigationScreen>
        get() = _clearBackstackSignal

    private val _actions = Channel<Action>()
    val actions: Flow<Action>
        get() = _actions.receiveAsFlow()

    private var currentAccount: Account? = null

    /**
     * TODO: refactor it
     * Navigation of the app is based on Root screens, managed by this view model.
     *
     * This variable needed to fix the problem https://relaycorp.atlassian.net/browse/LTR-136:
     * The problem happened when configuration was changed (screen rotation/switching between dark/light mode):
     * in this case, view subscribed to the StateFlow of root navigation screen, and navigated to it with popping the backstack, which resulted to losing state.
     *
     * Now, a subscriber of the root navigation screen flow must check this variable, and update it by calling [onRootNavigationScreenHandled], to not handle the same root navigation twice.
     */
    var rootNavigationScreenAlreadyHandled: Boolean = true
        private set

    /**
     * Used to figure out do we need to clear backstack after navigation event. We need to clear it, when account was changed
     */
    private var navigationHandledWithLastAccount: Long? = null

    init {
        viewModelScope.launch {
            accountRepository.currentAccount.collect { account ->
                _uiState.update {
                    if (account != null) {
                        it.copy(
                            currentAccount = account.accountId,
                            domain = account.domain,
                            accountStatus = account.status,
                        )
                    } else {
                        it
                    }
                }
                currentAccount = account
            }
        }

        viewModelScope.launch(mainDispatcher) {
            combine(
                accountRepository.currentAccount,
                contactsRepository.contactsState,
                awalaManager.awalaInitializationState,
                conversationsRepository.conversations,
            ) { currentAccount, contactsState, awalaInitializationState, conversations ->
                logger.d(TAG, "$currentAccount; $contactsState; $awalaInitializationState; ${conversations.size}")
                _uiState.update {
                    it.copy(
                        canSendMessages = contactsState.isPairedContactExist,
                    )
                }
                val rootNavigationScreen = when {
                    awalaInitializationState == AwalaInitializationState.AWALA_NOT_INSTALLED -> RootNavigationScreen.AwalaNotInstalled
                    awalaInitializationState == AwalaInitializationState.INITIALIZATION_NONFATAL_ERROR -> RootNavigationScreen.AwalaInitializationError(type = Route.AwalaInitializationError.TYPE_NON_FATAL_ERROR)
                    awalaInitializationState == AwalaInitializationState.INITIALIZATION_FATAL_ERROR -> RootNavigationScreen.AwalaInitializationError(type = Route.AwalaInitializationError.TYPE_FATAL_ERROR)
                    awalaInitializationState == AwalaInitializationState.COULD_NOT_REGISTER_FIRST_PARTY_ENDPOINT -> RootNavigationScreen.AwalaInitializationError(type = Route.AwalaInitializationError.TYPE_NEED_TO_OPEN_AWALA)
                    awalaInitializationState < AwalaInitializationState.INITIALIZED -> RootNavigationScreen.AwalaInitializing
                    currentAccount == null -> RootNavigationScreen.Registration
                    currentAccount.status == AccountStatus.CREATION_WAITING -> RootNavigationScreen.AccountCreationWaiting
                    currentAccount.status == AccountStatus.LINKING_WAITING -> RootNavigationScreen.AccountLinkingWaiting
                    currentAccount.status == AccountStatus.ERROR -> RootNavigationScreen.AccountCreationFailed
                    !contactsState.isPairRequestWasEverSent -> RootNavigationScreen.WelcomeToLetro
                    !contactsState.isPairedContactExist && conversations.isEmpty() -> RootNavigationScreen.NoContactsScreen
                    else -> RootNavigationScreen.Home
                }
                Pair(rootNavigationScreen, navigationHandledWithLastAccount != currentAccount?.id).also { navigationHandledWithLastAccount = currentAccount?.id }
            }
                .debounce(rootNavigationDebounceMs)
                .collect {
                    logger.i(TAG, "New root navigation ${it.first}")
                    val rootNavigationScreen = it.first
                    val lastRootNavigationScreen = _rootNavigationScreen.value
                    this@MainViewModel.rootNavigationScreenAlreadyHandled = true

                    if (rootNavigationScreen != RootNavigationScreen.AwalaNotInstalled && rootNavigationScreen != RootNavigationScreen.AwalaInitializing && rootNavigationScreen !is RootNavigationScreen.AwalaInitializationError) {
                        isAwalaInitialised.emit(true)
                    }

                    _rootNavigationScreen.emit(rootNavigationScreen)

                    val clearNavigationScreenToRoot = it.second
                    if (clearNavigationScreenToRoot && rootNavigationScreen == lastRootNavigationScreen) {
                        logger.d(TAG, "Send event to clear nav stack")
                        _clearBackstackSignal.emit(rootNavigationScreen)
                    }

                    if (isAwalaInitialised.value) {
                        accountIsReadyToProcessActions.emit(true)
                        logger.i(TAG, "Root changed, account is ready to process action")
                    }
                }
        }
    }

    fun onRootNavigationScreenHandled(rootNavigationScreen: RootNavigationScreen) {
        if (rootNavigationScreen == _rootNavigationScreen.value) {
            this.rootNavigationScreenAlreadyHandled = false
        }
    }

    fun onNewAction(action: Action) {
        logger.i(TAG, "ActionHandler: onNewAction ${action.javaClass}")

        viewModelScope.launch(actionProcessorThread) {
            // Do not emit actions while Awala is not initialised, because it cannot be handled
            while (!isAwalaInitialised.value) {
                logger.i(TAG, "ActionHandler: Awala is not initialised yet")
                delay(1_000L)
            }

            logger.i(TAG, "ActionHandler: processing")
            accountIsReadyToProcessActions.emit(false)
            val accountId = action.accountId

            if (accountId != null) {
                val isSwitched = accountRepository.switchAccount(accountId)
                if (!isSwitched) {
                    accountIsReadyToProcessActions.emit(true)
                    logger.i(TAG, "ActionHandler: The same account has to be used to process the action")
                }
            } else {
                accountIsReadyToProcessActions.emit(true)
                logger.i(TAG, "ActionHandler: Account ID is null. Try to process action...")
            }

            // Do not emit action if account was changed. Wait until root screen will be changed (= account was changed)
            while (!accountIsReadyToProcessActions.value) {
                logger.i(TAG, "ActionHandler: Account is not ready to handle actions. Waiting...")
                delay(1_000L)
            }

            logger.i(TAG, "ActionHandler: Sending action ${action.javaClass}")

            withContext(Dispatchers.Main) {
                _actions.send(action)
            }
        }
    }

    fun onLinkOpened(link: String) {
        val action = uriToActionConverter.convert(link) ?: return
        onNewAction(action)
    }

    fun onSendFilesRequested(
        files: List<AttachmentToShare>,
        contactId: Long? = null,
    ) {
        shareAttachmentsRepository.shareAttachmentsLater(files)
        onNewAction(
            action = Action.OpenComposeNewMessage(
                attachments = files,
                contactId = contactId,
            ),
        )
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
    val domain: String? = null,
    @AccountStatus val accountStatus: Int = AccountStatus.CREATED,
    val canSendMessages: Boolean = false,
)
