package com.beaconledger.welltrack.presentation.health

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.unit.dp
import com.beaconledger.welltrack.data.model.HealthMetric
import com.beaconledger.welltrack.data.model.HealthMetricType
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun HealthMetricCard(
    metric: HealthMetric,
    onDelete: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = getHealthMetricIcon(metric.type),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = getHealthMetricDisplayName(metric.type),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                onDelete?.let {
                    IconButton(
                        onClick = it,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "${metric.value} ${metric.unit}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = formatTimestamp(metric.timestamp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = "Source: ${formatDataSource(metric.source.name)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun HealthMetricsList(
    metrics: List<HealthMetric>,
    onDeleteMetric: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(metrics) { metric ->
            HealthMetricCard(
                metric = metric,
                onDelete = { onDeleteMetric(metric.id) },
                modifier = Modifier.width(280.dp)
            )
        }
    }
}

@Composable
fun HealthMetricTypeSelector(
    selectedType: HealthMetricType?,
    onTypeSelected: (HealthMetricType) -> Unit,
    modifier: Modifier = Modifier
) {
    val commonTypes = listOf(
        HealthMetricType.STEPS,
        HealthMetricType.HEART_RATE,
        HealthMetricType.WEIGHT,
        HealthMetricType.CALORIES_BURNED,
        HealthMetricType.SLEEP_DURATION,
        HealthMetricType.HYDRATION,
        HealthMetricType.BLOOD_PRESSURE,
        HealthMetricType.BLOOD_GLUCOSE
    )
    
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(commonTypes) { type ->
            FilterChip(
                onClick = { onTypeSelected(type) },
                label = {
                    Text(
                        text = getHealthMetricDisplayName(type),
                        style = MaterialTheme.typography.bodySmall
                    )
                },
                selected = selectedType == type,
                leadingIcon = {
                    Icon(
                        imageVector = getHealthMetricIcon(type),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            )
        }
    }
}

@Composable
fun HealthDataSyncButton(
    isSyncing: Boolean,
    onSync: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onSync,
        enabled = !isSyncing,
        modifier = modifier
    ) {
        if (isSyncing) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Syncing...")
        } else {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Sync Health Data")
        }
    }
}

@Composable
fun HealthPermissionCard(
    hasPermissions: Boolean,
    onRequestPermissions: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (hasPermissions) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (hasPermissions) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (hasPermissions) 
                        MaterialTheme.colorScheme.onPrimaryContainer 
                    else 
                        MaterialTheme.colorScheme.onErrorContainer
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (hasPermissions) "Permissions Granted" else "Permissions Required",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (hasPermissions) 
                        MaterialTheme.colorScheme.onPrimaryContainer 
                    else 
                        MaterialTheme.colorScheme.onErrorContainer
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = if (hasPermissions) 
                    "Health Connect permissions are granted. You can now sync your health data." 
                else 
                    "Health Connect permissions are required to access your health and fitness data.",
                color = if (hasPermissions) 
                    MaterialTheme.colorScheme.onPrimaryContainer 
                else 
                    MaterialTheme.colorScheme.onErrorContainer
            )
            
            if (!hasPermissions) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onRequestPermissions,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text("Grant Permissions")
                }
            }
        }
    }
}

@Composable
fun HealthMetricChart(
    metrics: List<HealthMetric>,
    modifier: Modifier = Modifier
) {
    // This is a placeholder for a chart component
    // In a real implementation, you would use a charting library like Vico or MPAndroidChart
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Health Trend Chart",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (metrics.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No data available",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // Placeholder for chart
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Chart Placeholder",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "${metrics.size} data points",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

// Helper functions

private fun getHealthMetricIcon(type: HealthMetricType): ImageVector {
    return when (type) {
        HealthMetricType.STEPS -> Icons.Default.DirectionsWalk
        HealthMetricType.HEART_RATE -> Icons.Default.Favorite
        HealthMetricType.WEIGHT -> Icons.Default.Person
        HealthMetricType.CALORIES_BURNED -> Icons.Default.Whatshot
        HealthMetricType.BLOOD_PRESSURE -> Icons.Default.Favorite
        HealthMetricType.BLOOD_GLUCOSE -> Icons.Default.Favorite
        HealthMetricType.SLEEP_DURATION -> Icons.Default.NightsStay
        HealthMetricType.EXERCISE_DURATION -> Icons.Default.FitnessCenter
        HealthMetricType.HYDRATION -> Icons.Default.LocalBar
        HealthMetricType.VO2_MAX -> Icons.Default.Speed
        HealthMetricType.BODY_FAT_PERCENTAGE -> Icons.Default.Person
        HealthMetricType.MUSCLE_MASS -> Icons.Default.FitnessCenter
        else -> Icons.Default.HealthAndSafety
    }
}

private fun getHealthMetricDisplayName(type: HealthMetricType): String {
    return when (type) {
        HealthMetricType.STEPS -> "Steps"
        HealthMetricType.HEART_RATE -> "Heart Rate"
        HealthMetricType.WEIGHT -> "Weight"
        HealthMetricType.CALORIES_BURNED -> "Calories"
        HealthMetricType.BLOOD_PRESSURE -> "Blood Pressure"
        HealthMetricType.BLOOD_GLUCOSE -> "Blood Glucose"
        HealthMetricType.SLEEP_DURATION -> "Sleep"
        HealthMetricType.EXERCISE_DURATION -> "Exercise"
        HealthMetricType.HYDRATION -> "Hydration"
        HealthMetricType.VO2_MAX -> "VO2 Max"
        HealthMetricType.BODY_FAT_PERCENTAGE -> "Body Fat"
        HealthMetricType.MUSCLE_MASS -> "Muscle Mass"
        HealthMetricType.BODY_COMPOSITION -> "Body Composition"
        else -> type.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
    }
}

private fun formatTimestamp(timestamp: String): String {
    return try {
        val dateTime = LocalDateTime.parse(timestamp, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        dateTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' HH:mm"))
    } catch (e: Exception) {
        timestamp
    }
}

private fun formatDataSource(source: String): String {
    return source.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
}