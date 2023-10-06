package tech.relaycorp.letro.notification.storage.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import tech.relaycorp.letro.account.model.Account
import tech.relaycorp.letro.account.storage.repository.AccountRepository
import tech.relaycorp.letro.notification.converter.ExtendedNotificationConverter
import tech.relaycorp.letro.notification.model.ExtendedNotification
import tech.relaycorp.letro.notification.storage.dao.NotificationsDao
import tech.relaycorp.letro.notification.storage.entity.Notification
import tech.relaycorp.letro.utils.di.IODispatcher
import javax.inject.Inject

interface NotificationsRepository {
    val notifications: StateFlow<List<ExtendedNotification>>

    suspend fun readAllNotifications()
}

class NotificationsRepositoryImpl @Inject constructor(
    private val notificationsDao: NotificationsDao,
    private val accountRepository: AccountRepository,
    private val extendedNotificationConverter: ExtendedNotificationConverter,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
) : NotificationsRepository {

    private val scope = CoroutineScope(ioDispatcher)

    private val _notifications: MutableStateFlow<List<Notification>> = MutableStateFlow(emptyList())
    override val notifications: StateFlow<List<ExtendedNotification>>
        get() = _notifications
            .map { it.map { extendedNotificationConverter.convert(it) } }
            .stateIn(scope, SharingStarted.Eagerly, emptyList())

    private var notificationsCollectionJob: Job? = null

    init {
        scope.launch {
            accountRepository.currentAccount.collect {
                if (it != null) {
                    startCollectNotifications(it)
                } else {
                    notificationsCollectionJob?.cancel()
                    notificationsCollectionJob = null
                    _notifications.emit(emptyList())
                }
            }
        }
    }

    override suspend fun readAllNotifications() {
        _notifications.value
            .filter { !it.isRead }
            .forEach {
                notificationsDao.update(
                    it.copy(
                        isRead = true,
                    ),
                )
            }
    }

    private fun startCollectNotifications(currentAccount: Account) {
        notificationsCollectionJob?.cancel()
        notificationsCollectionJob = null
        notificationsCollectionJob = scope.launch {
            notificationsDao.getAll().collect {
                _notifications.emit(
                    it
                        .filter { it.ownerId == currentAccount.accountId }
                        .sortedByDescending { it.timestamp },
                )
            }
        }
    }
}
