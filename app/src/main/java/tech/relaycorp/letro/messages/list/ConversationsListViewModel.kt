package tech.relaycorp.letro.messages.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tech.relaycorp.letro.R
import tech.relaycorp.letro.account.model.Account
import tech.relaycorp.letro.account.storage.AccountRepository
import tech.relaycorp.letro.messages.list.section.ConversationSectionInfo
import tech.relaycorp.letro.messages.onboarding.ConversationsOnboardingManager
import tech.relaycorp.letro.messages.repository.ConversationsRepository
import javax.inject.Inject

@Suppress("NAME_SHADOWING")
@HiltViewModel
class ConversationsListViewModel @Inject constructor(
    private val conversationsRepository: ConversationsRepository,
    private val conversationsOnboardingManager: ConversationsOnboardingManager,
    private val accountRepository: AccountRepository,
) : ViewModel() {

    private val _conversationSectionInfoState = MutableStateFlow(ConversationsSectionState())
    val conversationSectionState: StateFlow<ConversationsSectionState>
        get() = _conversationSectionInfoState

    val conversations: StateFlow<ConversationsListContent>
        get() = combine(
            conversationsRepository.conversations,
            conversationSectionState,
        ) { conversations, currentTab ->
            val conversations = when (currentTab.currentSection) {
                ConversationSectionInfo.Inbox -> {
                    conversations.filter { it.messages.any { !it.isOutgoing } }
                }
                ConversationSectionInfo.Sent -> {
                    conversations.filter { it.messages.any { it.isOutgoing } }
                }
            }
            if (conversations.isNotEmpty()) {
                ConversationsListContent.Conversations(conversations)
            } else {
                getEmptyConversationsStubInfo()
            }
        }
            .stateIn(viewModelScope, SharingStarted.Eagerly, initialValue = getEmptyConversationsStubInfo())

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

    fun onSectionChosen(section: ConversationSectionInfo) {
        _conversationSectionInfoState.update {
            it.copy(
                currentSection = section,
                sectionSelector = it.sectionSelector.copy(
                    isOpened = false,
                ),
            )
        }
    }

    fun onConversationSectionSelectorClick() {
        _conversationSectionInfoState.update {
            it.copy(
                sectionSelector = it.sectionSelector.copy(
                    isOpened = true,
                ),
            )
        }
    }

    fun onConversationSectionDialogDismissed() {
        _conversationSectionInfoState.update {
            it.copy(
                sectionSelector = it.sectionSelector.copy(
                    isOpened = false,
                ),
            )
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

    private fun getEmptyConversationsStubInfo() = ConversationsListContent.Empty(
        image = when (_conversationSectionInfoState.value.currentSection) {
            ConversationSectionInfo.Inbox -> R.drawable.empty_inbox_image
            ConversationSectionInfo.Sent -> R.drawable.empty_inbox_image
        },
        text = when (_conversationSectionInfoState.value.currentSection) {
            ConversationSectionInfo.Inbox -> R.string.conversations_empty_inbox_stub
            ConversationSectionInfo.Sent -> R.string.conversations_empty_sent_stub
        },
    )
}

data class ConversationsSectionState(
    val currentSection: ConversationSectionInfo = ConversationSectionInfo.Inbox,
    val sectionSelector: ConversationsSectionSelector = ConversationsSectionSelector(),
)

data class ConversationsSectionSelector(
    val isOpened: Boolean = false,
    val sections: List<ConversationSectionInfo> = ConversationSectionInfo.allSections(),
)
