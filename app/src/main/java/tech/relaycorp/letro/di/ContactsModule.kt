package tech.relaycorp.letro.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import tech.relaycorp.letro.contacts.storage.ContactsDao
import tech.relaycorp.letro.contacts.storage.ContactsRepository
import tech.relaycorp.letro.contacts.storage.ContactsRepositoryImpl
import tech.relaycorp.letro.pairing.parser.ContactPairingMatchParser
import tech.relaycorp.letro.pairing.parser.ContactPairingMatchParserImpl
import tech.relaycorp.letro.pairing.processor.ContactPairingMatchProcessor
import tech.relaycorp.letro.pairing.processor.ContactPairingMatchProcessorImpl
import tech.relaycorp.letro.storage.LetroDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ContactsModule {

    @Provides
    fun provideContactsDao(appDatabase: LetroDatabase): ContactsDao =
        appDatabase.contactsDao()

    @Module
    @InstallIn(SingletonComponent::class)
    interface Declarations {

        @Singleton
        @Binds
        fun bindContactsRepository(
            impl: ContactsRepositoryImpl,
        ): ContactsRepository

        @Binds
        fun bindContactPairingMatchParser(
            impl: ContactPairingMatchParserImpl,
        ): ContactPairingMatchParser

        @Binds
        @Singleton
        fun bindContactPairingMatchProcessor(
            impl: ContactPairingMatchProcessorImpl,
        ): ContactPairingMatchProcessor
    }
}
