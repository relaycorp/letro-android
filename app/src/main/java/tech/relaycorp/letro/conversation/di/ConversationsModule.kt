package tech.relaycorp.letro.conversation.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped
import dagger.hilt.components.SingletonComponent
import tech.relaycorp.awaladroid.messaging.Message
import tech.relaycorp.letro.awala.message.AwalaIncomingMessageContent
import tech.relaycorp.letro.awala.processor.AwalaMessageProcessor
import tech.relaycorp.letro.conversation.attachments.AttachmentsRepository
import tech.relaycorp.letro.conversation.attachments.AttachmentsRepositoryImpl
import tech.relaycorp.letro.conversation.attachments.ConversationFileConverter
import tech.relaycorp.letro.conversation.attachments.filepicker.FileConverter
import tech.relaycorp.letro.conversation.list.onboarding.ConversationsOnboardingManager
import tech.relaycorp.letro.conversation.list.onboarding.ConversationsOnboardingManagerImpl
import tech.relaycorp.letro.conversation.list.selection.ConversationSelector
import tech.relaycorp.letro.conversation.list.selection.ConversationSelectorImpl
import tech.relaycorp.letro.conversation.server.parser.NewConversationMessageParser
import tech.relaycorp.letro.conversation.server.parser.NewConversationMessageParserImpl
import tech.relaycorp.letro.conversation.server.parser.NewMessageMessageParser
import tech.relaycorp.letro.conversation.server.parser.NewMessageMessageParserImpl
import tech.relaycorp.letro.conversation.server.parser.OutgoingMessageMessageEncoder
import tech.relaycorp.letro.conversation.server.parser.OutgoingMessageMessageEncoderImpl
import tech.relaycorp.letro.conversation.server.processor.NewConversationProcessor
import tech.relaycorp.letro.conversation.server.processor.NewMessageProcessor
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
import javax.inject.Qualifier
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

    @Provides
    @MessageSizeLimitBytes
    fun provideMessageSizeLimit(): Int {
        return Message.MAX_CONTENT_SIZE
    }

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
        @NewConversationAwalaProcessor
        fun bindNewConversationProcessor(
            impl: NewConversationProcessor,
        ): AwalaMessageProcessor<AwalaIncomingMessageContent.NewMessage>

        @Binds
        fun bindNewConversationParser(
            imp: NewConversationMessageParserImpl,
        ): NewConversationMessageParser

        @Binds
        @NewMessageAwalaProcessor
        fun bindNewMessageProcessor(
            impl: NewMessageProcessor,
        ): AwalaMessageProcessor<AwalaIncomingMessageContent.NewMessage>

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

        @Binds
        @ConversationFileConverterAnnotation
        fun bindFileConverter(
            impl: ConversationFileConverter,
        ): FileConverter
    }
}

@Module
@InstallIn(ActivityRetainedComponent::class)
interface ConversationActivityRetainedModule {
    @Binds
    @ActivityRetainedScoped
    fun bindConversationSelector(
        impl: ConversationSelectorImpl,
    ): ConversationSelector
}

@Qualifier
annotation class NewConversationAwalaProcessor

@Qualifier
annotation class NewMessageAwalaProcessor

@Qualifier
annotation class MessageSizeLimitBytes

@Qualifier
annotation class ConversationFileConverterAnnotation
