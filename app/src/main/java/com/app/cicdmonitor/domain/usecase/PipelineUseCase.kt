package com.app.cicdmonitor.domain.usecase

import com.app.cicdmonitor.data.models.*
import com.app.cicdmonitor.domain.repository.PipelineRepository
import com.app.cicdmonitor.domain.repository.BuildRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PipelineUseCase @Inject constructor(
    private val pipelineRepository: PipelineRepository,
    private val buildRepository: BuildRepository
) {
    
    fun getAllActivePipelines(): Flow<List<Pipeline>> {
        return pipelineRepository.getAllActivePipelines()
    }
    
    fun getPipelinesByProvider(provider: CiProvider): Flow<List<Pipeline>> {
        return pipelineRepository.getPipelinesByProvider(provider)
    }
    
    suspend fun getPipelineById(id: String): Pipeline? {
        return pipelineRepository.getPipelineById(id)
    }
    
    suspend fun getPipelineWithBuilds(id: String): PipelineWithBuilds? {
        return pipelineRepository.getPipelineWithBuilds(id)
    }
    
    fun getPipelineWithRecentBuilds(pipelineId: String): Flow<PipelineWithBuilds?> {
        return combine(
            pipelineRepository.getAllActivePipelines(),
            buildRepository.getBuildsByPipeline(pipelineId, 10)
        ) { pipelines, builds ->
            val pipeline = pipelines.find { it.id == pipelineId }
            if (pipeline != null) {
                PipelineWithBuilds(pipeline, builds)
            } else null
        }
    }
    
    suspend fun syncPipelines(provider: CiProvider): Result<Unit> {
        return pipelineRepository.syncPipelines(provider)
    }
    
    suspend fun syncAllPipelines(): Result<Unit> {
        return pipelineRepository.syncAllPipelines()
    }
    
    suspend fun triggerPipeline(
        pipelineId: String,
        provider: CiProvider,
        branch: String? = null
    ): Result<Build> {
        return pipelineRepository.triggerPipeline(pipelineId, provider, branch)
    }
    
    suspend fun retryPipeline(pipelineId: String, provider: CiProvider): Result<Build> {
        return pipelineRepository.retryPipeline(pipelineId, provider)
    }
    
    suspend fun cancelPipeline(pipelineId: String, provider: CiProvider): Result<Unit> {
        return pipelineRepository.cancelPipeline(pipelineId, provider)
    }
    
    suspend fun addPipeline(pipeline: Pipeline): Result<Unit> {
        return try {
            pipelineRepository.insertPipeline(pipeline)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updatePipeline(pipeline: Pipeline): Result<Unit> {
        return try {
            pipelineRepository.updatePipeline(pipeline)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun removePipeline(pipelineId: String): Result<Unit> {
        return try {
            pipelineRepository.deactivatePipeline(pipelineId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deletePipeline(pipelineId: String): Result<Unit> {
        return try {
            pipelineRepository.deletePipeline(pipelineId)
            buildRepository.deleteBuildsByPipeline(pipelineId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun getRunningPipelines(): Flow<List<Pipeline>> {
        return combine(
            pipelineRepository.getAllActivePipelines(),
            buildRepository.getBuildsByStatus(listOf(BuildStatus.RUNNING, BuildStatus.PENDING))
        ) { pipelines, runningBuilds ->
            val runningPipelineIds = runningBuilds.map { it.pipelineId }.toSet()
            pipelines.filter { it.id in runningPipelineIds }
        }
    }
    
    fun getFailedPipelines(): Flow<List<Pipeline>> {
        return pipelineRepository.getAllActivePipelines().combine(
            buildRepository.getBuildsByStatus(listOf(BuildStatus.FAILURE))
        ) { pipelines, failedBuilds ->
            val failedPipelineIds = failedBuilds.map { it.pipelineId }.toSet()
            pipelines.filter { it.lastRunStatus == BuildStatus.FAILURE }
        }
    }
    
    suspend fun getPipelineStatistics(): PipelineStatistics {
        val allPipelines = pipelineRepository.getAllActivePipelines()
        // This is a simplified implementation - in reality you'd collect the flow
        return PipelineStatistics(
            totalPipelines = 0,
            activePipelines = 0,
            successfulPipelines = 0,
            failedPipelines = 0,
            runningPipelines = 0
        )
    }
}

data class PipelineStatistics(
    val totalPipelines: Int,
    val activePipelines: Int,
    val successfulPipelines: Int,
    val failedPipelines: Int,
    val runningPipelines: Int
)
