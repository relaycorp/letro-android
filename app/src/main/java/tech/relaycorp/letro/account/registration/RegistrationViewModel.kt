package tech.relaycorp.letro.account.registration

import android.util.Log
import androidx.annotation.StringRes
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tech.relaycorp.awaladroid.AwaladroidException
import tech.relaycorp.letro.R
import tech.relaycorp.letro.account.registration.storage.DuplicateAccountIdException
import tech.relaycorp.letro.account.registration.storage.RegistrationRepository
import tech.relaycorp.letro.account.registration.utils.RegistrationDomainProvider
import tech.relaycorp.letro.base.BaseViewModel
import tech.relaycorp.letro.base.utils.SnackbarString
import tech.relaycorp.letro.ui.utils.SnackbarStringsProvider
import tech.relaycorp.letro.utils.di.IODispatcher
import javax.inject.Inject

@HiltViewModel
class RegistrationViewModel @Inject constructor(
    private val registrationRepository: RegistrationRepository,
    private val domainProvider: RegistrationDomainProvider,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(
        RegistrationScreenUiState(
            domain = domainProvider.getDomain(),
        ),
    )
    val uiState: StateFlow<RegistrationScreenUiState>
        get() = _uiState

    fun onUsernameInput(username: String) {
        val isValidText = !username.contains(" ") && !username.contains("@") && username.length <= USER_NAME_MAX_LENGTH
        val isAccountWithThisIdAlreadyExists = registrationRepository.isAccountWithThisIdAlreadyExists(username, uiState.value.domain)
        _uiState.update {
            it.copy(
                username = username,
                isError = !isValidText || isAccountWithThisIdAlreadyExists,
                isCreateAccountButtonEnabled = isCreateAccountButtonEnabled(
                    isValidText = isValidText,
                    username = username,
                    isAccountWithThisIdAlreadyExists = isAccountWithThisIdAlreadyExists,
                ),
                inputSuggestionText = when {
                    !isValidText -> R.string.onboarding_create_account_wrong_username_hint
                    isAccountWithThisIdAlreadyExists -> R.string.you_already_have_account_with_this_id
                    else -> R.string.onboarding_create_account_username_unavailable_hint
                },
            )
        }
    }

    fun onCreateAccountClick() {
        if (uiState.value.isSendingMessage) {
            return
        }
        viewModelScope.launch(ioDispatcher) {
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
                Log.w(TAG, e)
                showSnackbarDebounced.emit(
                    SnackbarString(SnackbarStringsProvider.Type.SEND_MESSAGE_ERROR),
                )
            } catch (e: DuplicateAccountIdException) {
                Log.w(TAG, e)
                showSnackbarDebounced.emit(
                    SnackbarString(SnackbarStringsProvider.Type.ACCOUNT_LINKING_ID_ALREADY_EXISTS),
                )
            } finally {
                _uiState.update {
                    it.copy(
                        isSendingMessage = false,
                    )
                }
            }
        }
    }

    private fun isCreateAccountButtonEnabled(
        isValidText: Boolean,
        username: String,
        isAccountWithThisIdAlreadyExists: Boolean,
    ): Boolean {
        return isValidText && username.isNotEmpty() && !isAccountWithThisIdAlreadyExists
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

private const val TAG = "RegistrationViewModel"
