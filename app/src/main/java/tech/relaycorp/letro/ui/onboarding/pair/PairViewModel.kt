package tech.relaycorp.letro.ui.onboarding.pair

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

    private val _uiStateFlow: MutableStateFlow<PairUIStateModel> = MutableStateFlow(PairUIStateModel())
    val uiStateFlow: StateFlow<PairUIStateModel> get() = _uiStateFlow

    private val _navigateToPairingRequestSent: MutableSharedFlow<Unit> = MutableSharedFlow()
    val navigateToPairingRequestSent get() = _navigateToPairingRequestSent

    fun onAddressInput(address: String) {
        _uiStateFlow.update { it.copy(address = address) }
    }

    fun onAliasInput(alias: String) {
        _uiStateFlow.update { it.copy(alias = alias) }
    }

    fun onRequestPairingClicked() {
        accountRepository.currentAccountDataFlow.value?.let { currentAccount: AccountDataModel ->
            contactRepository.startPairingWithContact(
                accountId = currentAccount.id,
                accountAddress = currentAccount.address,
                contactAddress = _uiStateFlow.value.address,
                contactAlias = _uiStateFlow.value.alias,
            )
        }

        viewModelScope.launch {
            _navigateToPairingRequestSent.emit(Unit)
        }
    }
}
