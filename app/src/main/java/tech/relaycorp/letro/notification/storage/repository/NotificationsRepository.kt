package tech.relaycorp.letro.notification.storage.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import tech.relaycorp.letro.account.model.Account
import tech.relaycorp.letro.account.storage.repository.AccountRepository
import tech.relaycorp.letro.contacts.storage.repository.ContactsRepository
import tech.relaycorp.letro.notification.converter.ExtendedNotificationConverter
import tech.relaycorp.letro.notification.model.ExtendedNotification
import tech.relaycorp.letro.notification.storage.dao.NotificationsDao
import tech.relaycorp.letro.notification.storage.entity.Notification
import tech.relaycorp.letro.utils.di.IODispatcher
import tech.relaycorp.letro.utils.ext.emitOn
import tech.relaycorp.letro.utils.time.DeviceTimeChangedProvider
import tech.relaycorp.letro.utils.time.OnDeviceTimeChangedListener
import tech.relaycorp.letro.utils.time.isLessThanWeeksAgo
import javax.inject.Inject

interface NotificationsRepository {
    val notifications: StateFlow<List<ExtendedNotification>>

    suspend fun readAllNotifications()
}

class NotificationsRepositoryImpl @Inject constructor(
    private val notificationsDao: NotificationsDao,
    private val accountRepository: AccountRepository,
    private val contactsRepository: ContactsRepository,
    private val extendedNotificationConverter: ExtendedNotificationConverter,
    private val timeChangedProvider: DeviceTimeChangedProvider,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
) : NotificationsRepository {

    private val scope = CoroutineScope(ioDispatcher)

    private val _notifications: MutableStateFlow<List<Notification>> = MutableStateFlow(emptyList())
    private val _extendedNotifications: MutableStateFlow<List<ExtendedNotification>> = MutableStateFlow(emptyList())
    override val notifications: StateFlow<List<ExtendedNotification>>
        get() = _extendedNotifications

    private var notificationsCollectionJob: Job? = null

    private var currentAccount: Account? = null

    private val timeChangedListener = object : OnDeviceTimeChangedListener {
        override fun onChanged() {
            currentAccount?.let { account ->
                _extendedNotifications.emitOn(
                    _notifications.value.map { extendedNotificationConverter.convert(it, contactsRepository.getContactsSync(account.accountId)) },
                    scope,
                )
            }
        }
    }

    init {
        timeChangedProvider.addListener(timeChangedListener)
        scope.launch {
            accountRepository.currentAccount.collect {
                currentAccount = it
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
            combine(notificationsDao.getAll(), contactsRepository.getContacts(currentAccount.accountId)) { notifications, contacts ->
                val notificationsFiltered = notifications
                    .filter { it.ownerId == currentAccount.accountId && it.timestampUtc.isLessThanWeeksAgo(OLD_NOTIFICATIONS_FILTER_WEEKS) }
                    .sortedByDescending { it.timestampUtc }
                _notifications.emit(notificationsFiltered)
                _extendedNotifications.emit(
                    notificationsFiltered.map { extendedNotificationConverter.convert(it, contacts) },
                )
            }.collect {}
        }
    }
}

private const val OLD_NOTIFICATIONS_FILTER_WEEKS = 12L
