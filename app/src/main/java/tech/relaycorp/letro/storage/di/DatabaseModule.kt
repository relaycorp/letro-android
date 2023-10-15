package tech.relaycorp.letro.storage.di

import android.content.Context
import android.content.pm.PackageManager
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory
import tech.relaycorp.letro.account.storage.dao.AccountDao
import tech.relaycorp.letro.storage.LetroDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private var database: LetroDatabase? = null
    private const val DATABASE_NAME = "letro"

    @Singleton
    @Provides
    @Synchronized
    fun provideLetroDatabase(
        @ApplicationContext context: Context,
    ): LetroDatabase {
        synchronized(this) {
            val database = database
            if (database != null) {
                return database
            } else {
                val passphraseString = context.packageManager.getApplicationInfo(
                    context.packageName,
                    PackageManager.GET_META_DATA,
                ).metaData?.getString(META_DATA_PASSPHRASE_KEY) ?: throw IllegalStateException("You must set passphrase in Manifest!")

                val passphrase: ByteArray = SQLiteDatabase.getBytes(passphraseString.toCharArray())
                Room.databaseBuilder(context, LetroDatabase::class.java, DATABASE_NAME)
                    .openHelperFactory(SupportFactory(passphrase))
                    .build()
                    .also {
                        DatabaseModule.database = it
                        return it
                    }
            }
        }
    }

    @Provides
    fun provideAccountDao(
        letroDatabase: LetroDatabase,
    ): AccountDao {
        return letroDatabase.accountDao()
    }
}

private const val META_DATA_PASSPHRASE_KEY = "tech.relaycorp.letro.db_passphrase"
