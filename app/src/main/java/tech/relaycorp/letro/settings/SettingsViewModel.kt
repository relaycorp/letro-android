package tech.relaycorp.letro.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tech.relaycorp.letro.account.model.Account
import tech.relaycorp.letro.account.storage.repository.AccountRepository
import tech.relaycorp.letro.account.utils.AccountsSorter
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val accountsSorter: AccountsSorter,
) : ViewModel() {

    val accounts: StateFlow<List<Account>>
        get() = accountRepository.allAccounts
            .map(accountsSorter::withCurrentAccountFirst)
            .stateIn(viewModelScope, SharingStarted.Eagerly, accountRepository.allAccounts.value)

    private val _deleteAccountConfirmationDialog = MutableStateFlow(DeleteAccountDialogState())
    val deleteAccountConfirmationDialog: StateFlow<DeleteAccountDialogState>
        get() = _deleteAccountConfirmationDialog

    fun onAccountDeleteClick(account: Account) {
        _deleteAccountConfirmationDialog.update {
            it.copy(
                isShown = true,
                account = account,
            )
        }
    }

    fun onConfirmAccountDeleteClick(account: Account) {
        onConfirmAccountDeleteDialogDismissed()
        viewModelScope.launch {
            accountRepository.deleteAccount(account)
        }
    }

    fun onConfirmAccountDeleteDialogDismissed() {
        _deleteAccountConfirmationDialog.update {
            it.copy(
                isShown = false,
                account = null,
            )
        }
    }
}

data class DeleteAccountDialogState(
    val isShown: Boolean = false,
    val account: Account? = null,
)
