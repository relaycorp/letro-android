package tech.relaycorp.letro.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import tech.relaycorp.letro.account.storage.AccountRepository
import tech.relaycorp.letro.account.storage.AccountRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface AccountModule {

    @Binds
    @Singleton
    fun bindAccountRepository(
        impl: AccountRepositoryImpl
    ): AccountRepository

}