package tech.relaycorp.letro.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import tech.relaycorp.letro.data.dao.AccountDao
import tech.relaycorp.letro.data.entity.ACCOUNT_TABLE_NAME
import tech.relaycorp.letro.data.entity.AccountDataModel
import javax.inject.Singleton

@Singleton
@Database(
    entities = [
        AccountDataModel::class,
    ],
    version = 3,
    exportSchema = true,
)
abstract class LetroDatabase : RoomDatabase() {

    abstract fun accountDao(): AccountDao

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
                .addMigrations(MIGRATION_2_3)
                .build()
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE $ACCOUNT_TABLE_NAME ADD COLUMN isCurrent INTEGER NOT NULL DEFAULT 0")
    }
}
