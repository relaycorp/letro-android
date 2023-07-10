package tech.relaycorp.letro.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import tech.relaycorp.letro.data.dao.AccountDao
import tech.relaycorp.letro.data.database.LetroDatabase

@InstallIn(SingletonComponent::class)
@Module
object LetroDatabaseModule {

    @Singleton
    @Provides
    fun provideLetroDatabase(@ApplicationContext context: Context): LetroDatabase =
        LetroDatabase.getDatabase(context)

    @Provides
    fun provideUserDao(database: LetroDatabase): AccountDao =
        database.accountDao()
}