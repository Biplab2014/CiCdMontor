package com.app.cicdmonitor.data.network.api

import com.app.cicdmonitor.data.network.dto.*
import retrofit2.Response
import retrofit2.http.*

interface JenkinsApiService {
    
    @GET("api/json")
    suspend fun getJobs(
        @Query("tree") tree: String = "jobs[name,url,color,buildable,lastBuild[number,url,building,result,timestamp,duration],lastCompletedBuild[number,url,result,timestamp],lastSuccessfulBuild[number,url,timestamp],lastFailedBuild[number,url,timestamp]]",
        @Header("Authorization") authorization: String
    ): Response<JenkinsJobsResponse>
    
    @GET("job/{jobName}/api/json")
    suspend fun getJob(
        @Path("jobName") jobName: String,
        @Query("tree") tree: String = "name,url,color,buildable,description,displayName,fullDisplayName,lastBuild[*],lastCompletedBuild[*],lastSuccessfulBuild[*],lastFailedBuild[*],builds[number,url,building,result,timestamp,duration]",
        @Header("Authorization") authorization: String
    ): Response<JenkinsJob>
    
    @GET("job/{jobName}/{buildNumber}/api/json")
    suspend fun getBuild(
        @Path("jobName") jobName: String,
        @Path("buildNumber") buildNumber: Int,
        @Query("tree") tree: String = "*",
        @Header("Authorization") authorization: String
    ): Response<JenkinsBuild>
    
    @GET("job/{jobName}/lastBuild/api/json")
    suspend fun getLastBuild(
        @Path("jobName") jobName: String,
        @Query("tree") tree: String = "*",
        @Header("Authorization") authorization: String
    ): Response<JenkinsBuild>
    
    @POST("job/{jobName}/build")
    suspend fun triggerBuild(
        @Path("jobName") jobName: String,
        @Header("Authorization") authorization: String
    ): Response<Unit>
    
    @POST("job/{jobName}/buildWithParameters")
    suspend fun triggerBuildWithParameters(
        @Path("jobName") jobName: String,
        @QueryMap parameters: Map<String, String>,
        @Header("Authorization") authorization: String
    ): Response<Unit>
    
    @POST("job/{jobName}/{buildNumber}/stop")
    suspend fun stopBuild(
        @Path("jobName") jobName: String,
        @Path("buildNumber") buildNumber: Int,
        @Header("Authorization") authorization: String
    ): Response<Unit>
    
    @GET("job/{jobName}/{buildNumber}/consoleText")
    suspend fun getBuildConsoleOutput(
        @Path("jobName") jobName: String,
        @Path("buildNumber") buildNumber: Int,
        @Header("Authorization") authorization: String
    ): Response<String>
    
    @GET("queue/api/json")
    suspend fun getQueue(
        @Query("tree") tree: String = "items[id,inQueueSince,params,stuck,task[name,url,color],why,buildableStartMilliseconds,pending]",
        @Header("Authorization") authorization: String
    ): Response<JenkinsQueueResponse>
    
    @GET("user/{username}/api/json")
    suspend fun getUser(
        @Path("username") username: String,
        @Header("Authorization") authorization: String
    ): Response<JenkinsUser>
    
    @GET("me/api/json")
    suspend fun getCurrentUser(
        @Header("Authorization") authorization: String
    ): Response<JenkinsCurrentUser>
    
    @GET("crumbIssuer/api/json")
    suspend fun getCrumb(
        @Header("Authorization") authorization: String
    ): Response<JenkinsCrumb>
    
    companion object {
        fun createAuthHeader(username: String, apiToken: String): String {
            val credentials = "$username:$apiToken"
            val encodedCredentials = android.util.Base64.encodeToString(
                credentials.toByteArray(),
                android.util.Base64.NO_WRAP
            )
            return "Basic $encodedCredentials"
        }
        
        fun createBaseUrl(serverUrl: String): String = 
            "${serverUrl.trimEnd('/')}/"
    }
}

@kotlinx.serialization.Serializable
data class JenkinsQueueResponse(
    val items: List<JenkinsQueueItem>
)

@kotlinx.serialization.Serializable
data class JenkinsCurrentUser(
    val id: String,
    val fullName: String,
    val description: String?,
    val absoluteUrl: String
)

@kotlinx.serialization.Serializable
data class JenkinsCrumb(
    val crumb: String,
    val crumbRequestField: String
)
