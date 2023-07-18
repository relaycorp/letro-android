package tech.relaycorp.letro.repository

import kotlinx.coroutines.flow.StateFlow
import tech.relaycorp.letro.data.entity.AccountDataModel

interface IAccountRepository {
    val allAccountsDataFlow: StateFlow<List<AccountDataModel>>
    val currentAccountDataFlow: StateFlow<AccountDataModel?>

    fun startCreatingNewAccount(address: String)
}
