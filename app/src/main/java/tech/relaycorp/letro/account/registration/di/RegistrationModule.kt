package tech.relaycorp.letro.account.registration.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import dagger.hilt.components.SingletonComponent
import tech.relaycorp.letro.account.registration.server.AccountCreationProcessor
import tech.relaycorp.letro.account.registration.server.AccountCreationProcessorImpl
import tech.relaycorp.letro.account.registration.server.ConnectionParamsProcessor
import tech.relaycorp.letro.account.registration.server.ConnectionParamsProcessorImpl
import tech.relaycorp.letro.account.registration.server.MisconfiguredInternetEndpointProcessor
import tech.relaycorp.letro.account.registration.server.MisconfiguredInternetEndpointProcessorImpl
import tech.relaycorp.letro.account.registration.storage.RegistrationRepository
import tech.relaycorp.letro.account.registration.storage.RegistrationRepositoryImpl
import tech.relaycorp.letro.account.registration.utils.RegistrationDomainProvider
import tech.relaycorp.letro.account.registration.utils.RegistrationDomainProviderImpl
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
        impl: AccountCreationProcessorImpl,
    ): AccountCreationProcessor

    @Binds
    fun bindConnectionParamsProcessor(
        impl: ConnectionParamsProcessorImpl
    ): ConnectionParamsProcessor

    @Binds
    fun bindMisconfiguredInternetEndpointProcessor(
        impl: MisconfiguredInternetEndpointProcessorImpl
    ): MisconfiguredInternetEndpointProcessor
}
