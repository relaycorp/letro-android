package tech.relaycorp.letro.pairing.processor

import tech.relaycorp.awaladroid.messaging.IncomingMessage
import tech.relaycorp.letro.awala.AwalaManager
import tech.relaycorp.letro.awala.processor.AwalaMessageProcessor
import tech.relaycorp.letro.contacts.model.ContactPairingStatus
import tech.relaycorp.letro.contacts.storage.ContactsDao
import tech.relaycorp.letro.pairing.dto.ContactPairingAuthorizationIncomingMessage
import tech.relaycorp.letro.pairing.parser.ContactPairingAuthorizationParser
import javax.inject.Inject

interface ContactPairingAuthorizationProcessor : AwalaMessageProcessor

class ContactPairingAuthorizationProcessorImpl @Inject constructor(
    private val parser: ContactPairingAuthorizationParser,
    private val contactsDao: ContactsDao,
) : ContactPairingAuthorizationProcessor {

    override suspend fun process(message: IncomingMessage, awalaManager: AwalaManager) {
        val response = (parser.parse(message.content) as ContactPairingAuthorizationIncomingMessage).content
        val nodeId = awalaManager.importPrivateThirdPartyAuth(response.authData)

        contactsDao.getContactsByContactEndpointId(
            contactEndpointId = nodeId,
        ).forEach { contactToUpdate ->
            contactsDao.update(
                contactToUpdate.copy(
                    status = ContactPairingStatus.COMPLETED,
                ),
            )
        }
    }
}
