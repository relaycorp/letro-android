package tech.relaycorp.letro.notification.converter

import androidx.annotation.StringRes
import tech.relaycorp.letro.R
import tech.relaycorp.letro.notification.model.ExtendedNotification
import tech.relaycorp.letro.notification.storage.entity.Notification
import tech.relaycorp.letro.notification.storage.entity.NotificationType
import javax.inject.Inject

interface ExtendedNotificationConverter {
    fun convert(notification: Notification): ExtendedNotification
}

class ExtendedNotificationConverterImpl @Inject constructor(
    private val dateFormatter: ExtendedNotificationDateFormatter,
) : ExtendedNotificationConverter {

    override fun convert(notification: Notification): ExtendedNotification {
        return ExtendedNotification(
            id = notification.id,
            type = notification.type,
            ownerId = notification.ownerId,
            upperText = getUpperText(notification.type),
            bottomText = notification.contactVeraId,
            date = dateFormatter.format(notification.timestamp),
            isRead = notification.isRead,
        )
    }

    @StringRes
    private fun getUpperText(@NotificationType type: Int) = when (type) {
        NotificationType.PAIRING_COMPLETED -> R.string.you_re_now_connected_to
        NotificationType.UNSUCCESSFUL_PAIRING -> R.string.we_couldnt_connect_with
        else -> throw IllegalStateException("Unknown notification type $type")
    }
}
