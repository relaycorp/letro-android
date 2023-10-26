@file:OptIn(DelicateCoroutinesApi::class)

package tech.relaycorp.letro.main.di

import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.newSingleThreadContext
import tech.relaycorp.letro.R
import tech.relaycorp.letro.main.home.badge.UnreadBadgesManager
import tech.relaycorp.letro.main.home.badge.UnreadBadgesManagerImpl
import tech.relaycorp.letro.ui.utils.ConversationsStringsProvider
import tech.relaycorp.letro.ui.utils.ConversationsStringsProviderImpl
import tech.relaycorp.letro.ui.utils.SnackbarStringsProvider
import tech.relaycorp.letro.ui.utils.SnackbarStringsProviderImpl
import tech.relaycorp.letro.ui.utils.StringsProvider
import tech.relaycorp.letro.ui.utils.StringsProviderImpl
import javax.inject.Qualifier
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

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
    }
}

@Module
@InstallIn(ActivityRetainedComponent::class)
object MainActivityRetainedModule {

    @Provides
    @TermsAndConditionsLink
    fun provideTermsAndConditionsLink(
        @ApplicationContext context: Context,
    ): String {
        return context.getString(R.string.url_letro_terms_and_conditions)
    }
}

@Module
@InstallIn(SingletonComponent::class)
interface MainSingletonModule {
    @Binds
    @Singleton
    fun bindUnreadBadgesManager(
        impl: UnreadBadgesManagerImpl,
    ): UnreadBadgesManager
}

@Module
@InstallIn(ViewModelComponent::class)
@OptIn(ExperimentalCoroutinesApi::class)
object MainViewModelModule {

    @Provides
    @RootNavigationDebounceMs
    fun provideRootNavigationDebounceMs(): Long {
        return 300L
    }

    @Provides
    @MainViewModelActionProcessorThread
    fun provideMainViewModelActionProcessorThread(): CoroutineContext {
        return newSingleThreadContext("MainActionProcessorThread")
    }
}

@Qualifier
annotation class TermsAndConditionsLink

@Qualifier
annotation class RootNavigationDebounceMs

@Qualifier
annotation class MainViewModelActionProcessorThread
