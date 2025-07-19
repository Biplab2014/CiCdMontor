package com.app.cicdmonitor.domain.repository

import com.app.cicdmonitor.data.models.*
import kotlinx.coroutines.flow.Flow

interface BuildRepository {
    
    // Local database operations
    fun getBuildsByPipeline(pipelineId: String, limit: Int = 20): Flow<List<Build>>
    suspend fun getBuildById(id: String): Build?
    suspend fun getLatestBuildForPipeline(pipelineId: String): Build?
    fun getBuildsByStatus(statuses: List<BuildStatus>): Flow<List<Build>>
    suspend fun insertBuild(build: Build)
    suspend fun insertBuilds(builds: List<Build>)
    suspend fun updateBuild(build: Build)
    suspend fun deleteBuild(id: String)
    suspend fun deleteBuildsByPipeline(pipelineId: String)
    
    // Remote API operations
    suspend fun fetchBuildsFromRemote(pipelineId: String, provider: CiProvider): Result<List<Build>>
    suspend fun fetchBuildDetails(buildId: String, provider: CiProvider): Result<Build>
    suspend fun fetchBuildLogs(buildId: String, provider: CiProvider): Result<BuildLog>
    suspend fun retryBuild(buildId: String, provider: CiProvider): Result<Build>
    suspend fun cancelBuild(buildId: String, provider: CiProvider): Result<Unit>
    
    // Sync operations
    suspend fun syncBuildsForPipeline(pipelineId: String, provider: CiProvider): Result<Unit>
    suspend fun syncAllBuilds(): Result<Unit>
}
