package tech.relaycorp.letro.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.flow.MutableStateFlow
import tech.relaycorp.letro.ui.utils.AwalaInitializationStringsProvider
import tech.relaycorp.letro.ui.utils.AwalaInitializationStringsProviderImpl
import tech.relaycorp.letro.ui.utils.ConversationsStringsProvider
import tech.relaycorp.letro.ui.utils.ConversationsStringsProviderImpl
import tech.relaycorp.letro.ui.utils.SnackbarStringsProvider
import tech.relaycorp.letro.ui.utils.SnackbarStringsProviderImpl
import tech.relaycorp.letro.ui.utils.StringsProvider
import tech.relaycorp.letro.ui.utils.StringsProviderImpl
import javax.inject.Qualifier

@Module
@InstallIn(ActivityComponent::class)
object MainModule {

    @Module
    @InstallIn(ActivityComponent::class)
    interface Bindings {
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

    @Qualifier
    annotation class AwalaInitializationStringsIndexPointer
}

@Module
@InstallIn(ActivityRetainedComponent::class)
object MainActivityRetainedModule {

    @Provides
    @ActivityRetainedScoped
    @MainModule.AwalaInitializationStringsIndexPointer
    fun bindAwalaInitializationStringsIndexPointer(): MutableStateFlow<Int> {
        return MutableStateFlow(0)
    }
}
