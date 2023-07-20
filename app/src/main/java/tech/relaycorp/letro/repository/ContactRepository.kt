package tech.relaycorp.letro.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tech.relaycorp.letro.data.PairingMatchDataModel
import tech.relaycorp.letro.data.PairingRequestAdresses
import tech.relaycorp.letro.data.dao.AccountDao
import tech.relaycorp.letro.data.dao.ContactDao
import tech.relaycorp.letro.data.entity.ContactDataModel
import tech.relaycorp.letro.data.entity.PairingStatus
import javax.inject.Inject

class ContactRepository @Inject constructor(
    private val accountDao: AccountDao,
    private val contactDao: ContactDao,
    private val gatewayRepository: GatewayRepository,
) {
    private val databaseScope: CoroutineScope = CoroutineScope(Dispatchers.IO)

    init {
        databaseScope.launch {
            gatewayRepository.pairingRequestSent.collect { dataModel: PairingRequestAdresses ->
                val contactToUpdate = getContactFromDatabase(
                    accountAddress = dataModel.requesterVeraId,
                    contactAddress = dataModel.contactVeraId,
                ) ?: return@collect

                contactDao.update(contactToUpdate.copy(status = PairingStatus.RequestSent))
            }
        }

        databaseScope.launch {
            gatewayRepository.pairingMatchReceived.collect { dataModel: PairingMatchDataModel ->
                val contactToUpdate = getContactFromDatabase(
                    accountAddress = dataModel.receiverVeraId, // TODO DELETE NOTE to Gus: Notice the switch here. Is this correct?
                    contactAddress = dataModel.senderVeraId,
                ) ?: return@collect

                val updatedContact = updateContactWithPairingMatchData(contactToUpdate, dataModel)
                contactDao.update(updatedContact)

                gatewayRepository.sendPairingAuthorizationRequest(dataModel)
            }
        }

        databaseScope.launch {
            gatewayRepository.pairingAuthorizationSent.collect { dataModel: PairingMatchDataModel ->
                val contactToUpdate = getContactFromDatabase(
                    accountAddress = dataModel.senderVeraId,
                    contactAddress = dataModel.receiverVeraId,
                ) ?: return@collect

                contactDao.update(contactToUpdate.copy(status = PairingStatus.AuthorizationSent))
            }
        }

        databaseScope.launch {
            gatewayRepository.pairingAuthorizationReceived.collect { dataModel: PairingMatchDataModel ->
                val contactToUpdate = getContactFromDatabase(
                    accountAddress = dataModel.receiverVeraId, // TODO DELETE NOTE to Gus: Notice the switch here. Is this correct?
                    contactAddress = dataModel.senderVeraId,
                ) ?: return@collect

                contactDao.update(contactToUpdate.copy(status = PairingStatus.Complete))
            }
        }
    }

    private suspend fun getContactFromDatabase(
        accountAddress: String,
        contactAddress: String,
    ): ContactDataModel? {
        val account = accountDao.getByAddress(accountAddress)
            ?: return null

        return contactDao.getContactByAddress(accountId = account.id, address = contactAddress)
    }

    fun startPairingWithContact(accountId: Long, contactAddress: String, contactAlias: String) {
        val contact = ContactDataModel(
            accountId = accountId,
            address = contactAddress,
            alias = contactAlias,
        )

        databaseScope.launch {
            contactDao.insert(contact)
            startPairingWithContactInRepository(contactAddress)
        }
    }

    private fun updateContactWithPairingMatchData(
        contact: ContactDataModel,
        dataModel: PairingMatchDataModel,
    ): ContactDataModel {
        return contact.copy(
            contactEndpointId = dataModel.receiverEndpointId,
            contactEndpointPublicKey = dataModel.receiverEndpointPublicKey,
            status = PairingStatus.Match,
        )
    }

    private fun contactExistsInCurrentAccount(
        contactAddress: String,
        currentAccountsContacts: List<ContactDataModel>,
    ): Boolean {
        return currentAccountsContacts.any { it.address == contactAddress }
    }

    private fun startPairingWithContactInRepository(contactAddress: String) {
        gatewayRepository.startPairingWithContact(
            PairingRequestAdresses(
                requesterVeraId = contactAddress,
                contactVeraId = contactAddress,
            ),
        )
    }

//    private fun showError(errorMessage: String) {
//        // TODO Show the error message to the user
//    }
}
