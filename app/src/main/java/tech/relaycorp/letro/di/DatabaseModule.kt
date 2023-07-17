package tech.relaycorp.letro.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import tech.relaycorp.letro.data.dao.AccountDao
import tech.relaycorp.letro.data.database.LetroDatabase
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object DatabaseModule {

    @Singleton
    @Provides
    fun provideLetroDatabase(@ApplicationContext context: Context): LetroDatabase =
        LetroDatabase.getDatabase(context)

    @Provides
    fun provideAccountDao(database: LetroDatabase): AccountDao =
        database.accountDao()
}
