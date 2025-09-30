package com.beaconledger.welltrack.data.health

import android.content.Context
import com.beaconledger.welltrack.config.EnvironmentConfig
import com.beaconledger.welltrack.config.SecureConfigLoader
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.security.MessageDigest
import java.util.*

/**
 * OAuth 2.0 PKCE validation tests for Garmin Connect integration
 * Validates security implementation and compliance with OAuth 2.0 standards
 */
class GarminOAuthValidationTest {

    private lateinit var garminConnectManager: GarminConnectManager
    private val mockContext = mockk<Context>()
    private val mockEnvironmentConfig = mockk<EnvironmentConfig>()
    private val mockSecureConfigLoader = mockk<SecureConfigLoader>()

    @Before
    fun setup() {
        // Mock environment configuration with valid test values
        every { mockEnvironmentConfig.garminClientId } returns "test_client_id_12345"
        every { mockEnvironmentConfig.garminRedirectUri } returns "welltrack://garmin/callback"
        
        // Create manager instance
        garminConnectManager = GarminConnectManager(
            mockContext,
            mockEnvironmentConfig,
            mockSecureConfigLoader
        )
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    // =============================================================================
    // OAuth 2.0 PKCE Security Validation Tests
    // =============================================================================

    @Test
    fun `PKCE code verifier should meet RFC 7636 requirements`() {
        // When
        val (_, codeVerifier1) = garminConnectManager.generateAuthorizationUrl()
        val (_, codeVerifier2) = garminConnectManager.generateAuthorizationUrl()
        val (_, codeVerifier3) = garminConnectManager.generateAuthorizationUrl()
        
        // Then - RFC 7636 Section 4.1 requirements
        // Code verifier MUST be 43-128 characters long
        assertTrue("Code verifier must be at least 43 characters", codeVerifier1.length >= 43)
        assertTrue("Code verifier must be at most 128 characters", codeVerifier1.length <= 128)
        
        // Code verifier MUST use unreserved characters [A-Z] / [a-z] / [0-9] / "-" / "." / "_" / "~"
        val validCharsRegex = Regex("^[A-Za-z0-9._~-]+$")
        assertTrue("Code verifier must use only unreserved characters", codeVerifier1.matches(validCharsRegex))
        
        // Code verifiers MUST be cryptographically random (should be unique)
        assertNotEquals("Code verifiers must be unique", codeVerifier1, codeVerifier2)
        assertNotEquals("Code verifiers must be unique", codeVerifier2, codeVerifier3)
        assertNotEquals("Code verifiers must be unique", codeVerifier1, codeVerifier3)
        
        // Verify entropy - no repeated patterns
        assertFalse("Code verifier should not have repeated patterns", hasRepeatedPatterns(codeVerifier1))
    }

    @Test
    fun `PKCE code challenge should be valid SHA256 hash`() {
        // Given
        val (authUrl, codeVerifier) = garminConnectManager.generateAuthorizationUrl()
        val codeChallenge = extractCodeChallenge(authUrl)
        
        // When - Manually compute expected challenge
        val expectedChallenge = computeCodeChallenge(codeVerifier)
        
        // Then
        assertNotNull("Code challenge should be present in URL", codeChallenge)
        assertEquals("Code challenge should match SHA256 hash of verifier", expectedChallenge, codeChallenge)
        
        // Verify challenge is base64url encoded (no padding)
        assertFalse("Code challenge should not contain padding", codeChallenge!!.contains("="))
        assertTrue("Code challenge should be base64url safe", codeChallenge.matches(Regex("^[A-Za-z0-9_-]+$")))
    }

    @Test
    fun `OAuth authorization URL should contain all required parameters`() {
        // When
        val (authUrl, _) = garminConnectManager.generateAuthorizationUrl()
        
        // Then - Verify all required OAuth 2.0 parameters are present
        assertTrue("Must contain client_id", authUrl.contains("client_id=test_client_id_12345"))
        assertTrue("Must contain response_type=code", authUrl.contains("response_type=code"))
        assertTrue("Must contain redirect_uri", authUrl.contains("redirect_uri=welltrack://garmin/callback"))
        assertTrue("Must contain scope", authUrl.contains("scope=ghs-read"))
        
        // PKCE specific parameters
        assertTrue("Must contain code_challenge", authUrl.contains("code_challenge="))
        assertTrue("Must contain code_challenge_method=S256", authUrl.contains("code_challenge_method=S256"))
        
        // Security parameters
        assertTrue("Must contain state parameter", authUrl.contains("state="))
        
        // Verify URL structure
        assertTrue("Must use HTTPS", authUrl.startsWith("https://"))
        assertTrue("Must use correct Garmin endpoint", authUrl.contains("apis.garmin.com"))
    }

    @Test
    fun `OAuth state parameter should be cryptographically secure`() {
        // When
        val (authUrl1, _) = garminConnectManager.generateAuthorizationUrl()
        val (authUrl2, _) = garminConnectManager.generateAuthorizationUrl()
        val (authUrl3, _) = garminConnectManager.generateAuthorizationUrl()
        
        val state1 = extractStateParameter(authUrl1)
        val state2 = extractStateParameter(authUrl2)
        val state3 = extractStateParameter(authUrl3)
        
        // Then
        assertNotNull("State parameter should be present", state1)
        assertNotNull("State parameter should be present", state2)
        assertNotNull("State parameter should be present", state3)
        
        // States should be unique (cryptographically random)
        assertNotEquals("State parameters must be unique", state1, state2)
        assertNotEquals("State parameters must be unique", state2, state3)
        assertNotEquals("State parameters must be unique", state1, state3)
        
        // State should be sufficient length for security (at least 16 characters)
        assertTrue("State should be at least 16 characters", state1!!.length >= 16)
        
        // State should be base64url encoded
        assertTrue("State should be base64url safe", state1.matches(Regex("^[A-Za-z0-9_-]+$")))
    }

    @Test
    fun `OAuth scope should be correctly specified for Garmin Health API`() {
        // When
        val (authUrl, _) = garminConnectManager.generateAuthorizationUrl()
        
        // Then
        assertTrue("Must request ghs-read scope for Garmin Health Services", 
            authUrl.contains("scope=ghs-read"))
        
        // Verify scope is properly URL encoded if needed
        assertFalse("Scope should not contain spaces", authUrl.contains("scope=ghs read"))
    }

    @Test
    fun `redirect URI should be properly configured for mobile app`() {
        // When
        val (authUrl, _) = garminConnectManager.generateAuthorizationUrl()
        
        // Then
        assertTrue("Redirect URI should use custom scheme", 
            authUrl.contains("redirect_uri=welltrack://garmin/callback"))
        
        // Verify URI is properly URL encoded
        val decodedUrl = java.net.URLDecoder.decode(authUrl, "UTF-8")
        assertTrue("Decoded URI should be valid", 
            decodedUrl.contains("welltrack://garmin/callback"))
    }

    // =============================================================================
    // Security Best Practices Validation
    // =============================================================================

    @Test
    fun `code verifier should have sufficient entropy`() {
        // Generate multiple code verifiers
        val verifiers = (1..100).map { 
            garminConnectManager.generateAuthorizationUrl().second 
        }
        
        // Check for uniqueness (no collisions)
        val uniqueVerifiers = verifiers.toSet()
        assertEquals("All code verifiers should be unique", verifiers.size, uniqueVerifiers.size)
        
        // Check character distribution (should not be biased)
        verifiers.forEach { verifier ->
            val charCounts = verifier.groupBy { it }.mapValues { it.value.size }
            val maxCount = charCounts.values.maxOrNull() ?: 0
            val avgCount = verifier.length / charCounts.size.toDouble()
            
            // No character should appear more than 3x the average (rough entropy check)
            assertTrue("Character distribution should be reasonably uniform", 
                maxCount <= (avgCount * 3).toInt())
        }
    }

    @Test
    fun `PKCE implementation should be resistant to timing attacks`() {
        // Measure time for multiple code challenge generations
        val times = mutableListOf<Long>()
        
        repeat(50) {
            val startTime = System.nanoTime()
            garminConnectManager.generateAuthorizationUrl()
            val endTime = System.nanoTime()
            times.add(endTime - startTime)
        }
        
        // Check that timing is relatively consistent (no obvious timing leaks)
        val avgTime = times.average()
        val maxDeviation = times.maxOf { kotlin.math.abs(it - avgTime) }
        
        // Allow up to 50% deviation (generous for test environment)
        assertTrue("Timing should be relatively consistent to prevent timing attacks",
            maxDeviation < avgTime * 0.5)
    }

    @Test
    fun `authorization URL should not leak sensitive information`() {
        // When
        val (authUrl, codeVerifier) = garminConnectManager.generateAuthorizationUrl()
        
        // Then - Verify sensitive data is not in URL
        assertFalse("URL should not contain code verifier", authUrl.contains(codeVerifier))
        assertFalse("URL should not contain client secret", authUrl.contains("client_secret"))
        assertFalse("URL should not contain any password", authUrl.lowercase().contains("password"))
        assertFalse("URL should not contain any token", authUrl.lowercase().contains("token"))
        
        // Verify only code challenge (not verifier) is present
        assertTrue("URL should contain code challenge", authUrl.contains("code_challenge="))
        assertFalse("URL should not contain code verifier", authUrl.contains("code_verifier="))
    }

    // =============================================================================
    // OAuth 2.0 Compliance Tests
    // =============================================================================

    @Test
    fun `authorization URL should comply with OAuth 2.0 RFC 6749`() {
        // When
        val (authUrl, _) = garminConnectManager.generateAuthorizationUrl()
        val uri = java.net.URI(authUrl)
        val params = parseQueryParameters(uri.query)
        
        // Then - RFC 6749 Section 4.1.1 requirements
        assertEquals("response_type must be 'code'", "code", params["response_type"])
        assertEquals("client_id must match configured value", "test_client_id_12345", params["client_id"])
        assertNotNull("redirect_uri must be present", params["redirect_uri"])
        assertNotNull("scope must be present", params["scope"])
        assertNotNull("state must be present", params["state"])
        
        // PKCE RFC 7636 requirements
        assertNotNull("code_challenge must be present", params["code_challenge"])
        assertEquals("code_challenge_method must be S256", "S256", params["code_challenge_method"])
    }

    @Test
    fun `PKCE implementation should comply with RFC 7636`() {
        // When
        val (authUrl, codeVerifier) = garminConnectManager.generateAuthorizationUrl()
        val codeChallenge = extractCodeChallenge(authUrl)
        
        // Then - RFC 7636 compliance checks
        
        // Section 4.1: Code verifier requirements
        assertTrue("Code verifier length must be 43-128 characters", 
            codeVerifier.length in 43..128)
        assertTrue("Code verifier must use unreserved characters only",
            codeVerifier.matches(Regex("^[A-Za-z0-9._~-]+$")))
        
        // Section 4.2: Code challenge requirements  
        assertNotNull("Code challenge must be present", codeChallenge)
        assertTrue("Code challenge must be base64url encoded",
            codeChallenge!!.matches(Regex("^[A-Za-z0-9_-]+$")))
        
        // Section 4.3: Challenge method must be S256
        assertTrue("Challenge method must be S256", authUrl.contains("code_challenge_method=S256"))
        
        // Verify challenge is correct SHA256 hash
        val expectedChallenge = computeCodeChallenge(codeVerifier)
        assertEquals("Code challenge must be SHA256 hash of verifier", expectedChallenge, codeChallenge)
    }

    // =============================================================================
    // Garmin-Specific Validation Tests
    // =============================================================================

    @Test
    fun `authorization URL should use correct Garmin Connect endpoints`() {
        // When
        val (authUrl, _) = garminConnectManager.generateAuthorizationUrl()
        
        // Then
        assertTrue("Must use Garmin APIs domain", authUrl.contains("apis.garmin.com"))
        assertTrue("Must use correct OAuth endpoint", authUrl.contains("/oauth-service/oauth/preauthorized"))
        assertTrue("Must use HTTPS", authUrl.startsWith("https://"))
    }

    @Test
    fun `OAuth scope should be appropriate for health data access`() {
        // When
        val (authUrl, _) = garminConnectManager.generateAuthorizationUrl()
        
        // Then
        assertTrue("Must request ghs-read scope for health data", authUrl.contains("scope=ghs-read"))
        
        // Verify we're not requesting excessive permissions
        assertFalse("Should not request write permissions", authUrl.contains("ghs-write"))
        assertFalse("Should not request admin permissions", authUrl.contains("admin"))
    }

    // =============================================================================
    // Error Handling and Edge Cases
    // =============================================================================

    @Test
    fun `should handle missing environment configuration gracefully`() {
        // Given - Manager with missing configuration
        every { mockEnvironmentConfig.garminClientId } returns ""
        every { mockEnvironmentConfig.garminRedirectUri } returns ""
        
        val managerWithMissingConfig = GarminConnectManager(
            mockContext,
            mockEnvironmentConfig,
            mockSecureConfigLoader
        )
        
        // When & Then - Should not crash, but may produce invalid URLs
        try {
            val (authUrl, _) = managerWithMissingConfig.generateAuthorizationUrl()
            // URL may be invalid but should not crash
            assertNotNull("Should return some URL even with missing config", authUrl)
        } catch (e: Exception) {
            // If it throws, should be a configuration exception, not a crash
            assertTrue("Should be a configuration-related exception", 
                e.message?.contains("config") == true || e.message?.contains("client") == true)
        }
    }

    // =============================================================================
    // Helper Methods
    // =============================================================================

    private fun extractCodeChallenge(authUrl: String): String? {
        val regex = Regex("code_challenge=([^&]+)")
        return regex.find(authUrl)?.groupValues?.get(1)
    }

    private fun extractStateParameter(authUrl: String): String? {
        val regex = Regex("state=([^&]+)")
        return regex.find(authUrl)?.groupValues?.get(1)
    }

    private fun computeCodeChallenge(codeVerifier: String): String {
        val bytes = codeVerifier.toByteArray(Charsets.US_ASCII)
        val digest = MessageDigest.getInstance("SHA-256").digest(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(digest)
    }

    private fun hasRepeatedPatterns(input: String): Boolean {
        // Simple check for obvious repeated patterns
        for (i in 2..8) {
            val pattern = input.take(i)
            if (input.length >= pattern.length * 3) {
                val repeated = pattern.repeat(3)
                if (input.contains(repeated)) {
                    return true
                }
            }
        }
        return false
    }

    private fun parseQueryParameters(query: String?): Map<String, String> {
        if (query == null) return emptyMap()
        
        return query.split("&").associate { param ->
            val parts = param.split("=", limit = 2)
            if (parts.size == 2) {
                parts[0] to java.net.URLDecoder.decode(parts[1], "UTF-8")
            } else {
                parts[0] to ""
            }
        }
    }
}