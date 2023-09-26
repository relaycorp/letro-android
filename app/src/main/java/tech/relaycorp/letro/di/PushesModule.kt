package tech.relaycorp.letro.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import tech.relaycorp.letro.push.PushPermissionManager
import tech.relaycorp.letro.push.PushPermissionManagerImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface PushesModule {
    @Binds
    @Singleton
    fun bindNotificationPermissionManager(
        impl: PushPermissionManagerImpl,
    ): PushPermissionManager
}
