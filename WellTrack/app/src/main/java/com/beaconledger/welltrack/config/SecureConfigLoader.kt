package com.beaconledger.welltrack.config

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Secure configuration loader that handles encryption and secure storage of sensitive data
 * Uses Android Keystore and EncryptedSharedPreferences for maximum security
 */
@Singleton
class SecureConfigLoader @Inject constructor(
    private val context: Context
) {
    companion object {
        private const val PREFS_NAME = "welltrack_secure_config"
        private const val KEY_ALIAS = "welltrack_master_key"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_IV_LENGTH = 12
        private const val GCM_TAG_LENGTH = 16
    }

    private val masterKey: MasterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    private val encryptedPrefs: SharedPreferences by lazy {
        EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    /**
     * Store sensitive configuration data securely
     */
    fun storeSecureValue(key: String, value: String) {
        encryptedPrefs.edit()
            .putString(key, value)
            .apply()
    }

    /**
     * Retrieve sensitive configuration data securely
     */
    fun getSecureValue(key: String, defaultValue: String = ""): String {
        return encryptedPrefs.getString(key, defaultValue) ?: defaultValue
    }

    /**
     * Store API tokens with expiration
     */
    fun storeTokenWithExpiry(tokenKey: String, token: String, expiryTimeMillis: Long) {
        encryptedPrefs.edit()
            .putString(tokenKey, token)
            .putLong("${tokenKey}_expiry", expiryTimeMillis)
            .apply()
    }

    /**
     * Get API token if not expired
     */
    fun getValidToken(tokenKey: String): String? {
        val token = encryptedPrefs.getString(tokenKey, null)
        val expiry = encryptedPrefs.getLong("${tokenKey}_expiry", 0)
        
        return if (token != null && System.currentTimeMillis() < expiry) {
            token
        } else {
            // Token expired, remove it
            encryptedPrefs.edit()
                .remove(tokenKey)
                .remove("${tokenKey}_expiry")
                .apply()
            null
        }
    }

    /**
     * Clear all stored secure values
     */
    fun clearAllSecureValues() {
        encryptedPrefs.edit().clear().apply()
    }

    /**
     * Check if a secure value exists
     */
    fun hasSecureValue(key: String): Boolean {
        return encryptedPrefs.contains(key)
    }

    /**
     * Encrypt sensitive data for transmission
     */
    fun encryptForTransmission(data: String, key: String): String {
        return try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val secretKey = generateSecretKey(key)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            
            val iv = cipher.iv
            val encryptedData = cipher.doFinal(data.toByteArray())
            
            // Combine IV and encrypted data
            val combined = ByteArray(iv.size + encryptedData.size)
            System.arraycopy(iv, 0, combined, 0, iv.size)
            System.arraycopy(encryptedData, 0, combined, iv.size, encryptedData.size)
            
            Base64.encodeToString(combined, Base64.DEFAULT)
        } catch (e: Exception) {
            throw SecurityException("Failed to encrypt data", e)
        }
    }

    /**
     * Decrypt sensitive data from transmission
     */
    fun decryptFromTransmission(encryptedData: String, key: String): String {
        return try {
            val combined = Base64.decode(encryptedData, Base64.DEFAULT)
            
            // Extract IV and encrypted data
            val iv = ByteArray(GCM_IV_LENGTH)
            val encrypted = ByteArray(combined.size - GCM_IV_LENGTH)
            System.arraycopy(combined, 0, iv, 0, iv.size)
            System.arraycopy(combined, iv.size, encrypted, 0, encrypted.size)
            
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val secretKey = generateSecretKey(key)
            val spec = GCMParameterSpec(GCM_TAG_LENGTH * 8, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
            
            String(cipher.doFinal(encrypted))
        } catch (e: Exception) {
            throw SecurityException("Failed to decrypt data", e)
        }
    }

    /**
     * Generate a secret key from a string key
     */
    private fun generateSecretKey(key: String): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES)
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .build()
        
        keyGenerator.init(keyGenParameterSpec)
        return keyGenerator.generateKey()
    }

    /**
     * Validate configuration security
     */
    fun validateSecurityConfiguration(): SecurityStatus {
        val issues = mutableListOf<String>()
        
        try {
            // Test encryption/decryption
            val testData = "test_security_validation"
            val encrypted = encryptForTransmission(testData, "test_key")
            val decrypted = decryptFromTransmission(encrypted, "test_key")
            
            if (testData != decrypted) {
                issues.add("Encryption/decryption validation failed")
            }
        } catch (e: Exception) {
            issues.add("Security system initialization failed: ${e.message}")
        }
        
        // Check if secure storage is working
        try {
            storeSecureValue("test_key", "test_value")
            val retrieved = getSecureValue("test_key")
            if (retrieved != "test_value") {
                issues.add("Secure storage validation failed")
            }
            // Clean up test data
            encryptedPrefs.edit().remove("test_key").apply()
        } catch (e: Exception) {
            issues.add("Secure storage system failed: ${e.message}")
        }
        
        return SecurityStatus(
            isSecure = issues.isEmpty(),
            securityIssues = issues
        )
    }
}

/**
 * Security validation result
 */
data class SecurityStatus(
    val isSecure: Boolean,
    val securityIssues: List<String>
)