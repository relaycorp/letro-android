package tech.relaycorp.letro.storage

import androidx.room.Database
import androidx.room.RoomDatabase
import tech.relaycorp.letro.account.model.Account
import tech.relaycorp.letro.account.storage.AccountDao
import tech.relaycorp.letro.contacts.model.Contact
import tech.relaycorp.letro.contacts.storage.ContactsDao

@Database(
    entities = [
        Account::class,
        Contact::class,
    ],
    version = 1,
)
abstract class LetroDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun contactsDao(): ContactsDao
}
