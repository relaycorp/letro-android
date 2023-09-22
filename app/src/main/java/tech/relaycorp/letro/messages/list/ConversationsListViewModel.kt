package tech.relaycorp.letro.messages.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import tech.relaycorp.letro.account.model.Account
import tech.relaycorp.letro.account.storage.AccountRepository
import tech.relaycorp.letro.messages.model.ExtendedConversation
import tech.relaycorp.letro.messages.onboarding.ConversationsOnboardingManager
import tech.relaycorp.letro.messages.repository.ConversationsRepository
import javax.inject.Inject

@HiltViewModel
class ConversationsListViewModel @Inject constructor(
    private val conversationsRepository: ConversationsRepository,
    private val conversationsOnboardingManager: ConversationsOnboardingManager,
    private val accountRepository: AccountRepository,
) : ViewModel() {

    val conversations: StateFlow<List<ExtendedConversation>>
        get() = conversationsRepository.conversations

    private val _isOnboardingMessageVisible = MutableStateFlow(false)
    val isOnboardingMessageVisible: StateFlow<Boolean>
        get() = _isOnboardingMessageVisible

    private var currentAccount: Account? = null

    init {
        viewModelScope.launch {
            accountRepository.currentAccount.collect {
                currentAccount = it
                if (it != null) {
                    _isOnboardingMessageVisible.emit(!conversationsOnboardingManager.isOnboardingMessageWasShown(it.veraId))
                } else {
                    _isOnboardingMessageVisible.emit(false)
                }
            }
        }
    }

    fun onCloseOnboardingButtonClick() {
        viewModelScope.launch {
            currentAccount?.let {
                conversationsOnboardingManager.saveOnboardingMessageShown(it.veraId)
                _isOnboardingMessageVisible.emit(false)
            }
        }
    }
}
