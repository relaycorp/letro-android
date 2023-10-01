package tech.relaycorp.letro.conversation.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import tech.relaycorp.letro.conversation.attachments.AttachmentsRepository
import tech.relaycorp.letro.conversation.attachments.AttachmentsRepositoryImpl
import tech.relaycorp.letro.conversation.list.onboarding.ConversationsOnboardingManager
import tech.relaycorp.letro.conversation.list.onboarding.ConversationsOnboardingManagerImpl
import tech.relaycorp.letro.conversation.server.parser.NewConversationMessageParser
import tech.relaycorp.letro.conversation.server.parser.NewConversationMessageParserImpl
import tech.relaycorp.letro.conversation.server.parser.NewMessageMessageParser
import tech.relaycorp.letro.conversation.server.parser.NewMessageMessageParserImpl
import tech.relaycorp.letro.conversation.server.parser.OutgoingMessageMessageEncoder
import tech.relaycorp.letro.conversation.server.parser.OutgoingMessageMessageEncoderImpl
import tech.relaycorp.letro.conversation.server.processor.NewConversationProcessor
import tech.relaycorp.letro.conversation.server.processor.NewConversationProcessorImpl
import tech.relaycorp.letro.conversation.server.processor.NewMessageProcessor
import tech.relaycorp.letro.conversation.server.processor.NewMessageProcessorImpl
import tech.relaycorp.letro.conversation.storage.converter.ExtendedConversationConverter
import tech.relaycorp.letro.conversation.storage.converter.ExtendedConversationConverterImpl
import tech.relaycorp.letro.conversation.storage.converter.MessageTimestampFormatter
import tech.relaycorp.letro.conversation.storage.converter.MessageTimestampFormatterImpl
import tech.relaycorp.letro.conversation.storage.dao.AttachmentsDao
import tech.relaycorp.letro.conversation.storage.dao.ConversationsDao
import tech.relaycorp.letro.conversation.storage.dao.MessagesDao
import tech.relaycorp.letro.conversation.storage.repository.ConversationsRepository
import tech.relaycorp.letro.conversation.storage.repository.ConversationsRepositoryImpl
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
