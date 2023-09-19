package tech.relaycorp.letro.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import tech.relaycorp.letro.messages.converter.ExtendedConversationConverter
import tech.relaycorp.letro.messages.converter.ExtendedConversationConverterImpl
import tech.relaycorp.letro.messages.converter.MessageTimestampConverter
import tech.relaycorp.letro.messages.converter.MessageTimestampConverterImpl
import tech.relaycorp.letro.messages.repository.ConversationsRepository
import tech.relaycorp.letro.messages.repository.ConversationsRepositoryImpl
import tech.relaycorp.letro.messages.storage.ConversationsDao
import tech.relaycorp.letro.messages.storage.MessagesDao
import tech.relaycorp.letro.storage.LetroDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ConversationsModule {

    @Provides
    fun provideConversationsDao(
        letroDatabase: LetroDatabase,
    ): ConversationsDao = letroDatabase.conversationsDao()

    @Provides
    fun provideMessagesDao(
        letroDatabase: LetroDatabase,
    ): MessagesDao = letroDatabase.messagesDao()

    @Module
    @InstallIn(SingletonComponent::class)
    interface Bindings {

        @Singleton
        @Binds
        fun bindConversationsRepository(
            impl: ConversationsRepositoryImpl,
        ): ConversationsRepository

        @Binds
        fun bindExtendedConversationsConverter(
            impl: ExtendedConversationConverterImpl,
        ): ExtendedConversationConverter

        @Binds
        fun bindMessageTimestampConverter(
            impl: MessageTimestampConverterImpl,
        ): MessageTimestampConverter
    }
}
