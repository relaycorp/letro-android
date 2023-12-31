package tech.relaycorp.letro.push.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import tech.relaycorp.letro.R
import tech.relaycorp.letro.push.PushManager
import tech.relaycorp.letro.push.PushManagerImpl
import tech.relaycorp.letro.push.PushNewMessageTextFormatter
import tech.relaycorp.letro.push.PushNewMessageTextFormatterImpl
import tech.relaycorp.letro.push.PushPermissionManager
import tech.relaycorp.letro.push.PushPermissionManagerImpl
import tech.relaycorp.letro.push.model.PushChannel
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PushesModule {

    @Provides
    fun providePushChannels(): List<PushChannel> {
        return listOf(
            PushChannel(
                id = PushChannel.ChannelId.ID_CONVERSATIONS,
                name = R.string.conversations,
            ),
            PushChannel(
                id = PushChannel.ChannelId.ID_CONTACTS,
                name = R.string.contacts,
            ),
        )
    }

    @Module
    @InstallIn(SingletonComponent::class)
    interface Bindings {
        @Binds
        @Singleton
        fun bindPushManager(
            impl: PushManagerImpl,
        ): PushManager

        @Binds
        @Singleton
        fun bindNotificationPermissionManager(
            impl: PushPermissionManagerImpl,
        ): PushPermissionManager

        @Binds
        fun bindPushNewMessageTextFormatter(
            impl: PushNewMessageTextFormatterImpl,
        ): PushNewMessageTextFormatter
    }
}
