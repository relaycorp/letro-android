package tech.relaycorp.letro.utils.di

import androidx.security.crypto.MasterKey
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import tech.relaycorp.letro.storage.encryption.DatabasePassphrase
import tech.relaycorp.letro.storage.encryption.DatabasePassphraseImpl
import tech.relaycorp.letro.utils.crypto.MasterKeyProvider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object EncryptionModule {

    @Provides
    @Singleton
    fun provideMasterKey(): MasterKey {
        return MasterKeyProvider.masterKey
    }

    @Module
    @InstallIn(SingletonComponent::class)
    interface Bindings {
        @Binds
        @Singleton
        fun bindDatabasePassphrase(
            impl: DatabasePassphraseImpl,
        ): DatabasePassphrase
    }
}
