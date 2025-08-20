package com.beaconledger.welltrack.data.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import android.util.Base64
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages encryption and decryption of sensitive health data using Android Keystore
 */
@Singleton
class EncryptionManager @Inject constructor() {
    
    companion object {
        private const val KEYSTORE_ALIAS = "WellTrackHealthDataKey"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_IV_LENGTH = 12
        private const val GCM_TAG_LENGTH = 16
    }
    
    private val keyStore: KeyStore by lazy {
        KeyStore.getInstance(ANDROID_KEYSTORE).apply {
            load(null)
        }
    }
    
    init {
        generateKeyIfNeeded()
    }
    
    /**
     * Encrypts sensitive data using AES-GCM encryption
     */
    fun encrypt(data: String): EncryptedData? {
        return try {
            val secretKey = getSecretKey()
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            
            val iv = cipher.iv
            val encryptedBytes = cipher.doFinal(data.toByteArray(Charsets.UTF_8))
            
            EncryptedData(
                encryptedData = Base64.encodeToString(encryptedBytes, Base64.DEFAULT),
                iv = Base64.encodeToString(iv, Base64.DEFAULT)
            )
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Decrypts sensitive data using AES-GCM decryption
     */
    fun decrypt(encryptedData: EncryptedData): String? {
        return try {
            val secretKey = getSecretKey()
            val cipher = Cipher.getInstance(TRANSFORMATION)
            
            val iv = Base64.decode(encryptedData.iv, Base64.DEFAULT)
            val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH * 8, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec)
            
            val encryptedBytes = Base64.decode(encryptedData.encryptedData, Base64.DEFAULT)
            val decryptedBytes = cipher.doFinal(encryptedBytes)
            
            String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Encrypts a list of sensitive fields in a data object
     */
    fun encryptSensitiveFields(data: Map<String, Any?>, sensitiveFields: Set<String>): Map<String, Any?> {
        return data.mapValues { (key, value) ->
            if (key in sensitiveFields && value is String) {
                encrypt(value)?.let { encrypted ->
                    mapOf(
                        "encrypted" to encrypted.encryptedData,
                        "iv" to encrypted.iv,
                        "isEncrypted" to true
                    )
                } ?: value
            } else {
                value
            }
        }
    }
    
    /**
     * Decrypts sensitive fields in a data object
     */
    fun decryptSensitiveFields(data: Map<String, Any?>, sensitiveFields: Set<String>): Map<String, Any?> {
        return data.mapValues { (key, value) ->
            if (key in sensitiveFields && value is Map<*, *>) {
                val encryptedMap = value as? Map<String, Any?>
                if (encryptedMap?.get("isEncrypted") == true) {
                    val encryptedData = EncryptedData(
                        encryptedData = encryptedMap["encrypted"] as? String ?: "",
                        iv = encryptedMap["iv"] as? String ?: ""
                    )
                    decrypt(encryptedData) ?: value
                } else {
                    value
                }
            } else {
                value
            }
        }
    }
    
    private fun generateKeyIfNeeded() {
        if (!keyStore.containsAlias(KEYSTORE_ALIAS)) {
            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
            val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                KEYSTORE_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setUserAuthenticationRequired(false)
                .build()
            
            keyGenerator.init(keyGenParameterSpec)
            keyGenerator.generateKey()
        }
    }
    
    private fun getSecretKey(): SecretKey {
        return keyStore.getKey(KEYSTORE_ALIAS, null) as SecretKey
    }
}

/**
 * Represents encrypted data with initialization vector
 */
data class EncryptedData(
    val encryptedData: String,
    val iv: String
)

/**
 * Defines which fields should be encrypted for each entity type
 */
object SensitiveFieldsConfig {
    val HEALTH_METRIC_FIELDS = setOf("value", "metadata", "notes")
    val BIOMARKER_FIELDS = setOf("value", "notes", "testResults")
    val USER_FIELDS = setOf("email", "phoneNumber", "medicalNotes")
    val MEAL_FIELDS = setOf("notes", "healthNotes")
    val SUPPLEMENT_FIELDS = setOf("dosageNotes", "sideEffects")
}