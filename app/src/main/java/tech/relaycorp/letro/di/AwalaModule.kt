package tech.relaycorp.letro.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import tech.relaycorp.letro.awala.AwalaManager
import tech.relaycorp.letro.awala.AwalaManagerImpl
import tech.relaycorp.letro.awala.AwalaRepository
import tech.relaycorp.letro.awala.AwalaRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface AwalaModule {

    @Singleton
    @Binds
    fun bindAwalaManager(
        impl: AwalaManagerImpl
    ): AwalaManager

    @Singleton
    @Binds
    fun bindAwalaRepository(
        impl: AwalaRepositoryImpl
    ): AwalaRepository
}