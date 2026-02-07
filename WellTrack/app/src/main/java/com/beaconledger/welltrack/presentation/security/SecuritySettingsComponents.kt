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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.beaconledger.welltrack.data.security.PrivacyControlsManager
import com.beaconledger.welltrack.data.security.SecureDataDeletionManager

@Composable
fun PrivacyControlsSettings(
    privacySettings: PrivacyControlsManager.PrivacySettings,
    onUpdatePrivacySettings: (PrivacyControlsManager.PrivacySettings) -> Unit,
    onShowPrivacyDialog: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Data Sharing")
            Switch(
                checked = privacySettings.dataSharingEnabled,
                onCheckedChange = { enabled ->
                    onUpdatePrivacySettings(privacySettings.copy(dataSharingEnabled = enabled))
                }
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Analytics")
            Switch(
                checked = privacySettings.analyticsEnabled,
                onCheckedChange = { enabled ->
                    onUpdatePrivacySettings(privacySettings.copy(analyticsEnabled = enabled))
                }
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Crash Reports")
            Switch(
                checked = privacySettings.crashReportingEnabled,
                onCheckedChange = { enabled ->
                    onUpdatePrivacySettings(privacySettings.copy(crashReportingEnabled = enabled))
                }
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedButton(
            onClick = onShowPrivacyDialog,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Settings, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Advanced Privacy Settings")
        }
    }
}

@Composable
fun DataManagementSettings(
    onExportData: () -> Unit,
    onShowDataDeletionDialog: () -> Unit
) {
    Column {
        OutlinedButton(
            onClick = onExportData,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.CloudDownload, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Export My Data")
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Button(
            onClick = onShowDataDeletionDialog,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            )
        ) {
            Icon(Icons.Default.DeleteForever, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Delete My Data")
        }
    }
}

@Composable
fun AuditLogSettings(
    recentLogsCount: Int,
    onViewAuditLogs: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Security Activity")
                Text(
                    text = "$recentLogsCount recent events",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            TextButton(onClick = onViewAuditLogs) {
                Text("View Logs")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BiometricSetupDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Enable Biometric Authentication") },
        text = {
            Text("Use your fingerprint or face to unlock the app quickly and securely. You can disable this at any time in settings.")
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Enable")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataDeletionDialog(
    onConfirmDeletion: (SecureDataDeletionManager.DataType?) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedDeletionType by remember { mutableStateOf<DeletionType?>(null) }
    var showConfirmation by remember { mutableStateOf(false) }
    
    if (showConfirmation && selectedDeletionType != null) {
        AlertDialog(
            onDismissRequest = { showConfirmation = false },
            title = { Text("Confirm Data Deletion") },
            text = {
                Text(
                    when (selectedDeletionType) {
                        DeletionType.SPECIFIC_DATA -> "This will permanently delete the selected data type. This action cannot be undone."
                        DeletionType.ALL_DATA -> "This will permanently delete ALL your data including meals, recipes, health metrics, and account information. This action cannot be undone."
                        null -> ""
                    }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmation = false
                        when (selectedDeletionType) {
                            DeletionType.SPECIFIC_DATA -> onConfirmDeletion(SecureDataDeletionManager.DataType.MEALS)
                            DeletionType.ALL_DATA -> onConfirmDeletion(null)
                            null -> {}
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    } else {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Delete Data") },
            text = {
                Column {
                    Text("What would you like to delete?")
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedButton(
                        onClick = {
                            selectedDeletionType = DeletionType.SPECIFIC_DATA
                            showConfirmation = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Delete Specific Data Type")
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Button(
                        onClick = {
                            selectedDeletionType = DeletionType.ALL_DATA
                            showConfirmation = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Delete All Data & Account")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacySettingsDialog(
    privacySettings: PrivacyControlsManager.PrivacySettings,
    onUpdateSettings: (PrivacyControlsManager.PrivacySettings) -> Unit,
    onDismiss: () -> Unit
) {
    var currentSettings by remember { mutableStateOf(privacySettings) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Privacy Settings") },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    PrivacySettingItem(
                        title = "Health Data Sharing",
                        description = "Share health metrics with analytics",
                        checked = currentSettings.healthDataSharingEnabled,
                        onCheckedChange = { 
                            currentSettings = currentSettings.copy(healthDataSharingEnabled = it)
                        }
                    )
                }
                
                item {
                    PrivacySettingItem(
                        title = "Meal Data Sharing",
                        description = "Share meal logs for insights",
                        checked = currentSettings.mealDataSharingEnabled,
                        onCheckedChange = { 
                            currentSettings = currentSettings.copy(mealDataSharingEnabled = it)
                        }
                    )
                }
                
                item {
                    PrivacySettingItem(
                        title = "Recipe Sharing",
                        description = "Allow sharing recipes with others",
                        checked = currentSettings.recipeSharingEnabled,
                        onCheckedChange = { 
                            currentSettings = currentSettings.copy(recipeSharingEnabled = it)
                        }
                    )
                }
                
                item {
                    PrivacySettingItem(
                        title = "Social Features",
                        description = "Enable social sharing and collaboration",
                        checked = currentSettings.socialFeaturesEnabled,
                        onCheckedChange = { 
                            currentSettings = currentSettings.copy(socialFeaturesEnabled = it)
                        }
                    )
                }
                
                item {
                    PrivacySettingItem(
                        title = "Third-party Integrations",
                        description = "Allow data sharing with external services",
                        checked = currentSettings.thirdPartyIntegrationsEnabled,
                        onCheckedChange = { 
                            currentSettings = currentSettings.copy(thirdPartyIntegrationsEnabled = it)
                        }
                    )
                }
                
                item {
                    PrivacySettingItem(
                        title = "Marketing Communications",
                        description = "Receive promotional emails and notifications",
                        checked = currentSettings.marketingCommunicationsEnabled,
                        onCheckedChange = { 
                            currentSettings = currentSettings.copy(marketingCommunicationsEnabled = it)
                        }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onUpdateSettings(currentSettings) }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun PrivacySettingItem(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

private enum class DeletionType {
    SPECIFIC_DATA,
    ALL_DATA
}