package tech.relaycorp.letro.conversation.list

import android.util.Log
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tech.relaycorp.letro.R
import tech.relaycorp.letro.account.model.Account
import tech.relaycorp.letro.account.storage.repository.AccountRepository
import tech.relaycorp.letro.base.BaseViewModel
import tech.relaycorp.letro.base.utils.SnackbarString
import tech.relaycorp.letro.conversation.list.onboarding.ConversationsOnboardingManager
import tech.relaycorp.letro.conversation.list.section.ConversationSectionInfo
import tech.relaycorp.letro.conversation.list.selection.ConversationSelector
import tech.relaycorp.letro.conversation.list.ui.ConversationsListContent
import tech.relaycorp.letro.conversation.model.ExtendedConversation
import tech.relaycorp.letro.conversation.storage.repository.ConversationsRepository
import tech.relaycorp.letro.conversation.storage.repository.ConversationsRepositoryImpl
import tech.relaycorp.letro.main.home.badge.UnreadBadgesManager
import tech.relaycorp.letro.ui.utils.SnackbarStringsProvider
import tech.relaycorp.letro.utils.coroutines.Dispatchers
import tech.relaycorp.letro.utils.ext.emitOn
import javax.inject.Inject

@Suppress("NAME_SHADOWING")
@HiltViewModel
class ConversationsListViewModel @Inject constructor(
    private val conversationsRepository: ConversationsRepository,
    private val conversationsOnboardingManager: ConversationsOnboardingManager,
    private val conversationSelector: ConversationSelector,
    private val accountRepository: AccountRepository,
    private val unreadBadgesManager: UnreadBadgesManager,
    dispatchers: Dispatchers,
) : BaseViewModel(dispatchers) {

    private val _conversationSectionInfoState = MutableStateFlow(ConversationsSectionState())
    val conversationSectionState: StateFlow<ConversationsSectionState>
        get() = _conversationSectionInfoState

    val isSectionSelectorVisible: StateFlow<Boolean>
        get() = conversationSelector.selectedConversations
            .map { it.isEmpty() }
            .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    private val _openConversation = MutableSharedFlow<ExtendedConversation>()
    val openConversation: Flow<ExtendedConversation>
        get() = _openConversation

    val conversations: StateFlow<ConversationsListContent>
        get() = combine(
            conversationsRepository.conversations,
            conversationSelector.selectedConversations,
            conversationSectionState,
        ) { conversations, selectedConversations, currentTab ->
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
            Log.d(ConversationsRepositoryImpl.TAG, "Displaying new messages for state ${currentTab.currentSection} with size = ${conversations.size}. Selected conversations size = ${selectedConversations.size}")
            if (conversations.isNotEmpty()) {
                ConversationsListContent.Conversations(
                    conversations.map {
                        ConversationUiModel(
                            conversation = it,
                            isSelected = selectedConversations.contains(it.conversationId),
                        )
                    },
                )
            } else {
                getEmptyConversationsStubInfo()
            }
        }
            .stateIn(viewModelScope, SharingStarted.Eagerly, initialValue = getEmptyConversationsStubInfo())

    private val _isOnboardingMessageVisible = MutableStateFlow(false)
    val isOnboardingMessageVisible: StateFlow<Boolean>
        get() = _isOnboardingMessageVisible

    private val _isConfirmDeleteConversationsDialogVisible = MutableStateFlow(false)
    val isConfirmDeleteConversationsDialogVisible: StateFlow<Boolean>
        get() = _isConfirmDeleteConversationsDialogVisible

    private var currentAccount: Account? = null

    init {
        viewModelScope.launch {
            combine(
                accountRepository.currentAccount,
                conversationSelector.selectedConversations,
            ) { account, selectedConversations ->
                currentAccount = account
                if (account != null) {
                    _isOnboardingMessageVisible.emit(!conversationsOnboardingManager.isOnboardingMessageWasShown(account.accountId) && selectedConversations.isEmpty())
                } else {
                    _isOnboardingMessageVisible.emit(false)
                }
            }.collect {}
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

    fun onConversationClick(conversation: ConversationUiModel) {
        val selectedConversations = conversationSelector.selectedConversations.value
        when {
            selectedConversations.isEmpty() -> _openConversation.emitOn(conversation.conversation, viewModelScope)
            selectedConversations.contains(conversation.conversation.conversationId) -> conversationSelector.unselectConversation(conversation.conversation.conversationId)
            else -> conversationSelector.selectConversation(conversation.conversation.conversationId)
        }
    }

    fun onConversationLongClick(conversation: ConversationUiModel) {
        val selectedConversations = conversationSelector.selectedConversations.value
        when {
            selectedConversations.contains(conversation.conversation.conversationId) -> conversationSelector.unselectConversation(conversation.conversation.conversationId)
            else -> conversationSelector.selectConversation(conversation.conversation.conversationId)
        }
    }

    fun onArchiveSelectedConversationsClick() {
        val selectedConversations = conversationSelector.selectedConversations.value
        val isArchivedNow = conversationSectionState.value.currentSection is ConversationSectionInfo.Archived
        selectedConversations.forEach {
            conversationsRepository.archiveConversation(it.toString(), !isArchivedNow)
        }
        conversationSelector.unselectAll()
        showSnackbarDebounced.emitOn(if (isArchivedNow) SnackbarString(SnackbarStringsProvider.Type.CONVERSATIONS_UNARCHIVED) else SnackbarString(SnackbarStringsProvider.Type.CONVERSATIONS_ARCHIVED), viewModelScope)
    }

    fun onDeleteSelectedConversationsClick() {
        _isConfirmDeleteConversationsDialogVisible.value = true
    }

    fun onDeleteConversationDialogDismissed() {
        _isConfirmDeleteConversationsDialogVisible.value = false
    }

    fun onConfirmConversationsDeleteClicked() {
        _isConfirmDeleteConversationsDialogVisible.value = false
        val selectedConversations = conversationSelector.selectedConversations.value
        selectedConversations.forEach {
            conversationsRepository.deleteConversation(it.toString())
        }
        conversationSelector.unselectAll()
        showSnackbarDebounced.emitOn(SnackbarString(SnackbarStringsProvider.Type.CONVERSATIONS_DELETED), viewModelScope)
    }

    fun onCancelConversationsSelectionClick() {
        conversationSelector.unselectAll()
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

data class ConversationUiModel(
    val conversation: ExtendedConversation,
    val isSelected: Boolean = false,
)

data class ConversationsSectionState(
    val currentSection: ConversationSectionInfo = ConversationSectionInfo.Inbox(0),
    val sectionSelector: ConversationsSectionSelector = ConversationsSectionSelector(),
)

data class ConversationsSectionSelector(
    val isOpened: Boolean = false,
    val sections: List<ConversationSectionInfo> = ConversationSectionInfo.allSections(0),
)
