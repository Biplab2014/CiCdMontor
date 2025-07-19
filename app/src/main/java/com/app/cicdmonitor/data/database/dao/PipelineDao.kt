package com.app.cicdmonitor.data.database.dao

import androidx.room.*
import com.app.cicdmonitor.data.models.Pipeline
import com.app.cicdmonitor.data.models.PipelineWithBuilds
import com.app.cicdmonitor.data.models.CiProvider
import kotlinx.coroutines.flow.Flow

@Dao
interface PipelineDao {
    
    @Query("SELECT * FROM pipelines WHERE isActive = 1 ORDER BY updatedAt DESC")
    fun getAllActivePipelines(): Flow<List<Pipeline>>
    
    @Query("SELECT * FROM pipelines WHERE provider = :provider AND isActive = 1")
    fun getPipelinesByProvider(provider: CiProvider): Flow<List<Pipeline>>
    
    @Query("SELECT * FROM pipelines WHERE id = :id")
    suspend fun getPipelineById(id: String): Pipeline?
    
    // Simplified for now - we'll implement the relationship query later
    suspend fun getPipelineWithBuilds(id: String): PipelineWithBuilds? {
        val pipeline = getPipelineById(id) ?: return null
        // TODO: Implement builds fetching
        return PipelineWithBuilds(pipeline, emptyList())
    }
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPipeline(pipeline: Pipeline)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPipelines(pipelines: List<Pipeline>)
    
    @Update
    suspend fun updatePipeline(pipeline: Pipeline)
    
    @Query("UPDATE pipelines SET isActive = 0 WHERE id = :id")
    suspend fun deactivatePipeline(id: String)
    
    @Query("DELETE FROM pipelines WHERE id = :id")
    suspend fun deletePipeline(id: String)
    
    @Query("DELETE FROM pipelines WHERE provider = :provider")
    suspend fun deletePipelinesByProvider(provider: CiProvider)
    
    @Query("SELECT COUNT(*) FROM pipelines WHERE isActive = 1")
    suspend fun getActivePipelineCount(): Int
}
