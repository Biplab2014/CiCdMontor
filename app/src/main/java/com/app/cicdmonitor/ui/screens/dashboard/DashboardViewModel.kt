package com.app.cicdmonitor.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.cicdmonitor.data.models.Pipeline
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class DashboardViewModel : ViewModel() {
    
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadPipelines()
    }

    private fun loadPipelines() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            // TODO: Load pipelines from repository
            kotlinx.coroutines.delay(1000) // Simulate loading

            // For demo purposes, create some sample pipelines
            val samplePipelines = createSamplePipelines()

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                pipelines = samplePipelines,
                errorMessage = null
            )
        }
    }

    fun refreshPipelines() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true)

            // TODO: Implement actual pipeline refresh
            kotlinx.coroutines.delay(1000) // Simulate refresh

            _uiState.value = _uiState.value.copy(isRefreshing = false)
        }
    }

    private fun createSamplePipelines(): List<Pipeline> {
        return listOf(
            Pipeline(
                id = "1",
                name = "Frontend Build",
                provider = com.app.cicdmonitor.data.models.CiProvider.GITHUB_ACTIONS,
                repositoryUrl = "https://github.com/example/frontend",
                branch = "main",
                status = com.app.cicdmonitor.data.models.PipelineStatus.ACTIVE,
                lastRunId = "123",
                lastRunStatus = com.app.cicdmonitor.data.models.BuildStatus.SUCCESS,
                lastRunDuration = 300000,
                lastRunTimestamp = "2024-07-19T08:00:00Z",
                lastCommitMessage = "Fix login bug",
                lastCommitAuthor = "John Doe"
            ),
            Pipeline(
                id = "2",
                name = "Backend API",
                provider = com.app.cicdmonitor.data.models.CiProvider.GITLAB_CI,
                repositoryUrl = "https://gitlab.com/example/backend",
                branch = "develop",
                status = com.app.cicdmonitor.data.models.PipelineStatus.ACTIVE,
                lastRunId = "456",
                lastRunStatus = com.app.cicdmonitor.data.models.BuildStatus.FAILURE,
                lastRunDuration = 450000,
                lastRunTimestamp = "2024-07-19T07:30:00Z",
                lastCommitMessage = "Add new API endpoint",
                lastCommitAuthor = "Jane Smith"
            ),
            Pipeline(
                id = "3",
                name = "Mobile App",
                provider = com.app.cicdmonitor.data.models.CiProvider.JENKINS,
                repositoryUrl = "https://jenkins.example.com/job/mobile-app",
                branch = "main",
                status = com.app.cicdmonitor.data.models.PipelineStatus.ACTIVE,
                lastRunId = "789",
                lastRunStatus = com.app.cicdmonitor.data.models.BuildStatus.RUNNING,
                lastRunDuration = null,
                lastRunTimestamp = "2024-07-19T08:15:00Z",
                lastCommitMessage = "Update dependencies",
                lastCommitAuthor = "Bob Wilson"
            )
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

data class DashboardUiState(
    val pipelines: List<Pipeline> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null
)
