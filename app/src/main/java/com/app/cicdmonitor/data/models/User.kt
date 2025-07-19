package com.app.cicdmonitor.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "users")
@Serializable
data class User(
    @PrimaryKey
    val id: String,
    val provider: CiProvider,
    val username: String,
    val displayName: String?,
    val email: String?,
    val avatarUrl: String?,
    val profileUrl: String?,
    val isActive: Boolean = true,
    val createdAt: String,
    val updatedAt: String
)

data class UserPreferences(
    val pollingInterval: Int = 5, // minutes
    val enableNotifications: Boolean = true,
    val notifyOnSuccess: Boolean = false,
    val notifyOnFailure: Boolean = true,
    val notifyOnStart: Boolean = false,
    val enableVibration: Boolean = true,
    val enableSound: Boolean = true,
    val darkMode: Boolean = false,
    val autoRefresh: Boolean = true
)
