package tech.relaycorp.letro.push

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import tech.relaycorp.letro.R
import tech.relaycorp.letro.push.model.PushChannel
import tech.relaycorp.letro.push.model.PushData
import tech.relaycorp.letro.ui.MainActivity
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
) : PushManager {

    private val notificationManager by lazy { NotificationManagerCompat.from(context) }

    @SuppressLint("MissingPermission")
    override fun showPush(pushData: PushData) {
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra(KEY_PUSH_ACTION, pushData.action)
        }

        val groupName = pushData.recipientAccountId
        val notification = NotificationCompat.Builder(context, pushData.channelId)
            .setSmallIcon(R.drawable.letro_icon) // TODO: icon
            .setContentTitle(pushData.title)
            .setContentText(pushData.text)
            .setContentIntent(PendingIntent.getActivity(context, pushData.notificationId, intent, PendingIntent.FLAG_IMMUTABLE))
            .setGroup(groupName)
            .setAutoCancel(true)
            .build()

        val notificationsInGroupCount = notificationManager.activeNotifications.count { it.notification.group == groupName }.takeIf { it > 0 } ?: 1
        val summaryNotification = NotificationCompat.Builder(context, pushData.channelId)
            .setSmallIcon(R.drawable.letro_icon) // TODO: icon
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

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }
        val notificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        channels
            .map { channel ->
                NotificationChannel(channel.id, context.getString(channel.name), NotificationManager.IMPORTANCE_DEFAULT).apply {
                    description = context.getString(channel.description)
                }
            }
            .forEach(notificationManager::createNotificationChannel)
    }
}

const val KEY_PUSH_ACTION = "push_action"