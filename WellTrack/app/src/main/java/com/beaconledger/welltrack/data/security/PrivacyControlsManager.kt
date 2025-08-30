package com.beaconledger.welltrack.data.security

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PrivacyControlsManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val securePreferencesManager: SecurePreferencesManager,
    private val auditLogger: AuditLogger
) {
    
    companion object {
        private const val PREF_DATA_SHARING_ENABLED = "data_sharing_enabled"
        private const val PREF_ANALYTICS_ENABLED = "analytics_enabled"
        private const val PREF_CRASH_REPORTING_ENABLED = "crash_reporting_enabled"
        private const val PREF_HEALTH_DATA_SHARING = "health_data_sharing_enabled"
        private const val PREF_MEAL_DATA_SHARING = "meal_data_sharing_enabled"
        private const val PREF_RECIPE_SHARING = "recipe_sharing_enabled"
        private const val PREF_SOCIAL_FEATURES_ENABLED = "social_features_enabled"
        private const val PREF_LOCATION_SHARING = "location_sharing_enabled"
        private const val PREF_THIRD_PARTY_INTEGRATIONS = "third_party_integrations_enabled"
        private const val PREF_DATA_EXPORT_ALLOWED = "data_export_allowed"
        private const val PREF_MARKETING_COMMUNICATIONS = "marketing_communications_enabled"
        private const val PREF_PERSONALIZED_ADS = "personalized_ads_enabled"
    }
    
    data class PrivacySettings(
        val dataSharingEnabled: Boolean = false,
        val analyticsEnabled: Boolean = false,
        val crashReportingEnabled: Boolean = true, // Default to true for app stability
        val healthDataSharingEnabled: Boolean = false,
        val mealDataSharingEnabled: Boolean = false,
        val recipeSharingEnabled: Boolean = true,
        val socialFeaturesEnabled: Boolean = false,
        val locationSharingEnabled: Boolean = false,
        val thirdPartyIntegrationsEnabled: Boolean = false,
        val dataExportAllowed: Boolean = true,
        val marketingCommunicationsEnabled: Boolean = false,
        val personalizedAdsEnabled: Boolean = false
    )
    
    private val _privacySettings = MutableStateFlow(loadPrivacySettings())
    val privacySettings: StateFlow<PrivacySettings> = _privacySettings.asStateFlow()
    
    private fun loadPrivacySettings(): PrivacySettings {
        return PrivacySettings(
            dataSharingEnabled = securePreferencesManager.getBoolean(PREF_DATA_SHARING_ENABLED, false),
            analyticsEnabled = securePreferencesManager.getBoolean(PREF_ANALYTICS_ENABLED, false),
            crashReportingEnabled = securePreferencesManager.getBoolean(PREF_CRASH_REPORTING_ENABLED, true),
            healthDataSharingEnabled = securePreferencesManager.getBoolean(PREF_HEALTH_DATA_SHARING, false),
            mealDataSharingEnabled = securePreferencesManager.getBoolean(PREF_MEAL_DATA_SHARING, false),
            recipeSharingEnabled = securePreferencesManager.getBoolean(PREF_RECIPE_SHARING, true),
            socialFeaturesEnabled = securePreferencesManager.getBoolean(PREF_SOCIAL_FEATURES_ENABLED, false),
            locationSharingEnabled = securePreferencesManager.getBoolean(PREF_LOCATION_SHARING, false),
            thirdPartyIntegrationsEnabled = securePreferencesManager.getBoolean(PREF_THIRD_PARTY_INTEGRATIONS, false),
            dataExportAllowed = securePreferencesManager.getBoolean(PREF_DATA_EXPORT_ALLOWED, true),
            marketingCommunicationsEnabled = securePreferencesManager.getBoolean(PREF_MARKETING_COMMUNICATIONS, false),
            personalizedAdsEnabled = securePreferencesManager.getBoolean(PREF_PERSONALIZED_ADS, false)
        )
    }
    
    fun updatePrivacySettings(settings: PrivacySettings, userId: String? = null) {
        securePreferencesManager.putBoolean(PREF_DATA_SHARING_ENABLED, settings.dataSharingEnabled)
        securePreferencesManager.putBoolean(PREF_ANALYTICS_ENABLED, settings.analyticsEnabled)
        securePreferencesManager.putBoolean(PREF_CRASH_REPORTING_ENABLED, settings.crashReportingEnabled)
        securePreferencesManager.putBoolean(PREF_HEALTH_DATA_SHARING, settings.healthDataSharingEnabled)
        securePreferencesManager.putBoolean(PREF_MEAL_DATA_SHARING, settings.mealDataSharingEnabled)
        securePreferencesManager.putBoolean(PREF_RECIPE_SHARING, settings.recipeSharingEnabled)
        securePreferencesManager.putBoolean(PREF_SOCIAL_FEATURES_ENABLED, settings.socialFeaturesEnabled)
        securePreferencesManager.putBoolean(PREF_LOCATION_SHARING, settings.locationSharingEnabled)
        securePreferencesManager.putBoolean(PREF_THIRD_PARTY_INTEGRATIONS, settings.thirdPartyIntegrationsEnabled)
        securePreferencesManager.putBoolean(PREF_DATA_EXPORT_ALLOWED, settings.dataExportAllowed)
        securePreferencesManager.putBoolean(PREF_MARKETING_COMMUNICATIONS, settings.marketingCommunicationsEnabled)
        securePreferencesManager.putBoolean(PREF_PERSONALIZED_ADS, settings.personalizedAdsEnabled)
        
        _privacySettings.value = settings
        
        // Log privacy settings changes
        userId?.let { 
            auditLogger.logPrivacySettingsChange(it, "PRIVACY_SETTINGS_UPDATED", settings.toString())
        }
    }
    
    fun isDataSharingAllowed(dataType: DataSharingType): Boolean {
        val settings = _privacySettings.value
        return when (dataType) {
            DataSharingType.HEALTH_DATA -> settings.healthDataSharingEnabled && settings.dataSharingEnabled
            DataSharingType.MEAL_DATA -> settings.mealDataSharingEnabled && settings.dataSharingEnabled
            DataSharingType.RECIPES -> settings.recipeSharingEnabled
            DataSharingType.ANALYTICS -> settings.analyticsEnabled
            DataSharingType.CRASH_REPORTS -> settings.crashReportingEnabled
            DataSharingType.LOCATION -> settings.locationSharingEnabled
            DataSharingType.SOCIAL -> settings.socialFeaturesEnabled
            DataSharingType.THIRD_PARTY -> settings.thirdPartyIntegrationsEnabled
            DataSharingType.MARKETING -> settings.marketingCommunicationsEnabled
            DataSharingType.PERSONALIZED_ADS -> settings.personalizedAdsEnabled
        }
    }
    
    fun canExportData(): Boolean {
        return _privacySettings.value.dataExportAllowed
    }
    
    fun getDataRetentionPeriod(dataType: DataRetentionType): Int {
        // Return retention period in days
        return when (dataType) {
            DataRetentionType.HEALTH_METRICS -> 365 * 7 // 7 years for health data
            DataRetentionType.MEAL_LOGS -> 365 * 2 // 2 years for meal data
            DataRetentionType.RECIPES -> -1 // Indefinite for recipes
            DataRetentionType.AUDIT_LOGS -> 365 * 3 // 3 years for audit logs
            DataRetentionType.CRASH_REPORTS -> 90 // 90 days for crash reports
            DataRetentionType.ANALYTICS -> 365 // 1 year for analytics
        }
    }
    
    fun shouldCollectAnalytics(): Boolean {
        return isDataSharingAllowed(DataSharingType.ANALYTICS)
    }
    
    fun shouldSendCrashReports(): Boolean {
        return isDataSharingAllowed(DataSharingType.CRASH_REPORTS)
    }
    
    fun canShareWithThirdParties(): Boolean {
        return isDataSharingAllowed(DataSharingType.THIRD_PARTY)
    }
    
    fun resetToDefaults(userId: String? = null) {
        val defaultSettings = PrivacySettings()
        updatePrivacySettings(defaultSettings, userId)
        
        userId?.let {
            auditLogger.logPrivacySettingsChange(it, "PRIVACY_SETTINGS_RESET_TO_DEFAULTS")
        }
    }
    
    fun exportPrivacySettings(): Map<String, Any> {
        val settings = _privacySettings.value
        return mapOf(
            "dataSharingEnabled" to settings.dataSharingEnabled,
            "analyticsEnabled" to settings.analyticsEnabled,
            "crashReportingEnabled" to settings.crashReportingEnabled,
            "healthDataSharingEnabled" to settings.healthDataSharingEnabled,
            "mealDataSharingEnabled" to settings.mealDataSharingEnabled,
            "recipeSharingEnabled" to settings.recipeSharingEnabled,
            "socialFeaturesEnabled" to settings.socialFeaturesEnabled,
            "locationSharingEnabled" to settings.locationSharingEnabled,
            "thirdPartyIntegrationsEnabled" to settings.thirdPartyIntegrationsEnabled,
            "dataExportAllowed" to settings.dataExportAllowed,
            "marketingCommunicationsEnabled" to settings.marketingCommunicationsEnabled,
            "personalizedAdsEnabled" to settings.personalizedAdsEnabled,
            "exportedAt" to System.currentTimeMillis()
        )
    }
    
    enum class DataSharingType {
        HEALTH_DATA,
        MEAL_DATA,
        RECIPES,
        ANALYTICS,
        CRASH_REPORTS,
        LOCATION,
        SOCIAL,
        THIRD_PARTY,
        MARKETING,
        PERSONALIZED_ADS
    }
    
    enum class DataRetentionType {
        HEALTH_METRICS,
        MEAL_LOGS,
        RECIPES,
        AUDIT_LOGS,
        CRASH_REPORTS,
        ANALYTICS
    }
}