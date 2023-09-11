package tech.relaycorp.letro.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import tech.relaycorp.letro.data.dao.AccountDao
import tech.relaycorp.letro.data.dao.ContactDao
import tech.relaycorp.letro.data.dao.ConversationDao
import tech.relaycorp.letro.data.dao.MessageDao
import tech.relaycorp.letro.data.database.LetroDatabase
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object LetroDatabaseModule {

    @Singleton
    @Provides
    fun provideLetroDatabase(@ApplicationContext context: Context): LetroDatabase =
        LetroDatabase.getDatabase(context)

    @Provides
    fun provideAccountDao(database: LetroDatabase): AccountDao =
        database.accountDao()

    @Provides
    fun provideContactDao(database: LetroDatabase): ContactDao =
        database.contactDao()

    @Provides
    fun provideConversationDao(database: LetroDatabase): ConversationDao =
        database.conversationDao()

    @Provides
    fun provideMessageDao(database: LetroDatabase): MessageDao =
        database.messageDao()
}
