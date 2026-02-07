package com.beaconledger.welltrack.presentation.security

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.beaconledger.welltrack.R
import com.beaconledger.welltrack.data.security.BiometricAuthManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecuritySettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SecuritySettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    
    var showBiometricDialog by remember { mutableStateOf(false) }
    var showDataDeletionDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        viewModel.loadSettings()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Security & Privacy") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // App Lock Section
            item {
                SecuritySection(
                    title = "App Lock",
                    icon = Icons.Default.Lock
                ) {
                    AppLockSettings(
                        isEnabled = uiState.appLockEnabled,
                        timeoutMinutes = uiState.lockTimeoutMinutes,
                        onToggleAppLock = viewModel::toggleAppLock,
                        onTimeoutChanged = viewModel::setLockTimeout
                    )
                }
            }
            
            // Biometric Authentication Section
            item {
                SecuritySection(
                    title = "Biometric Authentication",
                    icon = Icons.Default.Fingerprint
                ) {
                    BiometricSettings(
                        isAvailable = uiState.biometricAvailable,
                        isEnabled = uiState.biometricEnabled,
                        onToggleBiometric = { enabled ->
                            if (enabled) {
                                showBiometricDialog = true
                            } else {
                                viewModel.setBiometricEnabled(false)
                            }
                        }
                    )
                }
            }
            
            // Privacy Controls Section
            item {
                SecuritySection(
                    title = "Privacy Controls",
                    icon = Icons.Default.PrivacyTip
                ) {
                    PrivacyControlsSettings(
                        privacySettings = uiState.privacySettings,
                        onUpdatePrivacySettings = viewModel::updatePrivacySettings,
                        onShowPrivacyDialog = { showPrivacyDialog = true }
                    )
                }
            }
            
            // Data Management Section
            item {
                SecuritySection(
                    title = "Data Management",
                    icon = Icons.Default.Storage
                ) {
                    DataManagementSettings(
                        onExportData = viewModel::exportUserData,
                        onShowDataDeletionDialog = { showDataDeletionDialog = true }
                    )
                }
            }
            
            // Audit Logs Section
            if (uiState.showAuditLogs) {
                item {
                    SecuritySection(
                        title = "Security Audit",
                        icon = Icons.Default.Security
                    ) {
                        AuditLogSettings(
                            recentLogsCount = uiState.recentAuditLogsCount,
                            onViewAuditLogs = viewModel::viewAuditLogs
                        )
                    }
                }
            }
        }
    }
    
    // Biometric Setup Dialog
    if (showBiometricDialog) {
        BiometricSetupDialog(
            onConfirm = {
                showBiometricDialog = false
                viewModel.setupBiometric(context as FragmentActivity)
            },
            onDismiss = { showBiometricDialog = false }
        )
    }
    
    // Data Deletion Dialog
    if (showDataDeletionDialog) {
        DataDeletionDialog(
            onConfirmDeletion = { deleteType ->
                showDataDeletionDialog = false
                viewModel.deleteUserData(deleteType)
            },
            onDismiss = { showDataDeletionDialog = false }
        )
    }
    
    // Privacy Settings Dialog
    if (showPrivacyDialog) {
        PrivacySettingsDialog(
            privacySettings = uiState.privacySettings,
            onUpdateSettings = { settings ->
                showPrivacyDialog = false
                viewModel.updatePrivacySettings(settings)
            },
            onDismiss = { showPrivacyDialog = false }
        )
    }
    
    // Handle UI events
    LaunchedEffect(uiState.message) {
        uiState.message?.let { message ->
            // Show snackbar or toast
            viewModel.clearMessage()
        }
    }
}

@Composable
private fun SecuritySection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            content()
        }
    }
}

@Composable
private fun AppLockSettings(
    isEnabled: Boolean,
    timeoutMinutes: Int,
    onToggleAppLock: (Boolean) -> Unit,
    onTimeoutChanged: (Int) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Enable App Lock")
            Switch(
                checked = isEnabled,
                onCheckedChange = onToggleAppLock
            )
        }
        
        if (isEnabled) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Lock Timeout",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            val timeoutOptions = listOf(1, 2, 5, 10, 15, 30, 60)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = timeoutOptions,
                    key = { it }
                ) { timeout ->
                    FilterChip(
                        selected = timeout == timeoutMinutes,
                        onClick = { onTimeoutChanged(timeout) },
                        label = { Text("${timeout}m") }
                    )
                }
            }
        }
    }
}

@Composable
private fun BiometricSettings(
    isAvailable: Boolean,
    isEnabled: Boolean,
    onToggleBiometric: (Boolean) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Biometric Authentication")
                if (!isAvailable) {
                    Text(
                        text = "Not available on this device",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            Switch(
                checked = isEnabled,
                onCheckedChange = onToggleBiometric,
                enabled = isAvailable
            )
        }
        
        if (isAvailable && isEnabled) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Use fingerprint or face unlock to access the app",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}