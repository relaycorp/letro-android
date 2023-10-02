package tech.relaycorp.letro.contacts.pairing.processor

import tech.relaycorp.awaladroid.messaging.IncomingMessage
import tech.relaycorp.letro.awala.AwalaManager
import tech.relaycorp.letro.awala.processor.AwalaMessageProcessor
import tech.relaycorp.letro.contacts.model.ContactPairingStatus
import tech.relaycorp.letro.contacts.pairing.dto.ContactPairingMatchIncomingMessage
import tech.relaycorp.letro.contacts.pairing.parser.ContactPairingMatchParser
import tech.relaycorp.letro.contacts.storage.dao.ContactsDao
import javax.inject.Inject

interface ContactPairingMatchProcessor : AwalaMessageProcessor

class ContactPairingMatchProcessorImpl @Inject constructor(
    private val parser: ContactPairingMatchParser,
    private val contactsDao: ContactsDao,
) : ContactPairingMatchProcessor {

    override suspend fun process(message: IncomingMessage, awalaManager: AwalaManager) {
        val response = (parser.parse(message.content) as ContactPairingMatchIncomingMessage).content
        contactsDao.getContact(
            ownerVeraId = response.ownerVeraId,
            contactVeraId = response.contactVeraId,
        )?.let { contactToUpdate ->
            contactsDao.update(
                contactToUpdate.copy(
                    contactEndpointId = response.contactEndpointId,
                    status = ContactPairingStatus.MATCH,
                ),
            )
        }
        awalaManager.authorizeUsers(response.contactEndpointPublicKey)
    }
}