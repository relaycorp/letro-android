package tech.relaycorp.letro.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import tech.relaycorp.letro.storage.Preferences
import tech.relaycorp.letro.storage.PreferencesImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface PreferencesModule {

    @Singleton
    @Binds
    fun bindPreferences(
        impl: PreferencesImpl
    ): Preferences

}