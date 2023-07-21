package tech.relaycorp.letro.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import tech.relaycorp.letro.data.dao.AccountDao
import tech.relaycorp.letro.data.dao.ContactDao
import tech.relaycorp.letro.data.dao.ConversationDao
import tech.relaycorp.letro.data.dao.MessageDao
import tech.relaycorp.letro.data.entity.AccountDataModel
import tech.relaycorp.letro.data.entity.CONTACT_TABLE_NAME
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
    version = 2,
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
                .addMigrations(MIGRATION_1_2)
                .fallbackToDestructiveMigrationOnDowngrade()
                .build()
    }
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "CREATE TABLE IF NOT EXISTS `new_contact` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`accountId` INTEGER NOT NULL, " +
                "`address` TEXT NOT NULL, " +
                "`alias` TEXT NOT NULL, " +
                "`contactEndpointId` TEXT, " +
                "`status` TEXT NOT NULL, " +
                "FOREIGN KEY(`accountId`) REFERENCES `account`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )",
        )

        database.execSQL(
            "INSERT INTO `new_contact` (`id`, `accountId`, `address`, `alias`, `contactEndpointId`, `status`) " +
                "SELECT `id`, `accountId`, `address`, `alias`, `contactEndpointId`, `status` FROM $CONTACT_TABLE_NAME",
        )

        database.execSQL("DROP TABLE $CONTACT_TABLE_NAME")
        database.execSQL("ALTER TABLE `new_contact` RENAME TO $CONTACT_TABLE_NAME")
        database.execSQL("CREATE INDEX `index_contact_accountId` ON `contact` (`accountId`)")
    }
}
