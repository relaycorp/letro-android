package tech.relaycorp.letro.di

import android.content.ContentResolver
import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AndroidModule {

    @Provides
    fun provideContentResolver(
        @ApplicationContext context: Context,
    ): ContentResolver {
        return context.contentResolver
    }
}
