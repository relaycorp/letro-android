package tech.relaycorp.letro

import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import org.mockito.Mockito.mock
import tech.relaycorp.letro.di.AppModule
import tech.relaycorp.letro.repository.AccountRepository
import tech.relaycorp.letro.repository.GatewayRepository

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [AppModule::class],
)
object TestModule {

    @Provides
    fun provideGatewayRepository(): GatewayRepository = mock(GatewayRepository::class.java)

    @Provides
    fun provideAccountRepository(): AccountRepository = mock(AccountRepository::class.java)
}
