package com.app.cicdmonitor.data.repository

import com.app.cicdmonitor.data.database.dao.PipelineDao
import com.app.cicdmonitor.data.models.*
import com.app.cicdmonitor.data.network.api.GitHubApiService
import com.app.cicdmonitor.data.network.api.GitLabApiService
import com.app.cicdmonitor.data.network.api.JenkinsApiService
import com.app.cicdmonitor.data.network.dto.*
import com.app.cicdmonitor.di.NetworkModule
import com.app.cicdmonitor.domain.repository.AuthRepository
import com.app.cicdmonitor.domain.repository.PipelineRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PipelineRepositoryImpl @Inject constructor(
    private val pipelineDao: PipelineDao,
    private val authRepository: AuthRepository,
    private val gitHubApiService: GitHubApiService,
    private val gitLabApiService: GitLabApiService,
    private val okHttpClient: OkHttpClient,
    private val json: Json
) : PipelineRepository {

    override fun getAllActivePipelines(): Flow<List<Pipeline>> {
        return pipelineDao.getAllActivePipelines()
    }

    override fun getPipelinesByProvider(provider: CiProvider): Flow<List<Pipeline>> {
        return pipelineDao.getPipelinesByProvider(provider)
    }

    override suspend fun getPipelineById(id: String): Pipeline? {
        return pipelineDao.getPipelineById(id)
    }

    override suspend fun getPipelineWithBuilds(id: String): PipelineWithBuilds? {
        return pipelineDao.getPipelineWithBuilds(id)
    }

    override suspend fun insertPipeline(pipeline: Pipeline) {
        pipelineDao.insertPipeline(pipeline)
    }

    override suspend fun insertPipelines(pipelines: List<Pipeline>) {
        pipelineDao.insertPipelines(pipelines)
    }

    override suspend fun updatePipeline(pipeline: Pipeline) {
        pipelineDao.updatePipeline(pipeline.copy(updatedAt = LocalDateTime.now().toString()))
    }

    override suspend fun deactivatePipeline(id: String) {
        pipelineDao.deactivatePipeline(id)
    }

    override suspend fun deletePipeline(id: String) {
        pipelineDao.deletePipeline(id)
    }

    override suspend fun fetchPipelinesFromRemote(provider: CiProvider): Result<List<Pipeline>> {
        return try {
            val token = authRepository.getAuthToken(provider)
            if (token == null) {
                return Result.failure(Exception("No authentication token found for $provider"))
            }

            when (provider) {
                CiProvider.GITHUB_ACTIONS -> fetchGitHubWorkflows(token.accessToken)
                CiProvider.GITLAB_CI -> fetchGitLabPipelines(token.accessToken)
                CiProvider.JENKINS -> fetchJenkinsJobs(token)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun fetchPipelineDetails(pipelineId: String, provider: CiProvider): Result<Pipeline> {
        return try {
            val token = authRepository.getAuthToken(provider)
            if (token == null) {
                return Result.failure(Exception("No authentication token found for $provider"))
            }

            when (provider) {
                CiProvider.GITHUB_ACTIONS -> fetchGitHubWorkflowDetails(pipelineId, token.accessToken)
                CiProvider.GITLAB_CI -> fetchGitLabPipelineDetails(pipelineId, token.accessToken)
                CiProvider.JENKINS -> fetchJenkinsJobDetails(pipelineId, token)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun triggerPipeline(pipelineId: String, provider: CiProvider, branch: String?): Result<Build> {
        return try {
            val token = authRepository.getAuthToken(provider)
            if (token == null) {
                return Result.failure(Exception("No authentication token found for $provider"))
            }

            when (provider) {
                CiProvider.GITHUB_ACTIONS -> triggerGitHubWorkflow(pipelineId, token.accessToken, branch ?: "main")
                CiProvider.GITLAB_CI -> triggerGitLabPipeline(pipelineId, token.accessToken, branch ?: "main")
                CiProvider.JENKINS -> triggerJenkinsJob(pipelineId, token)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun retryPipeline(pipelineId: String, provider: CiProvider): Result<Build> {
        return try {
            val token = authRepository.getAuthToken(provider)
            if (token == null) {
                return Result.failure(Exception("No authentication token found for $provider"))
            }

            when (provider) {
                CiProvider.GITHUB_ACTIONS -> retryGitHubWorkflow(pipelineId, token.accessToken)
                CiProvider.GITLAB_CI -> retryGitLabPipeline(pipelineId, token.accessToken)
                CiProvider.JENKINS -> retryJenkinsJob(pipelineId, token)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun cancelPipeline(pipelineId: String, provider: CiProvider): Result<Unit> {
        return try {
            val token = authRepository.getAuthToken(provider)
            if (token == null) {
                return Result.failure(Exception("No authentication token found for $provider"))
            }

            when (provider) {
                CiProvider.GITHUB_ACTIONS -> cancelGitHubWorkflow(pipelineId, token.accessToken)
                CiProvider.GITLAB_CI -> cancelGitLabPipeline(pipelineId, token.accessToken)
                CiProvider.JENKINS -> cancelJenkinsJob(pipelineId, token)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun syncPipelines(provider: CiProvider): Result<Unit> {
        return try {
            val remotePipelines = fetchPipelinesFromRemote(provider).getOrThrow()
            insertPipelines(remotePipelines)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun syncAllPipelines(): Result<Unit> {
        return try {
            val results = CiProvider.values().map { provider ->
                syncPipelines(provider)
            }
            
            val failures = results.filter { it.isFailure }
            if (failures.isNotEmpty()) {
                Result.failure(Exception("Some syncs failed: ${failures.size}/${results.size}"))
            } else {
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // GitHub implementation
    private suspend fun fetchGitHubWorkflows(accessToken: String): Result<List<Pipeline>> {
        return try {
            // For demo purposes, we'll fetch workflows from a sample repository
            // In a real app, users would configure which repositories to monitor
            val authHeader = GitHubApiService.createAuthHeader(accessToken)
            
            // This is a placeholder - in reality, you'd need to get user's repositories first
            val sampleOwner = "octocat"
            val sampleRepo = "Hello-World"
            
            val response = gitHubApiService.getWorkflows(sampleOwner, sampleRepo, authHeader)
            if (response.isSuccessful && response.body() != null) {
                val workflows = response.body()!!.workflows.map { workflow ->
                    mapGitHubWorkflowToPipeline(workflow, sampleOwner, sampleRepo)
                }
                Result.success(workflows)
            } else {
                Result.failure(Exception("Failed to fetch GitHub workflows: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun fetchGitHubWorkflowDetails(workflowId: String, accessToken: String): Result<Pipeline> {
        // TODO: Implement GitHub workflow details fetching
        return Result.failure(Exception("GitHub workflow details fetching not implemented"))
    }

    private suspend fun triggerGitHubWorkflow(workflowId: String, accessToken: String, branch: String): Result<Build> {
        // TODO: Implement GitHub workflow triggering
        return Result.failure(Exception("GitHub workflow triggering not implemented"))
    }

    private suspend fun retryGitHubWorkflow(runId: String, accessToken: String): Result<Build> {
        // TODO: Implement GitHub workflow retry
        return Result.failure(Exception("GitHub workflow retry not implemented"))
    }

    private suspend fun cancelGitHubWorkflow(runId: String, accessToken: String): Result<Unit> {
        // TODO: Implement GitHub workflow cancellation
        return Result.failure(Exception("GitHub workflow cancellation not implemented"))
    }

    // GitLab implementation
    private suspend fun fetchGitLabPipelines(accessToken: String): Result<List<Pipeline>> {
        return try {
            // For demo purposes, we'll use a sample project ID
            // In a real app, users would configure which projects to monitor
            val authHeader = GitLabApiService.createAuthHeader(accessToken)
            val sampleProjectId = "278964" // GitLab's sample project
            
            val response = gitLabApiService.getPipelines(sampleProjectId, authorization = authHeader)
            if (response.isSuccessful && response.body() != null) {
                val pipelines = response.body()!!.map { pipeline ->
                    mapGitLabPipelineToPipeline(pipeline, sampleProjectId)
                }
                Result.success(pipelines)
            } else {
                Result.failure(Exception("Failed to fetch GitLab pipelines: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun fetchGitLabPipelineDetails(pipelineId: String, accessToken: String): Result<Pipeline> {
        // TODO: Implement GitLab pipeline details fetching
        return Result.failure(Exception("GitLab pipeline details fetching not implemented"))
    }

    private suspend fun triggerGitLabPipeline(projectId: String, accessToken: String, branch: String): Result<Build> {
        // TODO: Implement GitLab pipeline triggering
        return Result.failure(Exception("GitLab pipeline triggering not implemented"))
    }

    private suspend fun retryGitLabPipeline(pipelineId: String, accessToken: String): Result<Build> {
        // TODO: Implement GitLab pipeline retry
        return Result.failure(Exception("GitLab pipeline retry not implemented"))
    }

    private suspend fun cancelGitLabPipeline(pipelineId: String, accessToken: String): Result<Unit> {
        // TODO: Implement GitLab pipeline cancellation
        return Result.failure(Exception("GitLab pipeline cancellation not implemented"))
    }

    // Jenkins implementation
    private suspend fun fetchJenkinsJobs(token: AuthToken): Result<List<Pipeline>> {
        return try {
            if (token.serverUrl == null || token.username == null) {
                return Result.failure(Exception("Jenkins server URL or username not configured"))
            }

            val jenkinsService = NetworkModule.createJenkinsApiService(token.serverUrl, okHttpClient, json)
            val authHeader = JenkinsApiService.createAuthHeader(token.username, token.accessToken)
            
            val response = jenkinsService.getJobs(authorization = authHeader)
            if (response.isSuccessful && response.body() != null) {
                val jobs = response.body()!!.jobs.map { job ->
                    mapJenkinsJobToPipeline(job, token.serverUrl)
                }
                Result.success(jobs)
            } else {
                Result.failure(Exception("Failed to fetch Jenkins jobs: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun fetchJenkinsJobDetails(jobName: String, token: AuthToken): Result<Pipeline> {
        // TODO: Implement Jenkins job details fetching
        return Result.failure(Exception("Jenkins job details fetching not implemented"))
    }

    private suspend fun triggerJenkinsJob(jobName: String, token: AuthToken): Result<Build> {
        // TODO: Implement Jenkins job triggering
        return Result.failure(Exception("Jenkins job triggering not implemented"))
    }

    private suspend fun retryJenkinsJob(jobName: String, token: AuthToken): Result<Build> {
        // TODO: Implement Jenkins job retry
        return Result.failure(Exception("Jenkins job retry not implemented"))
    }

    private suspend fun cancelJenkinsJob(buildId: String, token: AuthToken): Result<Unit> {
        // TODO: Implement Jenkins job cancellation
        return Result.failure(Exception("Jenkins job cancellation not implemented"))
    }

    // Mapping functions
    private fun mapGitHubWorkflowToPipeline(workflow: GitHubWorkflow, owner: String, repo: String): Pipeline {
        return Pipeline(
            id = "github_${workflow.id}",
            name = workflow.name,
            provider = CiProvider.GITHUB_ACTIONS,
            repositoryUrl = "https://github.com/$owner/$repo",
            branch = "main", // Default branch
            status = if (workflow.state == "active") PipelineStatus.ACTIVE else PipelineStatus.INACTIVE,
            lastRunId = null,
            lastRunStatus = null,
            lastRunDuration = null,
            lastRunTimestamp = workflow.updatedAt,
            lastCommitMessage = null,
            lastCommitAuthor = null,
            isActive = workflow.state == "active",
            createdAt = workflow.createdAt,
            updatedAt = workflow.updatedAt
        )
    }

    private fun mapGitLabPipelineToPipeline(pipeline: GitLabPipeline, projectId: String): Pipeline {
        return Pipeline(
            id = "gitlab_${pipeline.id}",
            name = "Pipeline #${pipeline.iid}",
            provider = CiProvider.GITLAB_CI,
            repositoryUrl = pipeline.webUrl,
            branch = pipeline.ref,
            status = PipelineStatus.ACTIVE,
            lastRunId = pipeline.id.toString(),
            lastRunStatus = mapGitLabStatusToBuildStatus(pipeline.status),
            lastRunDuration = pipeline.duration,
            lastRunTimestamp = pipeline.updatedAt,
            lastCommitMessage = null,
            lastCommitAuthor = pipeline.user.name,
            isActive = true,
            createdAt = pipeline.createdAt,
            updatedAt = pipeline.updatedAt
        )
    }

    private fun mapJenkinsJobToPipeline(job: JenkinsJob, serverUrl: String): Pipeline {
        return Pipeline(
            id = "jenkins_${job.name}",
            name = job.name,
            provider = CiProvider.JENKINS,
            repositoryUrl = job.url,
            branch = "main", // Jenkins doesn't always have branch info
            status = if (job.buildable == true) PipelineStatus.ACTIVE else PipelineStatus.INACTIVE,
            lastRunId = job.lastBuild?.number?.toString(),
            lastRunStatus = mapJenkinsColorToBuildStatus(job.color),
            lastRunDuration = null,
            lastRunTimestamp = null,
            lastCommitMessage = null,
            lastCommitAuthor = null,
            isActive = job.buildable == true,
            createdAt = LocalDateTime.now().toString(),
            updatedAt = LocalDateTime.now().toString()
        )
    }

    private fun mapGitLabStatusToBuildStatus(status: String): BuildStatus {
        return when (status.lowercase()) {
            "success" -> BuildStatus.SUCCESS
            "failed" -> BuildStatus.FAILURE
            "running" -> BuildStatus.RUNNING
            "pending" -> BuildStatus.PENDING
            "canceled", "cancelled" -> BuildStatus.CANCELLED
            "skipped" -> BuildStatus.SKIPPED
            else -> BuildStatus.UNKNOWN
        }
    }

    private fun mapJenkinsColorToBuildStatus(color: String): BuildStatus {
        return when (color.lowercase()) {
            "blue" -> BuildStatus.SUCCESS
            "red" -> BuildStatus.FAILURE
            "yellow" -> BuildStatus.SUCCESS // Unstable but successful
            "grey" -> BuildStatus.PENDING
            "disabled" -> BuildStatus.CANCELLED
            "aborted" -> BuildStatus.CANCELLED
            else -> BuildStatus.UNKNOWN
        }
    }
}
