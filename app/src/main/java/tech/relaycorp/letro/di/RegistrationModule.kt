package tech.relaycorp.letro.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import dagger.hilt.components.SingletonComponent
import tech.relaycorp.letro.onboarding.registration.RegistrationDomainProvider
import tech.relaycorp.letro.onboarding.registration.RegistrationDomainProviderImpl
import tech.relaycorp.letro.onboarding.registration.RegistrationRepository
import tech.relaycorp.letro.onboarding.registration.RegistrationRepositoryImpl
import tech.relaycorp.letro.onboarding.registration.parser.RegistrationMessageParser
import tech.relaycorp.letro.onboarding.registration.parser.RegistrationMessageParserImpl
import javax.inject.Singleton

@Module
@InstallIn(ViewModelComponent::class)
interface RegistrationModule {

    @Binds
    @ViewModelScoped
    fun bindDomainProvider(
        impl: RegistrationDomainProviderImpl
    ): RegistrationDomainProvider
}

@Module
@InstallIn(SingletonComponent::class)
interface RegistrationModuleSingleton {

    @Binds
    @Singleton
    fun bindRegistrationRepository(
        impl: RegistrationRepositoryImpl
    ): RegistrationRepository

    @Binds
    fun bindRegistrationMessageParser(
        impl: RegistrationMessageParserImpl
    ): RegistrationMessageParser
}