package com.beaconledger.welltrack.presentation.dataexport

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.domain.repository.ImportPreview
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportDialog(
    onDismiss: () -> Unit,
    onExport: (ExportRequest) -> Unit,
    userId: String
) {
    var exportType by remember { mutableStateOf(ExportType.FULL_BACKUP) }
    var exportFormat by remember { mutableStateOf(ExportFormat.JSON) }
    var showPdfPreview by remember { mutableStateOf(false) }
    var includeHealthData by remember { mutableStateOf(true) }
    var includeMealData by remember { mutableStateOf(true) }
    var includeSupplementData by remember { mutableStateOf(true) }
    var includeBiomarkerData by remember { mutableStateOf(true) }
    var includeGoalData by remember { mutableStateOf(true) }
    var useDateRange by remember { mutableStateOf(false) }
    var startDate by remember { mutableStateOf(LocalDateTime.now().minusMonths(3)) }
    var endDate by remember { mutableStateOf(LocalDateTime.now()) }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Custom Export",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // Export Type Selection
                Text(
                    text = "Export Type",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Column(modifier = Modifier.selectableGroup()) {
                    ExportType.values().forEach { type ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = (exportType == type),
                                    onClick = { exportType = type },
                                    role = Role.RadioButton
                                )
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (exportType == type),
                                onClick = null
                            )
                            Text(
                                text = type.name.replace("_", " "),
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Format Selection
                Text(
                    text = "Export Format",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ExportFormat.values().forEach { format ->
                        FilterChip(
                            onClick = { exportFormat = format },
                            label = { Text(format.name) },
                            selected = exportFormat == format,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // PDF Preview Option
                if (exportFormat == ExportFormat.PDF) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { showPdfPreview = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Preview,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Preview PDF Report")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Data Selection
                Text(
                    text = "Include Data Types",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = includeHealthData,
                            onCheckedChange = { includeHealthData = it }
                        )
                        Text("Health Data", modifier = Modifier.padding(start = 8.dp))
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = includeMealData,
                            onCheckedChange = { includeMealData = it }
                        )
                        Text("Meal Data", modifier = Modifier.padding(start = 8.dp))
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = includeSupplementData,
                            onCheckedChange = { includeSupplementData = it }
                        )
                        Text("Supplement Data", modifier = Modifier.padding(start = 8.dp))
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = includeBiomarkerData,
                            onCheckedChange = { includeBiomarkerData = it }
                        )
                        Text("Biomarker Data", modifier = Modifier.padding(start = 8.dp))
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = includeGoalData,
                            onCheckedChange = { includeGoalData = it }
                        )
                        Text("Goal Data", modifier = Modifier.padding(start = 8.dp))
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Date Range Selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = useDateRange,
                        onCheckedChange = { useDateRange = it }
                    )
                    Text("Use Date Range", modifier = Modifier.padding(start = 8.dp))
                }
                
                if (useDateRange) {
                    // Date range pickers would go here
                    // For now, showing text representation
                    Text(
                        text = "From: ${startDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))}",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 32.dp, top = 4.dp)
                    )
                    Text(
                        text = "To: ${endDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))}",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 32.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val request = ExportRequest(
                                userId = userId,
                                exportType = exportType,
                                format = exportFormat,
                                dateRange = if (useDateRange) DateRange(startDate, endDate) else null,
                                includeHealthData = includeHealthData,
                                includeMealData = includeMealData,
                                includeSupplementData = includeSupplementData,
                                includeBiomarkerData = includeBiomarkerData,
                                includeGoalData = includeGoalData
                            )
                            onExport(request)
                        }
                    ) {
                        Text("Export")
                    }
                }
            }
        }
    }
}

