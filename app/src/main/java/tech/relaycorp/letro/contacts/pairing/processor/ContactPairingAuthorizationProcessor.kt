package tech.relaycorp.letro.contacts.pairing.processor

import android.util.Log
import tech.relaycorp.awaladroid.messaging.IncomingMessage
import tech.relaycorp.letro.awala.AwalaManager
import tech.relaycorp.letro.awala.processor.AwalaMessageProcessor
import tech.relaycorp.letro.contacts.model.ContactPairingStatus
import tech.relaycorp.letro.contacts.pairing.dto.ContactPairingAuthorizationIncomingMessage
import tech.relaycorp.letro.contacts.pairing.notification.ContactPairingNotificationManager
import tech.relaycorp.letro.contacts.pairing.parser.ContactPairingAuthorizationParser
import tech.relaycorp.letro.contacts.storage.dao.ContactsDao
import javax.inject.Inject

interface ContactPairingAuthorizationProcessor : AwalaMessageProcessor

class ContactPairingAuthorizationProcessorImpl @Inject constructor(
    private val parser: ContactPairingAuthorizationParser,
    private val contactsDao: ContactsDao,
    private val contactPairingNotificationManager: ContactPairingNotificationManager,
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
            contactPairingNotificationManager.showSuccessPairingNotification(contact)
        }
    }

    private companion object {
        private const val TAG = "ContactPairingAuthorizationProcessorImpl"
    }
}
