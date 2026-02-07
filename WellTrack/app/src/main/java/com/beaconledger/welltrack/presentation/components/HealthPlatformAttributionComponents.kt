package com.beaconledger.welltrack.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beaconledger.welltrack.R
import com.beaconledger.welltrack.data.model.DataSource
import com.beaconledger.welltrack.data.model.HealthMetric

/**
 * Comprehensive health platform attribution components
 * 
 * Provides proper attribution and branding for all health platform integrations:
 * - Garmin Connect (already implemented in GarminAttributionComponents)
 * - Samsung Health
 * - Google Health Connect
 * - Multi-platform data displays
 */

/**
 * Samsung Health attribution component for data sourced from Samsung Health
 */
@Composable
fun SamsungHealthAttribution(
    modifier: Modifier = Modifier,
    size: SamsungHealthAttributionSize = SamsungHealthAttributionSize.Medium,
    showIcon: Boolean = true
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (showIcon) {
            // Samsung Health icon (would need to be added to drawable resources)
            Icon(
                painter = painterResource(id = R.drawable.ic_samsung_health),
                contentDescription = "Samsung Health",
                modifier = Modifier.size(size.iconSize),
                tint = Color(0xFF1428A0) // Samsung Health blue
            )
        }
        
        Text(
            text = stringResource(R.string.samsung_health_attribution),
            fontSize = size.textSize,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF1428A0)
        )
    }
}

/**
 * Google Health Connect attribution component
 */
@Composable
fun HealthConnectAttribution(
    modifier: Modifier = Modifier,
    size: HealthConnectAttributionSize = HealthConnectAttributionSize.Medium,
    showIcon: Boolean = true
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (showIcon) {
            // Health Connect icon
            Icon(
                painter = painterResource(id = R.drawable.ic_health_connect),
                contentDescription = "Health Connect",
                modifier = Modifier.size(size.iconSize),
                tint = Color(0xFF4285F4) // Google blue
            )
        }
        
        Text(
            text = stringResource(R.string.health_connect_attribution),
            fontSize = size.textSize,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF4285F4)
        )
    }
}

/**
 * Multi-platform attribution component for data from multiple sources
 */
