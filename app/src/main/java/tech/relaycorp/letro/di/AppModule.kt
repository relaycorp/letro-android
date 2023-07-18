package tech.relaycorp.letro.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import tech.relaycorp.letro.data.dao.AccountDao
import tech.relaycorp.letro.repository.AccountRepository
import tech.relaycorp.letro.repository.GatewayRepository
import tech.relaycorp.letro.repository.PreferencesDataStoreRepository
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object AppModule {

    @Singleton
    @Provides
    fun provideGatewayRepository(
        @ApplicationContext context: Context,
        preferencesDataStoreRepository: PreferencesDataStoreRepository,
    ): GatewayRepository =
        GatewayRepository(context, preferencesDataStoreRepository)

    @Singleton
    @Provides
    fun providePreferencesDataStoreRepository(@ApplicationContext context: Context): PreferencesDataStoreRepository =
        PreferencesDataStoreRepository(context)

    @Singleton
    @Provides
    fun provideAccountRepository(
        accountDao: AccountDao,
        gatewayRepository: GatewayRepository,
    ): AccountRepository =
        AccountRepository(accountDao, gatewayRepository)
}
