package com.beaconledger.welltrack.data.sync

import android.content.Context
import android.content.SharedPreferences
import android.provider.Settings
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Provides a unique device identifier for sync operations
 */
@Singleton
class DeviceIdProvider @Inject constructor(
    private val context: Context
) {
    
    companion object {
        private const val PREFS_NAME = "welltrack_device_prefs"
        private const val KEY_DEVICE_ID = "device_id"
    }
    
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    /**
     * Gets or generates a unique device ID
     */
    fun getDeviceId(): String {
        var deviceId = prefs.getString(KEY_DEVICE_ID, null)
        
        if (deviceId == null) {
            deviceId = generateDeviceId()
            prefs.edit().putString(KEY_DEVICE_ID, deviceId).apply()
        }
        
        return deviceId
    }
    
    private fun generateDeviceId(): String {
        return try {
            // Try to use Android ID first
            val androidId = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ANDROID_ID
            )
            
            if (androidId != null && androidId != "9774d56d682e549c") {
                "android_$androidId"
            } else {
                // Fallback to UUID
                "uuid_${UUID.randomUUID()}"
            }
        } catch (e: Exception) {
            // Final fallback
            "uuid_${UUID.randomUUID()}"
        }
    }
}