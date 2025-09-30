package com.beaconledger.welltrack.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beaconledger.welltrack.R
import com.beaconledger.welltrack.data.compliance.GarminBrandComplianceManager
import com.beaconledger.welltrack.data.model.HealthMetric
import com.beaconledger.welltrack.ui.theme.WellTrackTheme

/**
 * Garmin attribution components that comply with Garmin API Brand Guidelines
 * These components ensure proper attribution is displayed for all Garmin-sourced data
 */

/**
 * Primary attribution component for title-level displays
 * Must be positioned directly beneath or adjacent to primary title/heading
 */
@Composable
fun GarminPrimaryAttribution(
    healthMetrics: List<HealthMetric>,
    complianceManager: GarminBrandComplianceManager,
    modifier: Modifier = Modifier,
    showLogo: Boolean = true
) {
    val attribution = complianceManager.generateGarminAttributionForMultiple(healthMetrics)
    
    if (attribution != null) {
        Row(
            modifier = modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            if (showLogo) {
                GarminTagLogo(
                    modifier = Modifier.size(16.dp),
                    isDarkTheme = !WellTrackTheme.colors.isLight
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            
            Text(
                text = attribution,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Secondary attribution for detailed views and reports
 * Used in expanded views, subscreens, and multi-entry displays
 */
@Composable
fun GarminSecondaryAttribution(
    healthMetrics: List<HealthMetric>,
    complianceManager: GarminBrandComplianceManager,
    modifier: Modifier = Modifier,
    isGlobalAttribution: Boolean = false
) {
    val attribution = if (isGlobalAttribution) {
        complianceManager.generateGarminAttributionForMultiple(healthMetrics)
    } else {
        // For individual entries
        healthMetrics.firstOrNull()?.let { complianceManager.generateGarminAttribution(it) }
    }
    
    if (attribution != null) {
        Text(
            text = attribution,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier
        )
    }
}

/**
 * Combined data attribution for analytics and insights
 * Used when Garmin data is combined with other sources
 */
@Composable
fun GarminCombinedDataAttribution(
    garminMetrics: List<HealthMetric>,
    otherSources: List<com.beaconledger.welltrack.data.model.DataSource>,
    complianceManager: GarminBrandComplianceManager,
    modifier: Modifier = Modifier
) {
    val attribution = complianceManager.generateCombinedDataAttribution(garminMetrics, otherSources)
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = "Data Sources",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = attribution,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Export attribution for reports and shared content
 * Must be included in all exported data (CSV, PDF, social media)
 */
@Composable
fun GarminExportAttribution(
    healthMetrics: List<HealthMetric>,
    complianceManager: GarminBrandComplianceManager,
    modifier: Modifier = Modifier,
    isForSocialMedia: Boolean = false
) {
    val attribution = if (isForSocialMedia) {
        complianceManager.generateSocialMediaAttribution(healthMetrics)
    } else {
        complianceManager.generateExportAttribution(healthMetrics)
    }
    
    if (attribution != null) {
        Surface(
            modifier = modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(4.dp)
        ) {
            Row(
                modifier = Modifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GarminTagLogo(
                    modifier = Modifier.size(14.dp),
                    isDarkTheme = !WellTrackTheme.colors.isLight
                )
                
                Spacer(modifier = Modifier.width(6.dp))
                
                Text(
                    text = attribution,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

/**
 * "Works with Garmin" badge component
 * Must follow Garmin Consumer Brand Style Guide requirements
 */
@Composable
fun WorksWithGarminBadge(
    modifier: Modifier = Modifier,
    size: BadgeSize = BadgeSize.MEDIUM
) {
    val badgeHeight = when (size) {
        BadgeSize.SMALL -> 24.dp
        BadgeSize.MEDIUM -> 32.dp
        BadgeSize.LARGE -> 48.dp
    }
    
    Card(
        modifier = modifier.height(badgeHeight),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Garmin Connect badge image would go here
            // Using placeholder for now - actual badge should be loaded from assets
            Image(
                painter = painterResource(id = R.drawable.ic_health), // Placeholder
                contentDescription = "Works with Garmin",
                modifier = Modifier.size(badgeHeight - 8.dp),
                contentScale = ContentScale.Fit
            )
            
            Spacer(modifier = Modifier.width(6.dp))
            
            Text(
                text = "Works with\nGarmin",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = when (size) {
                        BadgeSize.SMALL -> 8.sp
                        BadgeSize.MEDIUM -> 10.sp
                        BadgeSize.LARGE -> 12.sp
                    },
                    fontWeight = FontWeight.Medium,
                    lineHeight = when (size) {
                        BadgeSize.SMALL -> 10.sp
                        BadgeSize.MEDIUM -> 12.sp
                        BadgeSize.LARGE -> 14.sp
                    }
                ),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Garmin tag logo component
 * Must not be altered or animated according to guidelines
 */
@Composable
private fun GarminTagLogo(
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = false
) {
    // In a real implementation, this would load the actual Garmin tag logo
    // from the branding assets provided by Garmin
    val logoResource = if (isDarkTheme) {
        R.drawable.ic_health // Placeholder - should be garmin_tag_white
    } else {
        R.drawable.ic_health // Placeholder - should be garmin_tag_black
    }
    
    Image(
        painter = painterResource(id = logoResource),
        contentDescription = "Garmin",
        modifier = modifier,
        contentScale = ContentScale.Fit
    )
}

/**
 * Attribution compliance indicator for debugging/validation
 * Shows whether attribution is properly implemented (debug builds only)
 */
@Composable
fun GarminAttributionComplianceIndicator(
    healthMetrics: List<HealthMetric>,
    displayedAttribution: String?,
    complianceManager: GarminBrandComplianceManager,
    modifier: Modifier = Modifier
) {
    val complianceResult = complianceManager.validateAttributionCompliance(
        healthMetrics, 
        displayedAttribution
    )
    
    // Only show in debug builds
    if (com.beaconledger.welltrack.BuildConfig.DEBUG) {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (complianceResult.isCompliant) {
                    Color.Green.copy(alpha = 0.1f)
                } else {
                    Color.Red.copy(alpha = 0.1f)
                }
            )
        ) {
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    text = "Attribution Compliance",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = if (complianceResult.isCompliant) Color.Green else Color.Red
                )
                
                Text(
                    text = complianceResult.message,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

/**
 * Health metric card with proper Garmin attribution
 * Ensures attribution is always displayed with Garmin data
 */
@Composable
fun HealthMetricCardWithAttribution(
    healthMetric: HealthMetric,
    complianceManager: GarminBrandComplianceManager,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Main content
            content()
            
            // Attribution (required for Garmin data)
            if (healthMetric.source == com.beaconledger.welltrack.data.model.DataSource.GARMIN) {
                Spacer(modifier = Modifier.height(8.dp))
                
                GarminSecondaryAttribution(
                    healthMetrics = listOf(healthMetric),
                    complianceManager = complianceManager,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

enum class BadgeSize {
    SMALL, MEDIUM, LARGE
}