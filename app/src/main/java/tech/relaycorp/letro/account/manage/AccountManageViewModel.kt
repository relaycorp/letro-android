package tech.relaycorp.letro.account.manage

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tech.relaycorp.letro.account.manage.avatar.AvatarRepository
import tech.relaycorp.letro.account.model.Account
import tech.relaycorp.letro.account.storage.repository.AccountRepository
import tech.relaycorp.letro.base.BaseViewModel
import tech.relaycorp.letro.base.utils.SnackbarString
import tech.relaycorp.letro.conversation.attachments.filepicker.FileSizeExceedsLimitException
import tech.relaycorp.letro.ui.navigation.Route
import tech.relaycorp.letro.ui.utils.SnackbarStringsProvider
import tech.relaycorp.letro.utils.coroutines.Dispatchers
import javax.inject.Inject

@HiltViewModel
class AccountManageViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val avatarRepository: AvatarRepository,
    savedStateHandle: SavedStateHandle,
    dispatchers: Dispatchers,
) : BaseViewModel(dispatchers) {

    private val accountId = savedStateHandle.get<String>(Route.AccountManage.ACCOUNT_ID)?.toLong()!!
    private var account: Account? = null

    val uiState: StateFlow<AccountManageUiState> by lazy {
        accountRepository.allAccounts
            .mapNotNull { it.firstOrNull { it.id == accountId } }
            .onEach { account = it }
            .map { AccountManageUiState(it) }
            .stateIn(viewModelScope, SharingStarted.Eagerly, AccountManageUiState())
    }

    private val _deleteAccountConfirmationDialog = MutableStateFlow(DeleteAccountDialogState())
    val deleteAccountConfirmationDialog: StateFlow<DeleteAccountDialogState>
        get() = _deleteAccountConfirmationDialog

    fun onAvatarPicked(uri: String?) {
        uri ?: return
        viewModelScope.launch(dispatchers.IO) {
            val account = account ?: return@launch
            try {
                avatarRepository.saveAvatar(account, uri)
            } catch (e: FileSizeExceedsLimitException) {
                showSnackbarDebounced.emit(
                    SnackbarString(SnackbarStringsProvider.Type.AVATAR_TOO_BIG_ERROR),
                )
            }
        }
    }

    fun onAvatarDeleteClick() {
        viewModelScope.launch(dispatchers.IO) {
            val account = account ?: return@launch
            avatarRepository.deleteAvatar(account)
        }
    }

    fun onAccountDeleteClick() {
        _deleteAccountConfirmationDialog.update {
            it.copy(
                isShown = true,
                account = account,
            )
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun onConfirmAccountDeleteClick(account: Account) {
        onConfirmAccountDeleteDialogDismissed()
        GlobalScope.launch(dispatchers.IO) {
            accountRepository.deleteAccount(account)
        }
    }

    fun onConfirmAccountDeleteDialogDismissed() {
        _deleteAccountConfirmationDialog.update {
            it.copy(
                isShown = false,
            )
        }
    }
}

data class AccountManageUiState(
    val account: Account? = null,
)

data class DeleteAccountDialogState(
    val isShown: Boolean = false,
    val account: Account? = null,
)
