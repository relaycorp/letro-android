package tech.relaycorp.letro.ui.onboarding.accountCreation

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import tech.relaycorp.letro.repository.GatewayRepository
import javax.inject.Inject

@HiltViewModel
class AccountCreationViewModel @Inject constructor(
    private val gatewayRepository: GatewayRepository,
) : ViewModel() {

    private val _accountCreationUIState: MutableStateFlow<AccountCreationUIState> =
        MutableStateFlow(AccountCreationUIState())
    val accountCreationUIState: StateFlow<AccountCreationUIState> get() = _accountCreationUIState

    fun onUsernameChanged(username: String) {
        _accountCreationUIState.update { it.copy(username = username) }
    }

    fun onScreenResumed() {
        gatewayRepository.checkIfGatewayIsAvailable()
    }
}
