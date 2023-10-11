package tech.relaycorp.letro.account.registration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tech.relaycorp.letro.account.registration.storage.RegistrationRepository
import tech.relaycorp.letro.account.registration.utils.RegistrationDomainProvider
import tech.relaycorp.letro.utils.ext.isNotEmptyOrBlank
import javax.inject.Inject

const val HARDCODED_TOKEN = "652697d03fc3be0d7666c3bd"

@HiltViewModel
class UseExistingAccountViewModel @Inject constructor(
    private val registrationRepository: RegistrationRepository,
): ViewModel() {

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
        registrationRepository.loginToExistingAccount(
            uiState.value.domain,
            uiState.value.awalaEndpoint,
            uiState.value.token
        )
    }

    private fun isProceedButtonEnabled(
        domain: String,
        endpoint: String,
        token: String,
    ) = token.isNotEmptyOrBlank() && (endpoint.isNotEmptyOrBlank() || domain.isNotEmptyOrBlank() && domain.matches(CORRECT_DOMAIN_REGEX))

}

data class UseExistingAccountUiState(
    val domain: String = "chores.fans",
    val awalaEndpoint: String = "",
    val token: String = HARDCODED_TOKEN,
    val isProceedButtonEnabled: Boolean = false,
)

private const val TOKEN_MAX_LENGTH = 64
private val CORRECT_DOMAIN_REGEX = """\p{L}{1,63}(\.\p{L}{1,63})+$""".toRegex()