package tech.relaycorp.letro.ui.onboarding.useExistingAccount

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow

@HiltViewModel
class UseExistingAccountViewModel @Inject constructor() : ViewModel() {

    private val _domainNameUIFlow: MutableStateFlow<String> = MutableStateFlow("")
    val domainNameUIFlow get() = _domainNameUIFlow

    private val _tokenUIFlow: MutableStateFlow<String> = MutableStateFlow("")
    val tokenUIFlow get() = _tokenUIFlow

    fun onDomainNameInput(domainName: String) {
        _domainNameUIFlow.value = domainName
    }

    fun onTokenInput(token: String) {
        _tokenUIFlow.value = token
    }

    fun onConfirmClicked() {
        // TODO
    }
}