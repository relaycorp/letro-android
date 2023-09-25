package tech.relaycorp.letro.home.badge

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import tech.relaycorp.letro.messages.repository.ConversationsRepository
import tech.relaycorp.letro.notification.repository.NotificationsRepository
import javax.inject.Inject

interface UnreadBadgesManager {
    val unreadConversations: StateFlow<Int>
    val unreadNotifications: StateFlow<Int>
}

class UnreadBadgesManagerImpl @Inject constructor(
    private val conversationsRepository: ConversationsRepository,
    private val notificationsRepository: NotificationsRepository,
) : UnreadBadgesManager {

    private val scope = CoroutineScope(Dispatchers.IO)

    private val _unreadConversations = MutableStateFlow(0)
    override val unreadConversations: StateFlow<Int>
        get() = _unreadConversations

    private val _unreadNotifications = MutableStateFlow(0)
    override val unreadNotifications: StateFlow<Int>
        get() = _unreadNotifications

    init {
        scope.launch {
            conversationsRepository.conversations.collect { conversations ->
                _unreadConversations.emit(
                    conversations.count { !it.isRead },
                )
            }
        }
        scope.launch {
            notificationsRepository.notifications.collect { notifications ->
                _unreadNotifications.emit(
                    notifications.count { !it.isRead },
                )
            }
        }
    }
}
