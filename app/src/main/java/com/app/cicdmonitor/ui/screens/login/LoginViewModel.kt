package com.app.cicdmonitor.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.cicdmonitor.data.models.CiProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
    
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()
    
    fun selectProvider(provider: CiProvider) {
        _uiState.value = _uiState.value.copy(
            selectedProvider = provider,
            showTokenInput = true,
            errorMessage = null
        )
    }

    fun authenticateWithToken(token: String, serverUrl: String? = null, username: String? = null) {
        val provider = _uiState.value.selectedProvider ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            // TODO: Implement actual authentication logic
            kotlinx.coroutines.delay(1000) // Simulate network call

            // For demo purposes, always succeed
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                isAuthenticated = true
            )
        }
    }

    fun checkExistingAuthentication() {
        viewModelScope.launch {
            // TODO: Check if user is already authenticated
            // For now, always require login
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            showTokenInput = false,
            selectedProvider = null
        )
    }
}

data class LoginUiState(
    val selectedProvider: CiProvider? = null,
    val showTokenInput: Boolean = false,
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val errorMessage: String? = null
)
