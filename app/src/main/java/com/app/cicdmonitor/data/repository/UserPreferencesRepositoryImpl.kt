package com.app.cicdmonitor.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import com.app.cicdmonitor.data.models.UserPreferences
import com.app.cicdmonitor.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferencesRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : UserPreferencesRepository {

    private object PreferencesKeys {
        val POLLING_INTERVAL = intPreferencesKey("polling_interval")
        val ENABLE_NOTIFICATIONS = booleanPreferencesKey("enable_notifications")
        val NOTIFY_ON_SUCCESS = booleanPreferencesKey("notify_on_success")
        val NOTIFY_ON_FAILURE = booleanPreferencesKey("notify_on_failure")
        val NOTIFY_ON_START = booleanPreferencesKey("notify_on_start")
        val ENABLE_VIBRATION = booleanPreferencesKey("enable_vibration")
        val ENABLE_SOUND = booleanPreferencesKey("enable_sound")
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val AUTO_REFRESH = booleanPreferencesKey("auto_refresh")
    }

    override fun getUserPreferences(): Flow<UserPreferences> {
        return dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                UserPreferences(
                    pollingInterval = preferences[PreferencesKeys.POLLING_INTERVAL] ?: 5,
                    enableNotifications = preferences[PreferencesKeys.ENABLE_NOTIFICATIONS] ?: true,
                    notifyOnSuccess = preferences[PreferencesKeys.NOTIFY_ON_SUCCESS] ?: false,
                    notifyOnFailure = preferences[PreferencesKeys.NOTIFY_ON_FAILURE] ?: true,
                    notifyOnStart = preferences[PreferencesKeys.NOTIFY_ON_START] ?: false,
                    enableVibration = preferences[PreferencesKeys.ENABLE_VIBRATION] ?: true,
                    enableSound = preferences[PreferencesKeys.ENABLE_SOUND] ?: true,
                    darkMode = preferences[PreferencesKeys.DARK_MODE] ?: false,
                    autoRefresh = preferences[PreferencesKeys.AUTO_REFRESH] ?: true
                )
            }
    }

    override suspend fun updatePollingInterval(intervalMinutes: Int) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.POLLING_INTERVAL] = intervalMinutes.coerceIn(1, 60)
        }
    }

    override suspend fun updateNotificationSettings(
        enableNotifications: Boolean,
        notifyOnSuccess: Boolean,
        notifyOnFailure: Boolean,
        notifyOnStart: Boolean
    ) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.ENABLE_NOTIFICATIONS] = enableNotifications
            preferences[PreferencesKeys.NOTIFY_ON_SUCCESS] = notifyOnSuccess
            preferences[PreferencesKeys.NOTIFY_ON_FAILURE] = notifyOnFailure
            preferences[PreferencesKeys.NOTIFY_ON_START] = notifyOnStart
        }
    }

    override suspend fun updateUISettings(
        enableVibration: Boolean,
        enableSound: Boolean,
        darkMode: Boolean,
        autoRefresh: Boolean
    ) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.ENABLE_VIBRATION] = enableVibration
            preferences[PreferencesKeys.ENABLE_SOUND] = enableSound
            preferences[PreferencesKeys.DARK_MODE] = darkMode
            preferences[PreferencesKeys.AUTO_REFRESH] = autoRefresh
        }
    }

    override suspend fun resetToDefaults() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
