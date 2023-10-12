package tech.relaycorp.letro.account.registration

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
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
import tech.relaycorp.letro.account.registration.storage.RegistrationRepository
import tech.relaycorp.letro.account.registration.utils.RegistrationDomainProvider
import tech.relaycorp.letro.ui.utils.SnackbarStringsProvider
import javax.inject.Inject

@HiltViewModel
class RegistrationViewModel @Inject constructor(
    private val registrationRepository: RegistrationRepository,
    private val domainProvider: RegistrationDomainProvider,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        RegistrationScreenUiState(
            domain = domainProvider.getDomain(),
        ),
    )
    val uiState: StateFlow<RegistrationScreenUiState>
        get() = _uiState

    private val _showSnackbar: MutableSharedFlow<Int> = MutableSharedFlow()
    val showSnackbar: SharedFlow<Int>
        get() = _showSnackbar

    fun onUsernameInput(username: String) {
        val isValidText = !username.contains(" ") && !username.contains("@") && username.length <= USER_NAME_MAX_LENGTH
        _uiState.update {
            it.copy(
                username = username,
                isError = !isValidText,
                isCreateAccountButtonEnabled = isValidText && username.isNotEmpty(),
                inputSuggestionText = if (isValidText) R.string.onboarding_create_account_username_unavailable_hint else R.string.onboarding_create_account_wrong_username_hint,
            )
        }
    }

    fun onCreateAccountClick() {
        if (uiState.value.isSendingMessage) {
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update {
                it.copy(
                    isSendingMessage = true,
                )
            }
            try {
                registrationRepository.createNewAccount(
                    requestedUserName = uiState.value.username,
                    domainName = uiState.value.domain,
                    locale = domainProvider.getDomainLocale(),
                )
            } catch (e: AwaladroidException) {
                _showSnackbar.emit(SnackbarStringsProvider.Type.SEND_MESSAGE_ERROR)
            } finally {
                _uiState.update {
                    it.copy(
                        isSendingMessage = false,
                    )
                }
            }
        }
    }

    private companion object {
        private const val USER_NAME_MAX_LENGTH = 16
    }
}

data class RegistrationScreenUiState(
    val username: String = "",
    val domain: String = "",
    @StringRes val inputSuggestionText: Int = R.string.onboarding_create_account_username_unavailable_hint,
    val isError: Boolean = false,
    val isCreateAccountButtonEnabled: Boolean = false,
    val isSendingMessage: Boolean = false,
)
