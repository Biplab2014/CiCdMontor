package com.app.cicdmonitor.domain.repository

import com.app.cicdmonitor.data.models.UserPreferences
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    
    fun getUserPreferences(): Flow<UserPreferences>
    suspend fun updatePollingInterval(intervalMinutes: Int)
    suspend fun updateNotificationSettings(
        enableNotifications: Boolean,
        notifyOnSuccess: Boolean,
        notifyOnFailure: Boolean,
        notifyOnStart: Boolean
    )
    suspend fun updateUISettings(
        enableVibration: Boolean,
        enableSound: Boolean,
        darkMode: Boolean,
        autoRefresh: Boolean
    )
    suspend fun resetToDefaults()
}
