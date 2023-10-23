package tech.relaycorp.letro.contacts.pairing.processor

import tech.relaycorp.awaladroid.AwaladroidException
import tech.relaycorp.awaladroid.messaging.IncomingMessage
import tech.relaycorp.letro.awala.AwalaManager
import tech.relaycorp.letro.awala.processor.ServerMessageProcessor
import tech.relaycorp.letro.contacts.model.ContactPairingStatus
import tech.relaycorp.letro.contacts.pairing.dto.ContactPairingMatchIncomingMessage
import tech.relaycorp.letro.contacts.pairing.notification.ContactPairingNotificationManager
import tech.relaycorp.letro.contacts.pairing.parser.ContactPairingMatchParser
import tech.relaycorp.letro.contacts.storage.dao.ContactsDao
import javax.inject.Inject

interface ContactPairingMatchProcessor : ServerMessageProcessor

class ContactPairingMatchProcessorImpl @Inject constructor(
    private val parser: ContactPairingMatchParser,
    private val contactsDao: ContactsDao,
    private val contactPairingNotificationManager: ContactPairingNotificationManager,
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
                contactPairingNotificationManager.showFailedPairingNotification(contact)
            }
        }
    }
}
