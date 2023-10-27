package tech.relaycorp.letro.awala.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.newSingleThreadContext
import tech.relaycorp.letro.awala.AwalaManager
import tech.relaycorp.letro.awala.AwalaManagerImpl
import tech.relaycorp.letro.awala.AwalaRepository
import tech.relaycorp.letro.awala.AwalaRepositoryImpl
import tech.relaycorp.letro.awala.AwalaWrapper
import tech.relaycorp.letro.awala.AwalaWrapperImpl
import tech.relaycorp.letro.awala.message.AwalaIncomingMessageContent
import tech.relaycorp.letro.awala.message.MessageType
import tech.relaycorp.letro.awala.processor.AwalaCommonMessageProcessor
import tech.relaycorp.letro.awala.processor.AwalaCommonMessageProcessorImpl
import tech.relaycorp.letro.awala.processor.AwalaMessageProcessor
import tech.relaycorp.letro.conversation.di.NewConversationAwalaProcessor
import tech.relaycorp.letro.conversation.di.NewMessageAwalaProcessor
import tech.relaycorp.letro.utils.Logger
import javax.inject.Qualifier
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

@DelicateCoroutinesApi
@OptIn(ExperimentalCoroutinesApi::class)
@Module
@InstallIn(SingletonComponent::class)
object AwalaModule {

    @Provides
    fun provideMessageProcessor(
        accountCreationProcessor: AwalaMessageProcessor<AwalaIncomingMessageContent.AccountCreation>,
        connectionParamsProcessor: AwalaMessageProcessor<AwalaIncomingMessageContent.ConnectionParams>,
        misconfiguredInternetEndpointProcessor: AwalaMessageProcessor<AwalaIncomingMessageContent.MisconfiguredInternetEndpoint>,
        veraIdMemberBundleProcessor: AwalaMessageProcessor<AwalaIncomingMessageContent.VeraIdMemberBundle>,
        contactPairingMatchProcessor: AwalaMessageProcessor<AwalaIncomingMessageContent.ContactPairingMatch>,
        contactPairingAuthorizationProcessor: AwalaMessageProcessor<AwalaIncomingMessageContent.ContactPairingAuthorization>,
        @NewConversationAwalaProcessor newConversationProcessor: AwalaMessageProcessor<AwalaIncomingMessageContent.NewMessage>,
        @NewMessageAwalaProcessor newMessageProcessor: AwalaMessageProcessor<AwalaIncomingMessageContent.NewMessage>,
        logger: Logger,
    ): AwalaCommonMessageProcessor {
        val processors = mapOf(
            MessageType.AccountCreation to accountCreationProcessor,
            MessageType.ConnectionParams to connectionParamsProcessor,
            MessageType.MisconfiguredInternetEndpoint to misconfiguredInternetEndpointProcessor,
            MessageType.VeraIdMemberBundle to veraIdMemberBundleProcessor,
            MessageType.ContactPairingMatch to contactPairingMatchProcessor,
            MessageType.ContactPairingAuthorization to contactPairingAuthorizationProcessor,
            MessageType.NewConversation to newConversationProcessor,
            MessageType.NewMessage to newMessageProcessor,
        )
        return AwalaCommonMessageProcessorImpl(
            processors = processors,
            logger = logger,
        )
    }

    @Provides
    @AwalaThreadContext
    fun provideAwalaThreadContext(): CoroutineContext = newSingleThreadContext("AwalaManagerThread")

    @Module
    @InstallIn(SingletonComponent::class)
    interface Bindings {
        @Singleton
        @Binds
        fun bindAwalaManager(
            impl: AwalaManagerImpl,
        ): AwalaManager

        @Singleton
        @Binds
        fun bindAwalaRepository(
            impl: AwalaRepositoryImpl,
        ): AwalaRepository

        @Singleton
        @Binds
        fun bindAwalaWrapper(
            impl: AwalaWrapperImpl,
        ): AwalaWrapper
    }
}

@Qualifier
annotation class AwalaThreadContext
