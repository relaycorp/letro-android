package tech.relaycorp.letro.main.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tech.relaycorp.letro.contacts.storage.repository.ContactsRepository
import tech.relaycorp.letro.conversation.list.selection.ConversationSelector
import tech.relaycorp.letro.main.home.badge.UnreadBadgesManager
import tech.relaycorp.letro.main.home.ui.HomeFloatingActionButtonConfig
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val unreadBadgesManager: UnreadBadgesManager,
    private val contactsRepository: ContactsRepository,
    private val conversationSelector: ConversationSelector,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState>
        get() = _uiState

    private val _createNewConversationSignal: MutableSharedFlow<Unit> = MutableSharedFlow()
    val createNewConversationSignal: SharedFlow<Unit>
        get() = _createNewConversationSignal

    private val _showNoContactsSnackbarSignal: MutableSharedFlow<Unit> = MutableSharedFlow()
    val showNoContactsSnackbarSignal: MutableSharedFlow<Unit>
        get() = _showNoContactsSnackbarSignal

    init {
        viewModelScope.launch {
            unreadBadgesManager.unreadConversations.collect {
                updateTabBadges(
                    unreadConversations = it,
                )
            }
        }
        viewModelScope.launch {
            unreadBadgesManager.unreadNotifications.collect {
                updateTabBadges(
                    unreadNotifications = it,
                )
            }
        }
        viewModelScope.launch {
            contactsRepository.contactsState.collect {
                _uiState.update {
                    it.copy(
                        floatingActionButtonConfig = getFloatingActionButtonConfig(),
                    )
                }
            }
        }
        viewModelScope.launch {
            conversationSelector.selectedConversations.collect { selectedConversations ->
                _uiState.update {
                    it.copy(
                        selectedConversations = selectedConversations.size,
                    )
                }
            }
        }
    }

    fun onTabClick(index: Int) {
        if (index == _uiState.value.currentTab) {
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update {
                it.copy(
                    currentTab = index,
                    floatingActionButtonConfig = getFloatingActionButtonConfig(index),
                )
            }
        }
    }

    fun onOutsideFloatingMenuClicked() {
        closeContactFloatingMenu()
    }

    fun onOptionFromContactsFloatingMenuClicked() {
        closeContactFloatingMenu()
    }

    fun onFloatingActionButtonClick() {
        viewModelScope.launch {
            when (uiState.value.currentTab) {
                TAB_CHATS -> {
                    if (contactsRepository.contactsState.value.isPairedContactExist) {
                        _createNewConversationSignal.emit(Unit)
                    } else {
                        _showNoContactsSnackbarSignal.emit(Unit)
                    }
                }
                TAB_CONTACTS -> {
                    _uiState.update {
                        it.copy(
                            isAddContactFloatingMenuVisible = !it.isAddContactFloatingMenuVisible,
                        )
                    }
                }
            }
        }
    }

    private fun closeContactFloatingMenu() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isAddContactFloatingMenuVisible = false,
                )
            }
        }
    }

    private fun updateTabBadges(
        unreadConversations: Int? = null,
        unreadNotifications: Int? = null,
    ) {
        _uiState.update {
            it.copy(
                tabCounters = HashMap(it.tabCounters).apply {
                    unreadConversations?.let {
                        put(TAB_CHATS, getUnreadBadgeText(it))
                    }
                    unreadNotifications?.let {
                        put(TAB_NOTIFICATIONS, getUnreadBadgeText(it))
                    }
                },
            )
        }
    }

    private fun getUnreadBadgeText(count: Int) = when {
        count > 9 -> "9+"
        count > 0 -> count.toString()
        else -> null
    }

    private fun getFloatingActionButtonConfig(
        tabIndex: Int = _uiState.value.currentTab,
    ) = when (tabIndex) {
        TAB_CHATS -> HomeFloatingActionButtonConfig.ChatListFloatingActionButtonConfig
        TAB_CONTACTS -> {
            if (contactsRepository.contactsState.value.totalCount > 0) {
                HomeFloatingActionButtonConfig.ContactsFloatingActionButtonConfig
            } else {
                null
            }
        }
        TAB_NOTIFICATIONS -> null
        else -> throw IllegalStateException("Unsupported tab with index $tabIndex")
    }
}

data class HomeUiState(
    val currentTab: Int = TAB_CHATS,
    val tabCounters: Map<Int, String?> = emptyMap(),
    val floatingActionButtonConfig: HomeFloatingActionButtonConfig? = HomeFloatingActionButtonConfig.ChatListFloatingActionButtonConfig,
    val isAddContactFloatingMenuVisible: Boolean = false,
    val selectedConversations: Int = 0,
)

const val TAB_CHATS = 0
const val TAB_CONTACTS = 1
const val TAB_NOTIFICATIONS = 2
