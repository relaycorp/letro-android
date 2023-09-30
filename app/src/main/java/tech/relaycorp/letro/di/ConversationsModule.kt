package tech.relaycorp.letro.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import tech.relaycorp.letro.messages.attachments.AttachmentsRepository
import tech.relaycorp.letro.messages.attachments.AttachmentsRepositoryImpl
import tech.relaycorp.letro.messages.converter.ExtendedConversationConverter
import tech.relaycorp.letro.messages.converter.ExtendedConversationConverterImpl
import tech.relaycorp.letro.messages.converter.MessageTimestampFormatter
import tech.relaycorp.letro.messages.converter.MessageTimestampFormatterImpl
import tech.relaycorp.letro.messages.onboarding.ConversationsOnboardingManager
import tech.relaycorp.letro.messages.onboarding.ConversationsOnboardingManagerImpl
import tech.relaycorp.letro.messages.parser.NewConversationMessageParser
import tech.relaycorp.letro.messages.parser.NewConversationMessageParserImpl
import tech.relaycorp.letro.messages.parser.NewMessageMessageParser
import tech.relaycorp.letro.messages.parser.NewMessageMessageParserImpl
import tech.relaycorp.letro.messages.parser.OutgoingMessageMessageEncoder
import tech.relaycorp.letro.messages.parser.OutgoingMessageMessageEncoderImpl
import tech.relaycorp.letro.messages.processor.NewConversationProcessor
import tech.relaycorp.letro.messages.processor.NewConversationProcessorImpl
import tech.relaycorp.letro.messages.processor.NewMessageProcessor
import tech.relaycorp.letro.messages.processor.NewMessageProcessorImpl
import tech.relaycorp.letro.messages.repository.ConversationsRepository
import tech.relaycorp.letro.messages.repository.ConversationsRepositoryImpl
import tech.relaycorp.letro.messages.storage.AttachmentsDao
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

    @Provides
    fun provideAttachmentsDao(
        letroDatabase: LetroDatabase,
    ): AttachmentsDao = letroDatabase.attachmentsDao()

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
            impl: MessageTimestampFormatterImpl,
        ): MessageTimestampFormatter

        @Binds
        fun bindOutgoingConversationMessageEncoder(
            impl: OutgoingMessageMessageEncoderImpl,
        ): OutgoingMessageMessageEncoder

        @Binds
        fun bindNewConversationProcessor(
            impl: NewConversationProcessorImpl,
        ): NewConversationProcessor

        @Binds
        fun bindNewConversationParser(
            imp: NewConversationMessageParserImpl,
        ): NewConversationMessageParser

        @Binds
        fun bindNewMessageProcessor(
            impl: NewMessageProcessorImpl,
        ): NewMessageProcessor

        @Binds
        fun bindNewMessageParser(
            impl: NewMessageMessageParserImpl,
        ): NewMessageMessageParser

        @Binds
        fun bindOnboardingMessageManager(
            impl: ConversationsOnboardingManagerImpl,
        ): ConversationsOnboardingManager

        @Binds
        @Singleton
        fun bindAttachmentsRepository(
            impl: AttachmentsRepositoryImpl,
        ): AttachmentsRepository
    }
}
