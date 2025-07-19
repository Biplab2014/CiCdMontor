package com.app.cicdmonitor.ui.screens.pipeline

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.cicdmonitor.data.models.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PipelineDetailScreen(
    pipelineId: String,
    onBackClick: () -> Unit,
    viewModel: PipelineDetailViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(pipelineId) {
        viewModel.loadPipelineDetails(pipelineId)
    }

    // Show success/error messages
    val successMessage = uiState.successMessage
    if (successMessage != null) {
        LaunchedEffect(successMessage) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.pipeline?.name ?: "Pipeline Details",
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    val pipeline = uiState.pipeline
                    if (pipeline != null) {
                        IconButton(
                            onClick = { viewModel.triggerBuild() },
                            enabled = !uiState.isTriggering
                        ) {
                            if (uiState.isTriggering) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Trigger Build"
                                )
                            }
                        }
                    }
                }
            )
        },
        snackbarHost = {
            val successMsg = uiState.successMessage
            if (successMsg != null) {
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    action = {
                        TextButton(onClick = { viewModel.clearMessages() }) {
                            Text("Dismiss")
                        }
                    }
                ) {
                    Text(successMsg)
                }
            }
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.pipeline != null -> {
                val pipeline = uiState.pipeline
                var selectedTab by remember { mutableIntStateOf(0) }
                val tabs = listOf("Overview", "Builds", "Logs")

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    // Tab Row
                    TabRow(selectedTabIndex = selectedTab) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                text = { Text(title) }
                            )
                        }
                    }

                    // Tab Content
                    when (selectedTab) {
                        0 -> pipeline?.let { p ->
                            PipelineOverviewTab(
                                pipeline = p,
                                recentBuilds = uiState.builds.take(3),
                                onTriggerBuild = { viewModel.triggerBuild() },
                                isTriggering = uiState.isTriggering
                            )
                        }
                        1 -> BuildsTab(
                            builds = uiState.builds,
                            onRetryBuild = { buildId -> viewModel.retryBuild(buildId) },
                            onCancelBuild = { buildId -> viewModel.cancelBuild(buildId) },
                            onViewLogs = { buildId -> viewModel.loadBuildLogs(buildId) },
                            isRetrying = uiState.isRetrying,
                            isCancelling = uiState.isCancelling
                        )
                        2 -> LogsTab(
                            logs = uiState.selectedBuildLogs,
                            isLoading = uiState.isLoadingLogs,
                            onClearLogs = { viewModel.clearLogs() }
                        )
                    }
                }
            }

            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Pipeline not found",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun PipelineOverviewTab(
    pipeline: Pipeline,
    recentBuilds: List<Build>,
    onTriggerBuild: () -> Unit,
    isTriggering: Boolean
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Pipeline Info Card
        item {
            PipelineInfoCard(pipeline = pipeline)
        }

        // Quick Actions Card
        item {
            QuickActionsCard(
                onTriggerBuild = onTriggerBuild,
                isTriggering = isTriggering,
                canTrigger = pipeline.status == PipelineStatus.ACTIVE
            )
        }

        // Recent Builds Card
        item {
            RecentBuildsCard(builds = recentBuilds)
        }

        // Pipeline Statistics Card
        item {
            PipelineStatsCard(pipeline = pipeline, builds = recentBuilds)
        }
    }
}

@Composable
private fun BuildsTab(
    builds: List<Build>,
    onRetryBuild: (String) -> Unit,
    onCancelBuild: (String) -> Unit,
    onViewLogs: (String) -> Unit,
    isRetrying: Boolean,
    isCancelling: Boolean
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(builds) { build ->
            BuildCard(
                build = build,
                onRetry = { onRetryBuild(build.id) },
                onCancel = { onCancelBuild(build.id) },
                onViewLogs = { onViewLogs(build.id) },
                isRetrying = isRetrying,
                isCancelling = isCancelling
            )
        }
    }
}

@Composable
private fun LogsTab(
    logs: String?,
    isLoading: Boolean,
    onClearLogs: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Logs header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Build Logs",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            if (logs != null) {
                TextButton(onClick = onClearLogs) {
                    Text("Clear")
                }
            }
        }

        // Logs content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                logs != null -> {
                    Card(
                        modifier = Modifier.fillMaxSize(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text(
                            text = logs,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                                .verticalScroll(rememberScrollState()),
                            fontFamily = FontFamily.Monospace,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                else -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No logs selected",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Select a build from the Builds tab to view logs",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PipelineInfoCard(pipeline: Pipeline) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Pipeline Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                ProviderChip(provider = pipeline.provider)
            }

            Spacer(modifier = Modifier.height(16.dp))

            InfoRow(label = "Repository", value = pipeline.repositoryUrl)
            InfoRow(label = "Branch", value = pipeline.branch)
            InfoRow(label = "Status", value = pipeline.status.name)

            if (pipeline.lastCommitMessage != null) {
                InfoRow(label = "Last Commit", value = pipeline.lastCommitMessage)
            }

            if (pipeline.lastCommitAuthor != null) {
                InfoRow(label = "Author", value = pipeline.lastCommitAuthor)
            }

            if (pipeline.lastRunTimestamp != null) {
                InfoRow(
                    label = "Last Run",
                    value = formatRelativeTime(pipeline.lastRunTimestamp)
                )
            }
        }
    }
}

