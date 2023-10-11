package tech.relaycorp.letro.utils.di

import android.content.ContentResolver
import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import tech.relaycorp.letro.BuildConfig
import tech.relaycorp.letro.storage.Preferences
import tech.relaycorp.letro.storage.PreferencesImpl
import tech.relaycorp.letro.utils.Logger
import tech.relaycorp.letro.utils.LoggerImpl
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
    fun provideIODispatcher() = Dispatchers.IO

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
    }
}

@Qualifier
annotation class IODispatcher

@Qualifier
annotation class AppVersion
