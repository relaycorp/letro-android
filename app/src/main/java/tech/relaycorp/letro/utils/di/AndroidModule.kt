package tech.relaycorp.letro.utils.di

import android.content.ContentResolver
import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import tech.relaycorp.letro.BuildConfig
import tech.relaycorp.letro.storage.Preferences
import tech.relaycorp.letro.storage.PreferencesImpl
import tech.relaycorp.letro.utils.Logger
import tech.relaycorp.letro.utils.LoggerImpl
import tech.relaycorp.letro.utils.navigation.UriToActionConverter
import tech.relaycorp.letro.utils.navigation.UriToActionConverterImpl
import tech.relaycorp.letro.utils.time.DeviceTimeChangedProvider
import tech.relaycorp.letro.utils.time.DeviceTimeChangedProviderImpl
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AndroidModule {

    @Provides
    @AppVersion
    fun provideAppVersion(): String {
        return BuildConfig.VERSION_NAME
    }

    @Provides
    fun provideContentResolver(
        @ApplicationContext context: Context,
    ): ContentResolver {
        return context.contentResolver
    }

    @Provides
    @IODispatcher
    fun provideIODispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @MainDispatcher
    fun provideMainDispatcher(): CoroutineDispatcher = Dispatchers.Main

    @Module
    @InstallIn(SingletonComponent::class)
    interface Bindings {
        @Singleton
        @Binds
        fun bindPreferences(
            impl: PreferencesImpl,
        ): Preferences

        @Singleton
        @Binds
        fun provideLogger(
            impl: LoggerImpl,
        ): Logger

        @Singleton
        @Binds
        fun bindUriToActionConverter(
            impl: UriToActionConverterImpl,
        ): UriToActionConverter

        @Binds
        @Singleton
        fun bindDeviceTimeChangedProvider(
            impl: DeviceTimeChangedProviderImpl,
        ): DeviceTimeChangedProvider
    }
}

@Qualifier
annotation class MainDispatcher

@Qualifier
annotation class IODispatcher

@Qualifier
annotation class AppVersion
