package com.app.cicdmonitor.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class JenkinsJob(
    val name: String,
    val url: String,
    val color: String,
    @SerialName("_class")
    val className: String,
    val buildable: Boolean? = null,
    val builds: List<JenkinsBuildRef>? = null,
    val description: String? = null,
    val displayName: String? = null,
    val displayNameOrNull: String? = null,
    val fullDisplayName: String? = null,
    val fullName: String? = null,
    val inQueue: Boolean? = null,
    val keepDependencies: Boolean? = null,
    val lastBuild: JenkinsBuildRef? = null,
    val lastCompletedBuild: JenkinsBuildRef? = null,
    val lastFailedBuild: JenkinsBuildRef? = null,
    val lastStableBuild: JenkinsBuildRef? = null,
    val lastSuccessfulBuild: JenkinsBuildRef? = null,
    val lastUnstableBuild: JenkinsBuildRef? = null,
    val lastUnsuccessfulBuild: JenkinsBuildRef? = null,
    val nextBuildNumber: Int? = null,
    val queueItem: JenkinsQueueItem? = null,
    val concurrentBuild: Boolean? = null,
    val disabled: Boolean? = null,
    val downstreamProjects: List<JenkinsJobRef>? = null,
    val labelExpression: String? = null,
    val scm: JenkinsScm? = null,
    val upstreamProjects: List<JenkinsJobRef>? = null
)

@Serializable
data class JenkinsBuild(
    val number: Int,
    val url: String,
    @SerialName("_class")
    val className: String,
    val actions: List<JenkinsAction>? = null,
    val artifacts: List<JenkinsArtifact>? = null,
    val building: Boolean,
    val description: String? = null,
    val displayName: String,
    val duration: Long,
    val estimatedDuration: Long,
    val executor: JenkinsExecutor? = null,
    val fullDisplayName: String,
    val id: String,
    val keepLog: Boolean,
    val queueId: Long,
    val result: String?,
    val timestamp: Long,
    val builtOn: String? = null,
    val changeSet: JenkinsChangeSet? = null,
    val culprits: List<JenkinsUser>? = null,
    val fingerprint: List<JenkinsFingerprint>? = null,
    val nextBuild: JenkinsBuildRef? = null,
    val previousBuild: JenkinsBuildRef? = null
)

@Serializable
data class JenkinsBuildRef(
    val number: Int,
    val url: String,
    @SerialName("_class")
    val className: String? = null
)

@Serializable
data class JenkinsJobRef(
    val name: String,
    val url: String,
    val color: String? = null,
    @SerialName("_class")
    val className: String? = null
)

@Serializable
data class JenkinsAction(
    @SerialName("_class")
    val className: String,
    val causes: List<JenkinsCause>? = null,
    val parameters: List<JenkinsParameter>? = null,
    val lastBuiltRevision: JenkinsRevision? = null,
    val remoteUrls: List<String>? = null,
    val scmName: String? = null
)

@Serializable
data class JenkinsCause(
    @SerialName("_class")
    val className: String,
    val shortDescription: String,
    val userId: String? = null,
    val userName: String? = null
)

@Serializable
data class JenkinsParameter(
    @SerialName("_class")
    val className: String,
    val name: String,
    val value: String
)

@Serializable
data class JenkinsRevision(
    @SerialName("SHA1")
    val sha1: String,
    val branch: List<JenkinsBranch>? = null
)

@Serializable
data class JenkinsBranch(
    @SerialName("SHA1")
    val sha1: String,
    val name: String
)

@Serializable
data class JenkinsArtifact(
    val displayPath: String,
    val fileName: String,
    val relativePath: String
)

@Serializable
data class JenkinsExecutor(
    val currentExecutable: JenkinsBuildRef? = null,
    val currentWorkUnit: JenkinsWorkUnit? = null,
    val idle: Boolean,
    val likelyStuck: Boolean,
    val number: Int,
    val progress: Int
)

@Serializable
data class JenkinsWorkUnit(
    @SerialName("_class")
    val className: String
)

@Serializable
data class JenkinsChangeSet(
    @SerialName("_class")
    val className: String,
    val items: List<JenkinsChangeSetItem>,
    val kind: String? = null
)

@Serializable
data class JenkinsChangeSetItem(
    @SerialName("_class")
    val className: String,
    val affectedPaths: List<String>,
    val commitId: String,
    val timestamp: Long,
    val author: JenkinsUser,
    val authorEmail: String,
    val comment: String,
    val date: String,
    val id: String,
    val msg: String,
    val paths: List<JenkinsPath>
)

@Serializable
data class JenkinsPath(
    val editType: String,
    val file: String
)

@Serializable
data class JenkinsUser(
    val absoluteUrl: String,
    val fullName: String
)

@Serializable
data class JenkinsFingerprint(
    val fileName: String,
    val hash: String,
    val original: JenkinsBuildRef,
    val timestamp: Long,
    val usage: List<JenkinsFingerprintUsage>
)

@Serializable
data class JenkinsFingerprintUsage(
    val name: String,
    val ranges: JenkinsFingerprintRanges
)

@Serializable
data class JenkinsFingerprintRanges(
    val ranges: List<JenkinsFingerprintRange>
)

@Serializable
data class JenkinsFingerprintRange(
    val end: Int,
    val start: Int
)

@Serializable
data class JenkinsQueueItem(
    val id: Long,
    val inQueueSince: Long,
    val params: String,
    val stuck: Boolean,
    val task: JenkinsTask,
    val url: String,
    val why: String? = null,
    val buildableStartMilliseconds: Long? = null,
    val pending: Boolean? = null
)

@Serializable
data class JenkinsTask(
    @SerialName("_class")
    val className: String,
    val name: String,
    val url: String,
    val color: String
)

@Serializable
data class JenkinsScm(
    @SerialName("_class")
    val className: String
)

@Serializable
data class JenkinsJobsResponse(
    @SerialName("_class")
    val className: String,
    val jobs: List<JenkinsJob>
)

@Serializable
data class JenkinsBuildTriggerResponse(
    val queueItem: String
)
