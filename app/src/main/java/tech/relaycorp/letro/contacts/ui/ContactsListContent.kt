package tech.relaycorp.letro.contacts.ui

import tech.relaycorp.letro.contacts.model.Contact

sealed interface ContactsListContent {

    data class Contacts(
        val contacts: List<Contact>,
    ) : ContactsListContent

    data object Empty : ContactsListContent
}
