package tech.relaycorp.letro.pairing.processor

import android.util.Log
import tech.relaycorp.awaladroid.messaging.IncomingMessage
import tech.relaycorp.letro.awala.AwalaManager
import tech.relaycorp.letro.awala.processor.AwalaMessageProcessor
import tech.relaycorp.letro.contacts.model.ContactPairingStatus
import tech.relaycorp.letro.contacts.storage.ContactsDao
import tech.relaycorp.letro.notification.storage.dao.NotificationsDao
import tech.relaycorp.letro.notification.storage.entity.Notification
import tech.relaycorp.letro.notification.storage.entity.NotificationType
import tech.relaycorp.letro.pairing.dto.ContactPairingAuthorizationIncomingMessage
import tech.relaycorp.letro.pairing.parser.ContactPairingAuthorizationParser
import java.time.LocalDateTime
import javax.inject.Inject

interface ContactPairingAuthorizationProcessor : AwalaMessageProcessor

class ContactPairingAuthorizationProcessorImpl @Inject constructor(
    private val parser: ContactPairingAuthorizationParser,
    private val contactsDao: ContactsDao,
    private val notificationsDao: NotificationsDao,
) : ContactPairingAuthorizationProcessor {

    override suspend fun process(message: IncomingMessage, awalaManager: AwalaManager) {
        val response = (parser.parse(message.content) as ContactPairingAuthorizationIncomingMessage).content
        val nodeId = awalaManager.importPrivateThirdPartyAuth(response.authData)

        Log.d(TAG, "Contact auth received.")
        contactsDao.getContactsByContactEndpointId(
            contactEndpointId = nodeId,
        ).forEach { contactToUpdate ->
            Log.d(TAG, "Update status for nodeId=$nodeId")
            contactsDao.update(
                contactToUpdate.copy(
                    status = ContactPairingStatus.COMPLETED,
                ),
            )
            notificationsDao.insert(
                Notification(
                    type = NotificationType.PAIRING_COMPLETED,
                    ownerId = contactToUpdate.ownerVeraId,
                    contactVeraId = contactToUpdate.contactVeraId,
                    timestamp = LocalDateTime.now(),
                ),
            )
        }
    }

    private companion object {
        private const val TAG = "ContactPairingAuthorizationProcessorImpl"
    }
}
