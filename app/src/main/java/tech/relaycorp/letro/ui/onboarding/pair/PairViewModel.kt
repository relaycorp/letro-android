package tech.relaycorp.letro.ui.onboarding.pair

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tech.relaycorp.letro.data.entity.AccountDataModel
import tech.relaycorp.letro.repository.AccountRepository
import tech.relaycorp.letro.repository.ContactRepository
import javax.inject.Inject

@HiltViewModel
class PairViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val contactRepository: ContactRepository,
) : ViewModel() {

    private val _addressUIFlow: MutableStateFlow<String> = MutableStateFlow("")
    val idUIFlow get() = _addressUIFlow

    private val _aliasUIFlow: MutableStateFlow<String> = MutableStateFlow("")
    val aliasUIFlow get() = _aliasUIFlow

    private val _navigateToPairingRequestSent: MutableSharedFlow<Unit> = MutableSharedFlow()
    val navigateToPairingRequestSent get() = _navigateToPairingRequestSent

    fun onIdInput(id: String) {
        _addressUIFlow.update { id }
    }

    fun onAliasInput(alias: String) {
        _aliasUIFlow.update { alias }
    }

    fun onRequestPairingClicked() {
        accountRepository.currentAccountDataFlow.value?.let { currentAccount: AccountDataModel ->
            contactRepository.startPairingWithContact(
                accountId = currentAccount.id,
                contactAddress = _aliasUIFlow.value,
                contactAlias = _addressUIFlow.value,
            )
        }

        viewModelScope.launch {
            _navigateToPairingRequestSent.emit(Unit)
        }
    }
}
