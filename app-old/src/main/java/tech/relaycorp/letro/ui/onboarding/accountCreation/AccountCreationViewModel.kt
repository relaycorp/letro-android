package tech.relaycorp.letro.ui.onboarding.accountCreation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tech.relaycorp.letro.repository.AccountRepository
import tech.relaycorp.letro.repository.GatewayRepository
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class AccountCreationViewModel @Inject constructor(
    private val gatewayRepository: GatewayRepository,
    private val accountRepository: AccountRepository,
) : ViewModel() {

    private val _accountCreationUIState: MutableStateFlow<AccountCreationUIState> =
        MutableStateFlow(AccountCreationUIState())
    val accountCreationUIState: StateFlow<AccountCreationUIState> get() = _accountCreationUIState

    private val _goToLoadingScreen: MutableSharedFlow<Unit> = MutableSharedFlow()
    val goToLoadingScreen: SharedFlow<Unit> get() = _goToLoadingScreen

    fun onUsernameInput(username: String) {
        _accountCreationUIState.update { it.copy(username = username) }
    }

    init {
        val locale = Locale.getDefault()
        val domain = when (locale.toString()) {
            "en_US" -> "@applepie.fans"
            "es_ve" -> "@guarapo.cafe"
            else -> "@nautilus.ink"
        }
        _accountCreationUIState.update { it.copy(domain = domain) }
    }

    fun onScreenResumed() { // TODO Maybe not needed
        gatewayRepository.checkIfGatewayIsAvailable()
    }

    fun onCreateAccountClicked() {
        accountRepository.startCreatingNewAccount(
            veraId = _accountCreationUIState.value.username + _accountCreationUIState.value.domain,
        )
        viewModelScope.launch {
            _goToLoadingScreen.emit(Unit)
        }
    }
}
