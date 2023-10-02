package tech.relaycorp.letro.push

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import tech.relaycorp.letro.R
import tech.relaycorp.letro.main.ui.MainActivity
import tech.relaycorp.letro.push.model.PushChannel
import tech.relaycorp.letro.push.model.PushData
import javax.inject.Inject

interface PushManager {

    fun showPush(
        pushData: PushData,
    )

    fun createNotificationChannelsForAccounts(
        accounts: List<String>,
    )
}

class PushManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val permissionManager: PushPermissionManager,
    private val channels: List<PushChannel>,
) : PushManager {

    private val notificationManager by lazy { NotificationManagerCompat.from(context) }

    @SuppressLint("MissingPermission")
    override fun showPush(pushData: PushData) {
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra(KEY_PUSH_ACTION, pushData.action)
        }

        val groupName = pushData.recipientAccountId
        val notification = NotificationCompat.Builder(context, pushData.channelId)
            .setSmallIcon(R.drawable.letro_notification_icon)
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
            .build()

        if (permissionManager.isPermissionGranted()) {
            notificationManager.notify(pushData.notificationId, notification)
            notificationManager.notify(groupName.hashCode(), summaryNotification)
        }
    }

    override fun createNotificationChannelsForAccounts(accounts: List<String>) {
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

const val KEY_PUSH_ACTION = "push_action"
