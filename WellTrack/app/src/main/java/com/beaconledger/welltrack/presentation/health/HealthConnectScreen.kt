package com.beaconledger.welltrack.presentation.health

import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.health.connect.client.PermissionController
import androidx.hilt.navigation.compose.hiltViewModel
import com.beaconledger.welltrack.data.model.HealthMetricType
import com.beaconledger.welltrack.domain.usecase.HealthSummary
import com.beaconledger.welltrack.domain.usecase.HealthTrends
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthConnectScreen(
    viewModel: HealthConnectViewModel = hiltViewModel(),
    userId: String
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    // Set current user when screen loads
    LaunchedEffect(userId) {
        viewModel.setCurrentUser(userId)
    }
    
    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = PermissionController.createRequestPermissionResultContract()
    ) { granted ->
        if (granted.isNotEmpty()) {
            viewModel.refreshData()
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Health Connect",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            if (uiState.hasAllPermissions) {
                IconButton(
                    onClick = { viewModel.syncHealthData() },
                    enabled = !uiState.isSyncing
                ) {
                    if (uiState.isSyncing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.Refresh, contentDescription = "Sync")
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Error message
        uiState.error?.let { error ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { viewModel.clearError() }) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Health Connect Status
            item {
                HealthConnectStatusCard(
                    isAvailable = uiState.isHealthConnectAvailable,
                    hasPermissions = uiState.hasAllPermissions,
                    onRequestPermissions = {
                        permissionLauncher.launch(uiState.requiredPermissions)
                    }
                )
            }
            
            // Today's Summary
            uiState.todaysSummary?.let { summary ->
                item {
                    TodaysSummaryCard(summary = summary)
                }
            }
            
            // Health Trends
            uiState.healthTrends?.let { trends ->
                item {
                    HealthTrendsCard(trends = trends)
                }
            }
            
            // Last Sync Info
            uiState.lastSyncTime?.let { syncTime ->
                item {
                    LastSyncCard(syncTime = syncTime)
                }
            }
        }
    }
}

@Composable
private fun HealthConnectStatusCard(
    isAvailable: Boolean,
    hasPermissions: Boolean,
    onRequestPermissions: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isAvailable && hasPermissions) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (isAvailable && hasPermissions) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Health Connect Status",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            when {
                !isAvailable -> {
                    Text(
                        text = "Health Connect is not available on this device",
                        color = MaterialTheme.colorScheme.error
                    )
                }
                !hasPermissions -> {
                    Text(
                        text = "Health Connect permissions are required to sync health data",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onRequestPermissions,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Grant Permissions")
                    }
                }
                else -> {
                    Text(
                        text = "Health Connect is ready to sync your health data",
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun TodaysSummaryCard(summary: HealthSummary) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Today's Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                HealthMetricItem(
                    icon = Icons.Default.DirectionsWalk,
                    label = "Steps",
                    value = "${summary.steps}",
                    modifier = Modifier.weight(1f)
                )
                
                HealthMetricItem(
                    icon = Icons.Default.LocalFireDepartment,
                    label = "Calories",
                    value = "${summary.caloriesBurned.toInt()}",
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                HealthMetricItem(
                    icon = Icons.Default.Bedtime,
                    label = "Sleep",
                    value = summary.sleepHours?.let { "${String.format("%.1f", it)}h" } ?: "N/A",
                    modifier = Modifier.weight(1f)
                )
                
                HealthMetricItem(
                    icon = Icons.Default.LocalDrink,
                    label = "Water",
                    value = "${String.format("%.1f", summary.hydrationLiters)}L",
                    modifier = Modifier.weight(1f)
                )
            }
            
            summary.averageHeartRate?.let { hr ->
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    HealthMetricItem(
                        icon = Icons.Default.Favorite,
                        label = "Avg HR",
                        value = "${hr.toInt()} bpm",
                        modifier = Modifier.weight(1f)
                    )
                    
                    summary.weight?.let { weight ->
                        HealthMetricItem(
                            icon = Icons.Default.Person,
                            label = "Weight",
                            value = "${String.format("%.1f", weight)} kg",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HealthMetricItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
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
private fun HealthTrendsCard(trends: HealthTrends) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Health Trends (30 days)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Steps trend
            if (trends.stepsData.isNotEmpty()) {
                val avgSteps = trends.stepsData.map { it.second }.average().toInt()
                TrendItem(
                    label = "Average Steps",
                    value = "$avgSteps steps/day",
                    icon = Icons.Default.DirectionsWalk
                )
            }
            
            // Calories trend
            if (trends.caloriesData.isNotEmpty()) {
                val avgCalories = trends.caloriesData.map { it.second }.average().toInt()
                TrendItem(
                    label = "Average Calories",
                    value = "$avgCalories cal/day",
                    icon = Icons.Default.LocalFireDepartment
                )
            }
            
            // Sleep trend
            if (trends.sleepData.isNotEmpty()) {
                val avgSleep = trends.sleepData.map { it.second }.average()
                TrendItem(
                    label = "Average Sleep",
                    value = "${String.format("%.1f", avgSleep)} hours/night",
                    icon = Icons.Default.Bedtime
                )
            }
            
            // Hydration trend
            if (trends.hydrationData.isNotEmpty()) {
                val avgHydration = trends.hydrationData.map { it.second }.average()
                TrendItem(
                    label = "Average Hydration",
                    value = "${String.format("%.1f", avgHydration)} L/day",
                    icon = Icons.Default.LocalDrink
                )
            }
        }
    }
}

@Composable
private fun TrendItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LastSyncCard(syncTime: Long) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Last Sync",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = java.text.SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", java.util.Locale.getDefault())
                        .format(java.util.Date(syncTime)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}