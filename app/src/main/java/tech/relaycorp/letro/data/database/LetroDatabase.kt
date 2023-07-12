package tech.relaycorp.letro.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import tech.relaycorp.letro.data.dao.AccountDao
import tech.relaycorp.letro.data.entity.AccountDataModel
import javax.inject.Singleton

@Singleton
@Database(
    entities = [
        AccountDataModel::class,
    ],
    version = 2,
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
                .build()
    }
}