@Composable
fun MultiPlatformAttribution(
    dataSources: List<DataSource>,
    modifier: Modifier = Modifier,
    size: MultiPlatformAttributionSize = MultiPlatformAttributionSize.Medium
) {
    if (dataSources.isEmpty()) return
    
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = stringResource(R.string.data_sources_label),
            fontSize = (size.textSize.value - 2).sp,
            fontWeight = FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            dataSources.distinct().forEach { source ->
                when (source) {
                    DataSource.GARMIN -> {
                        GarminPrimaryAttribution(
                            size = GarminAttributionSize.Small,
                            showIcon = true
                        )
                    }
                    DataSource.SAMSUNG_HEALTH -> {
                        SamsungHealthAttribution(
                            size = SamsungHealthAttributionSize.Small,
                            showIcon = true
                        )
                    }
                    DataSource.HEALTH_CONNECT -> {
                        HealthConnectAttribution(
                            size = HealthConnectAttributionSize.Small,
                            showIcon = true
                        )
                    }
                    else -> {
                        // Manual or other sources
                        Text(
                            text = source.displayName,
                            fontSize = size.textSize,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * Health metric card with proper attribution based on data source
 */
@Composable
fun HealthMetricCardWithAttribution(
    healthMetric: HealthMetric,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick ?: {},
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Metric header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = healthMetric.type.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                // Data source attribution
                when (healthMetric.source) {
                    DataSource.GARMIN -> {
                        GarminPrimaryAttribution(
                            size = GarminAttributionSize.Small,
                            deviceModel = healthMetric.deviceModel
                        )
                    }
                    DataSource.SAMSUNG_HEALTH -> {
                        SamsungHealthAttribution(
                            size = SamsungHealthAttributionSize.Small
                        )
                    }
                    DataSource.HEALTH_CONNECT -> {
                        HealthConnectAttribution(
                            size = HealthConnectAttributionSize.Small
                        )
                    }
                    else -> {
                        Text(
                            text = healthMetric.source.displayName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // Metric value
            Row(
                verticalAlignment = Alignment.Baseline,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = healthMetric.formattedValue,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Text(
                    text = healthMetric.unit,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Timestamp
            Text(
                text = healthMetric.formattedTimestamp,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * "Works with" badges for health platforms
 */
@Composable
fun WorksWithHealthPlatformsBadge(
    connectedPlatforms: List<DataSource>,
    modifier: Modifier = Modifier,
    size: HealthPlatformBadgeSize = HealthPlatformBadgeSize.Medium
) {
    if (connectedPlatforms.isEmpty()) return
    
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.works_with_platforms),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                connectedPlatforms.distinct().forEach { platform ->
                    when (platform) {
                        DataSource.GARMIN -> {
                            WorksWithGarminBadge(size = GarminBadgeSize.Small)
                        }
                        DataSource.SAMSUNG_HEALTH -> {
                            WorksWithSamsungHealthBadge(size = size)
                        }
                        DataSource.HEALTH_CONNECT -> {
                            WorksWithHealthConnectBadge(size = size)
                        }
                        else -> { /* Skip other sources */ }
                    }
                }
            }
        }
    }
}

/**
 * "Works with Samsung Health" badge
 */
@Composable
fun WorksWithSamsungHealthBadge(
    modifier: Modifier = Modifier,
    size: HealthPlatformBadgeSize = HealthPlatformBadgeSize.Medium
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        color = Color(0xFF1428A0),
        contentColor = Color.White
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = size.horizontalPadding,
                vertical = size.verticalPadding
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_samsung_health),
                contentDescription = null,
                modifier = Modifier.size(size.iconSize),
                tint = Color.White
            )
            
            Text(
                text = stringResource(R.string.works_with_samsung_health),
                fontSize = size.textSize,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * "Works with Health Connect" badge
 */
@Composable
fun WorksWithHealthConnectBadge(
    modifier: Modifier = Modifier,
    size: HealthPlatformBadgeSize = HealthPlatformBadgeSize.Medium
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        color = Color(0xFF4285F4),
        contentColor = Color.White
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = size.horizontalPadding,
                vertical = size.verticalPadding
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_health_connect),
                contentDescription = null,
                modifier = Modifier.size(size.iconSize),
                tint = Color.White
            )
            
            Text(
                text = stringResource(R.string.works_with_health_connect),
                fontSize = size.textSize,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Data export attribution component for exported reports
 */
@Composable
fun DataExportAttribution(
    dataSources: List<DataSource>,
    modifier: Modifier = Modifier
) {
    if (dataSources.isEmpty()) return
    
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = stringResource(R.string.data_export_attribution_header),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
        )
        
        dataSources.distinct().forEach { source ->
            when (source) {
                DataSource.GARMIN -> {
                    Text(
                        text = stringResource(R.string.garmin_export_attribution),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                DataSource.SAMSUNG_HEALTH -> {
                    Text(
                        text = stringResource(R.string.samsung_health_export_attribution),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                DataSource.HEALTH_CONNECT -> {
                    Text(
                        text = stringResource(R.string.health_connect_export_attribution),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                else -> { /* Skip other sources */ }
            }
        }
    }
}

// Size enums for different attribution components

enum class SamsungHealthAttributionSize(
    val iconSize: androidx.compose.ui.unit.Dp,
    val textSize: androidx.compose.ui.unit.TextUnit
) {
    Small(12.dp, 10.sp),
    Medium(16.dp, 12.sp),
    Large(20.dp, 14.sp)
}

enum class HealthConnectAttributionSize(
    val iconSize: androidx.compose.ui.unit.Dp,
    val textSize: androidx.compose.ui.unit.TextUnit
) {
    Small(12.dp, 10.sp),
    Medium(16.dp, 12.sp),
    Large(20.dp, 14.sp)
}

enum class MultiPlatformAttributionSize(
    val textSize: androidx.compose.ui.unit.TextUnit
) {
    Small(10.sp),
    Medium(12.sp),
    Large(14.sp)
}

enum class HealthPlatformBadgeSize(
    val iconSize: androidx.compose.ui.unit.Dp,
    val textSize: androidx.compose.ui.unit.TextUnit,
    val horizontalPadding: androidx.compose.ui.unit.Dp,
    val verticalPadding: androidx.compose.ui.unit.Dp
) {
    Small(12.dp, 10.sp, 8.dp, 4.dp),
    Medium(16.dp, 12.sp, 12.dp, 6.dp),
    Large(20.dp, 14.sp, 16.dp, 8.dp)
}