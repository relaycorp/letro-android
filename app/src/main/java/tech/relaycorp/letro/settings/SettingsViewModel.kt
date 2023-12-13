package tech.relaycorp.letro.settings

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tech.relaycorp.letro.account.model.Account
import tech.relaycorp.letro.account.model.AccountType
import tech.relaycorp.letro.account.storage.repository.AccountRepository
import tech.relaycorp.letro.account.utils.AccountsSorter
import tech.relaycorp.letro.base.BaseViewModel
import tech.relaycorp.letro.settings.SettingsViewModel.Companion.MAX_FREE_ACCOUNTS
import tech.relaycorp.letro.utils.coroutines.Dispatchers
import tech.relaycorp.letro.utils.di.AppVersion
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val accountsSorter: AccountsSorter,
    @AppVersion val appVersion: String,
    dispatchers: Dispatchers,
) : BaseViewModel(dispatchers) {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState>
        get() = _uiState

    private val _deleteAccountConfirmationDialog = MutableStateFlow(DeleteAccountDialogState())
    val deleteAccountConfirmationDialog: StateFlow<DeleteAccountDialogState>
        get() = _deleteAccountConfirmationDialog

    init {
        viewModelScope.launch(dispatchers.IO) {
            accountRepository.allAccounts
                .map(accountsSorter::withCurrentAccountFirst)
                .collect { accounts ->
                    val createdAccounts = accounts.count { it.accountType == AccountType.CREATED_FROM_SCRATCH }
                    _uiState.update {
                        it.copy(
                            accounts = accounts,
                            infoViewType = if (createdAccounts >= MAX_FREE_ACCOUNTS) SettingsAccountsInfoViewType.Warning(MAX_FREE_ACCOUNTS) else SettingsAccountsInfoViewType.Info(createdAccounts = createdAccounts, maxFreeAccounts = MAX_FREE_ACCOUNTS),
                        )
                    }
                }
        }
    }

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

    companion object {
        const val MAX_FREE_ACCOUNTS = 2
    }
}

data class SettingsUiState(
    val accounts: List<Account> = emptyList(),
    val infoViewType: SettingsAccountsInfoViewType = SettingsAccountsInfoViewType.Info(
        createdAccounts = 0,
        maxFreeAccounts = MAX_FREE_ACCOUNTS,
    ),
)

data class DeleteAccountDialogState(
    val isShown: Boolean = false,
    val account: Account? = null,
)

sealed interface SettingsAccountsInfoViewType {
    data class Info(
        val createdAccounts: Int,
        val maxFreeAccounts: Int,
    ) : SettingsAccountsInfoViewType

    data class Warning(
        val maxFreeAccounts: Int,
    ) : SettingsAccountsInfoViewType
}
