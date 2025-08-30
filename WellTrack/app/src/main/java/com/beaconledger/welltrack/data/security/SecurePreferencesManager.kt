package com.beaconledger.welltrack.data.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecurePreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        private const val SECURE_PREFS_FILE = "welltrack_secure_prefs"
    }
    
    private val masterKey: MasterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }
    
    private val securePrefs: SharedPreferences by lazy {
        EncryptedSharedPreferences.create(
            context,
            SECURE_PREFS_FILE,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
    
    fun putString(key: String, value: String) {
        securePrefs.edit().putString(key, value).apply()
    }
    
    fun getString(key: String, defaultValue: String? = null): String? {
        return securePrefs.getString(key, defaultValue)
    }
    
    fun putBoolean(key: String, value: Boolean) {
        securePrefs.edit().putBoolean(key, value).apply()
    }
    
    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return securePrefs.getBoolean(key, defaultValue)
    }
    
    fun putInt(key: String, value: Int) {
        securePrefs.edit().putInt(key, value).apply()
    }
    
    fun getInt(key: String, defaultValue: Int = 0): Int {
        return securePrefs.getInt(key, defaultValue)
    }
    
    fun putLong(key: String, value: Long) {
        securePrefs.edit().putLong(key, value).apply()
    }
    
    fun getLong(key: String, defaultValue: Long = 0L): Long {
        return securePrefs.getLong(key, defaultValue)
    }
    
    fun putFloat(key: String, value: Float) {
        securePrefs.edit().putFloat(key, value).apply()
    }
    
    fun getFloat(key: String, defaultValue: Float = 0f): Float {
        return securePrefs.getFloat(key, defaultValue)
    }
    
    fun remove(key: String) {
        securePrefs.edit().remove(key).apply()
    }
    
    fun removeKeysWithPrefix(prefix: String) {
        val editor = securePrefs.edit()
        val allKeys = securePrefs.all.keys
        allKeys.filter { it.startsWith(prefix) }.forEach { key ->
            editor.remove(key)
        }
        editor.apply()
    }
    
    fun contains(key: String): Boolean {
        return securePrefs.contains(key)
    }
    
    fun clearAll() {
        securePrefs.edit().clear().apply()
    }
    
    fun getAllKeys(): Set<String> {
        return securePrefs.all.keys
    }
    
    // Secure storage for sensitive authentication tokens
    fun storeAuthToken(userId: String, token: String) {
        putString("auth_token_$userId", token)
    }
    
    fun getAuthToken(userId: String): String? {
        return getString("auth_token_$userId")
    }
    
    fun removeAuthToken(userId: String) {
        remove("auth_token_$userId")
    }
    
    // Secure storage for biometric keys
    fun storeBiometricKey(userId: String, key: String) {
        putString("biometric_key_$userId", key)
    }
    
    fun getBiometricKey(userId: String): String? {
        return getString("biometric_key_$userId")
    }
    
    fun removeBiometricKey(userId: String) {
        remove("biometric_key_$userId")
    }
    
    // Secure storage for encryption keys
    fun storeEncryptionKey(keyId: String, key: String) {
        putString("encryption_key_$keyId", key)
    }
    
    fun getEncryptionKey(keyId: String): String? {
        return getString("encryption_key_$keyId")
    }
    
    fun removeEncryptionKey(keyId: String) {
        remove("encryption_key_$keyId")
    }
    
    // Backup and restore functionality
    fun exportSecurePreferences(userId: String): Map<String, Any?> {
        val userKeys = securePrefs.all.keys.filter { it.contains(userId) }
        val exportData = mutableMapOf<String, Any?>()
        
        userKeys.forEach { key ->
            securePrefs.all[key]?.let { value ->
                exportData[key] = value
            }
        }
        
        return exportData
    }
    
    fun importSecurePreferences(data: Map<String, Any?>) {
        val editor = securePrefs.edit()
        
        data.forEach { (key, value) ->
            when (value) {
                is String -> editor.putString(key, value)
                is Boolean -> editor.putBoolean(key, value)
                is Int -> editor.putInt(key, value)
                is Long -> editor.putLong(key, value)
                is Float -> editor.putFloat(key, value)
            }
        }
        
        editor.apply()
    }
}