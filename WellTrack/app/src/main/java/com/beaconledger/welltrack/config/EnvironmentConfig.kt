package com.beaconledger.welltrack.config

import android.content.Context
import com.beaconledger.welltrack.BuildConfig
import java.io.IOException
import java.util.Properties
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Environment configuration manager that loads settings from .env file and BuildConfig
 * Provides secure access to API keys, secrets, and configuration values
 */
@Singleton
class EnvironmentConfig @Inject constructor(
    private val context: Context
) {
    private val properties = Properties()
    private var isLoaded = false

    init {
        loadEnvironmentVariables()
    }

    /**
     * Load environment variables from .env file in assets
     * Falls back to BuildConfig values if .env file is not available
     */
    private fun loadEnvironmentVariables() {
        try {
            // Try to load from assets/.env file first
            context.assets.open(".env").use { inputStream ->
                properties.load(inputStream)
                isLoaded = true
            }
        } catch (e: IOException) {
            // .env file not found in assets, use BuildConfig defaults
            isLoaded = false
        }
    }

    /**
     * Get environment variable with fallback to BuildConfig
     */
    private fun getEnvVar(key: String, buildConfigValue: String? = null): String {
        return if (isLoaded) {
            properties.getProperty(key) ?: buildConfigValue ?: ""
        } else {
            buildConfigValue ?: ""
        }
    }

    // =============================================================================
    // SUPABASE CONFIGURATION
    // =============================================================================
    val supabaseUrl: String
        get() = getEnvVar("SUPABASE_URL", BuildConfig.SUPABASE_URL)

    val supabaseAnonKey: String
        get() = getEnvVar("SUPABASE_ANON_KEY", BuildConfig.SUPABASE_ANON_KEY)

    val supabaseServiceRoleKey: String
        get() = getEnvVar("SUPABASE_SERVICE_ROLE_KEY")

    // =============================================================================
    // GARMIN CONNECT INTEGRATION
    // =============================================================================
    val garminClientId: String
        get() = getEnvVar("GARMIN_CLIENT_ID")

    val garminClientSecret: String
        get() = getEnvVar("GARMIN_CLIENT_SECRET")

    val garminRedirectUri: String
        get() = getEnvVar("GARMIN_REDIRECT_URI", "welltrack://garmin/callback")

    // =============================================================================
    // SAMSUNG HEALTH INTEGRATION
    // =============================================================================
    val samsungHealthAppId: String
        get() = getEnvVar("SAMSUNG_HEALTH_APP_ID")

    val samsungHealthClientSecret: String
        get() = getEnvVar("SAMSUNG_HEALTH_CLIENT_SECRET")

    // =============================================================================
    // OPENAI INTEGRATION
    // =============================================================================
    val openAiApiKey: String
        get() = getEnvVar("OPENAI_API_KEY")

    val openAiOrganizationId: String
        get() = getEnvVar("OPENAI_ORGANIZATION_ID")

    // =============================================================================
    // ENCRYPTION & SECURITY
    // =============================================================================
    val encryptionKey: String
        get() = getEnvVar("ENCRYPTION_KEY", "WellTrack2024SecureKey123456789012")

    val jwtSecret: String
        get() = getEnvVar("JWT_SECRET", "WellTrackJWTSecret2024SuperSecureKey")

    // =============================================================================
    // EXTERNAL APIS
    // =============================================================================
    val spoonacularApiKey: String
        get() = getEnvVar("SPOONACULAR_API_KEY")

    val edamamAppId: String
        get() = getEnvVar("EDAMAM_APP_ID")

    val edamamAppKey: String
        get() = getEnvVar("EDAMAM_APP_KEY")

    // =============================================================================
    // ANALYTICS & MONITORING
    // =============================================================================
    val firebaseProjectId: String
        get() = getEnvVar("FIREBASE_PROJECT_ID")

    val crashlyticsApiKey: String
        get() = getEnvVar("CRASHLYTICS_API_KEY")

    // =============================================================================
    // BUILD CONFIGURATION
    // =============================================================================
    val environment: String
        get() = getEnvVar("ENVIRONMENT", if (BuildConfig.DEBUG) "development" else "production")

    val isDebugMode: Boolean
        get() = getEnvVar("DEBUG_MODE", BuildConfig.DEBUG.toString()).toBoolean()

    val isLoggingEnabled: Boolean
        get() = getEnvVar("ENABLE_LOGGING", BuildConfig.DEBUG.toString()).toBoolean()

    // =============================================================================
    // HEALTH CONNECT CONFIGURATION
    // =============================================================================
    val healthConnectPackageName: String
        get() = getEnvVar("HEALTH_CONNECT_PACKAGE_NAME", "com.google.android.apps.healthdata")

    // =============================================================================
    // UTILITY METHODS
    // =============================================================================
    
    /**
     * Check if all required API keys are configured
     */
    fun validateConfiguration(): ConfigurationStatus {
        val missingKeys = mutableListOf<String>()

        // Check required Supabase configuration
        if (supabaseUrl.isBlank()) missingKeys.add("SUPABASE_URL")
        if (supabaseAnonKey.isBlank()) missingKeys.add("SUPABASE_ANON_KEY")

        // Check optional but recommended configurations
        val warnings = mutableListOf<String>()
        if (garminClientId.isBlank()) warnings.add("GARMIN_CLIENT_ID (Garmin integration disabled)")
        if (openAiApiKey.isBlank()) warnings.add("OPENAI_API_KEY (AI features disabled)")

        return ConfigurationStatus(
            isValid = missingKeys.isEmpty(),
            missingRequiredKeys = missingKeys,
            missingOptionalKeys = warnings
        )
    }

    /**
     * Get configuration summary for debugging (without sensitive values)
     */
    fun getConfigurationSummary(): Map<String, String> {
        return mapOf(
            "Environment" to environment,
            "Debug Mode" to isDebugMode.toString(),
            "Logging Enabled" to isLoggingEnabled.toString(),
            "Supabase Configured" to (supabaseUrl.isNotBlank() && supabaseAnonKey.isNotBlank()).toString(),
            "Garmin Configured" to garminClientId.isNotBlank().toString(),
            "Samsung Health Configured" to samsungHealthAppId.isNotBlank().toString(),
            "OpenAI Configured" to openAiApiKey.isNotBlank().toString(),
            "Config Source" to if (isLoaded) ".env file" else "BuildConfig"
        )
    }
}

/**
 * Configuration validation result
 */
data class ConfigurationStatus(
    val isValid: Boolean,
    val missingRequiredKeys: List<String>,
    val missingOptionalKeys: List<String>
)