package tech.relaycorp.letro.storage

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import tech.relaycorp.letro.account.model.Account
import tech.relaycorp.letro.account.storage.AccountDao
import tech.relaycorp.letro.contacts.model.Contact
import tech.relaycorp.letro.contacts.storage.ContactsDao
import tech.relaycorp.letro.contacts.storage.converter.ContactPairingStatusConverter

@Database(
    entities = [
        Account::class,
        Contact::class,
    ],
    version = 1,
)
@TypeConverters(ContactPairingStatusConverter::class)
abstract class LetroDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun contactsDao(): ContactsDao
}
