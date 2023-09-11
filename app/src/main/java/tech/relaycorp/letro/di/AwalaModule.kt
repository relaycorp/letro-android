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
import tech.relaycorp.letro.awala.parser.AwalaMessageParser
import tech.relaycorp.letro.awala.parser.AwalaMessageParserImpl
import tech.relaycorp.letro.awala.parser.UnknownMessageParser
import tech.relaycorp.letro.awala.parser.UnknownMessageParserImpl
import tech.relaycorp.letro.onboarding.registration.parser.RegistrationMessageParser
import tech.relaycorp.letro.pairing.parser.ContactPairingMatchParser
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AwalaModule {

    @Provides
    fun provideMessageParser(
        registrationParser: RegistrationMessageParser,
        contactPairingMatchParser: ContactPairingMatchParser,
        unknownMessageParser: UnknownMessageParser,
    ): AwalaMessageParser {
        val parsers = mapOf(
            MessageType.AccountCreationCompleted to registrationParser,
            MessageType.ContactPairingMatch to contactPairingMatchParser,
            MessageType.Unknown to unknownMessageParser,
        )
        return AwalaMessageParserImpl(parsers)
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
