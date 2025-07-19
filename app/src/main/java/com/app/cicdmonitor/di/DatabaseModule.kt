package com.app.cicdmonitor.di

import android.content.Context
import androidx.room.Room
import com.app.cicdmonitor.data.database.CiCdDatabase
import com.app.cicdmonitor.data.database.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): CiCdDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            CiCdDatabase::class.java,
            CiCdDatabase.DATABASE_NAME
        ).build()
    }
    
    @Provides
    fun providePipelineDao(database: CiCdDatabase): PipelineDao {
        return database.pipelineDao()
    }
    
    @Provides
    fun provideBuildDao(database: CiCdDatabase): BuildDao {
        return database.buildDao()
    }
    
    @Provides
    fun provideAuthTokenDao(database: CiCdDatabase): AuthTokenDao {
        return database.authTokenDao()
    }
    
    @Provides
    fun provideUserDao(database: CiCdDatabase): UserDao {
        return database.userDao()
    }
}
