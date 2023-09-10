package tech.relaycorp.letro.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import tech.relaycorp.letro.account.storage.AccountDao
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
                Room.databaseBuilder(context, LetroDatabase::class.java, DATABASE_NAME)
                    .build()
                    .also {
                        this.database = it
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
