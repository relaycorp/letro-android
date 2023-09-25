package tech.relaycorp.letro.home

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
import tech.relaycorp.letro.home.badge.UnreadBadgesManager
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val unreadBadgesManager: UnreadBadgesManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState>
        get() = _uiState

    private val _createNewConversationSignal: MutableSharedFlow<Unit> = MutableSharedFlow()
    val createNewConversationSignal: SharedFlow<Unit>
        get() = _createNewConversationSignal

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

    fun onOptionFromContactsFloatingMenuClicked() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isAddContactFloatingMenuVisible = false,
                )
            }
        }
    }

    fun onFloatingActionButtonClick() {
        viewModelScope.launch {
            when (uiState.value.currentTab) {
                TAB_CHATS -> {
                    _createNewConversationSignal.emit(Unit)
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

    private fun getFloatingActionButtonConfig(tabIndex: Int) = when (tabIndex) {
        TAB_CHATS -> HomeFloatingActionButtonConfig.ChatListFloatingActionButtonConfig
        TAB_CONTACTS -> HomeFloatingActionButtonConfig.ContactsFloatingActionButtonConfig
        TAB_NOTIFICATIONS -> null
        else -> throw IllegalStateException("Unsupported tab with index $tabIndex")
    }
}

data class HomeUiState(
    val currentTab: Int = TAB_CHATS,
    val tabCounters: Map<Int, String?> = emptyMap(),
    val floatingActionButtonConfig: HomeFloatingActionButtonConfig? = HomeFloatingActionButtonConfig.ChatListFloatingActionButtonConfig,
    val isAddContactFloatingMenuVisible: Boolean = false,
)

const val TAB_CHATS = 0
const val TAB_CONTACTS = 1
const val TAB_NOTIFICATIONS = 2
