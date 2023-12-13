package tech.relaycorp.letro.account.registration.server

import tech.relaycorp.letro.account.model.AccountStatus
import tech.relaycorp.letro.account.registration.MisconfiguredInternetEndpointParser
import tech.relaycorp.letro.account.storage.repository.AccountRepository
import tech.relaycorp.letro.awala.AwalaManager
import tech.relaycorp.letro.awala.message.AwalaIncomingMessageContent
import tech.relaycorp.letro.awala.processor.ServerMessageProcessor
import tech.relaycorp.letro.contacts.model.ContactPairingStatus
import tech.relaycorp.letro.contacts.pairing.notification.ContactPairingNotificationManager
import tech.relaycorp.letro.contacts.storage.dao.ContactsDao
import tech.relaycorp.letro.utils.Logger
import javax.inject.Inject

class MisconfiguredInternetEndpointProcessor @Inject constructor(
    private val accountRepository: AccountRepository,
    private val contactsDao: ContactsDao,
    private val contactPairingNotificationManager: ContactPairingNotificationManager,
    parser: MisconfiguredInternetEndpointParser,
    logger: Logger,
) : ServerMessageProcessor<AwalaIncomingMessageContent.MisconfiguredInternetEndpoint>(parser, logger) {

    override suspend fun handleMessage(
        content: AwalaIncomingMessageContent.MisconfiguredInternetEndpoint,
        awalaManager: AwalaManager,
    ) {
        val domain = content.domain
        accountRepository.getByDomain(domain).forEach {
            accountRepository.updateAccount(
                account = it,
                status = AccountStatus.ERROR_LINKING,
            )
        }
        accountRepository.getByAwalaEndpoint(domain).forEach {
            accountRepository.updateAccount(
                account = it,
                status = AccountStatus.ERROR_LINKING,
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
