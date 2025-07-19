package com.app.cicdmonitor.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "builds")
@Serializable
data class Build(
    @PrimaryKey
    val id: String,
    val pipelineId: String,
    val buildNumber: String,
    val status: BuildStatus,
    val branch: String,
    val commitSha: String,
    val commitMessage: String,
    val commitAuthor: String,
    val startedAt: String?, // ISO 8601 format
    val finishedAt: String?, // ISO 8601 format
    val duration: Long?, // in milliseconds
    val webUrl: String?,
    val logsUrl: String?,
    val canRestart: Boolean = false,
    val canCancel: Boolean = false,
    val createdAt: String,
    val updatedAt: String
)

data class BuildLog(
    val buildId: String,
    val content: String,
    val timestamp: String
)
