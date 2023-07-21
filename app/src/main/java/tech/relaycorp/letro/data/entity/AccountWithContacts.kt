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
// fun AccountWithContacts.findContactByVeraId(contactVeraId: String): ContactDataModel? =
//    contacts.firstOrNull { it.veraId == contactVeraId }
