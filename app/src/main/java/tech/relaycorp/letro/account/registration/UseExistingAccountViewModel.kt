package tech.relaycorp.letro.account.registration

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tech.relaycorp.awaladroid.AwaladroidException
import tech.relaycorp.letro.account.BaseViewModel
import tech.relaycorp.letro.account.registration.storage.RegistrationRepository
import tech.relaycorp.letro.ui.utils.SnackbarStringsProvider
import tech.relaycorp.letro.utils.ext.isEmptyOrBlank
import tech.relaycorp.letro.utils.ext.isNotEmptyOrBlank
import javax.inject.Inject

@HiltViewModel
class UseExistingAccountViewModel @Inject constructor(
    private val registrationRepository: RegistrationRepository,
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(UseExistingAccountUiState())
    val uiState: StateFlow<UseExistingAccountUiState>
        get() = _uiState

    fun onDomainInput(domain: String) {
        _uiState.update {
            it.copy(
                domain = domain,
                isProceedButtonEnabled = isProceedButtonEnabled(
                    domain = domain,
                    endpoint = it.awalaEndpoint,
                    token = it.token,
                ),
            )
        }
    }

    fun onEndpointInput(endpoint: String) {
        _uiState.update {
            it.copy(
                awalaEndpoint = endpoint,
                isProceedButtonEnabled = isProceedButtonEnabled(
                    domain = it.domain,
                    endpoint = endpoint,
                    token = it.token,
                ),
            )
        }
    }

    fun onTokenInput(token: String) {
        if (token.length > TOKEN_MAX_LENGTH) {
            return
        }
        _uiState.update {
            it.copy(
                token = token,
                isProceedButtonEnabled = isProceedButtonEnabled(
                    domain = it.domain,
                    endpoint = it.awalaEndpoint,
                    token = token,
                ),
            )
        }
    }

    fun onConfirmButtonClick() {
        if (_uiState.value.isSendingMessage) {
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _uiState.update { it.copy(isSendingMessage = true) }
                registrationRepository.loginToExistingAccount(
                    uiState.value.domain,
                    uiState.value.awalaEndpoint,
                    uiState.value.token,
                )
            } catch (e: AwaladroidException) {
                _showSnackbar.emit(SnackbarStringsProvider.Type.SEND_MESSAGE_ERROR)
            } finally {
                _uiState.update { it.copy(isSendingMessage = false) }
            }
        }
    }

    private fun isProceedButtonEnabled(
        domain: String,
        endpoint: String,
        token: String,
    ) = token.isNotEmptyOrBlank() && (
        endpoint.isNotEmptyOrBlank() && endpoint.matches(CorrectDomainRegex) ||
            domain.isNotEmptyOrBlank() && domain.matches(CorrectDomainRegex) && endpoint.isEmptyOrBlank()
        )
}

data class UseExistingAccountUiState(
    val domain: String = "",
    val awalaEndpoint: String = "",
    val token: String = "",
    val isProceedButtonEnabled: Boolean = false,
    val isSendingMessage: Boolean = false,
)

private const val TOKEN_MAX_LENGTH = 64
private val CorrectDomainRegex = """\p{L}{1,63}(\.\p{L}{1,63})+$""".toRegex()
