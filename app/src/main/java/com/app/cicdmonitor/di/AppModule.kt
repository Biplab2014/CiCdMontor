package com.app.cicdmonitor.di

import com.app.cicdmonitor.data.repository.*
import com.app.cicdmonitor.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindPipelineRepository(
        pipelineRepositoryImpl: PipelineRepositoryImpl
    ): PipelineRepository

    @Binds
    @Singleton
    abstract fun bindBuildRepository(
        buildRepositoryImpl: BuildRepositoryImpl
    ): BuildRepository

    @Binds
    @Singleton
    abstract fun bindUserPreferencesRepository(
        userPreferencesRepositoryImpl: UserPreferencesRepositoryImpl
    ): UserPreferencesRepository
}
