package com.beaconledger.welltrack.data.compliance

import android.content.Context
import com.beaconledger.welltrack.data.model.DataSource
import com.beaconledger.welltrack.data.model.HealthMetric
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages Garmin brand compliance requirements according to Garmin API Brand Guidelines v6.30.2025
 * 
 * Key Requirements:
 * - All Garmin device-sourced data must include proper attribution
 * - Attribution must be visible in primary displays, secondary screens, and exports
 * - Device model information must be included when available
 * - "Works with Garmin" badge must be properly implemented
 * - Privacy policy must include Garmin data usage disclosures
 */
@Singleton
class GarminBrandComplianceManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    /**
     * Generate proper Garmin attribution text for health metrics
     * Format: "Garmin [device model]" or "Garmin" if device model unknown
     */
    fun generateGarminAttribution(healthMetric: HealthMetric): String? {
        if (healthMetric.source != DataSource.GARMIN) {
            return null
        }
        
        // Extract device model from metadata if available
        val deviceModel = extractDeviceModel(healthMetric.metadata)
        
        return if (deviceModel.isNotBlank()) {
            "Garmin $deviceModel"
        } else {
            "Garmin"
        }
    }
    
    /**
     * Generate attribution for multiple Garmin metrics
     * Used for displays showing multiple data points
     */
    fun generateGarminAttributionForMultiple(healthMetrics: List<HealthMetric>): String? {
        val garminMetrics = healthMetrics.filter { it.source == DataSource.GARMIN }
        if (garminMetrics.isEmpty()) {
            return null
        }
        
        // Get unique device models
        val deviceModels = garminMetrics.mapNotNull { metric ->
            extractDeviceModel(metric.metadata)
        }.distinct().filter { it.isNotBlank() }
        
        return when {
            deviceModels.isEmpty() -> "Garmin"
            deviceModels.size == 1 -> "Garmin ${deviceModels.first()}"
            else -> "Garmin (${deviceModels.joinToString(", ")})"
        }
    }
    
    /**
     * Generate attribution for combined/derived data that includes Garmin sources
     * Used when Garmin data is combined with other sources
     */
    fun generateCombinedDataAttribution(
        garminMetrics: List<HealthMetric>,
        otherSources: List<DataSource>
    ): String {
        val garminAttribution = generateGarminAttributionForMultiple(garminMetrics)
        val otherSourceNames = otherSources.map { it.displayName }.distinct()
        
        return when {
            garminAttribution != null && otherSourceNames.isNotEmpty() -> {
                "Data sources: $garminAttribution, ${otherSourceNames.joinToString(", ")}"
            }
            garminAttribution != null -> garminAttribution
            otherSourceNames.isNotEmpty() -> "Data sources: ${otherSourceNames.joinToString(", ")}"
            else -> "Multiple data sources"
        }
    }
    
    /**
     * Check if data requires Garmin attribution
     */
    fun requiresGarminAttribution(healthMetrics: List<HealthMetric>): Boolean {
        return healthMetrics.any { it.source == DataSource.GARMIN }
    }
    
    /**
     * Generate export attribution for CSV/PDF reports
     * Must be included on each page/section according to guidelines
     */
    fun generateExportAttribution(healthMetrics: List<HealthMetric>): String? {
        val garminAttribution = generateGarminAttributionForMultiple(healthMetrics)
        return garminAttribution?.let { "Data provided by $it" }
    }
    
    /**
     * Generate social media attribution for shared content
     * Must be visible in every shared image/post
     */
    fun generateSocialMediaAttribution(healthMetrics: List<HealthMetric>): String? {
        return generateGarminAttributionForMultiple(healthMetrics)
    }
    
    /**
     * Validate that attribution is properly implemented for a data display
     */
    fun validateAttributionCompliance(
        healthMetrics: List<HealthMetric>,
        displayedAttribution: String?
    ): AttributionComplianceResult {
        val hasGarminData = healthMetrics.any { it.source == DataSource.GARMIN }
        
        if (!hasGarminData) {
            return AttributionComplianceResult(
                isCompliant = true,
                message = "No Garmin data present, attribution not required"
            )
        }
        
        if (displayedAttribution.isNullOrBlank()) {
            return AttributionComplianceResult(
                isCompliant = false,
                message = "Garmin attribution required but not provided"
            )
        }
        
        val expectedAttribution = generateGarminAttributionForMultiple(healthMetrics)
        val isValidAttribution = displayedAttribution.contains("Garmin", ignoreCase = true)
        
        return AttributionComplianceResult(
            isCompliant = isValidAttribution,
            message = if (isValidAttribution) {
                "Attribution compliant"
            } else {
                "Attribution must include 'Garmin'. Expected: $expectedAttribution"
            }
        )
    }
    
    /**
     * Get Garmin privacy policy requirements text
     */
    fun getGarminPrivacyPolicyText(): String {
        return """
            Garmin Data Usage and Privacy
            
            This app integrates with Garmin Connect to access your health and fitness data from Garmin devices. 
            
            Data Collection:
            • We collect health metrics from your Garmin devices including heart rate variability (HRV), 
              training recovery scores, stress levels, biological age, and fitness metrics
            • Data is collected only with your explicit consent through Garmin Connect authorization
            • You can revoke access at any time through your Garmin Connect account settings
            
            Data Usage:
            • Garmin data is used to provide personalized health insights and recommendations
            • Data may be combined with other health sources to generate comprehensive analytics
            • All Garmin-sourced data displays include proper attribution as required by Garmin guidelines
            
            Data Sharing:
            • We do not share your Garmin data with third parties without your consent
            • Exported reports and shared content maintain Garmin attribution requirements
            • You control what data is shared through the app's privacy settings
            
            Data Retention:
            • Garmin data is stored securely and encrypted
            • You can delete your Garmin data at any time through the app settings
            • Data deletion requests are processed immediately and cannot be reversed
            
            For more information about Garmin's privacy practices, visit: https://www.garmin.com/privacy/
        """.trimIndent()
    }
    
    /**
     * Get "Works with Garmin" badge usage requirements
     */
    fun getWorksWithGarminBadgeRequirements(): GarminBadgeRequirements {
        return GarminBadgeRequirements(
            canUseBadge = true,
            placement = "Display prominently in app settings, about page, and marketing materials",
            restrictions = listOf(
                "Do not alter or animate the Garmin tag logo",
                "Do not use in avatars, badges, or unrelated imagery", 
                "Only use when Garmin device-sourced data is present",
                "Must follow Garmin Consumer Brand Style Guide"
            ),
            requiredText = "Works with Garmin",
            logoFiles = listOf(
                "garmin_tag_black_high_res.jpg",
                "garmin_tag_white_high_res.jpg",
                "garmin_connect_badge_digital.png"
            )
        )
    }
    
    /**
     * Extract device model from health metric metadata
     */
    private fun extractDeviceModel(metadata: String?): String {
        if (metadata.isNullOrBlank()) return ""
        
        try {
            // Parse JSON metadata to extract device model
            // This would be populated by the GarminConnectManager when syncing data
            val jsonRegex = """"deviceModel"\s*:\s*"([^"]+)"""".toRegex()
            val match = jsonRegex.find(metadata)
            return match?.groupValues?.get(1) ?: ""
        } catch (e: Exception) {
            return ""
        }
    }
}

/**
 * Result of attribution compliance validation
 */
data class AttributionComplianceResult(
    val isCompliant: Boolean,
    val message: String
)

/**
 * Requirements for using "Works with Garmin" badge
 */
data class GarminBadgeRequirements(
    val canUseBadge: Boolean,
    val placement: String,
    val restrictions: List<String>,
    val requiredText: String,
    val logoFiles: List<String>
)

/**
 * Extension property for DataSource display names
 */
private val DataSource.displayName: String
    get() = when (this) {
        DataSource.GARMIN -> "Garmin"
        DataSource.SAMSUNG_HEALTH -> "Samsung Health"
        DataSource.HEALTH_CONNECT -> "Health Connect"
        DataSource.MANUAL_ENTRY -> "Manual Entry"
        else -> this.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
    }