package tech.relaycorp.letro.account.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import tech.relaycorp.letro.account.storage.repository.AccountRepository
import tech.relaycorp.letro.account.storage.repository.AccountRepositoryImpl
import tech.relaycorp.letro.account.utils.AccountsSorter
import tech.relaycorp.letro.account.utils.AccountsSorterImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface AccountModule {

    @Binds
    @Singleton
    fun bindAccountRepository(
        impl: AccountRepositoryImpl,
    ): AccountRepository

    @Binds
    fun bindAccountSorter(
        impl: AccountsSorterImpl,
    ): AccountsSorter
}
