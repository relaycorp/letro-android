package tech.relaycorp.letro.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import tech.relaycorp.letro.ui.utils.SnackbarStringsProvider
import tech.relaycorp.letro.ui.utils.SnackbarStringsProviderImpl

@Module
@InstallIn(ActivityComponent::class)
interface MainModule {

    @Binds
    fun bindSnackbarStringsProvider(
        impl: SnackbarStringsProviderImpl,
    ): SnackbarStringsProvider
}
