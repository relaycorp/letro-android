package tech.relaycorp.letro.data.entity

import androidx.room.Embedded
import androidx.room.Relation

data class AccountWithContacts(
    @Embedded val account: AccountDataModel,
    @Relation(
        parentColumn = "id",
        entityColumn = "accountId",
    )
    val contacts: List<ContactDataModel>,
)

fun AccountWithContacts.findContactByAddress(contactAddress: String): ContactDataModel? =
    contacts.firstOrNull { it.address == contactAddress }
