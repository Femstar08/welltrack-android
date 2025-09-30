package com.beaconledger.welltrack.presentation.dataexport

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.beaconledger.welltrack.data.model.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataExportScreen(
    userId: String,
    viewModel: DataExportViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val exportHistory by viewModel.exportHistory.collectAsState()
    val context = LocalContext.current
    
    var showExportDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var showComplianceDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(userId) {
        viewModel.loadExportHistory(userId)
    }
    
    // Handle success messages
    uiState.showSuccessMessage?.let { message ->
        LaunchedEffect(message) {
            // Show snackbar or toast
            viewModel.clearSuccessMessage()
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "Data Export & Import",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        // Quick Actions
        QuickActionsSection(
            onExportAll = { viewModel.exportAllDataAsJson(userId) },
            onExportHealthReport = { viewModel.exportHealthReportAsPdf(userId) },
            onShowExportDialog = { showExportDialog = true },
            onShowImportDialog = { showImportDialog = true },
            onShowComplianceDialog = { showComplianceDialog = true },
            isLoading = uiState.isLoading
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Export History
        ExportHistorySection(
            exports = exportHistory,
            onCancelExport = viewModel::cancelExport,
            onDeleteExport = viewModel::deleteExport
        )
        
        // Loading indicator
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        
        // Error display
        uiState.error?.let { error ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(onClick = viewModel::clearError) {
                        Text("Dismiss")
                    }
                }
            }
        }
    }
    
    // Dialogs
    if (showExportDialog) {
        ExportDialog(
            onDismiss = { showExportDialog = false },
            onExport = { request ->
                viewModel.createExport(request)
                showExportDialog = false
            },
            userId = userId
        )
    }
    
    if (showImportDialog) {
        ImportDialog(
            onDismiss = { showImportDialog = false },
            onImport = { request ->
                viewModel.importData(request)
                showImportDialog = false
            },
            onValidateFile = viewModel::validateImportFile,
            uiState = uiState,
            userId = userId
        )
    }
    
    if (showComplianceDialog) {
        ComplianceDialog(
            onDismiss = { showComplianceDialog = false },
            onGdprExport = { 
                viewModel.generateGdprExport(userId)
                showComplianceDialog = false
            },
            onCcpaExport = { 
                viewModel.generateCcpaExport(userId)
                showComplianceDialog = false
            },
            onRequestDeletion = { days ->
                viewModel.requestDataDeletion(userId, days)
                showComplianceDialog = false
            }
        )
    }
    
    // Import Preview Dialog
    if (uiState.showImportPreview && uiState.importPreview != null) {
        ImportPreviewDialog(
            preview = uiState.importPreview!!,
            onDismiss = viewModel::hideImportPreview,
            onConfirm = { mergeStrategy ->
                // Create import request based on preview
                viewModel.hideImportPreview()
            }
        )
    }
}

@Composable
private fun QuickActionsSection(
    onExportAll: () -> Unit,
    onExportHealthReport: () -> Unit,
    onShowExportDialog: () -> Unit,
    onShowImportDialog: () -> Unit,
    onShowComplianceDialog: () -> Unit,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onExportAll,
                    enabled = !isLoading,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Export All")
                }
                
                OutlinedButton(
                    onClick = onExportHealthReport,
                    enabled = !isLoading,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("PDF Report")
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onShowExportDialog,
                    enabled = !isLoading,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Custom Export")
                }
                
                OutlinedButton(
                    onClick = onShowImportDialog,
                    enabled = !isLoading,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Upload,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Import Data")
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedButton(
                onClick = onShowComplianceDialog,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Data Rights & Compliance")
            }
        }
    }
}

@Composable
private fun ExportHistorySection(
    exports: List<DataExport>,
    onCancelExport: (String) -> Unit,
    onDeleteExport: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Export History",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            if (exports.isEmpty()) {
                Text(
                    text = "No exports yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.heightIn(max = 300.dp)
                ) {
                    items(exports) { export ->
                        ExportHistoryItem(
                            export = export,
                            onCancel = { onCancelExport(export.id) },
                            onDelete = { onDeleteExport(export.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExportHistoryItem(
    export: DataExport,
    onCancel: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${export.exportType.name} (${export.format.name})",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = export.createdAt.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                ExportStatusChip(status = export.status)
            }
            
            if (export.status == ExportStatus.COMPLETED && export.fileSize != null) {
                Text(
                    text = "Size: ${formatFileSize(export.fileSize)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            if (export.errorMessage != null) {
                Text(
                    text = "Error: ${export.errorMessage}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                when (export.status) {
                    ExportStatus.PENDING, ExportStatus.IN_PROGRESS -> {
                        TextButton(onClick = onCancel) {
                            Text("Cancel")
                        }
                    }
                    ExportStatus.COMPLETED, ExportStatus.FAILED, ExportStatus.CANCELLED -> {
                        TextButton(onClick = onDelete) {
                            Text("Delete")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExportStatusChip(status: ExportStatus) {
    val (color, text) = when (status) {
        ExportStatus.PENDING -> MaterialTheme.colorScheme.secondary to "Pending"
        ExportStatus.IN_PROGRESS -> MaterialTheme.colorScheme.primary to "In Progress"
        ExportStatus.COMPLETED -> MaterialTheme.colorScheme.tertiary to "Completed"
        ExportStatus.FAILED -> MaterialTheme.colorScheme.error to "Failed"
        ExportStatus.CANCELLED -> MaterialTheme.colorScheme.outline to "Cancelled"
    }
    
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.padding(4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

private fun formatFileSize(bytes: Long): String {
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    val gb = mb / 1024.0
    
    return when {
        gb >= 1 -> "%.1f GB".format(gb)
        mb >= 1 -> "%.1f MB".format(mb)
        kb >= 1 -> "%.1f KB".format(kb)
        else -> "$bytes B"
    }
}