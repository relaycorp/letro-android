package tech.relaycorp.letro.conversation.list

import android.util.Log
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
import tech.relaycorp.letro.account.storage.repository.AccountRepository
import tech.relaycorp.letro.conversation.list.onboarding.ConversationsOnboardingManager
import tech.relaycorp.letro.conversation.list.section.ConversationSectionInfo
import tech.relaycorp.letro.conversation.list.ui.ConversationsListContent
import tech.relaycorp.letro.conversation.storage.repository.ConversationsRepository
import tech.relaycorp.letro.conversation.storage.repository.ConversationsRepositoryImpl
import tech.relaycorp.letro.main.home.badge.UnreadBadgesManager
import javax.inject.Inject

@Suppress("NAME_SHADOWING")
@HiltViewModel
class ConversationsListViewModel @Inject constructor(
    private val conversationsRepository: ConversationsRepository,
    private val conversationsOnboardingManager: ConversationsOnboardingManager,
    private val accountRepository: AccountRepository,
    private val unreadBadgesManager: UnreadBadgesManager,
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
                is ConversationSectionInfo.Inbox -> {
                    conversations.filter { it.messages.any { !it.isOutgoing } && !it.isArchived }
                }
                ConversationSectionInfo.Sent -> {
                    conversations.filter { it.messages.any { it.isOutgoing } && !it.isArchived }
                }
                ConversationSectionInfo.Archived -> {
                    conversations.filter { it.isArchived }
                }
            }
            Log.d(ConversationsRepositoryImpl.TAG, "Displaying new messages for state ${currentTab.currentSection} with size = ${conversations.size}")
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
                    _isOnboardingMessageVisible.emit(!conversationsOnboardingManager.isOnboardingMessageWasShown(it.accountId))
                } else {
                    _isOnboardingMessageVisible.emit(false)
                }
            }
        }
        viewModelScope.launch {
            unreadBadgesManager.unreadConversations.collect { unreadMessages ->
                val sections = ConversationSectionInfo.allSections(unreadMessages)
                _conversationSectionInfoState.update {
                    it.copy(
                        currentSection = if (it.currentSection is ConversationSectionInfo.Inbox) sections.find { it is ConversationSectionInfo.Inbox }!! else it.currentSection,
                        sectionSelector = it.sectionSelector.copy(
                            sections = sections,
                        ),
                    )
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
                conversationsOnboardingManager.saveOnboardingMessageShown(it.accountId)
                _isOnboardingMessageVisible.emit(false)
            }
        }
    }

    private fun getEmptyConversationsStubInfo() = ConversationsListContent.Empty(
        image = when (_conversationSectionInfoState.value.currentSection) {
            is ConversationSectionInfo.Inbox -> R.drawable.empty_inbox_image
            ConversationSectionInfo.Sent -> R.drawable.empty_sent_image
            ConversationSectionInfo.Archived -> R.drawable.archive_stub
        },
        text = when (_conversationSectionInfoState.value.currentSection) {
            is ConversationSectionInfo.Inbox -> R.string.conversations_empty_inbox_stub
            ConversationSectionInfo.Sent -> R.string.conversations_empty_sent_stub
            ConversationSectionInfo.Archived -> R.string.conversations_empty_archive_stub
        },
    )
}

data class ConversationsSectionState(
    val currentSection: ConversationSectionInfo = ConversationSectionInfo.Inbox(0),
    val sectionSelector: ConversationsSectionSelector = ConversationsSectionSelector(),
)

data class ConversationsSectionSelector(
    val isOpened: Boolean = false,
    val sections: List<ConversationSectionInfo> = ConversationSectionInfo.allSections(0),
)
