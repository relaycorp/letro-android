package tech.relaycorp.letro.account.registration.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import dagger.hilt.components.SingletonComponent
import tech.relaycorp.letro.account.registration.MisconfiguredInternetEndpointParser
import tech.relaycorp.letro.account.registration.MisconfiguredInternetEndpointParserImpl
import tech.relaycorp.letro.account.registration.server.AccountCreationParser
import tech.relaycorp.letro.account.registration.server.AccountCreationParserImpl
import tech.relaycorp.letro.account.registration.server.AccountCreationProcessor
import tech.relaycorp.letro.account.registration.server.ConnectionParamsParser
import tech.relaycorp.letro.account.registration.server.ConnectionParamsParserImpl
import tech.relaycorp.letro.account.registration.server.ConnectionParamsProcessor
import tech.relaycorp.letro.account.registration.server.MisconfiguredInternetEndpointProcessor
import tech.relaycorp.letro.account.registration.server.VeraIdMemberBundleParser
import tech.relaycorp.letro.account.registration.server.VeraIdMemberBundleParserImpl
import tech.relaycorp.letro.account.registration.server.VeraIdMemberBundleProcessor
import tech.relaycorp.letro.account.registration.storage.RegistrationRepository
import tech.relaycorp.letro.account.registration.storage.RegistrationRepositoryImpl
import tech.relaycorp.letro.account.registration.utils.AccountIdBuilder
import tech.relaycorp.letro.account.registration.utils.AccountIdBuilderImpl
import tech.relaycorp.letro.account.registration.utils.RegistrationDomainProvider
import tech.relaycorp.letro.account.registration.utils.RegistrationDomainProviderImpl
import tech.relaycorp.letro.awala.message.AwalaIncomingMessageContent
import tech.relaycorp.letro.awala.processor.AwalaMessageProcessor
import javax.inject.Singleton

@Module
@InstallIn(ViewModelComponent::class)
interface RegistrationModule {

    @Binds
    @ViewModelScoped
    fun bindDomainProvider(
        impl: RegistrationDomainProviderImpl,
    ): RegistrationDomainProvider
}

@Module
@InstallIn(SingletonComponent::class)
interface RegistrationModuleSingleton {

    @Binds
    @Singleton
    fun bindRegistrationRepository(
        impl: RegistrationRepositoryImpl,
    ): RegistrationRepository

    @Binds
    fun bindRegistrationMessageProcessor(
        impl: AccountCreationProcessor,
    ): AwalaMessageProcessor<AwalaIncomingMessageContent.AccountCreation>

    @Binds
    fun bindAccountCreationParser(
        impl: AccountCreationParserImpl,
    ): AccountCreationParser

    @Binds
    fun bindMisconfiguredInternetEndpointParser(
        impl: MisconfiguredInternetEndpointParserImpl,
    ): MisconfiguredInternetEndpointParser

    @Binds
    fun bindConnectionParamsParser(
        impl: ConnectionParamsParserImpl,
    ): ConnectionParamsParser

    @Binds
    fun bindConnectionParamsProcessor(
        impl: ConnectionParamsProcessor,
    ): AwalaMessageProcessor<AwalaIncomingMessageContent.ConnectionParams>

    @Binds
    fun bindVeraIdMemberBundleParser(
        impl: VeraIdMemberBundleParserImpl,
    ): VeraIdMemberBundleParser

    @Binds
    fun bindVeraIdMemberBundleProcessor(
        impl: VeraIdMemberBundleProcessor,
    ): AwalaMessageProcessor<AwalaIncomingMessageContent.VeraIdMemberBundle>

    @Binds
    fun bindMisconfiguredInternetEndpointProcessor(
        impl: MisconfiguredInternetEndpointProcessor,
    ): AwalaMessageProcessor<AwalaIncomingMessageContent.MisconfiguredInternetEndpoint>

    @Binds
    fun bindAccountIdBuilder(
        impl: AccountIdBuilderImpl,
    ): AccountIdBuilder
}
