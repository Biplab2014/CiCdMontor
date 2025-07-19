package com.app.cicdmonitor.data.repository

import com.app.cicdmonitor.data.database.dao.BuildDao
import com.app.cicdmonitor.data.models.*
import com.app.cicdmonitor.data.network.api.GitHubApiService
import com.app.cicdmonitor.data.network.api.GitLabApiService
import com.app.cicdmonitor.data.network.api.JenkinsApiService
import com.app.cicdmonitor.di.NetworkModule
import com.app.cicdmonitor.domain.repository.AuthRepository
import com.app.cicdmonitor.domain.repository.BuildRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BuildRepositoryImpl @Inject constructor(
    private val buildDao: BuildDao,
    private val authRepository: AuthRepository,
    private val gitHubApiService: GitHubApiService,
    private val gitLabApiService: GitLabApiService,
    private val okHttpClient: OkHttpClient,
    private val json: Json
) : BuildRepository {

    override fun getBuildsByPipeline(pipelineId: String, limit: Int): Flow<List<Build>> {
        return buildDao.getBuildsByPipeline(pipelineId, limit)
    }

    override suspend fun getBuildById(id: String): Build? {
        return buildDao.getBuildById(id)
    }

    override suspend fun getLatestBuildForPipeline(pipelineId: String): Build? {
        return buildDao.getLatestBuildForPipeline(pipelineId)
    }

    override fun getBuildsByStatus(statuses: List<BuildStatus>): Flow<List<Build>> {
        return buildDao.getBuildsByStatus(statuses)
    }

    override suspend fun insertBuild(build: Build) {
        buildDao.insertBuild(build)
    }

    override suspend fun insertBuilds(builds: List<Build>) {
        buildDao.insertBuilds(builds)
    }

    override suspend fun updateBuild(build: Build) {
        buildDao.updateBuild(build.copy(updatedAt = LocalDateTime.now().toString()))
    }

    override suspend fun deleteBuild(id: String) {
        buildDao.deleteBuild(id)
    }

    override suspend fun deleteBuildsByPipeline(pipelineId: String) {
        buildDao.deleteBuildsByPipeline(pipelineId)
    }

    override suspend fun fetchBuildsFromRemote(pipelineId: String, provider: CiProvider): Result<List<Build>> {
        return try {
            val token = authRepository.getAuthToken(provider)
            if (token == null) {
                return Result.failure(Exception("No authentication token found for $provider"))
            }

            when (provider) {
                CiProvider.GITHUB_ACTIONS -> fetchGitHubWorkflowRuns(pipelineId, token.accessToken)
                CiProvider.GITLAB_CI -> fetchGitLabPipelineJobs(pipelineId, token.accessToken)
                CiProvider.JENKINS -> fetchJenkinsBuilds(pipelineId, token)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun fetchBuildDetails(buildId: String, provider: CiProvider): Result<Build> {
        return try {
            val token = authRepository.getAuthToken(provider)
            if (token == null) {
                return Result.failure(Exception("No authentication token found for $provider"))
            }

            when (provider) {
                CiProvider.GITHUB_ACTIONS -> fetchGitHubWorkflowRunDetails(buildId, token.accessToken)
                CiProvider.GITLAB_CI -> fetchGitLabJobDetails(buildId, token.accessToken)
                CiProvider.JENKINS -> fetchJenkinsBuildDetails(buildId, token)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun fetchBuildLogs(buildId: String, provider: CiProvider): Result<BuildLog> {
        return try {
            val token = authRepository.getAuthToken(provider)
            if (token == null) {
                return Result.failure(Exception("No authentication token found for $provider"))
            }

            when (provider) {
                CiProvider.GITHUB_ACTIONS -> fetchGitHubWorkflowLogs(buildId, token.accessToken)
                CiProvider.GITLAB_CI -> fetchGitLabJobLogs(buildId, token.accessToken)
                CiProvider.JENKINS -> fetchJenkinsBuildLogs(buildId, token)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun retryBuild(buildId: String, provider: CiProvider): Result<Build> {
        return try {
            val token = authRepository.getAuthToken(provider)
            if (token == null) {
                return Result.failure(Exception("No authentication token found for $provider"))
            }

            when (provider) {
                CiProvider.GITHUB_ACTIONS -> retryGitHubWorkflowRun(buildId, token.accessToken)
                CiProvider.GITLAB_CI -> retryGitLabJob(buildId, token.accessToken)
                CiProvider.JENKINS -> retryJenkinsBuild(buildId, token)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun cancelBuild(buildId: String, provider: CiProvider): Result<Unit> {
        return try {
            val token = authRepository.getAuthToken(provider)
            if (token == null) {
                return Result.failure(Exception("No authentication token found for $provider"))
            }

            when (provider) {
                CiProvider.GITHUB_ACTIONS -> cancelGitHubWorkflowRun(buildId, token.accessToken)
                CiProvider.GITLAB_CI -> cancelGitLabJob(buildId, token.accessToken)
                CiProvider.JENKINS -> cancelJenkinsBuild(buildId, token)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun syncBuildsForPipeline(pipelineId: String, provider: CiProvider): Result<Unit> {
        return try {
            val remoteBuilds = fetchBuildsFromRemote(pipelineId, provider).getOrThrow()
            insertBuilds(remoteBuilds)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun syncAllBuilds(): Result<Unit> {
        return try {
            // This would typically sync builds for all active pipelines
            // For now, we'll return success as a placeholder
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // GitHub implementation
    private suspend fun fetchGitHubWorkflowRuns(workflowId: String, accessToken: String): Result<List<Build>> {
        return try {
            // Extract owner and repo from workflow ID (this is a simplified approach)
            val authHeader = GitHubApiService.createAuthHeader(accessToken)
            val owner = "octocat" // Placeholder
            val repo = "Hello-World" // Placeholder
            
            val response = gitHubApiService.getWorkflowRuns(owner, repo, authorization = authHeader)
            if (response.isSuccessful && response.body() != null) {
                val builds = response.body()!!.workflowRuns.map { run ->
                    mapGitHubWorkflowRunToBuild(run, workflowId)
                }
                Result.success(builds)
            } else {
                Result.failure(Exception("Failed to fetch GitHub workflow runs: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun fetchGitHubWorkflowRunDetails(runId: String, accessToken: String): Result<Build> {
        // TODO: Implement GitHub workflow run details fetching
        return Result.failure(Exception("GitHub workflow run details fetching not implemented"))
    }

    private suspend fun fetchGitHubWorkflowLogs(runId: String, accessToken: String): Result<BuildLog> {
        // TODO: Implement GitHub workflow logs fetching
        return Result.failure(Exception("GitHub workflow logs fetching not implemented"))
    }

    private suspend fun retryGitHubWorkflowRun(runId: String, accessToken: String): Result<Build> {
        // TODO: Implement GitHub workflow run retry
        return Result.failure(Exception("GitHub workflow run retry not implemented"))
    }

    private suspend fun cancelGitHubWorkflowRun(runId: String, accessToken: String): Result<Unit> {
        // TODO: Implement GitHub workflow run cancellation
        return Result.failure(Exception("GitHub workflow run cancellation not implemented"))
    }

    // GitLab implementation
    private suspend fun fetchGitLabPipelineJobs(pipelineId: String, accessToken: String): Result<List<Build>> {
        return try {
            val authHeader = GitLabApiService.createAuthHeader(accessToken)
            val projectId = "278964" // Placeholder
            
            val response = gitLabApiService.getPipelineJobs(projectId, pipelineId.toLong(), authorization = authHeader)
            if (response.isSuccessful && response.body() != null) {
                val builds = response.body()!!.map { job ->
                    mapGitLabJobToBuild(job, pipelineId)
                }
                Result.success(builds)
            } else {
                Result.failure(Exception("Failed to fetch GitLab pipeline jobs: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun fetchGitLabJobDetails(jobId: String, accessToken: String): Result<Build> {
        // TODO: Implement GitLab job details fetching
        return Result.failure(Exception("GitLab job details fetching not implemented"))
    }

    private suspend fun fetchGitLabJobLogs(jobId: String, accessToken: String): Result<BuildLog> {
        // TODO: Implement GitLab job logs fetching
        return Result.failure(Exception("GitLab job logs fetching not implemented"))
    }

    private suspend fun retryGitLabJob(jobId: String, accessToken: String): Result<Build> {
        // TODO: Implement GitLab job retry
        return Result.failure(Exception("GitLab job retry not implemented"))
    }

    private suspend fun cancelGitLabJob(jobId: String, accessToken: String): Result<Unit> {
        // TODO: Implement GitLab job cancellation
        return Result.failure(Exception("GitLab job cancellation not implemented"))
    }

    // Jenkins implementation
    private suspend fun fetchJenkinsBuilds(jobName: String, token: AuthToken): Result<List<Build>> {
        return try {
            if (token.serverUrl == null || token.username == null) {
                return Result.failure(Exception("Jenkins server URL or username not configured"))
            }

            val jenkinsService = NetworkModule.createJenkinsApiService(token.serverUrl, okHttpClient, json)
            val authHeader = JenkinsApiService.createAuthHeader(token.username, token.accessToken)
            
            val response = jenkinsService.getJob(jobName, authorization = authHeader)
            if (response.isSuccessful && response.body() != null) {
                val job = response.body()!!
                val builds = job.builds?.map { buildRef ->
                    mapJenkinsBuildRefToBuild(buildRef, jobName)
                } ?: emptyList()
                Result.success(builds)
            } else {
                Result.failure(Exception("Failed to fetch Jenkins builds: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun fetchJenkinsBuildDetails(buildId: String, token: AuthToken): Result<Build> {
        // TODO: Implement Jenkins build details fetching
        return Result.failure(Exception("Jenkins build details fetching not implemented"))
    }

    private suspend fun fetchJenkinsBuildLogs(buildId: String, token: AuthToken): Result<BuildLog> {
        // TODO: Implement Jenkins build logs fetching
        return Result.failure(Exception("Jenkins build logs fetching not implemented"))
    }

    private suspend fun retryJenkinsBuild(buildId: String, token: AuthToken): Result<Build> {
        // TODO: Implement Jenkins build retry
        return Result.failure(Exception("Jenkins build retry not implemented"))
    }

    private suspend fun cancelJenkinsBuild(buildId: String, token: AuthToken): Result<Unit> {
        // TODO: Implement Jenkins build cancellation
        return Result.failure(Exception("Jenkins build cancellation not implemented"))
    }

    // Mapping functions
    private fun mapGitHubWorkflowRunToBuild(run: com.app.cicdmonitor.data.network.dto.GitHubWorkflowRun, pipelineId: String): Build {
        return Build(
            id = "github_${run.id}",
            pipelineId = pipelineId,
            buildNumber = run.runAttempt.toString(),
            status = mapGitHubStatusToBuildStatus(run.status, run.conclusion),
            branch = run.headCommit.id.take(8), // Use commit SHA as branch placeholder
            commitSha = run.headCommit.id,
            commitMessage = run.headCommit.message,
            commitAuthor = run.headCommit.author.name,
            startedAt = run.runStartedAt,
            finishedAt = run.updatedAt,
            duration = null, // GitHub doesn't provide duration directly
            webUrl = run.htmlUrl,
            logsUrl = run.logsUrl,
            canRestart = run.status == "completed",
            canCancel = run.status == "in_progress",
            createdAt = run.createdAt,
            updatedAt = run.updatedAt
        )
    }

    private fun mapGitLabJobToBuild(job: com.app.cicdmonitor.data.network.dto.GitLabJob, pipelineId: String): Build {
        return Build(
            id = "gitlab_${job.id}",
            pipelineId = pipelineId,
            buildNumber = job.id.toString(),
            status = mapGitLabStatusToBuildStatus(job.status),
            branch = job.ref,
            commitSha = job.commit.id,
            commitMessage = job.commit.title,
            commitAuthor = job.commit.authorName,
            startedAt = job.startedAt,
            finishedAt = job.finishedAt,
            duration = job.duration?.toLong(),
            webUrl = job.webUrl,
            logsUrl = null, // GitLab doesn't provide direct log URL in job response
            canRestart = job.status in listOf("failed", "canceled", "success"),
            canCancel = job.status in listOf("running", "pending"),
            createdAt = job.createdAt,
            updatedAt = job.createdAt // GitLab jobs don't have separate updated timestamp
        )
    }

    private fun mapJenkinsBuildRefToBuild(buildRef: com.app.cicdmonitor.data.network.dto.JenkinsBuildRef, pipelineId: String): Build {
        return Build(
            id = "jenkins_${buildRef.number}",
            pipelineId = pipelineId,
            buildNumber = buildRef.number.toString(),
            status = BuildStatus.UNKNOWN, // We'd need to fetch full build details to get status
            branch = "main", // Jenkins doesn't always provide branch in build ref
            commitSha = "",
            commitMessage = "",
            commitAuthor = "",
            startedAt = null,
            finishedAt = null,
            duration = null,
            webUrl = buildRef.url,
            logsUrl = "${buildRef.url}console",
            canRestart = true,
            canCancel = false, // We'd need to check if build is running
            createdAt = LocalDateTime.now().toString(),
            updatedAt = LocalDateTime.now().toString()
        )
    }

    private fun mapGitHubStatusToBuildStatus(status: String, conclusion: String?): BuildStatus {
        return when (status.lowercase()) {
            "completed" -> when (conclusion?.lowercase()) {
                "success" -> BuildStatus.SUCCESS
                "failure" -> BuildStatus.FAILURE
                "cancelled" -> BuildStatus.CANCELLED
                "skipped" -> BuildStatus.SKIPPED
                else -> BuildStatus.UNKNOWN
            }
            "in_progress" -> BuildStatus.RUNNING
            "queued" -> BuildStatus.PENDING
            else -> BuildStatus.UNKNOWN
        }
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
}
