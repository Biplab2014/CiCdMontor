package com.app.cicdmonitor.ui.screens.pipeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.cicdmonitor.data.models.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class PipelineDetailViewModel : ViewModel() {
    
    private val _uiState = MutableStateFlow(PipelineDetailUiState())
    val uiState: StateFlow<PipelineDetailUiState> = _uiState.asStateFlow()
    
    fun loadPipelineDetails(pipelineId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            // Simulate loading delay
            kotlinx.coroutines.delay(1000)
            
            // Create detailed pipeline data based on ID
            val pipeline = createDetailedPipeline(pipelineId)
            val builds = createSampleBuilds(pipelineId)
            
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                pipeline = pipeline,
                builds = builds,
                errorMessage = null
            )
        }
    }
    
    fun triggerBuild() {
        val pipeline = _uiState.value.pipeline ?: return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isTriggering = true)
            
            // Simulate API call
            kotlinx.coroutines.delay(2000)
            
            // Create new build
            val newBuild = Build(
                id = "new_${System.currentTimeMillis()}",
                pipelineId = pipeline.id,
                buildNumber = "${(_uiState.value.builds.size + 1)}",
                status = BuildStatus.RUNNING,
                branch = pipeline.branch,
                commitSha = "abc123def",
                commitMessage = "Manually triggered build",
                commitAuthor = "Current User",
                startedAt = LocalDateTime.now().toString(),
                finishedAt = null,
                duration = null,
                webUrl = "${pipeline.repositoryUrl}/builds/new",
                logsUrl = "${pipeline.repositoryUrl}/builds/new/logs",
                canRestart = false,
                canCancel = true,
                createdAt = LocalDateTime.now().toString(),
                updatedAt = LocalDateTime.now().toString()
            )
            
            val updatedBuilds = listOf(newBuild) + _uiState.value.builds
            val updatedPipeline = pipeline.copy(
                lastRunId = newBuild.id,
                lastRunStatus = BuildStatus.RUNNING,
                lastRunTimestamp = newBuild.startedAt,
                lastCommitMessage = newBuild.commitMessage,
                lastCommitAuthor = newBuild.commitAuthor
            )
            
            _uiState.value = _uiState.value.copy(
                isTriggering = false,
                pipeline = updatedPipeline,
                builds = updatedBuilds,
                successMessage = "Build triggered successfully!"
            )
        }
    }
    
    fun retryBuild(buildId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRetrying = true)
            
            // Simulate API call
            kotlinx.coroutines.delay(1500)
            
            // Update build status
            val updatedBuilds = _uiState.value.builds.map { build ->
                if (build.id == buildId) {
                    build.copy(
                        status = BuildStatus.RUNNING,
                        startedAt = LocalDateTime.now().toString(),
                        finishedAt = null,
                        duration = null
                    )
                } else build
            }
            
            _uiState.value = _uiState.value.copy(
                isRetrying = false,
                builds = updatedBuilds,
                successMessage = "Build restarted successfully!"
            )
        }
    }
    
    fun cancelBuild(buildId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCancelling = true)
            
            // Simulate API call
            kotlinx.coroutines.delay(1000)
            
            // Update build status
            val updatedBuilds = _uiState.value.builds.map { build ->
                if (build.id == buildId) {
                    build.copy(
                        status = BuildStatus.CANCELLED,
                        finishedAt = LocalDateTime.now().toString(),
                        duration = System.currentTimeMillis() - 
                            (build.startedAt?.let { parseTimestamp(it) } ?: System.currentTimeMillis())
                    )
                } else build
            }
            
            _uiState.value = _uiState.value.copy(
                isCancelling = false,
                builds = updatedBuilds,
                successMessage = "Build cancelled successfully!"
            )
        }
    }
    
    fun loadBuildLogs(buildId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingLogs = true)
            
            // Simulate loading logs
            kotlinx.coroutines.delay(1000)
            
            val logs = createSampleLogs(buildId)
            
            _uiState.value = _uiState.value.copy(
                isLoadingLogs = false,
                selectedBuildLogs = logs
            )
        }
    }
    
    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            successMessage = null,
            errorMessage = null
        )
    }
    
    fun clearLogs() {
        _uiState.value = _uiState.value.copy(selectedBuildLogs = null)
    }
    
    private fun createDetailedPipeline(pipelineId: String): Pipeline {
        return when (pipelineId) {
            "1" -> Pipeline(
                id = "1",
                name = "Frontend Build",
                provider = CiProvider.GITHUB_ACTIONS,
                repositoryUrl = "https://github.com/example/frontend",
                branch = "main",
                status = PipelineStatus.ACTIVE,
                lastRunId = "run_123",
                lastRunStatus = BuildStatus.SUCCESS,
                lastRunDuration = 300000, // 5 minutes
                lastRunTimestamp = "2024-07-19T08:00:00Z",
                lastCommitMessage = "Fix login bug and improve error handling",
                lastCommitAuthor = "John Doe",
                isActive = true,
                createdAt = "2024-07-01T10:00:00Z",
                updatedAt = "2024-07-19T08:00:00Z"
            )
            "2" -> Pipeline(
                id = "2",
                name = "Backend API",
                provider = CiProvider.GITLAB_CI,
                repositoryUrl = "https://gitlab.com/example/backend",
                branch = "develop",
                status = PipelineStatus.ACTIVE,
                lastRunId = "pipeline_456",
                lastRunStatus = BuildStatus.FAILURE,
                lastRunDuration = 450000, // 7.5 minutes
                lastRunTimestamp = "2024-07-19T07:30:00Z",
                lastCommitMessage = "Add new API endpoint for user management",
                lastCommitAuthor = "Jane Smith",
                isActive = true,
                createdAt = "2024-06-15T14:30:00Z",
                updatedAt = "2024-07-19T07:30:00Z"
            )
            "3" -> Pipeline(
                id = "3",
                name = "Mobile App",
                provider = CiProvider.JENKINS,
                repositoryUrl = "https://jenkins.example.com/job/mobile-app",
                branch = "main",
                status = PipelineStatus.ACTIVE,
                lastRunId = "build_789",
                lastRunStatus = BuildStatus.RUNNING,
                lastRunDuration = null,
                lastRunTimestamp = "2024-07-19T08:15:00Z",
                lastCommitMessage = "Update dependencies and fix security vulnerabilities",
                lastCommitAuthor = "Bob Wilson",
                isActive = true,
                createdAt = "2024-05-20T09:15:00Z",
                updatedAt = "2024-07-19T08:15:00Z"
            )
            else -> Pipeline(
                id = pipelineId,
                name = "Unknown Pipeline",
                provider = CiProvider.GITHUB_ACTIONS,
                repositoryUrl = "https://github.com/example/unknown",
                branch = "main",
                status = PipelineStatus.INACTIVE,
                lastRunId = null,
                lastRunStatus = null,
                lastRunDuration = null,
                lastRunTimestamp = null,
                lastCommitMessage = null,
                lastCommitAuthor = null
            )
        }
    }
    
    private fun createSampleBuilds(pipelineId: String): List<Build> {
        val baseTime = System.currentTimeMillis()
        return listOf(
            Build(
                id = "${pipelineId}_build_1",
                pipelineId = pipelineId,
                buildNumber = "156",
                status = if (pipelineId == "3") BuildStatus.RUNNING else BuildStatus.SUCCESS,
                branch = "main",
                commitSha = "a1b2c3d4e5f6",
                commitMessage = "Fix critical security vulnerability in authentication",
                commitAuthor = "John Doe",
                startedAt = formatTimestamp(baseTime - 300000),
                finishedAt = if (pipelineId == "3") null else formatTimestamp(baseTime - 60000),
                duration = if (pipelineId == "3") null else 240000,
                webUrl = "https://example.com/builds/156",
                logsUrl = "https://example.com/builds/156/logs",
                canRestart = pipelineId != "3",
                canCancel = pipelineId == "3",
                createdAt = formatTimestamp(baseTime - 300000),
                updatedAt = formatTimestamp(baseTime - 60000)
            ),
            Build(
                id = "${pipelineId}_build_2",
                pipelineId = pipelineId,
                buildNumber = "155",
                status = if (pipelineId == "2") BuildStatus.FAILURE else BuildStatus.SUCCESS,
                branch = "main",
                commitSha = "f6e5d4c3b2a1",
                commitMessage = "Add new feature for user dashboard",
                commitAuthor = "Jane Smith",
                startedAt = formatTimestamp(baseTime - 3600000),
                finishedAt = formatTimestamp(baseTime - 3300000),
                duration = 300000,
                webUrl = "https://example.com/builds/155",
                logsUrl = "https://example.com/builds/155/logs",
                canRestart = true,
                canCancel = false,
                createdAt = formatTimestamp(baseTime - 3600000),
                updatedAt = formatTimestamp(baseTime - 3300000)
            ),
            Build(
                id = "${pipelineId}_build_3",
                pipelineId = pipelineId,
                buildNumber = "154",
                status = BuildStatus.SUCCESS,
                branch = "develop",
                commitSha = "1a2b3c4d5e6f",
                commitMessage = "Update documentation and fix typos",
                commitAuthor = "Bob Wilson",
                startedAt = formatTimestamp(baseTime - 7200000),
                finishedAt = formatTimestamp(baseTime - 6900000),
                duration = 300000,
                webUrl = "https://example.com/builds/154",
                logsUrl = "https://example.com/builds/154/logs",
                canRestart = true,
                canCancel = false,
                createdAt = formatTimestamp(baseTime - 7200000),
                updatedAt = formatTimestamp(baseTime - 6900000)
            )
        )
    }
    
    private fun createSampleLogs(buildId: String): String {
        return """
            [2024-07-19 08:15:00] Starting build for commit a1b2c3d4e5f6
            [2024-07-19 08:15:01] Checking out repository...
            [2024-07-19 08:15:02] Repository checked out successfully
            [2024-07-19 08:15:03] Installing dependencies...
            [2024-07-19 08:15:15] Dependencies installed successfully
            [2024-07-19 08:15:16] Running tests...
            [2024-07-19 08:15:45] ✓ All tests passed (127 tests)
            [2024-07-19 08:15:46] Building application...
            [2024-07-19 08:16:30] ✓ Build completed successfully
            [2024-07-19 08:16:31] Running security scan...
            [2024-07-19 08:16:45] ✓ No security vulnerabilities found
            [2024-07-19 08:16:46] Deploying to staging environment...
            [2024-07-19 08:17:00] ✓ Deployment completed successfully
            [2024-07-19 08:17:01] Build finished with status: SUCCESS
        """.trimIndent()
    }
    
    private fun formatTimestamp(timestamp: Long): String {
        return LocalDateTime.ofEpochSecond(timestamp / 1000, 0, java.time.ZoneOffset.UTC)
            .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "Z"
    }
    
    private fun parseTimestamp(timestamp: String): Long {
        return try {
            LocalDateTime.parse(timestamp.removeSuffix("Z"))
                .toEpochSecond(java.time.ZoneOffset.UTC) * 1000
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }
}

data class PipelineDetailUiState(
    val isLoading: Boolean = false,
    val pipeline: Pipeline? = null,
    val builds: List<Build> = emptyList(),
    val selectedBuildLogs: String? = null,
    val isLoadingLogs: Boolean = false,
    val isTriggering: Boolean = false,
    val isRetrying: Boolean = false,
    val isCancelling: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)