@Composable
fun ImportDialog(
    onDismiss: () -> Unit,
    onImport: (ImportRequest) -> Unit,
    onValidateFile: (String, ImportDataType) -> Unit,
    uiState: DataExportUiState,
    userId: String
) {
    var filePath by remember { mutableStateOf("") }
    var sourceApp by remember { mutableStateOf("") }
    var dataType by remember { mutableStateOf(ImportDataType.HEALTH_DATA) }
    var mergeStrategy by remember { mutableStateOf(MergeStrategy.MERGE_NEW_ONLY) }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Import Data",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // File Selection
                OutlinedTextField(
                    value = filePath,
                    onValueChange = { filePath = it },
                    label = { Text("File Path") },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { /* Open file picker */ }) {
                            Icon(Icons.Default.Folder, contentDescription = "Browse")
                        }
                    }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Source App
                OutlinedTextField(
                    value = sourceApp,
                    onValueChange = { sourceApp = it },
                    label = { Text("Source App (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Data Type Selection
                Text(
                    text = "Data Type",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Column(modifier = Modifier.selectableGroup()) {
                    ImportDataType.values().forEach { type ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = (dataType == type),
                                    onClick = { dataType = type },
                                    role = Role.RadioButton
                                )
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (dataType == type),
                                onClick = null
                            )
                            Text(
                                text = type.name.replace("_", " "),
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Merge Strategy
                Text(
                    text = "Merge Strategy",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Column(modifier = Modifier.selectableGroup()) {
                    MergeStrategy.values().forEach { strategy ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = (mergeStrategy == strategy),
                                    onClick = { mergeStrategy = strategy },
                                    role = Role.RadioButton
                                )
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (mergeStrategy == strategy),
                                onClick = null
                            )
                            Text(
                                text = strategy.name.replace("_", " "),
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
                
                // Import Error
                uiState.importError?.let { error ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    if (filePath.isNotBlank()) {
                        OutlinedButton(
                            onClick = { onValidateFile(filePath, dataType) },
                            enabled = !uiState.isValidatingImport
                        ) {
                            if (uiState.isValidatingImport) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Validate")
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    
                    Button(
                        onClick = {
                            val request = ImportRequest(
                                userId = userId,
                                sourceApp = sourceApp.ifBlank { "Unknown" },
                                filePath = filePath,
                                dataType = dataType,
                                mergeStrategy = mergeStrategy
                            )
                            onImport(request)
                        },
                        enabled = filePath.isNotBlank() && !uiState.isImporting
                    ) {
                        if (uiState.isImporting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Import")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ComplianceDialog(
    onDismiss: () -> Unit,
    onGdprExport: () -> Unit,
    onCcpaExport: () -> Unit,
    onRequestDeletion: (Int) -> Unit
) {
    var deletionDays by remember { mutableStateOf(30) }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Data Rights & Compliance",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Text(
                    text = "Export your data in compliance with privacy regulations:",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // GDPR Export
                OutlinedButton(
                    onClick = onGdprExport,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Security, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generate GDPR Export")
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // CCPA Export
                OutlinedButton(
                    onClick = onCcpaExport,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Security, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generate CCPA Export")
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "Data Deletion Request",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Text(
                    text = "Request deletion of all your data. This action cannot be undone.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Deletion delay:")
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = deletionDays.toString(),
                        onValueChange = { 
                            it.toIntOrNull()?.let { days -> deletionDays = days }
                        },
                        modifier = Modifier.width(80.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("days")
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = { onRequestDeletion(deletionDays) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Request Data Deletion")
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Close")
                    }
                }
            }
        }
    }
}

@Composable
fun ImportPreviewDialog(
    preview: ImportPreview,
    onDismiss: () -> Unit,
    onConfirm: (MergeStrategy) -> Unit
) {
    var mergeStrategy by remember { mutableStateOf(MergeStrategy.MERGE_NEW_ONLY) }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Import Preview",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // Preview Information
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text("Records to import: ${preview.recordCount}")
                        Text("Data types: ${preview.dataTypes.joinToString(", ")}")
                        preview.dateRange?.let { range ->
                            Text("Date range: ${range.startDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))} - ${range.endDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))}")
                        }
                    }
                }
                
                // Conflicts
                if (preview.conflicts.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Conflicts Found",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    // Show conflicts
                }
                
                // Warnings
                if (preview.warnings.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Warnings",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    preview.warnings.forEach { warning ->
                        Text(
                            text = "• $warning",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Merge Strategy Selection
                Text(
                    text = "Merge Strategy",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Column(modifier = Modifier.selectableGroup()) {
                    MergeStrategy.values().forEach { strategy ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = (mergeStrategy == strategy),
                                    onClick = { mergeStrategy = strategy },
                                    role = Role.RadioButton
                                )
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (mergeStrategy == strategy),
                                onClick = null
                            )
                            Text(
                                text = strategy.name.replace("_", " "),
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { onConfirm(mergeStrategy) }) {
                        Text("Confirm Import")
                    }
                }
            }
        }
    }
}