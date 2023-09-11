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
import tech.relaycorp.letro.data.entity.ACCOUNT_TABLE_NAME
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
    version = 3,
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
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
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
        database.execSQL("CREATE INDEX `index_contact_accountId` ON $CONTACT_TABLE_NAME (`accountId`)")
    }
}

val MIGRATION_2_3: Migration = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // First, create temporary tables that include the `veraId` column
        database.execSQL(
            """
            CREATE TABLE new_account 
            (id INTEGER PRIMARY KEY NOT NULL, veraId TEXT NOT NULL, isCurrent INTEGER NOT NULL, isCreationConfirmed INTEGER NOT NULL)
        """,
        )

        database.execSQL(
            """
            CREATE TABLE new_contact 
            (id INTEGER PRIMARY KEY NOT NULL, accountId INTEGER NOT NULL, veraId TEXT NOT NULL, alias TEXT NOT NULL, 
            contactEndpointId TEXT, status TEXT NOT NULL, FOREIGN KEY(accountId) REFERENCES account(id) ON DELETE CASCADE)
        """,
        )

        // Copy the data from the old tables to the new tables, mapping `address` to `veraId`
        database.execSQL(
            """
            INSERT INTO new_account (id, veraId, isCurrent, isCreationConfirmed)
            SELECT id, address, isCurrent, isCreationConfirmed FROM account
        """,
        )

        database.execSQL(
            """
            INSERT INTO new_contact (id, accountId, veraId, alias, contactEndpointId, status)
            SELECT id, accountId, address, alias, contactEndpointId, status FROM contact
        """,
        )

        // Remove the old tables
        database.execSQL("DROP TABLE $ACCOUNT_TABLE_NAME")
        database.execSQL("DROP TABLE $CONTACT_TABLE_NAME")

        // Rename the new tables to the names of the old tables
        database.execSQL("ALTER TABLE new_account RENAME TO $ACCOUNT_TABLE_NAME")
        database.execSQL("ALTER TABLE new_contact RENAME TO $CONTACT_TABLE_NAME")

        // Create a new index on accountId in the contact table
        database.execSQL("CREATE INDEX index_contact_accountId ON $CONTACT_TABLE_NAME (accountId)")
    }
}
