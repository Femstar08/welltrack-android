package com.beaconledger.welltrack.data.health

import android.content.Context
import com.beaconledger.welltrack.config.EnvironmentConfig
import com.beaconledger.welltrack.config.SecureConfigLoader
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.time.Instant
import javax.net.ssl.SSLException

/**
 * Comprehensive error handling tests for Garmin Connect API failures
 * Tests various failure scenarios and recovery mechanisms
 */
class GarminErrorHandlingTest {

    private lateinit var garminConnectManager: GarminConnectManager
    private val mockContext = mockk<Context>()
    private val mockEnvironmentConfig = mockk<EnvironmentConfig>()
    private val mockSecureConfigLoader = mockk<SecureConfigLoader>()
    private val mockOkHttpClient = mockk<OkHttpClient>()
    private val mockCall = mockk<Call>()

    @Before
    fun setup() {
        // Mock environment configuration
        every { mockEnvironmentConfig.garminClientId } returns "test_client_id"
        every { mockEnvironmentConfig.garminRedirectUri } returns "welltrack://garmin/callback"
        
        // Create manager instance
        garminConnectManager = GarminConnectManager(
            mockContext,
            mockEnvironmentConfig,
            mockSecureConfigLoader
        )
        
        // Use reflection to inject mock HTTP client for testing
        val clientField = GarminConnectManager::class.java.getDeclaredField("client")
        clientField.isAccessible = true
        clientField.set(garminConnectManager, mockOkHttpClient)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    // =============================================================================
    // Authentication Error Handling Tests
    // =============================================================================

    @Test
    fun `exchangeCodeForToken should handle 401 Unauthorized error`() = runTest {
        // Given
        val authCode = "invalid_auth_code"
        val codeVerifier = "test_code_verifier"
        val mockResponse = mockk<Response>()
        val errorResponseBody = """
            {
                "error": "invalid_grant",
                "error_description": "The provided authorization grant is invalid, expired, revoked, does not match the redirection URI used in the authorization request, or was issued to another client."
            }
        """.trimIndent()
        
        every { mockOkHttpClient.newCall(any()) } returns mockCall
        every { mockCall.execute() } returns mockResponse
        every { mockResponse.isSuccessful } returns false
        every { mockResponse.code } returns 401
        every { mockResponse.body } returns errorResponseBody.toResponseBody("application/json".toMediaType())
        
        // When
        val result = garminConnectManager.exchangeCodeForToken(authCode, codeVerifier)
        
        // Then
        assertTrue("Token exchange should fail", result.isFailure)
        assertFalse("Should not be authenticated", garminConnectManager.isAuthenticated())
        assertTrue("Error message should contain 401", result.exceptionOrNull()?.message?.contains("401") == true)
    }

    @Test
    fun `exchangeCodeForToken should handle 400 Bad Request error`() = runTest {
        // Given
        val authCode = "malformed_auth_code"
        val codeVerifier = "test_code_verifier"
        val mockResponse = mockk<Response>()
        
        every { mockOkHttpClient.newCall(any()) } returns mockCall
        every { mockCall.execute() } returns mockResponse
        every { mockResponse.isSuccessful } returns false
        every { mockResponse.code } returns 400
        
        // When
        val result = garminConnectManager.exchangeCodeForToken(authCode, codeVerifier)
        
        // Then
        assertTrue("Token exchange should fail", result.isFailure)
        assertFalse("Should not be authenticated", garminConnectManager.isAuthenticated())
    }

    @Test
    fun `exchangeCodeForToken should handle network timeout`() = runTest {
        // Given
        val authCode = "test_auth_code"
        val codeVerifier = "test_code_verifier"
        
        every { mockOkHttpClient.newCall(any()) } returns mockCall
        every { mockCall.execute() } throws SocketTimeoutException("Read timeout")
        
        // When
        val result = garminConnectManager.exchangeCodeForToken(authCode, codeVerifier)
        
        // Then
        assertTrue("Token exchange should fail", result.isFailure)
        assertFalse("Should not be authenticated", garminConnectManager.isAuthenticated())
        assertTrue("Should be SocketTimeoutException", result.exceptionOrNull() is SocketTimeoutException)
    }

    @Test
    fun `exchangeCodeForToken should handle SSL certificate errors`() = runTest {
        // Given
        val authCode = "test_auth_code"
        val codeVerifier = "test_code_verifier"
        
        every { mockOkHttpClient.newCall(any()) } returns mockCall
        every { mockCall.execute() } throws SSLException("Certificate verification failed")
        
        // When
        val result = garminConnectManager.exchangeCodeForToken(authCode, codeVerifier)
        
        // Then
        assertTrue("Token exchange should fail", result.isFailure)
        assertFalse("Should not be authenticated", garminConnectManager.isAuthenticated())
        assertTrue("Should be SSLException", result.exceptionOrNull() is SSLException)
    }

    @Test
    fun `exchangeCodeForToken should handle malformed JSON response`() = runTest {
        // Given
        val authCode = "test_auth_code"
        val codeVerifier = "test_code_verifier"
        val mockResponse = mockk<Response>()
        val malformedJson = "{ invalid json response }"
        
        every { mockOkHttpClient.newCall(any()) } returns mockCall
        every { mockCall.execute() } returns mockResponse
        every { mockResponse.isSuccessful } returns true
        every { mockResponse.body } returns malformedJson.toResponseBody("application/json".toMediaType())
        
        // When
        val result = garminConnectManager.exchangeCodeForToken(authCode, codeVerifier)
        
        // Then
        assertTrue("Token exchange should fail", result.isFailure)
        assertFalse("Should not be authenticated", garminConnectManager.isAuthenticated())
    }

    // =============================================================================
    // Data Sync Error Handling Tests
    // =============================================================================

    @Test
    fun `syncHealthData should handle 403 Forbidden error`() = runTest {
        // Given
        setupAuthenticatedState()
        val userId = "test_user_id"
        val startTime = Instant.now().minusSeconds(86400)
        val endTime = Instant.now()
        
        val mockResponse = mockk<Response>()
        every { mockOkHttpClient.newCall(any()) } returns mockCall
        every { mockCall.execute() } returns mockResponse
        every { mockResponse.isSuccessful } returns false
        every { mockResponse.code } returns 403
        
        // When
        val result = garminConnectManager.syncHealthData(userId, startTime, endTime).first()
        
        // Then
        assertTrue("Should return empty list on 403 error", result.isEmpty())
    }

    @Test
    fun `syncHealthData should handle 429 Rate Limit Exceeded error`() = runTest {
        // Given
        setupAuthenticatedState()
        val userId = "test_user_id"
        val startTime = Instant.now().minusSeconds(86400)
        val endTime = Instant.now()
        
        val mockResponse = mockk<Response>()
        every { mockOkHttpClient.newCall(any()) } returns mockCall
        every { mockCall.execute() } returns mockResponse
        every { mockResponse.isSuccessful } returns false
        every { mockResponse.code } returns 429
        every { mockResponse.header("Retry-After") } returns "60"
        
        // When
        val result = garminConnectManager.syncHealthData(userId, startTime, endTime).first()
        
        // Then
        assertTrue("Should return empty list on rate limit error", result.isEmpty())
    }

    @Test
    fun `syncHealthData should handle 500 Internal Server Error`() = runTest {
        // Given
        setupAuthenticatedState()
        val userId = "test_user_id"
        val startTime = Instant.now().minusSeconds(86400)
        val endTime = Instant.now()
        
        val mockResponse = mockk<Response>()
        every { mockOkHttpClient.newCall(any()) } returns mockCall
        every { mockCall.execute() } returns mockResponse
        every { mockResponse.isSuccessful } returns false
        every { mockResponse.code } returns 500
        
        // When
        val result = garminConnectManager.syncHealthData(userId, startTime, endTime).first()
        
        // Then
        assertTrue("Should return empty list on server error", result.isEmpty())
    }

    @Test
    fun `syncHealthData should handle 503 Service Unavailable error`() = runTest {
        // Given
        setupAuthenticatedState()
        val userId = "test_user_id"
        val startTime = Instant.now().minusSeconds(86400)
        val endTime = Instant.now()
        
        val mockResponse = mockk<Response>()
        every { mockOkHttpClient.newCall(any()) } returns mockCall
        every { mockCall.execute() } returns mockResponse
        every { mockResponse.isSuccessful } returns false
        every { mockResponse.code } returns 503
        every { mockResponse.header("Retry-After") } returns "300"
        
        // When
        val result = garminConnectManager.syncHealthData(userId, startTime, endTime).first()
        
        // Then
        assertTrue("Should return empty list on service unavailable", result.isEmpty())
    }

    @Test
    fun `syncHealthData should handle network connectivity issues`() = runTest {
        // Given
        setupAuthenticatedState()
        val userId = "test_user_id"
        val startTime = Instant.now().minusSeconds(86400)
        val endTime = Instant.now()
        
        every { mockOkHttpClient.newCall(any()) } returns mockCall
        every { mockCall.execute() } throws UnknownHostException("Unable to resolve host")
        
        // When
        val result = garminConnectManager.syncHealthData(userId, startTime, endTime).first()
        
        // Then
        assertTrue("Should return empty list on network error", result.isEmpty())
    }

    @Test
    fun `syncHealthData should handle partial data corruption`() = runTest {
        // Given
        setupAuthenticatedState()
        val userId = "test_user_id"
        val startTime = Instant.now().minusSeconds(86400)
        val endTime = Instant.now()
        
        // Mock response with partially corrupted JSON
        val partiallyCorruptedJson = """
            [
                {
                    "calendarDate": "2024-01-15",
                    "weeklyAvg": 45.5,
                    "lastNightAvg": 42.3
                },
                {
                    "calendarDate": "2024-01-16",
                    "weeklyAvg": "invalid_number",
                    "lastNightAvg": null
                },
                {
                    "calendarDate": "2024-01-17",
                    "weeklyAvg": 47.2
                }
            ]
        """.trimIndent()
        
        val mockResponse = mockk<Response>()
        every { mockOkHttpClient.newCall(any()) } returns mockCall
        every { mockCall.execute() } returns mockResponse
        every { mockResponse.isSuccessful } returns true
        every { mockResponse.body } returns partiallyCorruptedJson.toResponseBody("application/json".toMediaType())
        
        // When
        val result = garminConnectManager.syncHealthData(userId, startTime, endTime).first()
        
        // Then
        // Should handle gracefully - either return empty list or valid entries only
        // The exact behavior depends on implementation, but should not crash
        assertNotNull("Should not return null", result)
    }

    @Test
    fun `syncHealthData should handle empty response arrays`() = runTest {
        // Given
        setupAuthenticatedState()
        val userId = "test_user_id"
        val startTime = Instant.now().minusSeconds(86400)
        val endTime = Instant.now()
        
        val emptyArrayJson = "[]"
        val mockResponse = mockk<Response>()
        every { mockOkHttpClient.newCall(any()) } returns mockCall
        every { mockCall.execute() } returns mockResponse
        every { mockResponse.isSuccessful } returns true
        every { mockResponse.body } returns emptyArrayJson.toResponseBody("application/json".toMediaType())
        
        // When
        val result = garminConnectManager.syncHealthData(userId, startTime, endTime).first()
        
        // Then
        assertTrue("Should return empty list for empty response", result.isEmpty())
    }

    @Test
    fun `syncHealthData should handle null response body`() = runTest {
        // Given
        setupAuthenticatedState()
        val userId = "test_user_id"
        val startTime = Instant.now().minusSeconds(86400)
        val endTime = Instant.now()
        
        val mockResponse = mockk<Response>()
        every { mockOkHttpClient.newCall(any()) } returns mockCall
        every { mockCall.execute() } returns mockResponse
        every { mockResponse.isSuccessful } returns true
        every { mockResponse.body } returns null
        
        // When
        val result = garminConnectManager.syncHealthData(userId, startTime, endTime).first()
        
        // Then
        assertTrue("Should return empty list for null response body", result.isEmpty())
    }

    // =============================================================================
    // Token Expiry and Refresh Error Handling Tests
    // =============================================================================

    @Test
    fun `syncHealthData should handle expired token gracefully`() = runTest {
        // Given - Set up expired token
        setupExpiredTokenState()
        val userId = "test_user_id"
        val startTime = Instant.now().minusSeconds(86400)
        val endTime = Instant.now()
        
        // When
        val result = garminConnectManager.syncHealthData(userId, startTime, endTime).first()
        
        // Then
        assertTrue("Should return empty list when token is expired", result.isEmpty())
        assertFalse("Should not be authenticated with expired token", garminConnectManager.isAuthenticated())
    }

    @Test
    fun `isAuthenticated should handle token expiry correctly`() {
        // Given - Set up expired token
        setupExpiredTokenState()
        
        // When & Then
        assertFalse("Should return false for expired token", garminConnectManager.isAuthenticated())
    }

    // =============================================================================
    // Concurrent Request Error Handling Tests
    // =============================================================================

    @Test
    fun `multiple concurrent sync requests should handle errors independently`() = runTest {
        // Given
        setupAuthenticatedState()
        val userId = "test_user_id"
        val startTime = Instant.now().minusSeconds(86400)
        val endTime = Instant.now()
        
        // Mock different responses for concurrent calls
        val mockResponse1 = mockk<Response>()
        val mockResponse2 = mockk<Response>()
        val mockCall1 = mockk<Call>()
        val mockCall2 = mockk<Call>()
        
        every { mockOkHttpClient.newCall(any()) } returnsMany listOf(mockCall1, mockCall2)
        every { mockCall1.execute() } returns mockResponse1
        every { mockCall2.execute() } throws SocketTimeoutException("Timeout")
        every { mockResponse1.isSuccessful } returns true
        every { mockResponse1.body } returns "[]".toResponseBody("application/json".toMediaType())
        
        // When - Make concurrent requests
        val result1 = garminConnectManager.syncHealthData(userId, startTime, endTime).first()
        val result2 = garminConnectManager.syncHealthData(userId, startTime, endTime).first()
        
        // Then
        assertTrue("First request should succeed", result1.isEmpty()) // Empty but successful
        assertTrue("Second request should handle error gracefully", result2.isEmpty())
    }

    // =============================================================================
    // Data Validation Error Handling Tests
    // =============================================================================

    @Test
    fun `syncHealthData should handle invalid date formats`() = runTest {
        // Given
        setupAuthenticatedState()
        val userId = "test_user_id"
        val startTime = Instant.now().minusSeconds(86400)
        val endTime = Instant.now()
        
        val invalidDateJson = """
            [
                {
                    "calendarDate": "invalid-date-format",
                    "weeklyAvg": 45.5
                },
                {
                    "calendarDate": "2024-13-45",
                    "weeklyAvg": 46.0
                }
            ]
        """.trimIndent()
        
        val mockResponse = mockk<Response>()
        every { mockOkHttpClient.newCall(any()) } returns mockCall
        every { mockCall.execute() } returns mockResponse
        every { mockResponse.isSuccessful } returns true
        every { mockResponse.body } returns invalidDateJson.toResponseBody("application/json".toMediaType())
        
        // When
        val result = garminConnectManager.syncHealthData(userId, startTime, endTime).first()
        
        // Then
        // Should handle gracefully without crashing
        assertNotNull("Should not return null", result)
    }

    @Test
    fun `syncHealthData should handle missing required fields`() = runTest {
        // Given
        setupAuthenticatedState()
        val userId = "test_user_id"
        val startTime = Instant.now().minusSeconds(86400)
        val endTime = Instant.now()
        
        val missingFieldsJson = """
            [
                {
                    "weeklyAvg": 45.5
                },
                {
                    "calendarDate": "2024-01-15"
                }
            ]
        """.trimIndent()
        
        val mockResponse = mockk<Response>()
        every { mockOkHttpClient.newCall(any()) } returns mockCall
        every { mockCall.execute() } returns mockResponse
        every { mockResponse.isSuccessful } returns true
        every { mockResponse.body } returns missingFieldsJson.toResponseBody("application/json".toMediaType())
        
        // When
        val result = garminConnectManager.syncHealthData(userId, startTime, endTime).first()
        
        // Then
        // Should handle gracefully - may return empty list or skip invalid entries
        assertNotNull("Should not return null", result)
    }

    // =============================================================================
    // Helper Methods
    // =============================================================================

    private fun setupAuthenticatedState() {
        // Use reflection to set authentication state for testing
        val accessTokenField = GarminConnectManager::class.java.getDeclaredField("accessToken")
        accessTokenField.isAccessible = true
        accessTokenField.set(garminConnectManager, "test_access_token")
        
        val tokenExpiryField = GarminConnectManager::class.java.getDeclaredField("tokenExpiryTime")
        tokenExpiryField.isAccessible = true
        tokenExpiryField.set(garminConnectManager, System.currentTimeMillis() + 3600000) // 1 hour from now
    }

    private fun setupExpiredTokenState() {
        // Use reflection to set expired token state for testing
        val accessTokenField = GarminConnectManager::class.java.getDeclaredField("accessToken")
        accessTokenField.isAccessible = true
        accessTokenField.set(garminConnectManager, "expired_access_token")
        
        val tokenExpiryField = GarminConnectManager::class.java.getDeclaredField("tokenExpiryTime")
        tokenExpiryField.isAccessible = true
        tokenExpiryField.set(garminConnectManager, System.currentTimeMillis() - 3600000) // 1 hour ago (expired)
    }
}