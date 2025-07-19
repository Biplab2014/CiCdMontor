package com.app.cicdmonitor.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GitHubWorkflowRunsResponse(
    @SerialName("total_count")
    val totalCount: Int,
    @SerialName("workflow_runs")
    val workflowRuns: List<GitHubWorkflowRun>
)

@Serializable
data class GitHubWorkflowRun(
    val id: Long,
    val name: String?,
    @SerialName("display_title")
    val displayTitle: String,
    val status: String,
    val conclusion: String?,
    @SerialName("workflow_id")
    val workflowId: Long,
    @SerialName("check_suite_id")
    val checkSuiteId: Long,
    @SerialName("check_suite_node_id")
    val checkSuiteNodeId: String,
    val url: String,
    @SerialName("html_url")
    val htmlUrl: String,
    @SerialName("pull_requests")
    val pullRequests: List<GitHubPullRequest>,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String,
    @SerialName("actor")
    val actor: GitHubUser,
    @SerialName("run_attempt")
    val runAttempt: Int,
    @SerialName("referenced_workflows")
    val referencedWorkflows: List<GitHubReferencedWorkflow>?,
    @SerialName("run_started_at")
    val runStartedAt: String?,
    @SerialName("triggering_actor")
    val triggeringActor: GitHubUser,
    @SerialName("jobs_url")
    val jobsUrl: String,
    @SerialName("logs_url")
    val logsUrl: String,
    @SerialName("check_suite_url")
    val checkSuiteUrl: String,
    @SerialName("artifacts_url")
    val artifactsUrl: String,
    @SerialName("cancel_url")
    val cancelUrl: String,
    @SerialName("rerun_url")
    val rerunUrl: String,
    @SerialName("previous_attempt_url")
    val previousAttemptUrl: String?,
    @SerialName("workflow_url")
    val workflowUrl: String,
    @SerialName("head_commit")
    val headCommit: GitHubCommit,
    val repository: GitHubRepository,
    @SerialName("head_repository")
    val headRepository: GitHubRepository
)

@Serializable
data class GitHubPullRequest(
    val id: Long,
    val number: Int,
    val url: String,
    @SerialName("html_url")
    val htmlUrl: String
)

@Serializable
data class GitHubUser(
    val login: String,
    val id: Long,
    @SerialName("node_id")
    val nodeId: String,
    @SerialName("avatar_url")
    val avatarUrl: String,
    @SerialName("gravatar_id")
    val gravatarId: String?,
    val url: String,
    @SerialName("html_url")
    val htmlUrl: String,
    @SerialName("followers_url")
    val followersUrl: String,
    @SerialName("following_url")
    val followingUrl: String,
    @SerialName("gists_url")
    val gistsUrl: String,
    @SerialName("starred_url")
    val starredUrl: String,
    @SerialName("subscriptions_url")
    val subscriptionsUrl: String,
    @SerialName("organizations_url")
    val organizationsUrl: String,
    @SerialName("repos_url")
    val reposUrl: String,
    @SerialName("events_url")
    val eventsUrl: String,
    @SerialName("received_events_url")
    val receivedEventsUrl: String,
    val type: String,
    @SerialName("site_admin")
    val siteAdmin: Boolean
)

@Serializable
data class GitHubReferencedWorkflow(
    val path: String,
    val sha: String,
    val ref: String?
)

@Serializable
data class GitHubCommit(
    val id: String,
    @SerialName("tree_id")
    val treeId: String,
    val message: String,
    val timestamp: String,
    val author: GitHubCommitAuthor,
    val committer: GitHubCommitAuthor
)

@Serializable
data class GitHubCommitAuthor(
    val name: String,
    val email: String
)

@Serializable
data class GitHubRepository(
    val id: Long,
    @SerialName("node_id")
    val nodeId: String,
    val name: String,
    @SerialName("full_name")
    val fullName: String,
    val owner: GitHubUser,
    val private: Boolean,
    @SerialName("html_url")
    val htmlUrl: String,
    val description: String?,
    val fork: Boolean,
    val url: String
)

@Serializable
data class GitHubWorkflow(
    val id: Long,
    @SerialName("node_id")
    val nodeId: String,
    val name: String,
    val path: String,
    val state: String,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String,
    val url: String,
    @SerialName("html_url")
    val htmlUrl: String,
    @SerialName("badge_url")
    val badgeUrl: String
)

@Serializable
data class GitHubWorkflowsResponse(
    @SerialName("total_count")
    val totalCount: Int,
    val workflows: List<GitHubWorkflow>
)
