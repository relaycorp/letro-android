package tech.relaycorp.letro.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import tech.relaycorp.letro.notification.NotificationPermissionManager
import tech.relaycorp.letro.notification.NotificationPermissionManagerImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface NotificationModule {

    @Binds
    @Singleton
    fun bindNotificationPermissionManager(
        impl: NotificationPermissionManagerImpl,
    ): NotificationPermissionManager
}
