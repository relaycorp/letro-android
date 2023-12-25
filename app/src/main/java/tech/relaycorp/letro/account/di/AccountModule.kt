package tech.relaycorp.letro.account.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.newSingleThreadContext
import tech.relaycorp.letro.account.manage.avatar.AvatarFileConverter
import tech.relaycorp.letro.account.manage.avatar.AvatarRepository
import tech.relaycorp.letro.account.manage.avatar.AvatarRepositoryImpl
import tech.relaycorp.letro.account.storage.repository.AccountRepository
import tech.relaycorp.letro.account.storage.repository.AccountRepositoryImpl
import tech.relaycorp.letro.account.utils.AccountsSorter
import tech.relaycorp.letro.account.utils.AccountsSorterImpl
import tech.relaycorp.letro.conversation.attachments.filepicker.FileConverter
import javax.inject.Qualifier
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

@Module
@InstallIn(SingletonComponent::class)
object AccountModule {

    @Provides
    @AvatarSizeLimitBytes
    fun provideAvatarSizeLimitBytes(): Int {
        return 5 * 1024 * 1024 // 5 MB
    }

    @Provides
    @Singleton
    @ContactsNewAvatarNotifierThread
    @OptIn(DelicateCoroutinesApi::class, ExperimentalCoroutinesApi::class)
    fun provideContactsNewAvatarNotifierContext(): CoroutineContext = newSingleThreadContext("ContactsNewAvatarNotifierThread")

    @Module
    @InstallIn(SingletonComponent::class)
    interface Bindings {
        @Binds
        @Singleton
        fun bindAccountRepository(
            impl: AccountRepositoryImpl,
        ): AccountRepository

        @Binds
        fun bindAccountSorter(
            impl: AccountsSorterImpl,
        ): AccountsSorter

        @Binds
        @AvatarFileConverterAnnotation
        fun bindAvatarFileConverter(
            impl: AvatarFileConverter,
        ): FileConverter

        @Binds
        fun bindAvatarRepository(
            impl: AvatarRepositoryImpl,
        ): AvatarRepository
    }
}

@Qualifier
annotation class AvatarFileConverterAnnotation

@Qualifier
annotation class AvatarSizeLimitBytes

@Qualifier
annotation class ContactsNewAvatarNotifierThread
