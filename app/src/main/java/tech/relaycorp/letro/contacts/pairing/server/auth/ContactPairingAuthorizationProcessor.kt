package tech.relaycorp.letro.contacts.pairing.server.auth

import android.util.Log
import tech.relaycorp.letro.awala.AwalaManager
import tech.relaycorp.letro.awala.message.AwalaIncomingMessageContent
import tech.relaycorp.letro.awala.processor.ServerMessageProcessor
import tech.relaycorp.letro.contacts.model.ContactPairingStatus
import tech.relaycorp.letro.contacts.pairing.notification.ContactPairingNotificationManager
import tech.relaycorp.letro.contacts.storage.dao.ContactsDao
import tech.relaycorp.letro.utils.Logger
import javax.inject.Inject

class ContactPairingAuthorizationProcessor @Inject constructor(
    private val contactsDao: ContactsDao,
    private val contactPairingNotificationManager: ContactPairingNotificationManager,
    parser: ContactPairingAuthorizationParser,
    logger: Logger,
) : ServerMessageProcessor<AwalaIncomingMessageContent.ContactPairingAuthorization>(parser, logger) {

    override suspend fun handleMessage(
        content: AwalaIncomingMessageContent.ContactPairingAuthorization,
        awalaManager: AwalaManager,
    ) {
        val nodeId = awalaManager.importPrivateThirdPartyAuth(content.authData)

        Log.d(TAG, "Contact auth received ($nodeId).")
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
