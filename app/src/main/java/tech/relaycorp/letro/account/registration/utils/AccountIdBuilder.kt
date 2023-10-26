package tech.relaycorp.letro.account.registration.utils

import javax.inject.Inject

interface AccountIdBuilder {
    fun build(requestedUserName: String, domainName: String): String
}

class AccountIdBuilderImpl @Inject constructor() : AccountIdBuilder {

    override fun build(requestedUserName: String, domainName: String): String {
        return "$requestedUserName@$domainName"
    }
}
