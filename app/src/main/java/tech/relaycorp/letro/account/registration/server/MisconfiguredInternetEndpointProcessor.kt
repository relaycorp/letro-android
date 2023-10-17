package tech.relaycorp.letro.account.registration.server

import tech.relaycorp.awaladroid.messaging.IncomingMessage
import tech.relaycorp.letro.account.model.AccountStatus
import tech.relaycorp.letro.account.storage.repository.AccountRepository
import tech.relaycorp.letro.awala.AwalaManager
import tech.relaycorp.letro.awala.processor.AwalaMessageProcessor
import tech.relaycorp.letro.contacts.model.ContactPairingStatus
import tech.relaycorp.letro.contacts.pairing.notification.ContactPairingNotificationManager
import tech.relaycorp.letro.contacts.storage.dao.ContactsDao
import javax.inject.Inject

interface MisconfiguredInternetEndpointProcessor : AwalaMessageProcessor

class MisconfiguredInternetEndpointProcessorImpl @Inject constructor(
    private val accountRepository: AccountRepository,
    private val contactsDao: ContactsDao,
    private val contactPairingNotificationManager: ContactPairingNotificationManager,
) : MisconfiguredInternetEndpointProcessor {

    override suspend fun process(message: IncomingMessage, awalaManager: AwalaManager) {
        val domain = message.content.decodeToString()
        accountRepository.getByDomain(domain).forEach {
            accountRepository.updateAccount(
                account = it,
                status = AccountStatus.ERROR,
            )
        }
        accountRepository.getByAwalaEndpoint(domain).forEach {
            accountRepository.updateAccount(
                account = it,
                status = AccountStatus.ERROR,
            )
        }
        contactsDao.getContactsWithNoEndpoint(
            contactVeraId = domain,
            pairingStatus = ContactPairingStatus.REQUEST_SENT,
        ).forEach {
            contactsDao.deleteContact(it)
            contactPairingNotificationManager.showFailedPairingNotification(it)
        }
    }
}
