package com.app.cicdmonitor.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.app.cicdmonitor.data.database.converters.Converters
import com.app.cicdmonitor.data.database.dao.*
import com.app.cicdmonitor.data.models.*

@Database(
    entities = [
        Pipeline::class,
        Build::class,
        AuthToken::class,
        User::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class CiCdDatabase : RoomDatabase() {
    
    abstract fun pipelineDao(): PipelineDao
    abstract fun buildDao(): BuildDao
    abstract fun authTokenDao(): AuthTokenDao
    abstract fun userDao(): UserDao
    
    companion object {
        const val DATABASE_NAME = "cicd_monitor_database"
        
        @Volatile
        private var INSTANCE: CiCdDatabase? = null
        
        fun getDatabase(context: Context): CiCdDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CiCdDatabase::class.java,
                    DATABASE_NAME
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
