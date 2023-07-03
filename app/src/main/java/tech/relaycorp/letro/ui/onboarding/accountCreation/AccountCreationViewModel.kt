package tech.relaycorp.letro.ui.onboarding.accountCreation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountCreationViewModel @Inject constructor() : ViewModel() {

    private val _accountCreationUIState: MutableStateFlow<AccountCreationUIState> =
        MutableStateFlow(AccountCreationUIState())
    val accountCreationUIState: StateFlow<AccountCreationUIState> get() = _accountCreationUIState

    fun onUsernameChanged(username: String) {
        viewModelScope.launch {
            _accountCreationUIState.update { it.copy(username = username) }
        }
    }
}
