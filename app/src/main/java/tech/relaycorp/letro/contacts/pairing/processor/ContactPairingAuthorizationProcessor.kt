package tech.relaycorp.letro.contacts.pairing.processor

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import tech.relaycorp.awaladroid.messaging.IncomingMessage
import tech.relaycorp.letro.R
import tech.relaycorp.letro.awala.AwalaManager
import tech.relaycorp.letro.awala.processor.AwalaMessageProcessor
import tech.relaycorp.letro.contacts.model.ContactPairingStatus
import tech.relaycorp.letro.contacts.pairing.dto.ContactPairingAuthorizationIncomingMessage
import tech.relaycorp.letro.contacts.pairing.parser.ContactPairingAuthorizationParser
import tech.relaycorp.letro.contacts.storage.dao.ContactsDao
import tech.relaycorp.letro.notification.storage.dao.NotificationsDao
import tech.relaycorp.letro.notification.storage.entity.Notification
import tech.relaycorp.letro.notification.storage.entity.NotificationType
import tech.relaycorp.letro.push.PushManager
import tech.relaycorp.letro.push.model.PushAction
import tech.relaycorp.letro.push.model.PushChannel
import tech.relaycorp.letro.push.model.PushData
import java.time.LocalDateTime
import javax.inject.Inject

interface ContactPairingAuthorizationProcessor : AwalaMessageProcessor

class ContactPairingAuthorizationProcessorImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val parser: ContactPairingAuthorizationParser,
    private val contactsDao: ContactsDao,
    private val notificationsDao: NotificationsDao,
    private val pushManager: PushManager,
) : ContactPairingAuthorizationProcessor {

    override suspend fun process(message: IncomingMessage, awalaManager: AwalaManager) {
        val response = (parser.parse(message.content) as ContactPairingAuthorizationIncomingMessage).content
        val nodeId = awalaManager.importPrivateThirdPartyAuth(response.authData)

        Log.d(TAG, "Contact auth received.")
        contactsDao.getContactsByContactEndpointId(
            contactEndpointId = nodeId,
        ).forEach { contact ->
            Log.d(TAG, "Update status for nodeId=$nodeId")
            contactsDao.update(
                contact.copy(
                    status = ContactPairingStatus.COMPLETED,
                ),
            )
            notificationsDao.insert(
                Notification(
                    type = NotificationType.PAIRING_COMPLETED,
                    ownerId = contact.ownerVeraId,
                    contactVeraId = contact.contactVeraId,
                    timestamp = LocalDateTime.now(),
                ),
            )
            pushManager.showPush(
                pushData = PushData(
                    title = context.getString(R.string.you_have_a_new_contact),
                    text = context.getString(R.string.you_re_now_connected_to_arg, contact.alias ?: contact.contactVeraId),
                    action = PushAction.OpenContacts(
                        accountId = contact.ownerVeraId,
                    ),
                    notificationId = contact.contactVeraId.hashCode(),
                    recipientAccountId = contact.ownerVeraId,
                    channelId = PushChannel.ChannelId.ID_CONTACTS,
                ),
            )
        }
    }

    private companion object {
        private const val TAG = "ContactPairingAuthorizationProcessorImpl"
    }
}
