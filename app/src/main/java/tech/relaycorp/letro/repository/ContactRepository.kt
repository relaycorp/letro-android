package tech.relaycorp.letro.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import tech.relaycorp.letro.data.PairingMatchDataModel
import tech.relaycorp.letro.data.PairingRequestAddressesDataModel
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

    private val _pairedContactsExist: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val pairedContactsExist: StateFlow<Boolean> get() = _pairedContactsExist

    init {
        databaseScope.launch {
            contactDao.getAll().collect { contacts ->
                _pairedContactsExist.value = contacts.any { it.status == PairingStatus.Complete }
            }
        }

        databaseScope.launch {
            gatewayRepository.pairingRequestSent.collect { dataModel: PairingRequestAddressesDataModel ->
                val contactToUpdate = getContactFromDatabase(
                    requesterAddress = dataModel.requesterVeraId,
                    contactAddress = dataModel.contactVeraId,
                ) ?: return@collect

                contactDao.update(contactToUpdate.copy(status = PairingStatus.RequestSent))
            }
        }

        databaseScope.launch {
            gatewayRepository.pairingMatchReceived.collect { dataModel: PairingMatchDataModel ->
                val contactToUpdate = getContactFromDatabase(
                    requesterAddress = dataModel.requesterVeraId,
                    contactAddress = dataModel.contactVeraId,
                ) ?: return@collect

                val updatedContact = updateContactWithPairingMatchData(contactToUpdate, dataModel)
                contactDao.update(updatedContact)

                gatewayRepository.sendPairingAuthorizationRequest(dataModel)
            }
        }

        databaseScope.launch {
            gatewayRepository.pairingAuthorizationSent.collect { dataModel: PairingMatchDataModel ->
                val contactToUpdate = getContactFromDatabase(
                    requesterAddress = dataModel.requesterVeraId,
                    contactAddress = dataModel.contactVeraId,
                ) ?: return@collect

                contactDao.update(contactToUpdate.copy(status = PairingStatus.AuthorizationSent))
            }
        }

        databaseScope.launch {
            gatewayRepository.pairingAuthorizationReceived.collect { contactNodeId: String ->
                val contactsToUpdate = contactDao.getContactsByContactEndpointId(contactNodeId)

                for (contactToUpdate in contactsToUpdate) {
                    contactDao.update(contactToUpdate.copy(status = PairingStatus.Complete))
                }
            }
        }
    }

    private suspend fun getContactFromDatabase(
        requesterAddress: String,
        contactAddress: String,
    ): ContactDataModel? {
        val account = accountDao.getByAddress(requesterAddress)
            ?: return null

        return contactDao.getContactByAddress(accountId = account.id, address = contactAddress)
    }

    fun startPairingWithContact(accountId: Long, contactAddress: String, contactAlias: String) {
        databaseScope.launch {
            val contact = ContactDataModel(
                accountId = accountId,
                address = contactAddress,
                alias = contactAlias,
            )
            val newContactInDatabaseId = contactDao.insert(contact)
            if (newContactInDatabaseId == -1L) {
                // TODO Show error
                return@launch
            }

            val account = accountDao.getById(accountId)
                ?: // TODO Show error
                return@launch

            startPairingWithContact(
                accountAddress = account.address,
                contactAddress = contactAddress,
            )
        }
    }

    private fun updateContactWithPairingMatchData(
        contact: ContactDataModel,
        dataModel: PairingMatchDataModel,
    ): ContactDataModel {
        return contact.copy(
            contactEndpointId = dataModel.contactEndpointId,
            status = PairingStatus.Match,
        )
    }

    private fun contactExistsInCurrentAccount(
        contactAddress: String,
        currentAccountsContacts: List<ContactDataModel>,
    ): Boolean {
        return currentAccountsContacts.any { it.address == contactAddress }
    }

    private fun startPairingWithContact(
        accountAddress: String,
        contactAddress: String,
    ) {
        gatewayRepository.startPairingWithContact(
            PairingRequestAddressesDataModel(
                requesterVeraId = accountAddress,
                contactVeraId = contactAddress,
            ),
        )
    }

//    private fun showError(errorMessage: String) {
//        // TODO Show the error message to the user
//    }
}
