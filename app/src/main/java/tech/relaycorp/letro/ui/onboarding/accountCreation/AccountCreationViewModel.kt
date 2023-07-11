package tech.relaycorp.letro.ui.onboarding.accountCreation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import tech.relaycorp.letro.repository.GatewayRepository
import javax.inject.Inject
import kotlinx.coroutines.launch
import tech.relaycorp.letro.data.ContentType
import tech.relaycorp.letro.repository.AccountRepository

@HiltViewModel
class AccountCreationViewModel @Inject constructor(
    private val gatewayRepository: GatewayRepository,
    private val accountRepository: AccountRepository,
) : ViewModel() {

    private val _accountCreationUIState: MutableStateFlow<AccountCreationUIState> =
        MutableStateFlow(AccountCreationUIState())
    val accountCreationUIState: StateFlow<AccountCreationUIState> get() = _accountCreationUIState

    init {
        viewModelScope.launch {
            gatewayRepository.incomingMessagesFromServer.collect { message ->
                if (message.type == ContentType.AccountCreationCompleted.value) {
                    _accountCreationUIState.update { it.copy(isLoading = false) }
                }
            }
        }
    }

    fun onUsernameInput(username: String) {
        _accountCreationUIState.update { it.copy(username = username) }
    }

    fun onScreenResumed() { // TODO Maybe not needed
        gatewayRepository.checkIfGatewayIsAvailable()
    }

    fun onCreateAccountClicked() {
        _accountCreationUIState.update { it.copy(isLoading = true) }
        accountRepository.createNewAccount(
            _accountCreationUIState.value.username + "@" + _accountCreationUIState.value.domain,
        )
    }
}
