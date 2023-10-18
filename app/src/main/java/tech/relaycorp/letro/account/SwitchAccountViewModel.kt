package tech.relaycorp.letro.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tech.relaycorp.letro.account.model.Account
import tech.relaycorp.letro.account.storage.repository.AccountRepository
import tech.relaycorp.letro.account.utils.AccountsSorter
import javax.inject.Inject

@HiltViewModel
class SwitchAccountViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val accountsSorter: AccountsSorter,
) : ViewModel() {

    private val accounts: MutableStateFlow<List<Account>> = MutableStateFlow(emptyList())

    private val _switchAccountsBottomSheetState = MutableStateFlow(SwitchAccountsBottomSheetState())
    val switchAccountBottomSheetState: StateFlow<SwitchAccountsBottomSheetState>
        get() = _switchAccountsBottomSheetState

    init {
        viewModelScope.launch {
            accountRepository.allAccounts.collect {
                accounts.emit(
                    accountsSorter.withCurrentAccountFirst(it),
                )
            }
        }
    }

    fun onSwitchAccountsClick() {
        setSwitchBottomSheetVisible(true)
    }

    fun onSwitchAccountRequested(account: Account) {
        setSwitchBottomSheetVisible(false)
        viewModelScope.launch(Dispatchers.IO) {
            accountRepository.switchAccount(account)
        }
    }

    fun onSwitchAccountDialogDismissed() {
        setSwitchBottomSheetVisible(false)
    }

    suspend fun onSwitchAccountRequested(accountId: String): Boolean {
        setSwitchBottomSheetVisible(false)
        return accountRepository.switchAccount(accountId)
    }

    private fun setSwitchBottomSheetVisible(isVisible: Boolean) {
        _switchAccountsBottomSheetState.update {
            it.copy(
                isShown = isVisible,
                accounts = if (isVisible) accounts.value else emptyList(),
            )
        }
    }
}

data class SwitchAccountsBottomSheetState(
    val isShown: Boolean = false,
    val accounts: List<Account> = emptyList(),
)
