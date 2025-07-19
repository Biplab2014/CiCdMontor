package com.app.cicdmonitor.data.network.api

import com.app.cicdmonitor.data.network.dto.*
import retrofit2.Response
import retrofit2.http.*

interface GitLabApiService {
    
    @GET("projects/{id}/pipelines")
    suspend fun getPipelines(
        @Path("id") projectId: String,
        @Query("ref") ref: String? = null,
        @Query("status") status: String? = null,
        @Query("scope") scope: String? = null,
        @Query("username") username: String? = null,
        @Query("updated_after") updatedAfter: String? = null,
        @Query("updated_before") updatedBefore: String? = null,
        @Query("order_by") orderBy: String = "updated_at",
        @Query("sort") sort: String = "desc",
        @Query("per_page") perPage: Int = 20,
        @Query("page") page: Int = 1,
        @Header("Authorization") authorization: String
    ): Response<List<GitLabPipeline>>
    
    @GET("projects/{id}/pipelines/{pipeline_id}")
    suspend fun getPipeline(
        @Path("id") projectId: String,
        @Path("pipeline_id") pipelineId: Long,
        @Header("Authorization") authorization: String
    ): Response<GitLabPipeline>
    
    @GET("projects/{id}/pipelines/{pipeline_id}/jobs")
    suspend fun getPipelineJobs(
        @Path("id") projectId: String,
        @Path("pipeline_id") pipelineId: Long,
        @Query("scope") scope: String? = null,
        @Query("include_retried") includeRetried: Boolean = false,
        @Header("Authorization") authorization: String
    ): Response<List<GitLabJob>>
    
    @POST("projects/{id}/pipeline")
    suspend fun createPipeline(
        @Path("id") projectId: String,
        @Query("ref") ref: String,
        @Query("variables") variables: Map<String, String>? = null,
        @Header("Authorization") authorization: String
    ): Response<GitLabPipeline>
    
    @POST("projects/{id}/pipelines/{pipeline_id}/retry")
    suspend fun retryPipeline(
        @Path("id") projectId: String,
        @Path("pipeline_id") pipelineId: Long,
        @Header("Authorization") authorization: String
    ): Response<GitLabPipeline>
    
    @POST("projects/{id}/pipelines/{pipeline_id}/cancel")
    suspend fun cancelPipeline(
        @Path("id") projectId: String,
        @Path("pipeline_id") pipelineId: Long,
        @Header("Authorization") authorization: String
    ): Response<GitLabPipeline>
    
    @DELETE("projects/{id}/pipelines/{pipeline_id}")
    suspend fun deletePipeline(
        @Path("id") projectId: String,
        @Path("pipeline_id") pipelineId: Long,
        @Header("Authorization") authorization: String
    ): Response<Unit>
    
    @GET("projects/{id}/jobs/{job_id}")
    suspend fun getJob(
        @Path("id") projectId: String,
        @Path("job_id") jobId: Long,
        @Header("Authorization") authorization: String
    ): Response<GitLabJob>
    
    @POST("projects/{id}/jobs/{job_id}/retry")
    suspend fun retryJob(
        @Path("id") projectId: String,
        @Path("job_id") jobId: Long,
        @Header("Authorization") authorization: String
    ): Response<GitLabJob>
    
    @POST("projects/{id}/jobs/{job_id}/cancel")
    suspend fun cancelJob(
        @Path("id") projectId: String,
        @Path("job_id") jobId: Long,
        @Header("Authorization") authorization: String
    ): Response<GitLabJob>
    
    @GET("projects/{id}/jobs/{job_id}/trace")
    suspend fun getJobTrace(
        @Path("id") projectId: String,
        @Path("job_id") jobId: Long,
        @Header("Authorization") authorization: String
    ): Response<String>
    
    @GET("user")
    suspend fun getCurrentUser(
        @Header("Authorization") authorization: String
    ): Response<GitLabUser>
    
    @GET("projects/{id}")
    suspend fun getProject(
        @Path("id") projectId: String,
        @Header("Authorization") authorization: String
    ): Response<GitLabProject>
    
    companion object {
        const val BASE_URL = "https://gitlab.com/api/v4/"
        
        fun createAuthHeader(token: String): String = "Bearer $token"
        
        fun createCustomBaseUrl(serverUrl: String): String = 
            "${serverUrl.trimEnd('/')}/api/v4/"
    }
}
