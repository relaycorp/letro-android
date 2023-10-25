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
import tech.relaycorp.letro.utils.crypto.MasterKeyProviderImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object EncryptionModule {

    @Provides
    @Singleton
    fun provideMasterKey(
        masterKeyProvider: MasterKeyProvider,
    ): MasterKey {
        return masterKeyProvider.masterKey
    }

    @Module
    @InstallIn(SingletonComponent::class)
    interface Bindings {
        @Binds
        @Singleton
        fun bindDatabasePassphrase(
            impl: DatabasePassphraseImpl,
        ): DatabasePassphrase

        @Binds
        @Singleton
        fun bindMasterKeyProvider(
            impl: MasterKeyProviderImpl,
        ): MasterKeyProvider
    }
}
