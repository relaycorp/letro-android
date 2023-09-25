package tech.relaycorp.letro.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tech.relaycorp.letro.notification.model.ExtendedNotification
import tech.relaycorp.letro.notification.repository.NotificationsRepository
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val notificationsRepository: NotificationsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState>
        get() = _uiState

    init {
        viewModelScope.launch {
            notificationsRepository.notifications.collect { newNotifications ->
                val notifications = getNotificationsToDisplay(newNotifications)
                _uiState.update {
                    it.copy(
                        unreadNotifications = notifications.unread,
                        readNotifications = notifications.read,
                    )
                }
            }
        }
    }

    private fun getNotificationsToDisplay(notifications: List<ExtendedNotification>): Notifications {
        val read = arrayListOf<ExtendedNotification>()
        val unread = arrayListOf<ExtendedNotification>()
        for (notification in notifications) {
            if (notification.isRead) {
                read.add(notification)
            } else {
                unread.add(notification)
            }
        }
        return Notifications(
            read = read,
            unread = unread,
        )
    }
}

data class NotificationsUiState(
    val unreadNotifications: List<ExtendedNotification> = emptyList(),
    val readNotifications: List<ExtendedNotification> = emptyList(),
)

private data class Notifications(
    val read: List<ExtendedNotification>,
    val unread: List<ExtendedNotification>,
)
