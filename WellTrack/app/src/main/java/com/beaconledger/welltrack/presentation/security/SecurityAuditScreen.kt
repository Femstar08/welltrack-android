package com.beaconledger.welltrack.presentation.security

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.beaconledger.welltrack.data.model.AuditLog
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityAuditScreen(
    onNavigateBack: () -> Unit,
    viewModel: SecurityAuditViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    LaunchedEffect(Unit) {
        viewModel.loadAuditLogs()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Security Audit") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshLogs() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                    IconButton(onClick = { viewModel.showFilterDialog() }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Summary Cards
            if (uiState.summary != null) {
                AuditSummarySection(
                    summary = uiState.summary,
                    modifier = Modifier.padding(16.dp)
                )
            }
            
            // Filter Chips
            if (uiState.activeFilters.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.activeFilters) { filter ->
                        FilterChip(
                            selected = true,
                            onClick = { viewModel.removeFilter(filter) },
                            label = { Text(filter) },
                            trailingIcon = {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Remove filter",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // Audit Logs List
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.auditLogs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.Default.Security,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "No audit logs found",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Security events will appear here",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.auditLogs) { auditLog ->
                        AuditLogItem(
                            auditLog = auditLog,
                            onItemClick = { viewModel.showLogDetails(auditLog) }
                        )
                    }
                }
            }
        }
    }
    
    // Filter Dialog
    if (uiState.showFilterDialog) {
        AuditFilterDialog(
            availableEventTypes = uiState.availableEventTypes,
            selectedEventTypes = uiState.selectedEventTypes,
            dateRange = uiState.dateRange,
            onApplyFilters = { eventTypes, dateRange ->
                viewModel.applyFilters(eventTypes, dateRange)
            },
            onDismiss = { viewModel.hideFilterDialog() }
        )
    }
    
    // Log Details Dialog
    if (uiState.selectedLog != null) {
        AuditLogDetailsDialog(
            auditLog = uiState.selectedLog,
            onDismiss = { viewModel.hideLogDetails() }
        )
    }
    
    // Error handling
    uiState.errorMessage?.let { error ->
        LaunchedEffect(error) {
            // Show snackbar or handle error
            viewModel.clearError()
        }
    }
}

