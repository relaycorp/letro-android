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
        awalaManager: AwalaManager,
    ) {
        val contact = contactsDao.getContact(
            ownerVeraId = content.ownerVeraId,
            contactVeraId = content.contactVeraId,
        )
        try {
            val contactEndpointId = awalaManager.authorizeContact(content.contactEndpointPublicKey)
            contact?.let {
                contactsDao.update(
                    contact.copy(
                        contactEndpointId = contactEndpointId,
                        status = ContactPairingStatus.MATCH,
                    ),
                )
            }
        } catch (e: AwaladroidException) {
            contact?.let {
                contactsDao.deleteContact(contact)
                contactPairingNotificationManager.showFailedPairingNotification(contact)
            }
        }
    }
}
