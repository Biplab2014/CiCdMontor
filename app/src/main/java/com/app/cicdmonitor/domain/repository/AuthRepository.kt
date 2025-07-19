package com.app.cicdmonitor.domain.repository

import com.app.cicdmonitor.data.models.*
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    
    // Token management
    suspend fun saveAuthToken(token: AuthToken): Result<Unit>
    suspend fun getAuthToken(provider: CiProvider): AuthToken?
    fun getAllActiveTokens(): Flow<List<AuthToken>>
    suspend fun updateAuthToken(token: AuthToken): Result<Unit>
    suspend fun deleteAuthToken(provider: CiProvider): Result<Unit>
    suspend fun refreshToken(provider: CiProvider): Result<AuthToken>
    
    // Token validation
    suspend fun validateToken(provider: CiProvider): Result<TokenValidationResult>
    suspend fun isTokenValid(provider: CiProvider): Boolean
    
    // OAuth operations
    suspend fun initiateOAuthFlow(provider: CiProvider, config: AuthConfig): Result<String>
    suspend fun handleOAuthCallback(provider: CiProvider, code: String, state: String): Result<AuthToken>
    
    // User operations
    suspend fun getCurrentUser(provider: CiProvider): Result<User>
    suspend fun saveUser(user: User): Result<Unit>
    suspend fun getUserByProvider(provider: CiProvider): User?
    
    // Security operations
    suspend fun encryptAndStoreToken(token: String, provider: CiProvider): Result<Unit>
    suspend fun decryptToken(provider: CiProvider): Result<String>
    suspend fun clearAllTokens(): Result<Unit>
}
