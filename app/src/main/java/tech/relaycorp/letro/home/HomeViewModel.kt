package tech.relaycorp.letro.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState>
        get() = _uiState

    private val _createNewMessageSignal: MutableSharedFlow<Unit> = MutableSharedFlow()
    val createNewMessageSignal: SharedFlow<Unit>
        get() = _createNewMessageSignal

    fun onTabClick(index: Int) {
        viewModelScope.launch {
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
                    _createNewMessageSignal.emit(Unit)
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

    private fun getFloatingActionButtonConfig(tabIndex: Int) = when (tabIndex) {
        TAB_CHATS -> HomeFloatingActionButtonConfig.ChatListFloatingActionButtonConfig
        TAB_CONTACTS -> HomeFloatingActionButtonConfig.ContactsFloatingActionButtonConfig
        TAB_NOTIFICATIONS -> null
        else -> throw IllegalStateException("Unsupported tab with index $tabIndex")
    }
}

data class HomeUiState(
    val currentTab: Int = TAB_CHATS,
    val floatingActionButtonConfig: HomeFloatingActionButtonConfig? = HomeFloatingActionButtonConfig.ChatListFloatingActionButtonConfig,
    val isAddContactFloatingMenuVisible: Boolean = false,
)

const val TAB_CHATS = 0
const val TAB_CONTACTS = 1
const val TAB_NOTIFICATIONS = 2