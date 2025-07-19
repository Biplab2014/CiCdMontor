package com.app.cicdmonitor.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.cicdmonitor.data.models.UserPreferences
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SettingsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadPreferences()
    }

    private fun loadPreferences() {
        viewModelScope.launch {
            // TODO: Load preferences from repository
            // For now, use default preferences
            _uiState.value = _uiState.value.copy(
                preferences = UserPreferences(),
                errorMessage = null
            )
        }
    }

    fun updatePollingInterval(intervalMinutes: Int) {
        viewModelScope.launch {
            // TODO: Update polling interval in repository
            val current = _uiState.value.preferences
            _uiState.value = _uiState.value.copy(
                preferences = current.copy(pollingInterval = intervalMinutes)
            )
        }
    }

    fun updateNotificationsEnabled(enabled: Boolean) {
        val current = _uiState.value.preferences
        _uiState.value = _uiState.value.copy(
            preferences = current.copy(enableNotifications = enabled)
        )
    }

    fun updateNotifyOnSuccess(enabled: Boolean) {
        val current = _uiState.value.preferences
        _uiState.value = _uiState.value.copy(
            preferences = current.copy(notifyOnSuccess = enabled)
        )
    }

    fun updateNotifyOnFailure(enabled: Boolean) {
        val current = _uiState.value.preferences
        _uiState.value = _uiState.value.copy(
            preferences = current.copy(notifyOnFailure = enabled)
        )
    }

    fun updateNotifyOnStart(enabled: Boolean) {
        val current = _uiState.value.preferences
        _uiState.value = _uiState.value.copy(
            preferences = current.copy(notifyOnStart = enabled)
        )
    }

    fun updateVibration(enabled: Boolean) {
        val current = _uiState.value.preferences
        _uiState.value = _uiState.value.copy(
            preferences = current.copy(enableVibration = enabled)
        )
    }

    fun updateSound(enabled: Boolean) {
        val current = _uiState.value.preferences
        _uiState.value = _uiState.value.copy(
            preferences = current.copy(enableSound = enabled)
        )
    }

    fun updateDarkMode(enabled: Boolean) {
        val current = _uiState.value.preferences
        _uiState.value = _uiState.value.copy(
            preferences = current.copy(darkMode = enabled)
        )
    }

    fun updateAutoRefresh(enabled: Boolean) {
        val current = _uiState.value.preferences
        _uiState.value = _uiState.value.copy(
            preferences = current.copy(autoRefresh = enabled)
        )
    }

    fun resetToDefaults() {
        _uiState.value = _uiState.value.copy(
            preferences = UserPreferences()
        )
    }

    fun logout() {
        // TODO: Implement logout logic
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

data class SettingsUiState(
    val preferences: UserPreferences = UserPreferences(),
    val errorMessage: String? = null
)
