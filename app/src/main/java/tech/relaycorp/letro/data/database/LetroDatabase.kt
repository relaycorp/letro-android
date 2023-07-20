package tech.relaycorp.letro.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import tech.relaycorp.letro.data.dao.AccountDao
import tech.relaycorp.letro.data.dao.ContactDao
import tech.relaycorp.letro.data.dao.ConversationDao
import tech.relaycorp.letro.data.dao.MessageDao
import tech.relaycorp.letro.data.entity.AccountDataModel
import tech.relaycorp.letro.data.entity.ContactDataModel
import tech.relaycorp.letro.data.entity.ConversationDataModel
import tech.relaycorp.letro.data.entity.MessageDataModel
import javax.inject.Singleton

@Singleton
@Database(
    entities = [
        AccountDataModel::class,
        ContactDataModel::class,
        ConversationDataModel::class,
        MessageDataModel::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class LetroDatabase : RoomDatabase() {

    abstract fun accountDao(): AccountDao
    abstract fun contactDao(): ContactDao
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao

    companion object {
        private const val DATABASE_NAME = "letro"

        @Volatile
        private var instance: LetroDatabase? = null

        fun getDatabase(context: Context): LetroDatabase =
            instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also {
                    instance = it
                }
            }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(context, LetroDatabase::class.java, DATABASE_NAME)
                .fallbackToDestructiveMigrationOnDowngrade()
                .build()
    }
}
