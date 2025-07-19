package com.app.cicdmonitor.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onLogoutClick: () -> Unit,
    viewModel: SettingsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Sync Settings
            SettingsSection(title = "Sync Settings") {
                SettingsSlider(
                    title = "Polling Interval",
                    subtitle = "${uiState.preferences.pollingInterval} minutes",
                    value = uiState.preferences.pollingInterval.toFloat(),
                    valueRange = 1f..60f,
                    onValueChange = { viewModel.updatePollingInterval(it.toInt()) }
                )

                SettingsSwitch(
                    title = "Auto Refresh",
                    subtitle = "Automatically refresh pipeline data",
                    checked = uiState.preferences.autoRefresh,
                    onCheckedChange = { viewModel.updateAutoRefresh(it) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Notification Settings
            SettingsSection(title = "Notifications") {
                SettingsSwitch(
                    title = "Enable Notifications",
                    subtitle = "Receive notifications for pipeline events",
                    checked = uiState.preferences.enableNotifications,
                    onCheckedChange = { viewModel.updateNotificationsEnabled(it) }
                )

                if (uiState.preferences.enableNotifications) {
                    SettingsSwitch(
                        title = "Success Notifications",
                        subtitle = "Notify when builds succeed",
                        checked = uiState.preferences.notifyOnSuccess,
                        onCheckedChange = { viewModel.updateNotifyOnSuccess(it) }
                    )

                    SettingsSwitch(
                        title = "Failure Notifications",
                        subtitle = "Notify when builds fail",
                        checked = uiState.preferences.notifyOnFailure,
                        onCheckedChange = { viewModel.updateNotifyOnFailure(it) }
                    )

                    SettingsSwitch(
                        title = "Start Notifications",
                        subtitle = "Notify when builds start",
                        checked = uiState.preferences.notifyOnStart,
                        onCheckedChange = { viewModel.updateNotifyOnStart(it) }
                    )

                    SettingsSwitch(
                        title = "Vibration",
                        subtitle = "Vibrate on notifications",
                        checked = uiState.preferences.enableVibration,
                        onCheckedChange = { viewModel.updateVibration(it) }
                    )

                    SettingsSwitch(
                        title = "Sound",
                        subtitle = "Play sound on notifications",
                        checked = uiState.preferences.enableSound,
                        onCheckedChange = { viewModel.updateSound(it) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // UI Settings
            SettingsSection(title = "Appearance") {
                SettingsSwitch(
                    title = "Dark Mode",
                    subtitle = "Use dark theme",
                    checked = uiState.preferences.darkMode,
                    onCheckedChange = { viewModel.updateDarkMode(it) }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Actions
            Button(
                onClick = { viewModel.resetToDefaults() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Text("Reset to Defaults")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    viewModel.logout()
                    onLogoutClick()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Logout")
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        content()
    }
}

@Composable
private fun SettingsSwitch(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun SettingsSlider(
    title: String,
    subtitle: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = (valueRange.endInclusive - valueRange.start).toInt() - 1
        )
    }
}
