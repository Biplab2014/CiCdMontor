package com.app.cicdmonitor.data.repository

import android.content.SharedPreferences
import android.util.Base64
import androidx.security.crypto.MasterKey
import com.app.cicdmonitor.data.database.dao.AuthTokenDao
import com.app.cicdmonitor.data.database.dao.UserDao
import com.app.cicdmonitor.data.models.*
import com.app.cicdmonitor.data.network.api.GitHubApiService
import com.app.cicdmonitor.data.network.api.GitLabApiService
import com.app.cicdmonitor.data.network.api.JenkinsApiService
import com.app.cicdmonitor.di.NetworkModule
import com.app.cicdmonitor.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import java.time.LocalDateTime
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authTokenDao: AuthTokenDao,
    private val userDao: UserDao,
    private val gitHubApiService: GitHubApiService,
    private val gitLabApiService: GitLabApiService,
    @Named("encrypted_prefs") private val encryptedPrefs: SharedPreferences,
    private val masterKey: MasterKey,
    private val okHttpClient: OkHttpClient,
    private val json: Json
) : AuthRepository {

    override suspend fun saveAuthToken(token: AuthToken): Result<Unit> {
        return try {
            // Encrypt and store the token
            encryptAndStoreToken(token.accessToken, token.provider)
            
            // Save token metadata to database
            authTokenDao.insertToken(token.copy(accessToken = "encrypted"))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAuthToken(provider: CiProvider): AuthToken? {
        return try {
            val tokenMetadata = authTokenDao.getTokenByProvider(provider)
            if (tokenMetadata != null) {
                val decryptedToken = decryptToken(provider).getOrNull()
                if (decryptedToken != null) {
                    tokenMetadata.copy(accessToken = decryptedToken)
                } else null
            } else null
        } catch (e: Exception) {
            null
        }
    }

    override fun getAllActiveTokens(): Flow<List<AuthToken>> {
        return authTokenDao.getAllActiveTokens()
    }

    override suspend fun updateAuthToken(token: AuthToken): Result<Unit> {
        return try {
            encryptAndStoreToken(token.accessToken, token.provider)
            authTokenDao.updateToken(token.copy(accessToken = "encrypted"))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteAuthToken(provider: CiProvider): Result<Unit> {
        return try {
            authTokenDao.deleteTokensByProvider(provider)
            encryptedPrefs.edit().remove("token_${provider.name}").apply()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun refreshToken(provider: CiProvider): Result<AuthToken> {
        return try {
            val currentToken = getAuthToken(provider)
            if (currentToken?.refreshToken != null) {
                // TODO: Implement OAuth refresh logic for each provider
                when (provider) {
                    CiProvider.GITHUB_ACTIONS -> refreshGitHubToken(currentToken)
                    CiProvider.GITLAB_CI -> refreshGitLabToken(currentToken)
                    CiProvider.JENKINS -> Result.failure(Exception("Jenkins doesn't support token refresh"))
                }
            } else {
                Result.failure(Exception("No refresh token available"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun validateToken(provider: CiProvider): Result<TokenValidationResult> {
        return try {
            val token = getAuthToken(provider)
            if (token != null) {
                when (provider) {
                    CiProvider.GITHUB_ACTIONS -> validateGitHubToken(token.accessToken)
                    CiProvider.GITLAB_CI -> validateGitLabToken(token.accessToken)
                    CiProvider.JENKINS -> validateJenkinsToken(token)
                }
            } else {
                Result.success(TokenValidationResult(false, null, null, "No token found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun isTokenValid(provider: CiProvider): Boolean {
        return validateToken(provider).getOrNull()?.isValid ?: false
    }

    override suspend fun initiateOAuthFlow(provider: CiProvider, config: AuthConfig): Result<String> {
        return try {
            when (provider) {
                CiProvider.GITHUB_ACTIONS -> {
                    val state = UUID.randomUUID().toString()
                    val authUrl = "https://github.com/login/oauth/authorize" +
                            "?client_id=${config.clientId}" +
                            "&redirect_uri=${config.redirectUri}" +
                            "&scope=repo,workflow" +
                            "&state=$state"
                    Result.success(authUrl)
                }
                CiProvider.GITLAB_CI -> {
                    val state = UUID.randomUUID().toString()
                    val baseUrl = config.serverUrl ?: "https://gitlab.com"
                    val authUrl = "$baseUrl/oauth/authorize" +
                            "?client_id=${config.clientId}" +
                            "&redirect_uri=${config.redirectUri}" +
                            "&response_type=code" +
                            "&scope=api" +
                            "&state=$state"
                    Result.success(authUrl)
                }
                CiProvider.JENKINS -> Result.failure(Exception("Jenkins doesn't support OAuth"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun handleOAuthCallback(provider: CiProvider, code: String, state: String): Result<AuthToken> {
        return try {
            when (provider) {
                CiProvider.GITHUB_ACTIONS -> handleGitHubCallback(code, state)
                CiProvider.GITLAB_CI -> handleGitLabCallback(code, state)
                CiProvider.JENKINS -> Result.failure(Exception("Jenkins doesn't support OAuth"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCurrentUser(provider: CiProvider): Result<User> {
        return try {
            val token = getAuthToken(provider)
            if (token != null) {
                when (provider) {
                    CiProvider.GITHUB_ACTIONS -> getCurrentGitHubUser(token.accessToken)
                    CiProvider.GITLAB_CI -> getCurrentGitLabUser(token.accessToken)
                    CiProvider.JENKINS -> getCurrentJenkinsUser(token)
                }
            } else {
                Result.failure(Exception("No authentication token found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveUser(user: User): Result<Unit> {
        return try {
            userDao.insertUser(user)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserByProvider(provider: CiProvider): User? {
        return userDao.getUserByProvider(provider)
    }

    override suspend fun encryptAndStoreToken(token: String, provider: CiProvider): Result<Unit> {
        return try {
            val keyAlias = "token_key_${provider.name}"
            val encryptedToken = encryptString(token, keyAlias)
            encryptedPrefs.edit().putString("token_${provider.name}", encryptedToken).apply()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun decryptToken(provider: CiProvider): Result<String> {
        return try {
            val encryptedToken = encryptedPrefs.getString("token_${provider.name}", null)
            if (encryptedToken != null) {
                val keyAlias = "token_key_${provider.name}"
                val decryptedToken = decryptString(encryptedToken, keyAlias)
                Result.success(decryptedToken)
            } else {
                Result.failure(Exception("No token found for provider"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun clearAllTokens(): Result<Unit> {
        return try {
            authTokenDao.getAllActiveTokens()
            encryptedPrefs.edit().clear().apply()
            // Clear database tokens
            CiProvider.values().forEach { provider ->
                authTokenDao.deleteTokensByProvider(provider)
                userDao.deleteUsersByProvider(provider)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Private helper methods
    private suspend fun refreshGitHubToken(token: AuthToken): Result<AuthToken> {
        // TODO: Implement GitHub token refresh
        return Result.failure(Exception("GitHub token refresh not implemented"))
    }

    private suspend fun refreshGitLabToken(token: AuthToken): Result<AuthToken> {
        // TODO: Implement GitLab token refresh
        return Result.failure(Exception("GitLab token refresh not implemented"))
    }

    private suspend fun validateGitHubToken(accessToken: String): Result<TokenValidationResult> {
        return try {
            val response = gitHubApiService.getCurrentUser(GitHubApiService.createAuthHeader(accessToken))
            if (response.isSuccessful) {
                Result.success(TokenValidationResult(true, null, listOf("repo", "workflow"), null))
            } else {
                Result.success(TokenValidationResult(false, null, null, "Invalid token"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun validateGitLabToken(accessToken: String): Result<TokenValidationResult> {
        return try {
            val response = gitLabApiService.getCurrentUser(GitLabApiService.createAuthHeader(accessToken))
            if (response.isSuccessful) {
                Result.success(TokenValidationResult(true, null, listOf("api"), null))
            } else {
                Result.success(TokenValidationResult(false, null, null, "Invalid token"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun validateJenkinsToken(token: AuthToken): Result<TokenValidationResult> {
        return try {
            if (token.serverUrl != null && token.username != null) {
                val jenkinsService = NetworkModule.createJenkinsApiService(token.serverUrl, okHttpClient, json)
                val authHeader = JenkinsApiService.createAuthHeader(token.username, token.accessToken)
                val response = jenkinsService.getCurrentUser(authHeader)
                if (response.isSuccessful) {
                    Result.success(TokenValidationResult(true, null, null, null))
                } else {
                    Result.success(TokenValidationResult(false, null, null, "Invalid credentials"))
                }
            } else {
                Result.success(TokenValidationResult(false, null, null, "Missing server URL or username"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun handleGitHubCallback(code: String, state: String): Result<AuthToken> {
        // TODO: Implement GitHub OAuth callback handling
        return Result.failure(Exception("GitHub OAuth callback not implemented"))
    }

    private suspend fun handleGitLabCallback(code: String, state: String): Result<AuthToken> {
        // TODO: Implement GitLab OAuth callback handling
        return Result.failure(Exception("GitLab OAuth callback not implemented"))
    }

    private suspend fun getCurrentGitHubUser(accessToken: String): Result<User> {
        return try {
            val response = gitHubApiService.getCurrentUser(GitHubApiService.createAuthHeader(accessToken))
            if (response.isSuccessful && response.body() != null) {
                val gitHubUser = response.body()!!
                val user = User(
                    id = gitHubUser.id.toString(),
                    provider = CiProvider.GITHUB_ACTIONS,
                    username = gitHubUser.login,
                    displayName = gitHubUser.login,
                    email = null, // GitHub API doesn't return email in user endpoint
                    avatarUrl = gitHubUser.avatarUrl,
                    profileUrl = gitHubUser.htmlUrl,
                    isActive = true,
                    createdAt = LocalDateTime.now().toString(),
                    updatedAt = LocalDateTime.now().toString()
                )
                Result.success(user)
            } else {
                Result.failure(Exception("Failed to get GitHub user"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun getCurrentGitLabUser(accessToken: String): Result<User> {
        return try {
            val response = gitLabApiService.getCurrentUser(GitLabApiService.createAuthHeader(accessToken))
            if (response.isSuccessful && response.body() != null) {
                val gitLabUser = response.body()!!
                val user = User(
                    id = gitLabUser.id.toString(),
                    provider = CiProvider.GITLAB_CI,
                    username = gitLabUser.username,
                    displayName = gitLabUser.name,
                    email = gitLabUser.publicEmail,
                    avatarUrl = gitLabUser.avatarUrl,
                    profileUrl = gitLabUser.webUrl,
                    isActive = true,
                    createdAt = LocalDateTime.now().toString(),
                    updatedAt = LocalDateTime.now().toString()
                )
                Result.success(user)
            } else {
                Result.failure(Exception("Failed to get GitLab user"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun getCurrentJenkinsUser(token: AuthToken): Result<User> {
        return try {
            if (token.serverUrl != null && token.username != null) {
                val jenkinsService = NetworkModule.createJenkinsApiService(token.serverUrl, okHttpClient, json)
                val authHeader = JenkinsApiService.createAuthHeader(token.username, token.accessToken)
                val response = jenkinsService.getCurrentUser(authHeader)
                if (response.isSuccessful && response.body() != null) {
                    val jenkinsUser = response.body()!!
                    val user = User(
                        id = jenkinsUser.id,
                        provider = CiProvider.JENKINS,
                        username = jenkinsUser.id,
                        displayName = jenkinsUser.fullName,
                        email = null,
                        avatarUrl = null,
                        profileUrl = jenkinsUser.absoluteUrl,
                        isActive = true,
                        createdAt = LocalDateTime.now().toString(),
                        updatedAt = LocalDateTime.now().toString()
                    )
                    Result.success(user)
                } else {
                    Result.failure(Exception("Failed to get Jenkins user"))
                }
            } else {
                Result.failure(Exception("Missing Jenkins server URL or username"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun encryptString(plainText: String, keyAlias: String): String {
        val key = SecretKeySpec(keyAlias.toByteArray().take(16).toByteArray(), "AES")
        val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val encryptedBytes = cipher.doFinal(plainText.toByteArray())
        return Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
    }

    private fun decryptString(encryptedText: String, keyAlias: String): String {
        val key = SecretKeySpec(keyAlias.toByteArray().take(16).toByteArray(), "AES")
        val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.DECRYPT_MODE, key)
        val encryptedBytes = Base64.decode(encryptedText, Base64.DEFAULT)
        val decryptedBytes = cipher.doFinal(encryptedBytes)
        return String(decryptedBytes)
    }
}
