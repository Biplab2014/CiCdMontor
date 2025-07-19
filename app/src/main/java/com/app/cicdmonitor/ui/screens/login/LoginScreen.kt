package com.app.cicdmonitor.ui.screens.login

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.cicdmonitor.data.models.CiProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App Logo and Title
        Icon(
            imageVector = Icons.Default.Build,
            contentDescription = "CI/CD Monitor",
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "CI/CD Pipeline Monitor",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = "Monitor your Jenkins, GitHub Actions, and GitLab CI pipelines",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp)
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        if (!uiState.showTokenInput) {
            // Provider Selection Cards
            Text(
                text = "Choose your CI/CD provider",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // GitHub Actions Card
            ProviderCard(
                title = "GitHub Actions",
                description = "Connect with GitHub Personal Access Token",
                onClick = { viewModel.selectProvider(CiProvider.GITHUB_ACTIONS) },
                isLoading = uiState.isLoading && uiState.selectedProvider == CiProvider.GITHUB_ACTIONS
            )

            Spacer(modifier = Modifier.height(12.dp))

            // GitLab CI Card
            ProviderCard(
                title = "GitLab CI",
                description = "Connect with GitLab Personal Access Token",
                onClick = { viewModel.selectProvider(CiProvider.GITLAB_CI) },
                isLoading = uiState.isLoading && uiState.selectedProvider == CiProvider.GITLAB_CI
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Jenkins Card
            ProviderCard(
                title = "Jenkins",
                description = "Connect with Jenkins API Token",
                onClick = { viewModel.selectProvider(CiProvider.JENKINS) },
                isLoading = uiState.isLoading && uiState.selectedProvider == CiProvider.JENKINS
            )
        } else {
            // Token Input Form
            TokenInputForm(
                provider = uiState.selectedProvider!!,
                isLoading = uiState.isLoading,
                onAuthenticate = { token, serverUrl, username ->
                    viewModel.authenticateWithToken(token, serverUrl, username)
                },
                onBack = { viewModel.clearError() }
            )
        }
        
        // Error message
        if (uiState.errorMessage != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = uiState.errorMessage!!,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
    
    // Handle navigation on successful login
    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated) {
            onLoginSuccess()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProviderCard(
    title: String,
    description: String,
    onClick: () -> Unit,
    isLoading: Boolean = false
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        enabled = !isLoading
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TokenInputForm(
    provider: CiProvider,
    isLoading: Boolean,
    onAuthenticate: (token: String, serverUrl: String?, username: String?) -> Unit,
    onBack: () -> Unit
) {
    var token by remember { mutableStateOf("") }
    var serverUrl by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var showToken by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
            Text(
                text = "Connect to ${provider.name.replace("_", " ")}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Jenkins requires additional fields
        if (provider == CiProvider.JENKINS) {
            OutlinedTextField(
                value = serverUrl,
                onValueChange = { serverUrl = it },
                label = { Text("Jenkins Server URL") },
                placeholder = { Text("https://jenkins.example.com") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Token input
        OutlinedTextField(
            value = token,
            onValueChange = { token = it },
            label = {
                Text(when (provider) {
                    CiProvider.GITHUB_ACTIONS -> "Personal Access Token"
                    CiProvider.GITLAB_CI -> "Personal Access Token"
                    CiProvider.JENKINS -> "API Token"
                })
            },
            placeholder = { Text("Enter your token") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = if (showToken) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { showToken = !showToken }) {
                    Text(
                        text = if (showToken) "👁️" else "🙈",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val url = if (provider == CiProvider.JENKINS && serverUrl.isNotBlank()) serverUrl else null
                val user = if (provider == CiProvider.JENKINS && username.isNotBlank()) username else null
                onAuthenticate(token, url, user)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading && token.isNotBlank() &&
                     (provider != CiProvider.JENKINS || (serverUrl.isNotBlank() && username.isNotBlank()))
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text("Connect")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Help text
        Text(
            text = when (provider) {
                CiProvider.GITHUB_ACTIONS -> "Create a Personal Access Token in GitHub Settings → Developer settings → Personal access tokens. Required scopes: repo, workflow"
                CiProvider.GITLAB_CI -> "Create a Personal Access Token in GitLab User Settings → Access Tokens. Required scopes: api, read_repository"
                CiProvider.JENKINS -> "Generate an API Token in Jenkins User Settings → Configure → API Token"
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