@Composable
private fun AuditSummarySection(
    summary: SecurityAuditViewModel.AuditSummary,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Security Summary (Last 7 Days)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SummaryCard(
                title = "Total Events",
                value = summary.totalEvents.toString(),
                icon = Icons.Default.Event,
                modifier = Modifier.weight(1f)
            )
            
            SummaryCard(
                title = "Login Attempts",
                value = summary.loginAttempts.toString(),
                icon = Icons.Default.Login,
                modifier = Modifier.weight(1f)
            )
            
            SummaryCard(
                title = "Data Access",
                value = summary.dataAccess.toString(),
                icon = Icons.Default.Storage,
                modifier = Modifier.weight(1f)
            )
        }
        
        if (summary.securityAlerts > 0) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${summary.securityAlerts} security alerts require attention",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AuditLogItem(
    auditLog: AuditLog,
    onItemClick: () -> Unit
) {
    Card(
        onClick = onItemClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Event Type Icon
            Icon(
                imageVector = getEventTypeIcon(auditLog.eventType),
                contentDescription = null,
                tint = getEventTypeColor(auditLog.eventType),
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Event Details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = formatEventTitle(auditLog),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = auditLog.timestamp.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                auditLog.additionalInfo?.let { info ->
                    Text(
                        text = info,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            // Severity Indicator
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        color = getEventSeverityColor(auditLog.eventType),
                        shape = RoundedCornerShape(4.dp)
                    )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AuditLogDetailsDialog(
    auditLog: AuditLog,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = getEventTypeIcon(auditLog.eventType),
                    contentDescription = null,
                    tint = getEventTypeColor(auditLog.eventType),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Audit Log Details")
            }
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    DetailRow("Event Type", auditLog.eventType)
                }
                item {
                    DetailRow("Action", auditLog.action)
                }
                item {
                    DetailRow("Resource", auditLog.resourceType)
                }
                auditLog.resourceId?.let { resourceId ->
                    item {
                        DetailRow("Resource ID", resourceId)
                    }
                }
                item {
                    DetailRow(
                        "Timestamp", 
                        auditLog.timestamp.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm:ss"))
                    )
                }
                auditLog.ipAddress?.let { ip ->
                    item {
                        DetailRow("Device Info", ip)
                    }
                }
                auditLog.userAgent?.let { userAgent ->
                    item {
                        DetailRow("User Agent", userAgent)
                    }
                }
                auditLog.additionalInfo?.let { info ->
                    item {
                        DetailRow("Additional Info", info)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun DetailRow(
    label: String,
    value: String
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AuditFilterDialog(
    availableEventTypes: List<String>,
    selectedEventTypes: Set<String>,
    dateRange: Pair<String?, String?>,
    onApplyFilters: (Set<String>, Pair<String?, String?>) -> Unit,
    onDismiss: () -> Unit
) {
    var tempSelectedTypes by remember { mutableStateOf(selectedEventTypes) }
    var tempDateRange by remember { mutableStateOf(dateRange) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter Audit Logs") },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text(
                        text = "Event Types",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                items(availableEventTypes) { eventType ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = eventType in tempSelectedTypes,
                            onCheckedChange = { checked ->
                                tempSelectedTypes = if (checked) {
                                    tempSelectedTypes + eventType
                                } else {
                                    tempSelectedTypes - eventType
                                }
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = eventType.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onApplyFilters(tempSelectedTypes, tempDateRange) }
            ) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun getEventTypeIcon(eventType: String): ImageVector {
    return when (eventType) {
        "LOGIN_SUCCESS", "LOGIN_FAILURE" -> Icons.Default.Login
        "BIOMETRIC_AUTH_SUCCESS", "BIOMETRIC_AUTH_FAILURE" -> Icons.Default.Fingerprint
        "APP_LOCK", "APP_UNLOCK" -> Icons.Default.Lock
        "HEALTH_DATA_READ", "HEALTH_DATA_WRITE", "HEALTH_DATA_DELETE" -> Icons.Default.Health
        "DATA_DELETION", "ACCOUNT_TERMINATION" -> Icons.Default.DeleteForever
        "PRIVACY_SETTINGS_CHANGE", "SECURITY_SETTINGS_CHANGE" -> Icons.Default.Settings
        "SENSITIVE_DATA_ACCESS" -> Icons.Default.Security
        "EXTERNAL_SYNC" -> Icons.Default.Sync
        else -> Icons.Default.Event
    }
}

@Composable
private fun getEventTypeColor(eventType: String): androidx.compose.ui.graphics.Color {
    return when (eventType) {
        "LOGIN_SUCCESS", "BIOMETRIC_AUTH_SUCCESS", "APP_UNLOCK" -> MaterialTheme.colorScheme.primary
        "LOGIN_FAILURE", "BIOMETRIC_AUTH_FAILURE" -> MaterialTheme.colorScheme.error
        "DATA_DELETION", "ACCOUNT_TERMINATION" -> MaterialTheme.colorScheme.error
        "HEALTH_DATA_DELETE", "SENSITIVE_DATA_ACCESS" -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}

@Composable
private fun getEventSeverityColor(eventType: String): androidx.compose.ui.graphics.Color {
    return when (eventType) {
        "LOGIN_FAILURE", "BIOMETRIC_AUTH_FAILURE", "DATA_DELETION", "ACCOUNT_TERMINATION" -> MaterialTheme.colorScheme.error
        "SENSITIVE_DATA_ACCESS", "PRIVACY_SETTINGS_CHANGE", "SECURITY_SETTINGS_CHANGE" -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.primary
    }
}

private fun formatEventTitle(auditLog: AuditLog): String {
    return when (auditLog.eventType) {
        "LOGIN_SUCCESS" -> "Successful login"
        "LOGIN_FAILURE" -> "Failed login attempt"
        "BIOMETRIC_AUTH_SUCCESS" -> "Biometric authentication successful"
        "BIOMETRIC_AUTH_FAILURE" -> "Biometric authentication failed"
        "APP_LOCK" -> "App locked"
        "APP_UNLOCK" -> "App unlocked"
        "HEALTH_DATA_READ" -> "Health data accessed"
        "HEALTH_DATA_WRITE" -> "Health data modified"
        "HEALTH_DATA_DELETE" -> "Health data deleted"
        "DATA_DELETION" -> "Data deletion performed"
        "ACCOUNT_TERMINATION" -> "Account terminated"
        "PRIVACY_SETTINGS_CHANGE" -> "Privacy settings changed"
        "SECURITY_SETTINGS_CHANGE" -> "Security settings changed"
        "SENSITIVE_DATA_ACCESS" -> "Sensitive data accessed"
        "EXTERNAL_SYNC" -> "External sync performed"
        else -> auditLog.eventType.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
    }
}