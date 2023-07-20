package tech.relaycorp.letro.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import tech.relaycorp.letro.data.AccountCreatedDataModel
import tech.relaycorp.letro.data.PairingMatchDataModel
import tech.relaycorp.letro.data.PairingRequestAdresses
import tech.relaycorp.letro.data.UpdateContactDataModel
import tech.relaycorp.letro.data.dao.AccountDao
import tech.relaycorp.letro.data.entity.AccountDataModel
import tech.relaycorp.letro.data.entity.ContactDataModel
import tech.relaycorp.letro.data.entity.PairingStatus
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountRepository @Inject constructor(
    private val accountDao: AccountDao,
    private val gatewayRepository: GatewayRepository,
) {
    private val databaseScope: CoroutineScope = CoroutineScope(Dispatchers.IO)

    private val _allAccountsDataFlow: MutableStateFlow<List<AccountDataModel>> =
        MutableStateFlow(emptyList())
    val allAccountsDataFlow: StateFlow<List<AccountDataModel>> get() = _allAccountsDataFlow

    private val _currentAccountDataFlow: MutableStateFlow<AccountDataModel?> =
        MutableStateFlow(null)
    val currentAccountDataFlow: StateFlow<AccountDataModel?> get() = _currentAccountDataFlow

    init {
        databaseScope.launch {
            accountDao.getAll().collect {
                _allAccountsDataFlow.emit(it)
            }
        }

        databaseScope.launch {
            allAccountsDataFlow.collect { list ->
                list.firstOrNull { it.isCurrent }?.let {
                    _currentAccountDataFlow.emit(it)
                }
            }
        }

        databaseScope.launch {
            gatewayRepository.accountCreationConfirmationReceivedFromServer.collect { dataModel: AccountCreatedDataModel ->
                databaseScope.launch {
                    val account = accountDao.getByAddress(dataModel.requestedAddress)
                    if (account != null) {
                        accountDao.updateAddress(account.id, dataModel.assignedAddress)
                        accountDao.setAccountCreationConfirmed(dataModel.assignedAddress)
                    }
                }
            }
        }

        databaseScope.launch {
            gatewayRepository.pairingRequestSent.collect { dataModel: PairingRequestAdresses ->
                val contactToUpdate = createUpdateContactDataModel(
                    accountId = dataModel.requesterVeraId,
                    contactId = dataModel.contactVeraId,
                ) ?: return@collect

                val updatedContact =
                    contactToUpdate.contact.copy(status = PairingStatus.RequestSent)
                updateContactInDatabase(contactToUpdate.account, updatedContact)
            }
        }

        databaseScope.launch {
            gatewayRepository.pairingMatchReceived.collect { dataModel: PairingMatchDataModel ->
                val contactToUpdate = createUpdateContactDataModel(
                    accountId = dataModel.receiverVeraId, // TODO DELETE NOTE to Gus: Notice the switch here
                    contactId = dataModel.senderVeraId,
                ) ?: return@collect

                val updatedContact = updateContactWithPairingMatchData(contactToUpdate.contact, dataModel)
                updateContactInDatabase(contactToUpdate.account, updatedContact)

                gatewayRepository.sendPairingAuthorizationRequest(dataModel)
            }
        }

        databaseScope.launch {
            gatewayRepository.pairingAuthorizationSent.collect { dataModel: PairingMatchDataModel ->
                val contactToUpdate = createUpdateContactDataModel(
                    accountId = dataModel.senderVeraId,
                    contactId = dataModel.receiverVeraId,
                ) ?: return@collect

                val updatedContact = contactToUpdate.contact.copy(status = PairingStatus.AuthorizationSent)
                updateContactInDatabase(contactToUpdate.account, updatedContact)
            }
        }

        databaseScope.launch {
            gatewayRepository.pairingAuthorizationReceived.collect { dataModel: PairingMatchDataModel ->
                val contactToUpdate = createUpdateContactDataModel(
                    accountId = dataModel.receiverVeraId, // TODO DELETE NOTE to Gus: Notice the switch here
                    contactId = dataModel.senderVeraId,
                ) ?: return@collect

                val updatedContact = contactToUpdate.contact.copy(status = PairingStatus.Complete)
                updateContactInDatabase(contactToUpdate.account, updatedContact)
            }
        }
    }

    private suspend fun createUpdateContactDataModel(
        accountId: String,
        contactId: String,
    ): UpdateContactDataModel? {
        val account = accountDao.getByAddress(accountId)
            ?: return null

        val contact = account.contacts.firstOrNull { it.address == contactId }
            ?: return null

        return UpdateContactDataModel(account, contact)
    }

    fun startCreatingNewAccount(address: String) {
        val account = AccountDataModel(address = address)
        databaseScope.launch {
            insertNewAccountIntoDatabase(account)
            gatewayRepository.sendCreateAccountRequest(address)
        }
    }

    fun startPairingWithContact(contactAddress: String, contactAlias: String) {
        val currentAccount = _currentAccountDataFlow.value ?: return showError("No current account")

        if (contactExistsInCurrentAccount(contactAddress, currentAccount.contacts)) {
            return showError("Contact already exists")
        }

        val contact = ContactDataModel(
            address = contactAddress,
            alias = contactAlias,
        )

        databaseScope.launch {
            addContactAndUpdateDatabase(
                accountId = currentAccount.id,
                contact = contact,
                currentContacts = currentAccount.contacts,
            )
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

    private suspend fun updateContactInDatabase(
        account: AccountDataModel,
        updatedContact: ContactDataModel,
    ) {
        val updatedContacts = account.contacts.filterNot {
            it.address == updatedContact.address
        }.toMutableList().apply {
            add(updatedContact)
        }

        accountDao.updateContacts(account.id, updatedContacts)
    }

    private fun contactExistsInCurrentAccount(
        contactAddress: String,
        currentAccountsContacts: List<ContactDataModel>,
    ): Boolean {
        return currentAccountsContacts.any { it.address == contactAddress }
    }

    private suspend fun addContactAndUpdateDatabase(
        accountId: Long,
        contact: ContactDataModel,
        currentContacts: List<ContactDataModel>,
    ) {
        val updatedContacts = currentContacts.toMutableList().apply { add(contact) }
        accountDao.updateContacts(accountId, updatedContacts)
    }

    private fun startPairingWithContactInRepository(contactAddress: String) {
        gatewayRepository.startPairingWithContact(
            PairingRequestAdresses(
                requesterVeraId = contactAddress,
                contactVeraId = contactAddress,
            ),
        )
    }

    private fun showError(errorMessage: String) {
        // TODO Show the error message to the user
    }

    private suspend fun insertNewAccountIntoDatabase(dataModel: AccountDataModel) {
        accountDao.insert(dataModel)
        accountDao.setCurrentAccount(dataModel.address)
    }
}
