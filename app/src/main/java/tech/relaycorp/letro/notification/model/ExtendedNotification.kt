package tech.relaycorp.letro.notification.model

import androidx.annotation.StringRes
import tech.relaycorp.letro.notification.converter.NotificationDateInfo

data class ExtendedNotification(
    val id: Long,
    val ownerId: String,
    @StringRes val upperText: Int,
    val bottomText: String,
    val date: NotificationDateInfo,
    val isRead: Boolean,
)
