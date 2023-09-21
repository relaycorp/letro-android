package tech.relaycorp.letro.onboarding.registration

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import tech.relaycorp.letro.R
import javax.inject.Inject

@HiltViewModel
class RegistrationViewModel @Inject constructor(
    private val registrationRepository: RegistrationRepository,
    private val domainProvider: RegistrationDomainProvider,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        RegistrationScreenUiState(
            domain = "@${domainProvider.getDomain()}",
        ),
    )
    val uiState: StateFlow<RegistrationScreenUiState>
        get() = _uiState

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
        registrationRepository.createNewAccount(
            requestedUserName = uiState.value.username,
            domainName = uiState.value.domain,
            locale = domainProvider.getDomainLocale(),
        )
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
)
