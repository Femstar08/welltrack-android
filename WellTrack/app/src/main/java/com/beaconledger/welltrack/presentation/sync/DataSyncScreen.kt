package com.beaconledger.welltrack.presentation.sync

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
import androidx.hilt.navigation.compose.hiltViewModel
import com.beaconledger.welltrack.domain.repository.ConflictResolution
import com.beaconledger.welltrack.data.backup.ExportFormat
import com.beaconledger.welltrack.domain.usecase.SyncStatusType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataSyncScreen(
    viewModel: DataSyncViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Show messages and errors
    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            // In a real app, you'd show a snackbar here
            viewModel.clearMessage()
        }
    }
    
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            // In a real app, you'd show an error snackbar here
            viewModel.clearError()
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "Data Synchronization",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Sync Status Card
        uiState.syncSummary?.let { summary ->
            SyncStatusCard(
                summary = summary,
                onSyncClick = { viewModel.performFullSync() },
                isLoading = uiState.isLoading
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Sync Statistics
        uiState.syncStats?.let { stats ->
            SyncStatsCard(stats = stats)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Backup and Export Section
        BackupExportSection(
            onCreateBackup = { viewModel.createBackup("current_user_id") },
            onExportData = { format -> viewModel.exportData("current_user_id", format) },
            isLoading = uiState.isLoading
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Conflicts Section
        if (uiState.conflicts.isNotEmpty()) {
            ConflictsSection(
                conflicts = uiState.conflicts,
                onResolveConflict = { conflictId, resolution ->
                    viewModel.resolveConflict(conflictId, resolution)
                }
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Cache Management
        CacheManagementSection(
            onClearCache = { viewModel.clearCache() },
            isLoading = uiState.isLoading
        )
    }
}

@Composable
private fun SyncStatusCard(
    summary: com.beaconledger.welltrack.domain.usecase.SyncSummary,
    onSyncClick: () -> Unit,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Sync Status",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (summary.isOnline) Icons.Default.Cloud else Icons.Default.CloudOff,
                            contentDescription = null,
                            tint = if (summary.isOnline) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (summary.isOnline) "Online" else "Offline",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    
                    Text(
                        text = when (summary.syncStatus) {
                            SyncStatusType.SYNCED -> "All data synchronized"
                            SyncStatusType.PENDING -> "${summary.totalPendingItems} items pending"
                            SyncStatusType.CONFLICTS -> "${summary.totalPendingItems} conflicts to resolve"
                            SyncStatusType.FAILED -> "Sync failed"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Button(
                    onClick = onSyncClick,
                    enabled = !isLoading && summary.isOnline
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sync Now")
                }
            }
        }
    }
}

@Composable
private fun SyncStatsCard(
    stats: com.beaconledger.welltrack.data.model.SyncStats
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Sync Statistics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem("Pending Upload", stats.pendingUpload.toString())
                StatItem("Pending Download", stats.pendingDownload.toString())
                StatItem("Conflicts", stats.conflicts.toString())
                StatItem("Failed", stats.failed.toString())
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun BackupExportSection(
    onCreateBackup: () -> Unit,
    onExportData: (ExportFormat) -> Unit,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Backup & Export",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onCreateBackup,
                    enabled = !isLoading,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudUpload,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Backup")
                }
                
                OutlinedButton(
                    onClick = { onExportData(ExportFormat.JSON) },
                    enabled = !isLoading,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudDownload,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Export")
                }
            }
        }
    }
}

@Composable
private fun ConflictsSection(
    conflicts: List<com.beaconledger.welltrack.data.model.SyncConflict>,
    onResolveConflict: (String, ConflictResolution) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Sync Conflicts (${conflicts.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            LazyColumn(
                modifier = Modifier.heightIn(max = 200.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(conflicts) { conflict ->
                    ConflictItem(
                        conflict = conflict,
                        onResolve = { resolution ->
                            onResolveConflict(conflict.entityId, resolution)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ConflictItem(
    conflict: com.beaconledger.welltrack.data.model.SyncConflict,
    onResolve: (ConflictResolution) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = "${conflict.entityType} Conflict",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "Local version: ${conflict.localVersion}, Cloud version: ${conflict.cloudVersion}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(
                    onClick = { onResolve(ConflictResolution.USE_LOCAL) }
                ) {
                    Text("Use Local")
                }
                
                TextButton(
                    onClick = { onResolve(ConflictResolution.USE_CLOUD) }
                ) {
                    Text("Use Cloud")
                }
            }
        }
    }
}

@Composable
private fun CacheManagementSection(
    onClearCache: () -> Unit,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Cache Management",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            OutlinedButton(
                onClick = onClearCache,
                enabled = !isLoading,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Clear Cache")
            }
        }
    }
}