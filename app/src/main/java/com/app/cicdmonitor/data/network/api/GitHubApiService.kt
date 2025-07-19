package com.app.cicdmonitor.data.network.api

import com.app.cicdmonitor.data.network.dto.*
import retrofit2.Response
import retrofit2.http.*

interface GitHubApiService {
    
    @GET("repos/{owner}/{repo}/actions/workflows")
    suspend fun getWorkflows(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Header("Authorization") authorization: String
    ): Response<GitHubWorkflowsResponse>
    
    @GET("repos/{owner}/{repo}/actions/runs")
    suspend fun getWorkflowRuns(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Query("workflow_id") workflowId: Long? = null,
        @Query("actor") actor: String? = null,
        @Query("branch") branch: String? = null,
        @Query("event") event: String? = null,
        @Query("status") status: String? = null,
        @Query("per_page") perPage: Int = 30,
        @Query("page") page: Int = 1,
        @Header("Authorization") authorization: String
    ): Response<GitHubWorkflowRunsResponse>
    
    @GET("repos/{owner}/{repo}/actions/runs/{run_id}")
    suspend fun getWorkflowRun(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("run_id") runId: Long,
        @Header("Authorization") authorization: String
    ): Response<GitHubWorkflowRun>
    
    @POST("repos/{owner}/{repo}/actions/workflows/{workflow_id}/dispatches")
    suspend fun triggerWorkflow(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("workflow_id") workflowId: Long,
        @Body request: GitHubWorkflowDispatchRequest,
        @Header("Authorization") authorization: String
    ): Response<Unit>
    
    @POST("repos/{owner}/{repo}/actions/runs/{run_id}/rerun")
    suspend fun rerunWorkflow(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("run_id") runId: Long,
        @Header("Authorization") authorization: String
    ): Response<Unit>
    
    @POST("repos/{owner}/{repo}/actions/runs/{run_id}/cancel")
    suspend fun cancelWorkflow(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("run_id") runId: Long,
        @Header("Authorization") authorization: String
    ): Response<Unit>
    
    @GET("user")
    suspend fun getCurrentUser(
        @Header("Authorization") authorization: String
    ): Response<GitHubUser>
    
    companion object {
        const val BASE_URL = "https://api.github.com/"
        
        fun createAuthHeader(token: String): String = "Bearer $token"
    }
}

@kotlinx.serialization.Serializable
data class GitHubWorkflowDispatchRequest(
    val ref: String,
    val inputs: Map<String, String> = emptyMap()
)
