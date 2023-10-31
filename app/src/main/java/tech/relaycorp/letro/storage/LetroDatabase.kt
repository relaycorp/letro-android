package tech.relaycorp.letro.storage

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import tech.relaycorp.letro.account.model.Account
import tech.relaycorp.letro.account.storage.dao.AccountDao
import tech.relaycorp.letro.contacts.model.Contact
import tech.relaycorp.letro.contacts.storage.dao.ContactsDao
import tech.relaycorp.letro.conversation.storage.dao.AttachmentsDao
import tech.relaycorp.letro.conversation.storage.dao.ConversationsDao
import tech.relaycorp.letro.conversation.storage.dao.MessagesDao
import tech.relaycorp.letro.conversation.storage.entity.Attachment
import tech.relaycorp.letro.conversation.storage.entity.Conversation
import tech.relaycorp.letro.conversation.storage.entity.Message
import tech.relaycorp.letro.notification.storage.dao.NotificationsDao
import tech.relaycorp.letro.notification.storage.entity.Notification
import tech.relaycorp.letro.storage.converter.ZonedDateTimeConverter

@Database(
    entities = [
        Account::class,
        Contact::class,
        Conversation::class,
        Message::class,
        Notification::class,
        Attachment::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(
    ZonedDateTimeConverter::class,
)
abstract class LetroDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun contactsDao(): ContactsDao
    abstract fun conversationsDao(): ConversationsDao
    abstract fun messagesDao(): MessagesDao
    abstract fun notificationsDao(): NotificationsDao
    abstract fun attachmentsDao(): AttachmentsDao
}
