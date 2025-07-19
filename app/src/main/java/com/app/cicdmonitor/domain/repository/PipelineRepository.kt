package com.app.cicdmonitor.domain.repository

import com.app.cicdmonitor.data.models.*
import kotlinx.coroutines.flow.Flow

interface PipelineRepository {
    
    // Local database operations
    fun getAllActivePipelines(): Flow<List<Pipeline>>
    fun getPipelinesByProvider(provider: CiProvider): Flow<List<Pipeline>>
    suspend fun getPipelineById(id: String): Pipeline?
    suspend fun getPipelineWithBuilds(id: String): PipelineWithBuilds?
    suspend fun insertPipeline(pipeline: Pipeline)
    suspend fun insertPipelines(pipelines: List<Pipeline>)
    suspend fun updatePipeline(pipeline: Pipeline)
    suspend fun deactivatePipeline(id: String)
    suspend fun deletePipeline(id: String)
    
    // Remote API operations
    suspend fun fetchPipelinesFromRemote(provider: CiProvider): Result<List<Pipeline>>
    suspend fun fetchPipelineDetails(pipelineId: String, provider: CiProvider): Result<Pipeline>
    suspend fun triggerPipeline(pipelineId: String, provider: CiProvider, branch: String? = null): Result<Build>
    suspend fun retryPipeline(pipelineId: String, provider: CiProvider): Result<Build>
    suspend fun cancelPipeline(pipelineId: String, provider: CiProvider): Result<Unit>
    
    // Sync operations
    suspend fun syncPipelines(provider: CiProvider): Result<Unit>
    suspend fun syncAllPipelines(): Result<Unit>
}
