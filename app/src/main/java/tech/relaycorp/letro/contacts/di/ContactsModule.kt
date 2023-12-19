package tech.relaycorp.letro.contacts.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import tech.relaycorp.letro.awala.message.AwalaIncomingMessageContent
import tech.relaycorp.letro.awala.processor.AwalaMessageProcessor
import tech.relaycorp.letro.contacts.pairing.notification.ContactPairingNotificationManager
import tech.relaycorp.letro.contacts.pairing.notification.ContactPairingNotificationManagerImpl
import tech.relaycorp.letro.contacts.pairing.server.auth.ContactPairingAuthorizationParser
import tech.relaycorp.letro.contacts.pairing.server.auth.ContactPairingAuthorizationParserImpl
import tech.relaycorp.letro.contacts.pairing.server.auth.ContactPairingAuthorizationProcessor
import tech.relaycorp.letro.contacts.pairing.server.match.ContactPairingMatchParser
import tech.relaycorp.letro.contacts.pairing.server.match.ContactPairingMatchParserImpl
import tech.relaycorp.letro.contacts.pairing.server.match.ContactPairingMatchProcessor
import tech.relaycorp.letro.contacts.pairing.server.photo.ContactPhotoUpdatedParser
import tech.relaycorp.letro.contacts.pairing.server.photo.ContactPhotoUpdatedParserImpl
import tech.relaycorp.letro.contacts.pairing.server.photo.ContactPhotoUpdatedProcessor
import tech.relaycorp.letro.contacts.storage.dao.ContactsDao
import tech.relaycorp.letro.contacts.storage.repository.ContactsRepository
import tech.relaycorp.letro.contacts.storage.repository.ContactsRepositoryImpl
import tech.relaycorp.letro.contacts.suggest.ContactSuggestsManager
import tech.relaycorp.letro.contacts.suggest.ContactSuggestsManagerImpl
import tech.relaycorp.letro.contacts.suggest.shortcut.AndroidShortcutContactsSuggestManager
import tech.relaycorp.letro.contacts.suggest.shortcut.AndroidShortcutContactsSuggestManagerImpl
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
            impl: ContactPairingMatchProcessor,
        ): AwalaMessageProcessor<AwalaIncomingMessageContent.ContactPairingMatch>

        @Binds
        @Singleton
        fun bindContactPhotoUpdatedProcessor(
            impl: ContactPhotoUpdatedProcessor,
        ): AwalaMessageProcessor<AwalaIncomingMessageContent.ContactPhotoUpdated>

        @Binds
        fun bindContactPhotoUpdatedParser(
            impl: ContactPhotoUpdatedParserImpl,
        ): ContactPhotoUpdatedParser

        @Binds
        fun bindContactPairingAuthParser(
            impl: ContactPairingAuthorizationParserImpl,
        ): ContactPairingAuthorizationParser

        @Binds
        @Singleton
        fun bindContactPairingAuthProcessor(
            impl: ContactPairingAuthorizationProcessor,
        ): AwalaMessageProcessor<AwalaIncomingMessageContent.ContactPairingAuthorization>

        @Binds
        fun bindContactPairingSuccessNotificationManager(
            impl: ContactPairingNotificationManagerImpl,
        ): ContactPairingNotificationManager

        @Binds
        fun bindContactSuggestsManager(
            impl: ContactSuggestsManagerImpl,
        ): ContactSuggestsManager

        @Binds
        @Singleton
        fun shortcutContactsSuggestManager(
            impl: AndroidShortcutContactsSuggestManagerImpl,
        ): AndroidShortcutContactsSuggestManager
    }
}
