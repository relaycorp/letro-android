package tech.relaycorp.letro.contacts.pairing.notification

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import tech.relaycorp.letro.R
import tech.relaycorp.letro.contacts.model.Contact
import tech.relaycorp.letro.notification.storage.dao.NotificationsDao
import tech.relaycorp.letro.notification.storage.entity.Notification
import tech.relaycorp.letro.notification.storage.entity.NotificationType
import tech.relaycorp.letro.push.PushManager
import tech.relaycorp.letro.push.model.PushChannel
import tech.relaycorp.letro.push.model.PushData
import tech.relaycorp.letro.ui.navigation.Action
import tech.relaycorp.letro.utils.time.nowUTC
import javax.inject.Inject

interface ContactPairingNotificationManager {
    fun showSuccessPairingNotification(contact: Contact)
    fun showFailedPairingNotification(contact: Contact)
}

class ContactPairingNotificationManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationsDao: NotificationsDao,
    private val pushManager: PushManager,
) : ContactPairingNotificationManager {

    override fun showSuccessPairingNotification(contact: Contact) {
        notificationsDao.insert(
            Notification(
                type = NotificationType.PAIRING_COMPLETED,
                ownerId = contact.ownerVeraId,
                contactVeraId = contact.contactVeraId,
                timestampUtc = nowUTC(),
            ),
        )
        pushManager.showPush(
            pushData = PushData(
                title = context.getString(R.string.you_have_a_new_contact),
                text = context.getString(R.string.you_re_now_connected_to_arg, contact.alias ?: contact.contactVeraId),
                action = Action.OpenContacts(
                    accountId = contact.ownerVeraId,
                ),
                notificationId = contact.contactVeraId.hashCode(),
                recipientAccountId = contact.ownerVeraId,
                channelId = PushChannel.ChannelId.ID_CONTACTS,
            ),
        )
    }

    override fun showFailedPairingNotification(contact: Contact) {
        notificationsDao.insert(
            notification = Notification(
                ownerId = contact.ownerVeraId,
                type = NotificationType.UNSUCCESSFUL_PAIRING,
                contactVeraId = contact.contactVeraId,
                timestampUtc = nowUTC(),
            ),
        )
        pushManager.showPush(
            pushData = PushData(
                title = context.getString(R.string.pairing_request_failed),
                text = context.getString(R.string.we_couldnt_connect_with_arg, contact.contactVeraId),
                action = Action.OpenContacts(
                    accountId = contact.ownerVeraId,
                ),
                notificationId = contact.contactVeraId.hashCode(),
                recipientAccountId = contact.ownerVeraId,
                channelId = PushChannel.ChannelId.ID_CONTACTS,
            ),
        )
    }
}
