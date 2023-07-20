package tech.relaycorp.letro.data.entity

// TODO IDEA for contact, conversation and message adding/deleting
// data class AccountWithContacts(
//    @Embedded val account: AccountDataModel,
//    @Relation(
//        parentColumn = "id",
//        entityColumn = "accountId",
//    )
//    val contacts: List<ContactDataModel>,
// )
//
// fun AccountWithContacts.findContactByAddress(contactAddress: String): ContactDataModel? =
//    contacts.firstOrNull { it.address == contactAddress }
