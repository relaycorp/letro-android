package tech.relaycorp.letro.notification.model

import androidx.annotation.StringRes
import tech.relaycorp.letro.notification.converter.NotificationDateInfo
import tech.relaycorp.letro.notification.storage.entity.NotificationType

data class ExtendedNotification(
    val id: Long,
    @NotificationType val type: Int,
    val ownerId: String,
    @StringRes val upperText: Int,
    val bottomText: String,
    val date: NotificationDateInfo,
    val isRead: Boolean,
)
