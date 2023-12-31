package tech.relaycorp.letro.account.registration

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tech.relaycorp.awaladroid.AwaladroidException
import tech.relaycorp.letro.account.registration.storage.DuplicateAccountIdException
import tech.relaycorp.letro.account.registration.storage.RegistrationRepository
import tech.relaycorp.letro.base.BaseViewModel
import tech.relaycorp.letro.base.utils.SnackbarString
import tech.relaycorp.letro.ui.navigation.Route
import tech.relaycorp.letro.ui.utils.SnackbarStringsProvider
import tech.relaycorp.letro.utils.coroutines.Dispatchers
import tech.relaycorp.letro.utils.ext.decodeFromUTF
import tech.relaycorp.letro.utils.ext.isNotEmptyOrBlank
import javax.inject.Inject

@HiltViewModel
class UseExistingAccountViewModel @Inject constructor(
    private val registrationRepository: RegistrationRepository,
    savedStateHandle: SavedStateHandle,
    dispatchers: Dispatchers,
) : BaseViewModel(dispatchers) {

    private val _uiState = MutableStateFlow(
        UseExistingAccountUiState(
            domain = (savedStateHandle.get(Route.UseExistingAccount.DOMAIN_ENCODED) as? String)?.decodeFromUTF() ?: "",
            awalaEndpoint = (savedStateHandle.get(Route.UseExistingAccount.AWALA_ENDPOINT_ENCODED) as? String)?.decodeFromUTF() ?: "",
            token = (savedStateHandle.get(Route.UseExistingAccount.TOKEN_ENCODED) as? String)?.decodeFromUTF() ?: "",
        ),
    )
    val uiState: StateFlow<UseExistingAccountUiState>
        get() = _uiState

    init {
        _uiState.update {
            it.copy(
                isProceedButtonEnabled = isProceedButtonEnabled(),
            )
        }
    }

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
        viewModelScope.launch(dispatchers.IO) {
            try {
                updateIsSendingMessage(true)
                registrationRepository.loginToExistingAccount(
                    uiState.value.domain,
                    uiState.value.awalaEndpoint,
                    uiState.value.token,
                )
                delay(2000L)
                updateIsSendingMessage(false)
            } catch (e: AwaladroidException) {
                showSnackbarDebounced.emit(
                    SnackbarString(SnackbarStringsProvider.Type.SEND_MESSAGE_ERROR),
                )
                updateIsSendingMessage(false)
            } catch (e: DuplicateAccountIdException) {
                showSnackbarDebounced.emit(
                    SnackbarString(
                        SnackbarStringsProvider.Type.ACCOUNT_LINKING_ID_ALREADY_EXISTS,
                        args = arrayOf(_uiState.value.domain),
                    ),
                )
                updateIsSendingMessage(false)
            }
        }
    }

    private fun isProceedButtonEnabled(
        domain: String = _uiState.value.domain,
        endpoint: String = _uiState.value.awalaEndpoint,
        token: String = _uiState.value.token,
    ) = token.isNotEmptyOrBlank() && domain.isNotEmptyOrBlank() && domain.matches(CorrectDomainRegex) && (
        endpoint.isEmpty() || endpoint.isNotEmptyOrBlank() && endpoint.matches(CorrectDomainRegex)
        )

    private fun updateIsSendingMessage(isSendingMessage: Boolean) {
        _uiState.update {
            it.copy(
                isSendingMessage = isSendingMessage,
            )
        }
    }
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
