package tech.relaycorp.letro.account.utils

import tech.relaycorp.letro.account.model.Account
import javax.inject.Inject

interface AccountsSorter {
    fun withCurrentAccountFirst(accounts: List<Account>): List<Account>
}

class AccountsSorterImpl @Inject constructor() : AccountsSorter {

    override fun withCurrentAccountFirst(accounts: List<Account>): List<Account> {
        val currentIndex = accounts.indexOfFirst { it.isCurrent }
        if (currentIndex == -1) {
            return accounts
        }
        return arrayListOf<Account>().apply {
            add(accounts[currentIndex])
            for (i in accounts.indices) {
                if (currentIndex == i) continue
                add(accounts[i])
            }
        }
    }
}
