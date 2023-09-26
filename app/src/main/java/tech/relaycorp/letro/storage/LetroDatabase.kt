package tech.relaycorp.letro.storage

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import tech.relaycorp.letro.account.model.Account
import tech.relaycorp.letro.account.storage.AccountDao
import tech.relaycorp.letro.contacts.model.Contact
import tech.relaycorp.letro.contacts.storage.ContactsDao
import tech.relaycorp.letro.messages.storage.ConversationsDao
import tech.relaycorp.letro.messages.storage.MessagesDao
import tech.relaycorp.letro.messages.storage.entity.Conversation
import tech.relaycorp.letro.messages.storage.entity.Message
import tech.relaycorp.letro.notification.storage.dao.NotificationsDao
import tech.relaycorp.letro.notification.storage.entity.Notification
import tech.relaycorp.letro.storage.converter.LocalDateTimeConverter

@Database(
    entities = [
        Account::class,
        Contact::class,
        Conversation::class,
        Message::class,
        Notification::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(
    LocalDateTimeConverter::class,
)
abstract class LetroDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun contactsDao(): ContactsDao
    abstract fun conversationsDao(): ConversationsDao
    abstract fun messagesDao(): MessagesDao
    abstract fun notificationsDao(): NotificationsDao
}
