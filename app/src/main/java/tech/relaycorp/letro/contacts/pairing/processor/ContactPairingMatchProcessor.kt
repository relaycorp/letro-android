package tech.relaycorp.letro.contacts.pairing.processor

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import tech.relaycorp.awaladroid.AwaladroidException
import tech.relaycorp.awaladroid.messaging.IncomingMessage
import tech.relaycorp.letro.R
import tech.relaycorp.letro.awala.AwalaManager
import tech.relaycorp.letro.awala.processor.AwalaMessageProcessor
import tech.relaycorp.letro.contacts.model.ContactPairingStatus
import tech.relaycorp.letro.contacts.pairing.dto.ContactPairingMatchIncomingMessage
import tech.relaycorp.letro.contacts.pairing.parser.ContactPairingMatchParser
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

interface ContactPairingMatchProcessor : AwalaMessageProcessor

class ContactPairingMatchProcessorImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val parser: ContactPairingMatchParser,
    private val contactsDao: ContactsDao,
    private val notificationsDao: NotificationsDao,
    private val pushManager: PushManager,
) : ContactPairingMatchProcessor {

    override suspend fun process(message: IncomingMessage, awalaManager: AwalaManager) {
        val response = (parser.parse(message.content) as ContactPairingMatchIncomingMessage).content
        val contact = contactsDao.getContact(
            ownerVeraId = response.ownerVeraId,
            contactVeraId = response.contactVeraId,
        )
        try {
            awalaManager.authorizeUsers(response.contactEndpointPublicKey)
            contact?.let {
                contactsDao.update(
                    contact.copy(
                        contactEndpointId = response.contactEndpointId,
                        status = ContactPairingStatus.MATCH,
                    ),
                )
            }
        } catch (e: AwaladroidException) {
            contact?.let {
                contactsDao.deleteContact(contact)
            }
            notificationsDao.insert(
                notification = Notification(
                    ownerId = response.ownerVeraId,
                    type = NotificationType.UNSUCCESSFUL_PAIRING,
                    contactVeraId = response.contactVeraId,
                    timestamp = LocalDateTime.now(),
                ),
            )
            pushManager.showPush(
                pushData = PushData( // TODO: check data here
                    title = response.ownerVeraId,
                    text = context.getString(R.string.we_couldnt_connect_with_arg, response.contactVeraId),
                    action = PushAction.OpenMainPage(
                        accountId = response.ownerVeraId,
                    ),
                    notificationId = response.contactVeraId.hashCode(),
                    recipientAccountId = response.ownerVeraId,
                    channelId = PushChannel.ChannelId.ID_CONTACTS,
                ),
            )
        }
    }
}
