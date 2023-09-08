package tech.relaycorp.letro.data

import tech.relaycorp.letro.data.entity.AccountDataModel
import tech.relaycorp.letro.data.entity.ContactDataModel

data class UpdateContactDataModel(
    val account: AccountDataModel,
    val contact: ContactDataModel,
)
