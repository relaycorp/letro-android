package tech.relaycorp.letro.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import tech.relaycorp.letro.ui.utils.AwalaInitializationStringsProvider
import tech.relaycorp.letro.ui.utils.AwalaInitializationStringsProviderImpl
import tech.relaycorp.letro.ui.utils.ConversationsStringsProvider
import tech.relaycorp.letro.ui.utils.ConversationsStringsProviderImpl
import tech.relaycorp.letro.ui.utils.SnackbarStringsProvider
import tech.relaycorp.letro.ui.utils.SnackbarStringsProviderImpl
import tech.relaycorp.letro.ui.utils.StringsProvider
import tech.relaycorp.letro.ui.utils.StringsProviderImpl

@Module
@InstallIn(ActivityComponent::class)
interface MainModule {

    @Binds
    fun bindStringsProvider(
        impl: StringsProviderImpl,
    ): StringsProvider

    @Binds
    fun bindSnackbarStringsProvider(
        impl: SnackbarStringsProviderImpl,
    ): SnackbarStringsProvider

    @Binds
    fun bindConversationsStringsProvider(
        impl: ConversationsStringsProviderImpl,
    ): ConversationsStringsProvider

    @Binds
    fun bindAwalaInitializationStringsProvider(
        impl: AwalaInitializationStringsProviderImpl,
    ): AwalaInitializationStringsProvider
}
