package tech.relaycorp.letro.contacts.pairing.server.match

import tech.relaycorp.awaladroid.AwaladroidException
import tech.relaycorp.letro.awala.AwalaManager
import tech.relaycorp.letro.awala.message.AwalaIncomingMessageContent
import tech.relaycorp.letro.awala.processor.ServerMessageProcessor
import tech.relaycorp.letro.contacts.model.ContactPairingStatus
import tech.relaycorp.letro.contacts.pairing.notification.ContactPairingNotificationManager
import tech.relaycorp.letro.contacts.storage.dao.ContactsDao
import tech.relaycorp.letro.utils.Logger
import javax.inject.Inject

class ContactPairingMatchProcessor @Inject constructor(
    private val contactsDao: ContactsDao,
    private val contactPairingNotificationManager: ContactPairingNotificationManager,
    parser: ContactPairingMatchParser,
    logger: Logger,
) : ServerMessageProcessor<AwalaIncomingMessageContent.ContactPairingMatch>(parser, logger) {

    override suspend fun handleMessage(
        content: AwalaIncomingMessageContent.ContactPairingMatch,
        senderNodeId: String,
        awalaManager: AwalaManager,
    ) {
        val contact = contactsDao.getContact(
            ownerVeraId = content.ownerVeraId,
            contactVeraId = content.contactVeraId,
        )
        if (contact == null) {
            logger.w(TAG, "Contact ${content.contactVeraId} not found (account: ${content.ownerVeraId})")
            return
        }
        val contactEndpointId = try {
            awalaManager.authorizeContact(content.contactEndpointPublicKey)
        } catch (exc: AwaladroidException) {
            contactsDao.deleteContact(contact)
            contactPairingNotificationManager.showFailedPairingNotification(contact)
            logger.e(TAG, "Failed to authorize contact", exc)
            return
        }

        contactsDao.update(
            contact.copy(
                contactEndpointId = contactEndpointId,
                status = ContactPairingStatus.MATCH,
            ),
        )
        logger.i("ContactPairingMatchProcessor", "Contact authorized ($contact)")
    }

    companion object {
        private const val TAG = "ContactPairingMatchProcessor"
    }
}
