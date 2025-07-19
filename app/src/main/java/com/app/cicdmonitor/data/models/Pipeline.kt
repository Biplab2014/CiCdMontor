package com.app.cicdmonitor.data.models

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Entity(tableName = "pipelines")
@Serializable
data class Pipeline(
    @PrimaryKey
    val id: String,
    val name: String,
    val provider: CiProvider,
    val repositoryUrl: String,
    val branch: String,
    val status: PipelineStatus,
    val lastRunId: String?,
    val lastRunStatus: BuildStatus?,
    val lastRunDuration: Long?, // in milliseconds
    val lastRunTimestamp: String?, // ISO 8601 format
    val lastCommitMessage: String?,
    val lastCommitAuthor: String?,
    val isActive: Boolean = true,
    val createdAt: String = LocalDateTime.now().toString(),
    val updatedAt: String = LocalDateTime.now().toString()
)

@Serializable
enum class CiProvider {
    GITHUB_ACTIONS,
    GITLAB_CI,
    JENKINS
}

@Serializable
enum class PipelineStatus {
    ACTIVE,
    INACTIVE,
    ERROR
}

@Serializable
enum class BuildStatus {
    PENDING,
    RUNNING,
    SUCCESS,
    FAILURE,
    CANCELLED,
    SKIPPED,
    UNKNOWN
}

// Simplified data class for now - we'll implement the relationship later
data class PipelineWithBuilds(
    val pipeline: Pipeline,
    val recentBuilds: List<Build>
)
