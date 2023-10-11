package tech.relaycorp.letro.awala.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.newSingleThreadContext
import tech.relaycorp.letro.account.registration.server.AccountCreationProcessor
import tech.relaycorp.letro.account.registration.server.ConnectionParamsProcessor
import tech.relaycorp.letro.account.registration.server.MisconfiguredInternetEndpointProcessor
import tech.relaycorp.letro.awala.AwalaManager
import tech.relaycorp.letro.awala.AwalaManagerImpl
import tech.relaycorp.letro.awala.AwalaRepository
import tech.relaycorp.letro.awala.AwalaRepositoryImpl
import tech.relaycorp.letro.awala.AwalaWrapper
import tech.relaycorp.letro.awala.AwalaWrapperImpl
import tech.relaycorp.letro.awala.message.MessageType
import tech.relaycorp.letro.awala.processor.AwalaMessageProcessor
import tech.relaycorp.letro.awala.processor.AwalaMessageProcessorImpl
import tech.relaycorp.letro.awala.processor.UnknownMessageProcessor
import tech.relaycorp.letro.contacts.pairing.processor.ContactPairingAuthorizationProcessor
import tech.relaycorp.letro.contacts.pairing.processor.ContactPairingMatchProcessor
import tech.relaycorp.letro.conversation.server.processor.NewConversationProcessor
import tech.relaycorp.letro.conversation.server.processor.NewMessageProcessor
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
        accountCreationProcessor: AccountCreationProcessor,
        connectionParamsProcessor: ConnectionParamsProcessor,
        misconfiguredInternetEndpointProcessor: MisconfiguredInternetEndpointProcessor,
        contactPairingMatchProcessor: ContactPairingMatchProcessor,
        contactPairingAuthorizationProcessor: ContactPairingAuthorizationProcessor,
        newConversationProcessor: NewConversationProcessor,
        newMessageProcessor: NewMessageProcessor,
        unknownMessageProcessor: UnknownMessageProcessor,
    ): AwalaMessageProcessor {
        val processors = mapOf(
            MessageType.AccountCreation to accountCreationProcessor,
            MessageType.ConnectionParams to connectionParamsProcessor,
            MessageType.MisconfiguredInternetEndpoint to misconfiguredInternetEndpointProcessor,
            MessageType.ContactPairingMatch to contactPairingMatchProcessor,
            MessageType.ContactPairingAuthorization to contactPairingAuthorizationProcessor,
            MessageType.NewConversation to newConversationProcessor,
            MessageType.NewMessage to newMessageProcessor,
            MessageType.Unknown to unknownMessageProcessor,
        )
        return AwalaMessageProcessorImpl(processors)
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
