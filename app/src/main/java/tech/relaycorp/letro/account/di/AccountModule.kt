package tech.relaycorp.letro.account.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
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

@Module
@InstallIn(SingletonComponent::class)
object AccountModule {

    @Provides
    @AvatarSizeLimitBytes
    fun provideAvatarSizeLimitBytes(): Int {
        return 5 * 1024 * 1024 // 5 MB
    }

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
