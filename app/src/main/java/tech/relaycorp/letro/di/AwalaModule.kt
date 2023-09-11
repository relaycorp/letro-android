package tech.relaycorp.letro.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import tech.relaycorp.letro.awala.AwalaManager
import tech.relaycorp.letro.awala.AwalaManagerImpl
import tech.relaycorp.letro.awala.AwalaRepository
import tech.relaycorp.letro.awala.AwalaRepositoryImpl
import tech.relaycorp.letro.awala.message.MessageType
import tech.relaycorp.letro.awala.parser.UnknownMessageParser
import tech.relaycorp.letro.awala.parser.UnknownMessageParserImpl
import tech.relaycorp.letro.awala.processor.AwalaMessageProcessor
import tech.relaycorp.letro.awala.processor.AwalaMessageProcessorImpl
import tech.relaycorp.letro.awala.processor.UnknownMessageProcessor
import tech.relaycorp.letro.onboarding.registration.processor.RegistrationMessageProcessor
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AwalaModule {

    @Provides
    fun provideMessageProcessor(
        registrationMessageProcessor: RegistrationMessageProcessor,
        unknownMessageProcessor: UnknownMessageProcessor,
    ): AwalaMessageProcessor {
        val processors = mapOf(
            MessageType.AccountCreationCompleted to registrationMessageProcessor,
            MessageType.Unknown to unknownMessageProcessor,
        )
        return AwalaMessageProcessorImpl(processors)
    }

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

        @Binds
        fun bindUnknownMessageParser(
            impl: UnknownMessageParserImpl,
        ): UnknownMessageParser
    }
}