@Composable
private fun QuickActionsCard(
    onTriggerBuild: () -> Unit,
    isTriggering: Boolean,
    canTrigger: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onTriggerBuild,
                    enabled = canTrigger && !isTriggering,
                    modifier = Modifier.weight(1f)
                ) {
                    if (isTriggering) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    } else {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("Trigger Build")
                }

                OutlinedButton(
                    onClick = { /* TODO: Implement settings */ },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Settings")
                }
            }
        }
    }
}

@Composable
private fun RecentBuildsCard(builds: List<Build>) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Recent Builds",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (builds.isEmpty()) {
                Text(
                    text = "No builds available",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                builds.forEach { build ->
                    BuildSummaryRow(build = build)
                    if (build != builds.last()) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun PipelineStatsCard(pipeline: Pipeline, builds: List<Build>) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Statistics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    label = "Total Builds",
                    value = builds.size.toString()
                )

                StatItem(
                    label = "Success Rate",
                    value = "${calculateSuccessRate(builds)}%"
                )

                StatItem(
                    label = "Avg Duration",
                    value = formatDuration(calculateAverageDuration(builds))
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BuildCard(
    build: Build,
    onRetry: () -> Unit,
    onCancel: () -> Unit,
    onViewLogs: () -> Unit,
    isRetrying: Boolean,
    isCancelling: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Build #${build.buildNumber}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                BuildStatusChip(status = build.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = build.commitMessage,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "by ${build.commitAuthor} • ${formatRelativeTime(build.startedAt ?: "")}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )

            if (build.duration != null) {
                Text(
                    text = "Duration: ${formatDuration(build.duration)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onViewLogs,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Logs")
                }

                if (build.canRestart) {
                    OutlinedButton(
                        onClick = onRetry,
                        enabled = !isRetrying,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (isRetrying) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Retry")
                        }
                    }
                }

                if (build.canCancel) {
                    OutlinedButton(
                        onClick = onCancel,
                        enabled = !isCancelling,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        if (isCancelling) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.error
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Cancel")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BuildSummaryRow(build: Build) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Build #${build.buildNumber}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = formatRelativeTime(build.startedAt ?: ""),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        BuildStatusChip(status = build.status)
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(2f)
        )
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ProviderChip(provider: CiProvider) {
    val (text, color) = when (provider) {
        CiProvider.GITHUB_ACTIONS -> "GitHub" to Color(0xFF24292e)
        CiProvider.GITLAB_CI -> "GitLab" to Color(0xFFFC6D26)
        CiProvider.JENKINS -> "Jenkins" to Color(0xFF335061)
    }

    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.clip(RoundedCornerShape(12.dp))
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun BuildStatusChip(status: BuildStatus) {
    val (text, color) = when (status) {
        BuildStatus.SUCCESS -> "Success" to Color(0xFF4CAF50)
        BuildStatus.FAILURE -> "Failed" to Color(0xFFF44336)
        BuildStatus.RUNNING -> "Running" to Color(0xFF2196F3)
        BuildStatus.PENDING -> "Pending" to Color(0xFFFF9800)
        BuildStatus.CANCELLED -> "Cancelled" to Color(0xFF9E9E9E)
        BuildStatus.SKIPPED -> "Skipped" to Color(0xFF607D8B)
        BuildStatus.UNKNOWN -> "Unknown" to Color(0xFF9E9E9E)
    }

    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.clip(RoundedCornerShape(12.dp))
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

// Utility functions
private fun formatRelativeTime(timestamp: String): String {
    return try {
        val dateTime = LocalDateTime.parse(timestamp.removeSuffix("Z"))
        val now = LocalDateTime.now()
        val duration = java.time.Duration.between(dateTime, now)

        when {
            duration.toMinutes() < 1 -> "Just now"
            duration.toMinutes() < 60 -> "${duration.toMinutes()}m ago"
            duration.toHours() < 24 -> "${duration.toHours()}h ago"
            duration.toDays() < 7 -> "${duration.toDays()}d ago"
            else -> dateTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
        }
    } catch (e: Exception) {
        "Unknown"
    }
}

private fun formatDuration(durationMs: Long?): String {
    if (durationMs == null) return "N/A"

    val seconds = durationMs / 1000
    val minutes = seconds / 60
    val hours = minutes / 60

    return when {
        hours > 0 -> "${hours}h ${minutes % 60}m"
        minutes > 0 -> "${minutes}m ${seconds % 60}s"
        else -> "${seconds}s"
    }
}

private fun calculateSuccessRate(builds: List<Build>): Int {
    if (builds.isEmpty()) return 0
    val successCount = builds.count { it.status == BuildStatus.SUCCESS }
    return (successCount * 100) / builds.size
}

private fun calculateAverageDuration(builds: List<Build>): Long? {
    val durations = builds.mapNotNull { it.duration }
    return if (durations.isNotEmpty()) durations.average().toLong() else null
}
