package tech.relaycorp.letro.notification.converter

import androidx.annotation.StringRes
import tech.relaycorp.letro.R
import tech.relaycorp.letro.contacts.model.Contact
import tech.relaycorp.letro.notification.model.ExtendedNotification
import tech.relaycorp.letro.notification.storage.entity.Notification
import tech.relaycorp.letro.notification.storage.entity.NotificationType
import tech.relaycorp.letro.utils.time.toSystemTimeZone
import javax.inject.Inject

interface ExtendedNotificationConverter {
    fun convert(notification: Notification, contacts: List<Contact>): ExtendedNotification
}

class ExtendedNotificationConverterImpl @Inject constructor(
    private val dateFormatter: ExtendedNotificationDateFormatter,
) : ExtendedNotificationConverter {

    override fun convert(notification: Notification, contacts: List<Contact>): ExtendedNotification {
        return ExtendedNotification(
            id = notification.id,
            type = notification.type,
            ownerId = notification.ownerId,
            upperText = getUpperText(notification.type),
            bottomText = notification.contactVeraId,
            date = dateFormatter.format(notification.timestampUtc.toSystemTimeZone()),
            isRead = notification.isRead,
            imageFilePath = contacts.find { it.contactVeraId == notification.contactVeraId }?.avatarFilePath,
        )
    }

    @StringRes
    private fun getUpperText(@NotificationType type: Int) = when (type) {
        NotificationType.PAIRING_COMPLETED -> R.string.you_re_now_connected_to
        NotificationType.UNSUCCESSFUL_PAIRING -> R.string.we_couldnt_connect_with
        else -> throw IllegalStateException("Unknown notification type $type")
    }
}
