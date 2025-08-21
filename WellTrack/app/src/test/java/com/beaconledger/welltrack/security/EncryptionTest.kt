package com.beaconledger.welltrack.security

import com.beaconledger.welltrack.data.security.EncryptionManager
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@RunWith(MockitoJUnitRunner::class)
class EncryptionTest {

    private lateinit var encryptionManager: EncryptionManager

    @Before
    fun setup() {
        encryptionManager = EncryptionManager()
    }

    @Test
    fun `encrypt and decrypt data successfully`() {
        val originalData = "Sensitive health data: Blood pressure 120/80"
        
        // Encrypt data
        val encryptedData = encryptionManager.encrypt(originalData)
        
        // Verify encryption worked
        assertNotNull(encryptedData)
        assertNotEquals(originalData, encryptedData)
        
        // Decrypt data
        val decryptedData = encryptionManager.decrypt(encryptedData)
        
        // Verify decryption worked
        assertEquals(originalData, decryptedData)
    }

    @Test
    fun `encrypt produces different output for same input`() {
        val data = "Test health metric"
        
        val encrypted1 = encryptionManager.encrypt(data)
        val encrypted2 = encryptionManager.encrypt(data)
        
        // Should produce different encrypted values due to IV
        assertNotEquals(encrypted1, encrypted2)
        
        // But both should decrypt to original
        assertEquals(data, encryptionManager.decrypt(encrypted1))
        assertEquals(data, encryptionManager.decrypt(encrypted2))
    }

    @Test
    fun `encrypt handles empty string`() {
        val emptyData = ""
        
        val encrypted = encryptionManager.encrypt(emptyData)
        val decrypted = encryptionManager.decrypt(encrypted)
        
        assertEquals(emptyData, decrypted)
    }

    @Test
    fun `encrypt handles large data`() {
        val largeData = "Large health data: " + "x".repeat(10000)
        
        val encrypted = encryptionManager.encrypt(largeData)
        val decrypted = encryptionManager.decrypt(encrypted)
        
        assertEquals(largeData, decrypted)
    }

    @Test
    fun `encrypt handles special characters`() {
        val specialData = "Health data with special chars: àáâãäåæçèéêë 中文 🏥💊"
        
        val encrypted = encryptionManager.encrypt(specialData)
        val decrypted = encryptionManager.decrypt(encrypted)
        
        assertEquals(specialData, decrypted)
    }

    @Test
    fun `generateSecureKey creates valid key`() {
        val key1 = encryptionManager.generateSecureKey()
        val key2 = encryptionManager.generateSecureKey()
        
        // Keys should be different
        assertNotEquals(key1, key2)
        
        // Keys should have appropriate length (256 bits = 32 bytes = 44 base64 chars)
        assertTrue(key1.length >= 32)
        assertTrue(key2.length >= 32)
    }

    @Test
    fun `hashPassword creates secure hash`() {
        val password = "userPassword123"
        
        val hash1 = encryptionManager.hashPassword(password)
        val hash2 = encryptionManager.hashPassword(password)
        
        // Hashes should be different due to salt
        assertNotEquals(hash1, hash2)
        
        // But both should verify correctly
        assertTrue(encryptionManager.verifyPassword(password, hash1))
        assertTrue(encryptionManager.verifyPassword(password, hash2))
    }

    @Test
    fun `verifyPassword rejects wrong password`() {
        val correctPassword = "correctPassword123"
        val wrongPassword = "wrongPassword456"
        
        val hash = encryptionManager.hashPassword(correctPassword)
        
        assertTrue(encryptionManager.verifyPassword(correctPassword, hash))
        assertTrue(!encryptionManager.verifyPassword(wrongPassword, hash))
    }

    @Test
    fun `sanitizeInput removes dangerous characters`() {
        val dangerousInput = "<script>alert('xss')</script>'; DROP TABLE users; --"
        
        val sanitized = encryptionManager.sanitizeInput(dangerousInput)
        
        // Should not contain dangerous characters
        assertTrue(!sanitized.contains("<script>"))
        assertTrue(!sanitized.contains("DROP TABLE"))
        assertTrue(!sanitized.contains("--"))
    }

    @Test
    fun `validateInput rejects malicious patterns`() {
        val validInput = "Normal health data entry"
        val sqlInjection = "'; DROP TABLE meals; --"
        val xssAttempt = "<script>alert('xss')</script>"
        
        assertTrue(encryptionManager.validateInput(validInput))
        assertTrue(!encryptionManager.validateInput(sqlInjection))
        assertTrue(!encryptionManager.validateInput(xssAttempt))
    }
}