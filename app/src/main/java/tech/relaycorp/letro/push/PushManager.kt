package tech.relaycorp.letro.push

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.ProcessLifecycleOwner
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import tech.relaycorp.letro.R
import tech.relaycorp.letro.account.model.Account
import tech.relaycorp.letro.account.storage.repository.AccountRepository
import tech.relaycorp.letro.main.ui.MainActivity
import tech.relaycorp.letro.push.model.LargeIcon
import tech.relaycorp.letro.push.model.PushChannel
import tech.relaycorp.letro.push.model.PushData
import tech.relaycorp.letro.ui.navigation.Action
import tech.relaycorp.letro.utils.android.LifecycleObserver
import tech.relaycorp.letro.utils.android.toCircle
import tech.relaycorp.letro.utils.coroutines.Dispatchers
import javax.inject.Inject

interface PushManager {

    fun showPush(
        pushData: PushData,
    )
}

class PushManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val permissionManager: PushPermissionManager,
    private val channels: List<PushChannel>,
    private val accountRepository: AccountRepository,
    dispatchers: Dispatchers,
) : PushManager {

    private val scope = CoroutineScope(dispatchers.Main)

    private val notificationManager by lazy { NotificationManagerCompat.from(context) }
    private var isAppInForeground = false
    private var currentAccount: Account? = null

    private val lifecycleObserver = LifecycleObserver(
        onStart = {
            isAppInForeground = true
            clearNotificationsForCurrentAccount()
        },
        onStop = {
            isAppInForeground = false
        },
    )

    init {
        scope.launch(dispatchers.IO) {
            accountRepository.allAccounts.collect {
                createNotificationChannelsForAccounts(it.map { it.accountId })
            }
        }
        scope.launch(dispatchers.Main) {
            accountRepository.currentAccount.collect {
                currentAccount = it
                clearNotificationsForCurrentAccount()
            }
        }
        ProcessLifecycleOwner.get().lifecycle.addObserver(lifecycleObserver)
    }

    @SuppressLint("MissingPermission")
    override fun showPush(pushData: PushData) {
        if (isAppInForeground && currentAccount?.accountId == pushData.recipientAccountId) {
            Log.i(TAG, "Don't show push, because app is in foreground, and recipient account is active")
            return
        }
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra(KEY_PUSH_ACTION, pushData.action)
            action = KEY_PUSH_ACTION
        }

        val groupName = pushData.recipientAccountId

        val groupIntent = Intent(context, MainActivity::class.java).apply {
            putExtra(KEY_PUSH_ACTION, Action.OpenMainPage(groupName))
            action = KEY_PUSH_ACTION
        }
        val notification = NotificationCompat.Builder(context, pushData.channelId)
            .setSmallIcon(R.drawable.letro_notification_icon)
            .setLargeIcon(pushData.largeIcon)
            .setContentTitle(pushData.title)
            .setContentText(pushData.text)
            .setContentIntent(PendingIntent.getActivity(context, pushData.notificationId, intent, PendingIntent.FLAG_IMMUTABLE))
            .setGroup(groupName)
            .setAutoCancel(true)
            .build()

        val notificationsInGroupCount = notificationManager.activeNotifications.count { it.notification.group == groupName }.takeIf { it > 0 } ?: 1
        val summaryNotification = NotificationCompat.Builder(context, pushData.channelId)
            .setSmallIcon(R.drawable.letro_notification_icon)
            .setContentTitle(groupName)
            .setContentText(context.resources.getQuantityString(R.plurals.new_notifications_group_count, notificationsInGroupCount, notificationsInGroupCount))
            .setGroup(groupName)
            .setGroupSummary(true)
            .setLargeIcon(pushData.largeIcon)
            .setContentIntent(PendingIntent.getActivity(context, groupName.hashCode(), groupIntent, PendingIntent.FLAG_IMMUTABLE))
            .setAutoCancel(true)
            .build()

        if (permissionManager.isPermissionGranted()) {
            notificationManager.notify(pushData.notificationId, notification)
            notificationManager.notify(groupName.hashCode(), summaryNotification)
        }
    }

    private fun NotificationCompat.Builder.setLargeIcon(icon: LargeIcon?): NotificationCompat.Builder {
        return when (icon) {
            is LargeIcon.File -> {
                BitmapFactory.decodeFile(icon.path)?.toCircle()?.let { bitmap ->
                    return@let setLargeIcon(bitmap)
                } ?: this
            }
            is LargeIcon.DefaultAvatar -> setLargeIcon(BitmapFactory.decodeResource(context.resources, R.drawable.default_profile_picture))
            else -> this
        }
    }

    private fun clearNotificationsForCurrentAccount() {
        notificationManager.activeNotifications
            .filter { currentAccount?.accountId != null && it.notification.group == currentAccount?.accountId }
            .forEach {
                notificationManager.cancel(it.id)
            }
    }

    private fun createNotificationChannelsForAccounts(accounts: List<String>) {
        accounts.forEach {
            createGroupedNotificationChannels(it)
        }
    }

    private fun createGroupedNotificationChannels(account: String) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }
        notificationManager.createNotificationChannelGroup(
            NotificationChannelGroup(account, account),
        )
        createNotificationChannels(account)
    }

    private fun createNotificationChannels(groupName: String) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }
        channels
            .map { channel ->
                NotificationChannel(channel.id, context.getString(channel.name), NotificationManager.IMPORTANCE_DEFAULT).apply {
                    group = groupName
                }
            }
            .forEach(notificationManager::createNotificationChannel)
    }
}

private const val TAG = "PushManager"
const val KEY_PUSH_ACTION = "tech.relaycorp.letro.push_action"
