package com.app.cicdmonitor.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "auth_tokens")
@Serializable
data class AuthToken(
    @PrimaryKey
    val id: String,
    val provider: CiProvider,
    val tokenType: TokenType,
    val accessToken: String,
    val refreshToken: String?,
    val expiresAt: String?, // ISO 8601 format
    val scope: String?,
    val serverUrl: String?, // For Jenkins and self-hosted GitLab
    val username: String?, // For Jenkins basic auth
    val isActive: Boolean = true,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
enum class TokenType {
    OAUTH2,
    PERSONAL_ACCESS_TOKEN,
    API_KEY
}

data class AuthConfig(
    val provider: CiProvider,
    val serverUrl: String? = null, // For Jenkins and self-hosted GitLab
    val clientId: String? = null, // For OAuth2
    val clientSecret: String? = null, // For OAuth2
    val redirectUri: String? = null // For OAuth2
)

data class TokenValidationResult(
    val isValid: Boolean,
    val expiresAt: String?,
    val scopes: List<String>?,
    val error: String?
)
