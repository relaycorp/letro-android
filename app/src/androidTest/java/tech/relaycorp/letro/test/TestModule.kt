package tech.relaycorp.letro.test

import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import tech.relaycorp.letro.data.AccountCreatedDataModel
import tech.relaycorp.letro.di.AppModule
import tech.relaycorp.letro.repository.IAccountRepository
import tech.relaycorp.letro.repository.IGatewayRepository
import tech.relaycorp.letro.repository.IPreferencesDataStoreRepository

val mockGatewayRepo: IGatewayRepository = mock(IGatewayRepository::class.java)
val mockAccountRepo: IAccountRepository = mock(IAccountRepository::class.java)
val mockPreferencesDataStoreRepo: IPreferencesDataStoreRepository = mock(IPreferencesDataStoreRepository::class.java)

val accountCreatedConfirmationReceived: SharedFlow<AccountCreatedDataModel> = MutableSharedFlow()
val firstPartyEndpointNodeId: StateFlow<String?> = MutableStateFlow(null)
val serverThirdPartyEndpointNodeId: StateFlow<String?> = MutableStateFlow(null)

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [AppModule::class],
)
object TestModule {

    @Provides
    fun provideGatewayRepository(): IGatewayRepository {
        `when`(mockGatewayRepo.accountCreatedConfirmationReceived).thenReturn(accountCreatedConfirmationReceived)
        `when`(mockGatewayRepo.serverFirstPartyEndpointNodeId).thenReturn(firstPartyEndpointNodeId)
        `when`(mockGatewayRepo.serverThirdPartyEndpointNodeId).thenReturn(serverThirdPartyEndpointNodeId)
        return mockGatewayRepo
    }

    @Provides
    fun provideAccountRepository(): IAccountRepository = mockAccountRepo

    @Provides
    fun providePreferencesDataStoreRepository(): IPreferencesDataStoreRepository =
        mockPreferencesDataStoreRepo
}
