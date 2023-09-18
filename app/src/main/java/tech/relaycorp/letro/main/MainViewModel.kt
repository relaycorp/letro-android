package tech.relaycorp.letro.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tech.relaycorp.letro.account.model.Account
import tech.relaycorp.letro.account.storage.AccountRepository
import tech.relaycorp.letro.awala.AwalaManager
import tech.relaycorp.letro.contacts.storage.ContactsRepository
import tech.relaycorp.letro.ui.navigation.RootNavigationScreen
import tech.relaycorp.letro.ui.navigation.Route
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val awalaManager: AwalaManager,
    private val accountRepository: AccountRepository,
    private val contactsRepository: ContactsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState>
        get() = _uiState

    private val _showInstallAwalaScreen = MutableStateFlow(false)
    val showInstallAwalaScreen: StateFlow<Boolean>
        get() = _showInstallAwalaScreen

    private val _openLinkSignal = MutableSharedFlow<String>()
    val openLinkSignal: SharedFlow<String>
        get() = _openLinkSignal

    private val _joinMeOnLetroSignal = MutableSharedFlow<String>()
    val joinMeOnLetroSignal: SharedFlow<String>
        get() = _joinMeOnLetroSignal

    private val _rootNavigationScreen: MutableStateFlow<RootNavigationScreen> =
        MutableStateFlow(RootNavigationScreen.Splash)
    val rootNavigationScreen: StateFlow<RootNavigationScreen> get() = _rootNavigationScreen

    private var currentAccount: Account? = null

    init {
        viewModelScope.launch {
            accountRepository.currentAccount.collect { account ->
                _uiState.update {
                    if (account != null) {
                        it.copy(
                            currentAccount = account.veraId,
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
            ) { currentAccount, contactsState ->
                Pair(currentAccount, contactsState)
            }
                .distinctUntilChanged()
                .onStart { Log.d(TAG, "Start collecting the combined Flow") }
                .collect {
                    val currentAccount = it.first
                    val contactsState = it.second
                    Log.d(TAG, "$currentAccount; $contactsState")
                    if (currentAccount != null) {
                        when {
                            !currentAccount.isCreated -> {
                                _rootNavigationScreen.emit(RootNavigationScreen.RegistrationWaiting)
                            }
                            !contactsState.isPairRequestWasEverSent -> {
                                _rootNavigationScreen.emit(RootNavigationScreen.WelcomeToLetro)
                            }
                            !contactsState.isPairedContactExist -> {
                                _rootNavigationScreen.emit(RootNavigationScreen.NoContactsScreen)
                            }
                            else -> _rootNavigationScreen.emit(RootNavigationScreen.Home)
                        }
                    } else {
                        _rootNavigationScreen.emit(RootNavigationScreen.Registration)
                    }
                }
        }
    }

    fun onScreenResumed(currentRoute: Route) {
        viewModelScope.launch(Dispatchers.IO) {
            val isAwalaInstalled = awalaManager.isAwalaInstalled(currentRoute)
            _showInstallAwalaScreen.emit(!isAwalaInstalled)
        }
    }

    fun onInstallAwalaClick() {
        viewModelScope.launch {
            _openLinkSignal.emit(AWALA_GOOGLE_PLAY_LINK)
        }
    }

    fun onShareIdClick() {
        currentAccount?.veraId?.let { accountId ->
            viewModelScope.launch {
                _joinMeOnLetroSignal.emit(getJoinMeLink(accountId))
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
