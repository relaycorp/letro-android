package tech.relaycorp.letro.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import tech.relaycorp.letro.data.PairingMatchDataModel
import tech.relaycorp.letro.data.dao.AccountDao
import tech.relaycorp.letro.data.entity.AccountDataModel
import tech.relaycorp.letro.data.entity.ContactDataModel
import tech.relaycorp.letro.data.entity.ContactStatus
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

        // TODO merge next 2 blocks into one
        databaseScope.launch {
            gatewayRepository.accountCreatedConfirmationReceived.collect {
                _currentAccountDataFlow.value?.let { accountData ->
                    accountDao.setCurrentAccount(accountData.address)
                    accountDao.setAccountCreationConfirmed(accountData.address)
                }
            }
        }
        databaseScope.launch {
            gatewayRepository.accountCreatedConfirmationReceived.collect {
                accountCreatedOnTheServer(it.requestedAddress, it.assignedAddress)
            }
        }

        databaseScope.launch {
            gatewayRepository.pairingRequestSent.collect {contactId: String ->

            }
        }

        databaseScope.launch {
            gatewayRepository.pairingMatchReceived.collect { dataModel: PairingMatchDataModel ->
                // get account that requested the pairing with this contact
                val account = accountDao.getByAddress(dataModel.requesterVeraId)
                    ?: // TODO handle this error
                    return@collect

                // update the contact with the endpoint id and public key
                val contact = account.contacts.firstOrNull { it.address == dataModel.contactVeraId }
                    ?: // TODO handle this error
                    return@collect

                val updatedContact = contact.copy(
                    contactEndpointId = dataModel.contactEndpointId,
                    contactEndpointPublicKey = dataModel.contactEndpointPublicKey,
                    status = ContactStatus.PairingMatch,
                )

                // potentially update the UI because now we have a pairing match

            }
        }
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
                currentContacts = currentAccount.contacts
            )
            startPairingWithContactInRepository(contactAddress)
        }
    }

    private fun contactExistsInCurrentAccount(
        contactAddress: String,
        currentAccountsContacts: List<ContactDataModel>
    ): Boolean {
        return currentAccountsContacts.any { it.address == contactAddress }
    }

    private suspend fun addContactAndUpdateDatabase(
        accountId: Long,
        contact: ContactDataModel,
        currentContacts: List<ContactDataModel>
    ) {
        val updatedContacts = currentContacts.toMutableList().apply { add(contact) }
        accountDao.updateContacts(accountId, updatedContacts)
    }

    private fun startPairingWithContactInRepository(contactAddress: String) {
        gatewayRepository.startPairingWithContact(
            requesterVeraId = contactAddress,
            contactVeraId = contactAddress
        )
    }

    private fun showError(errorMessage: String) {
        // TODO Show the error message to the user
    }

    private suspend fun accountCreatedOnTheServer(
        requestedAddress: String,
        assignedAddress: String
    ) {
        databaseScope.launch {
            val account = accountDao.getByAddress(requestedAddress)
            accountDao.updateAddress(account.id, assignedAddress)
            accountDao.setAccountCreationConfirmed(assignedAddress)
        }
    }

    private suspend fun insertNewAccountIntoDatabase(dataModel: AccountDataModel) {
        accountDao.insert(dataModel)
        accountDao.setCurrentAccount(dataModel.address)
    }
}
