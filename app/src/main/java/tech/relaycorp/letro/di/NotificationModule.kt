package tech.relaycorp.letro.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import tech.relaycorp.letro.notification.NotificationPermissionManager
import tech.relaycorp.letro.notification.NotificationPermissionManagerImpl
import tech.relaycorp.letro.notification.converter.ExtendedNotificationConverter
import tech.relaycorp.letro.notification.converter.ExtendedNotificationConverterImpl
import tech.relaycorp.letro.notification.converter.ExtendedNotificationDateFormatter
import tech.relaycorp.letro.notification.converter.ExtendedNotificationDateFormatterImpl
import tech.relaycorp.letro.notification.repository.NotificationsRepository
import tech.relaycorp.letro.notification.repository.NotificationsRepositoryImpl
import tech.relaycorp.letro.notification.storage.dao.NotificationsDao
import tech.relaycorp.letro.storage.LetroDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NotificationModule {

    @Provides
    fun provideNotificationsDao(
        database: LetroDatabase,
    ): NotificationsDao {
        return database.notificationsDao()
    }

    @Module
    @InstallIn(SingletonComponent::class)
    interface Bindings {

        @Binds
        @Singleton
        fun bindNotificationPermissionManager(
            impl: NotificationPermissionManagerImpl,
        ): NotificationPermissionManager

        @Binds
        @Singleton
        fun bindNotificationsRepository(
            impl: NotificationsRepositoryImpl,
        ): NotificationsRepository

        @Binds
        fun bindExtendedNotificationConverter(
            impl: ExtendedNotificationConverterImpl,
        ): ExtendedNotificationConverter

        @Binds
        fun bindNotificationTimeFormatter(
            impl: ExtendedNotificationDateFormatterImpl,
        ): ExtendedNotificationDateFormatter
    }
}
