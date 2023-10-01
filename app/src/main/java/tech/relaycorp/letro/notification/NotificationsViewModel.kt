package tech.relaycorp.letro.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tech.relaycorp.letro.notification.model.ExtendedNotification
import tech.relaycorp.letro.notification.storage.entity.NotificationType
import tech.relaycorp.letro.notification.storage.repository.NotificationsRepository
import tech.relaycorp.letro.notification.ui.NotificationClickAction
import tech.relaycorp.letro.utils.ext.emitOn
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val notificationsRepository: NotificationsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState>
        get() = _uiState

    private val unreadNotificationsOnPreviousResume = hashSetOf<Long>()

    private val _actions: MutableSharedFlow<NotificationClickAction> = MutableSharedFlow()
    val actions: SharedFlow<NotificationClickAction>
        get() = _actions

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

    fun onNotificationClick(notification: ExtendedNotification) {
        when (notification.type) {
            NotificationType.PAIRING_COMPLETED -> {
                _actions.emitOn(NotificationClickAction.OpenContacts, viewModelScope)
            }
        }
    }

    /**
     * This logic is here, because we expect to get such behaviour:
     * 1. Unread notifications counter in TabBar must be presented UNTIL the user goes to the Notifications screen. After that we must clear this counter.
     * 2. At the same time, we must display user 2 sections of Notifications: Unread and Read.
     * 3. Unread notifications must be moved to Read as soon as user quits this screen. In other words, the user has to see all notifications as Read as he comes to this screen again
     *
     * #1 and #2 + #3 are hard to live together, because we have a single source of truth for notifications, so we have to remember which notifications were read when user came to this screen during the last visit.
     */
    fun onScreenResumed() {
        viewModelScope.launch(Dispatchers.IO) {
            if (unreadNotificationsOnPreviousResume.isNotEmpty()) {
                val alreadyReadNotificationsOnPreviousResume = _uiState.value.unreadNotifications.filter { !unreadNotificationsOnPreviousResume.contains(it.id) }
                _uiState.update {
                    it.copy(
                        unreadNotifications = alreadyReadNotificationsOnPreviousResume,
                        readNotifications = it.readNotifications
                            .plus(
                                it.unreadNotifications
                                    .filter { !alreadyReadNotificationsOnPreviousResume.contains(it) },
                            )
                            .map { it.copy(isRead = true) }
                            .sortedByDescending { it.date.timestamp },
                    )
                }
                unreadNotificationsOnPreviousResume.clear()
            }

            val unreadNotificationsOnResume = uiState.value.unreadNotifications
            notificationsRepository.readAllNotifications()
            delay(2_000L)
            unreadNotificationsOnPreviousResume.addAll(unreadNotificationsOnResume.map { it.id })
        }
    }

    private fun getNotificationsToDisplay(notifications: List<ExtendedNotification>): Notifications {
        val currentUnreadNotifications = _uiState.value.unreadNotifications
            .map { it.id }
            .toSet()

        val read = arrayListOf<ExtendedNotification>()
        val unread = arrayListOf<ExtendedNotification>()
        for (notification in notifications) {
            when {
                notification.isRead && currentUnreadNotifications.contains(notification.id) -> {
                    unread.add(notification.copy(isRead = false))
                }
                !notification.isRead -> {
                    unread.add(notification)
                }
                else -> {
                    read.add(notification)
                }
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
