package com.app.cicdmonitor.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GitLabPipeline(
    val id: Long,
    val iid: Long,
    @SerialName("project_id")
    val projectId: Long,
    val sha: String,
    val ref: String,
    val status: String,
    val source: String,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String,
    @SerialName("web_url")
    val webUrl: String,
    val before_sha: String?,
    val tag: Boolean,
    @SerialName("yaml_errors")
    val yamlErrors: String?,
    val user: GitLabUser,
    @SerialName("started_at")
    val startedAt: String?,
    @SerialName("finished_at")
    val finishedAt: String?,
    @SerialName("committed_at")
    val committedAt: String?,
    val duration: Long?,
    @SerialName("queued_duration")
    val queuedDuration: Long?,
    val coverage: String?,
    @SerialName("detailed_status")
    val detailedStatus: GitLabDetailedStatus?
)

@Serializable
data class GitLabUser(
    val id: Long,
    val username: String,
    val name: String,
    val state: String,
    @SerialName("avatar_url")
    val avatarUrl: String?,
    @SerialName("web_url")
    val webUrl: String,
    @SerialName("created_at")
    val createdAt: String,
    val bio: String?,
    val location: String?,
    @SerialName("public_email")
    val publicEmail: String?,
    val skype: String?,
    val linkedin: String?,
    val twitter: String?,
    @SerialName("website_url")
    val websiteUrl: String?,
    val organization: String?
)

@Serializable
data class GitLabDetailedStatus(
    val icon: String,
    val text: String,
    val label: String,
    val group: String,
    val tooltip: String,
    @SerialName("has_details")
    val hasDetails: Boolean,
    @SerialName("details_path")
    val detailsPath: String?,
    val illustration: String?,
    val favicon: String?
)

@Serializable
data class GitLabJob(
    val id: Long,
    val status: String,
    val stage: String,
    val name: String,
    val ref: String,
    val tag: Boolean,
    val coverage: String?,
    @SerialName("allow_failure")
    val allowFailure: Boolean,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("started_at")
    val startedAt: String?,
    @SerialName("finished_at")
    val finishedAt: String?,
    @SerialName("erased_at")
    val erasedAt: String?,
    val duration: Double?,
    @SerialName("queued_duration")
    val queuedDuration: Double?,
    val user: GitLabUser,
    val commit: GitLabCommit,
    val pipeline: GitLabPipelineRef,
    @SerialName("web_url")
    val webUrl: String,
    val project: GitLabProject,
    val artifacts: List<GitLabArtifact>?,
    val runner: GitLabRunner?,
    @SerialName("artifacts_expire_at")
    val artifactsExpireAt: String?,
    @SerialName("tag_list")
    val tagList: List<String>
)

@Serializable
data class GitLabCommit(
    val id: String,
    @SerialName("short_id")
    val shortId: String,
    val title: String,
    @SerialName("author_name")
    val authorName: String,
    @SerialName("author_email")
    val authorEmail: String,
    @SerialName("authored_date")
    val authoredDate: String,
    @SerialName("committer_name")
    val committerName: String,
    @SerialName("committer_email")
    val committerEmail: String,
    @SerialName("committed_date")
    val committedDate: String,
    @SerialName("created_at")
    val createdAt: String,
    val message: String,
    @SerialName("parent_ids")
    val parentIds: List<String>,
    @SerialName("web_url")
    val webUrl: String
)

@Serializable
data class GitLabPipelineRef(
    val id: Long,
    val sha: String,
    val ref: String,
    val status: String,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String,
    @SerialName("web_url")
    val webUrl: String
)

@Serializable
data class GitLabProject(
    val id: Long,
    val name: String,
    @SerialName("name_with_namespace")
    val nameWithNamespace: String,
    val path: String,
    @SerialName("path_with_namespace")
    val pathWithNamespace: String,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("default_branch")
    val defaultBranch: String,
    @SerialName("tag_list")
    val tagList: List<String>,
    @SerialName("topics")
    val topics: List<String>,
    @SerialName("ssh_url_to_repo")
    val sshUrlToRepo: String,
    @SerialName("http_url_to_repo")
    val httpUrlToRepo: String,
    @SerialName("web_url")
    val webUrl: String,
    @SerialName("readme_url")
    val readmeUrl: String?,
    @SerialName("avatar_url")
    val avatarUrl: String?,
    @SerialName("forks_count")
    val forksCount: Int,
    @SerialName("star_count")
    val starCount: Int,
    @SerialName("last_activity_at")
    val lastActivityAt: String,
    val namespace: GitLabNamespace
)

@Serializable
data class GitLabNamespace(
    val id: Long,
    val name: String,
    val path: String,
    val kind: String,
    @SerialName("full_path")
    val fullPath: String,
    @SerialName("parent_id")
    val parentId: Long?,
    @SerialName("avatar_url")
    val avatarUrl: String?,
    @SerialName("web_url")
    val webUrl: String
)

@Serializable
data class GitLabArtifact(
    @SerialName("file_type")
    val fileType: String,
    val size: Long,
    val filename: String,
    @SerialName("file_format")
    val fileFormat: String?
)

@Serializable
data class GitLabRunner(
    val id: Long,
    val description: String,
    @SerialName("ip_address")
    val ipAddress: String?,
    val active: Boolean,
    @SerialName("paused")
    val paused: Boolean,
    @SerialName("is_shared")
    val isShared: Boolean,
    @SerialName("runner_type")
    val runnerType: String,
    val name: String?,
    val online: Boolean,
    val status: String
)
