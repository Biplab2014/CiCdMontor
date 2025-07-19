package com.app.cicdmonitor.domain.usecase

import com.app.cicdmonitor.data.models.*
import com.app.cicdmonitor.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    
    suspend fun authenticateWithPAT(
        provider: CiProvider,
        token: String,
        serverUrl: String? = null,
        username: String? = null
    ): Result<User> {
        return try {
            // Create auth token
            val authToken = AuthToken(
                id = UUID.randomUUID().toString(),
                provider = provider,
                tokenType = TokenType.PERSONAL_ACCESS_TOKEN,
                accessToken = token,
                refreshToken = null,
                expiresAt = null,
                scope = null,
                serverUrl = serverUrl,
                username = username,
                isActive = true,
                createdAt = LocalDateTime.now().toString(),
                updatedAt = LocalDateTime.now().toString()
            )
            
            // Validate token by getting current user
            val userResult = authRepository.getCurrentUser(provider)
            if (userResult.isSuccess) {
                val user = userResult.getOrThrow()
                
                // Save token and user
                authRepository.saveAuthToken(authToken).getOrThrow()
                authRepository.saveUser(user).getOrThrow()
                
                Result.success(user)
            } else {
                Result.failure(userResult.exceptionOrNull() ?: Exception("Authentication failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun authenticateWithOAuth(
        provider: CiProvider,
        config: AuthConfig
    ): Result<String> {
        return authRepository.initiateOAuthFlow(provider, config)
    }
    
    suspend fun handleOAuthCallback(
        provider: CiProvider,
        code: String,
        state: String
    ): Result<User> {
        return try {
            val tokenResult = authRepository.handleOAuthCallback(provider, code, state)
            if (tokenResult.isSuccess) {
                val user = authRepository.getCurrentUser(provider).getOrThrow()
                authRepository.saveUser(user).getOrThrow()
                Result.success(user)
            } else {
                Result.failure(tokenResult.exceptionOrNull() ?: Exception("OAuth callback failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun isAuthenticated(provider: CiProvider): Boolean {
        return authRepository.isTokenValid(provider)
    }
    
    suspend fun isAnyProviderAuthenticated(): Boolean {
        return CiProvider.values().any { provider ->
            authRepository.isTokenValid(provider)
        }
    }
    
    fun getActiveTokens(): Flow<List<AuthToken>> {
        return authRepository.getAllActiveTokens()
    }
    
    suspend fun logout(provider: CiProvider): Result<Unit> {
        return authRepository.deleteAuthToken(provider)
    }
    
    suspend fun logoutAll(): Result<Unit> {
        return authRepository.clearAllTokens()
    }
    
    suspend fun refreshTokenIfNeeded(provider: CiProvider): Result<Unit> {
        return try {
            val token = authRepository.getAuthToken(provider)
            if (token != null && token.refreshToken != null) {
                // Check if token needs refresh (simplified logic)
                val validationResult = authRepository.validateToken(provider).getOrNull()
                if (validationResult?.isValid == false) {
                    authRepository.refreshToken(provider).getOrThrow()
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getCurrentUser(provider: CiProvider): Result<User> {
        return authRepository.getCurrentUser(provider)
    }
    
    suspend fun getStoredUser(provider: CiProvider): User? {
        return authRepository.getUserByProvider(provider)
    }
}
