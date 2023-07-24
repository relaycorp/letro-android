package tech.relaycorp.letro.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import tech.relaycorp.letro.data.PairingMatchDataModel
import tech.relaycorp.letro.data.PairingRequestVeraIdsDataModel
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
            gatewayRepository.pairingRequestSent.collect { dataModel: PairingRequestVeraIdsDataModel ->
                val contactToUpdate = getContactFromDatabase(
                    requesterVeraId = dataModel.requesterVeraId,
                    contactVeraId = dataModel.contactVeraId,
                ) ?: return@collect

                contactDao.update(contactToUpdate.copy(status = PairingStatus.RequestSent))
            }
        }

        databaseScope.launch {
            gatewayRepository.pairingMatchReceived.collect { dataModel: PairingMatchDataModel ->
                val contactToUpdate = getContactFromDatabase(
                    requesterVeraId = dataModel.requesterVeraId,
                    contactVeraId = dataModel.contactVeraId,
                ) ?: return@collect

                contactDao.update(
                    contactToUpdate.copy(
                        contactEndpointId = dataModel.contactEndpointId,
                        status = PairingStatus.Match,
                    ),
                )

                gatewayRepository.sendPairingAuthorizationRequest(dataModel)
            }
        }

        databaseScope.launch {
            gatewayRepository.pairingAuthorizationSent.collect { dataModel: PairingMatchDataModel ->
                val contactToUpdate = getContactFromDatabase(
                    requesterVeraId = dataModel.requesterVeraId,
                    contactVeraId = dataModel.contactVeraId,
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
        requesterVeraId: String,
        contactVeraId: String,
    ): ContactDataModel? {
        val account = accountDao.getByVeraId(requesterVeraId)
            ?: return null

        return contactDao.getContactByVeraId(accountId = account.id, veraId = contactVeraId)
    }

    fun startPairingWithContact(
        accountId: Long,
        accountVeraId: String,
        contactVeraId: String,
        contactAlias: String,
    ) {
        databaseScope.launch {
            val contact = ContactDataModel(
                accountId = accountId,
                veraId = contactVeraId,
                alias = contactAlias,
            )
            val contactExistsInCurrentAccount =  contactDao.getContactByVeraId(
                veraId = contactVeraId,
                accountId = accountId,
            )
            if (contactExistsInCurrentAccount == null) {
                val newContactInDatabaseId = contactDao.insert(contact)
                if (newContactInDatabaseId == -1L) {
                    // TODO Show error that the contact could not be added
                    return@launch
                }
            }

            gatewayRepository.startPairingWithContact(
                PairingRequestVeraIdsDataModel(
                    requesterVeraId = accountVeraId,
                    contactVeraId = contactVeraId,
                ),
            )
        }
    }

    private fun contactExistsInCurrentAccount(
        contactVeraId: String,
        currentAccountsContacts: List<ContactDataModel>,
    ): Boolean {
        return currentAccountsContacts.any { it.veraId == contactVeraId }
    }

//    private fun showError(errorMessage: String) {
//        // TODO Show the error message to the user
//    }
}
