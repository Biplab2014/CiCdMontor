package com.app.cicdmonitor.data.database.dao

import androidx.room.*
import com.app.cicdmonitor.data.models.Build
import com.app.cicdmonitor.data.models.BuildStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface BuildDao {
    
    @Query("SELECT * FROM builds WHERE pipelineId = :pipelineId ORDER BY createdAt DESC LIMIT :limit")
    fun getBuildsByPipeline(pipelineId: String, limit: Int = 20): Flow<List<Build>>
    
    @Query("SELECT * FROM builds WHERE id = :id")
    suspend fun getBuildById(id: String): Build?
    
    @Query("SELECT * FROM builds WHERE pipelineId = :pipelineId ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLatestBuildForPipeline(pipelineId: String): Build?
    
    @Query("SELECT * FROM builds WHERE status IN (:statuses) ORDER BY createdAt DESC")
    fun getBuildsByStatus(statuses: List<BuildStatus>): Flow<List<Build>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBuild(build: Build)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBuilds(builds: List<Build>)
    
    @Update
    suspend fun updateBuild(build: Build)
    
    @Query("DELETE FROM builds WHERE id = :id")
    suspend fun deleteBuild(id: String)
    
    @Query("DELETE FROM builds WHERE pipelineId = :pipelineId")
    suspend fun deleteBuildsByPipeline(pipelineId: String)
    
    @Query("DELETE FROM builds WHERE createdAt < :cutoffDate")
    suspend fun deleteOldBuilds(cutoffDate: String)
    
    @Query("SELECT COUNT(*) FROM builds WHERE pipelineId = :pipelineId")
    suspend fun getBuildCountForPipeline(pipelineId: String): Int
}
