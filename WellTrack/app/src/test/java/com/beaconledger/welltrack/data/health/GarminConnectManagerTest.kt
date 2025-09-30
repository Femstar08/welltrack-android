package com.beaconledger.welltrack.data.health

import android.content.Context
import com.beaconledger.welltrack.config.EnvironmentConfig
import com.beaconledger.welltrack.config.SecureConfigLoader
import com.beaconledger.welltrack.data.model.DataSource
import com.beaconledger.welltrack.data.model.HealthMetricType
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
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Comprehensive test suite for Garmin Connect integration
 * Tests OAuth 2.0 PKCE flow, data synchronization, and error handling
 */
class GarminConnectManagerTest {

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
    // OAuth 2.0 PKCE Authentication Tests
    // =============================================================================

    @Test
    fun `generateAuthorizationUrl should create valid PKCE authorization URL`() {
        // When
        val (authUrl, codeVerifier) = garminConnectManager.generateAuthorizationUrl()
        
        // Then
        assertNotNull(authUrl)
        assertNotNull(codeVerifier)
        assertTrue("Authorization URL should contain client_id", authUrl.contains("client_id=test_client_id"))
        assertTrue("Authorization URL should contain response_type=code", authUrl.contains("response_type=code"))
        assertTrue("Authorization URL should contain scope=ghs-read", authUrl.contains("scope=ghs-read"))
        assertTrue("Authorization URL should contain redirect_uri", authUrl.contains("redirect_uri=welltrack://garmin/callback"))
        assertTrue("Authorization URL should contain code_challenge", authUrl.contains("code_challenge="))
        assertTrue("Authorization URL should contain code_challenge_method=S256", authUrl.contains("code_challenge_method=S256"))
        assertTrue("Authorization URL should contain state", authUrl.contains("state="))
        
        // Code verifier should be base64url encoded and 43-128 characters
        assertTrue("Code verifier should be valid length", codeVerifier.length in 43..128)
        assertTrue("Code verifier should be base64url encoded", codeVerifier.matches(Regex("[A-Za-z0-9_-]+")))
    }

    @Test
    fun `exchangeCodeForToken should successfully exchange authorization code for tokens`() = runTest {
        // Given
        val authCode = "test_auth_code"
        val codeVerifier = "test_code_verifier"
        val mockResponse = mockk<Response>()
        val tokenResponseJson = """
            {
                "access_token": "test_access_token",
                "refresh_token": "test_refresh_token",
                "expires_in": 3600,
                "token_type": "Bearer"
            }
        """.trimIndent()
        
        every { mockOkHttpClient.newCall(any()) } returns mockCall
        every { mockCall.execute() } returns mockResponse
        every { mockResponse.isSuccessful } returns true
        every { mockResponse.body } returns tokenResponseJson.toResponseBody("application/json".toMediaType())
        
        // When
        val result = garminConnectManager.exchangeCodeForToken(authCode, codeVerifier)
        
        // Then
        assertTrue("Token exchange should succeed", result.isSuccess)
        assertTrue("Should be authenticated after token exchange", garminConnectManager.isAuthenticated())
        
        // Verify the request was made correctly
        verify { mockOkHttpClient.newCall(match { request ->
            request.url.toString().contains("oauth/token") &&
            request.method == "POST"
        }) }
    }

    @Test
    fun `exchangeCodeForToken should handle authentication failure`() = runTest {
        // Given
        val authCode = "invalid_auth_code"
        val codeVerifier = "test_code_verifier"
        val mockResponse = mockk<Response>()
        
        every { mockOkHttpClient.newCall(any()) } returns mockCall
        every { mockCall.execute() } returns mockResponse
        every { mockResponse.isSuccessful } returns false
        every { mockResponse.code } returns 401
        
        // When
        val result = garminConnectManager.exchangeCodeForToken(authCode, codeVerifier)
        
        // Then
        assertTrue("Token exchange should fail", result.isFailure)
        assertFalse("Should not be authenticated after failed token exchange", garminConnectManager.isAuthenticated())
    }

    @Test
    fun `isAuthenticated should return false when no token is set`() {
        // Given - fresh manager instance
        
        // When & Then
        assertFalse("Should not be authenticated without token", garminConnectManager.isAuthenticated())
    }

    // =============================================================================
    // Health Data Synchronization Tests
    // =============================================================================

    @Test
    fun `syncHealthData should return empty list when not authenticated`() = runTest {
        // Given
        val userId = "test_user_id"
        val startTime = Instant.now().minusSeconds(86400) // 24 hours ago
        val endTime = Instant.now()
        
        // When
        val result = garminConnectManager.syncHealthData(userId, startTime, endTime).first()
        
        // Then
        assertTrue("Should return empty list when not authenticated", result.isEmpty())
    }

    @Test
    fun `syncHRVData should parse HRV data correctly`() = runTest {
        // Given
        setupAuthenticatedState()
        val userId = "test_user_id"
        val startTime = Instant.now().minusSeconds(86400)
        val endTime = Instant.now()
        
        val hrvResponseJson = """
            [
                {
                    "calendarDate": "2024-01-15",
                    "weeklyAvg": 45.5,
                    "lastNightAvg": 42.3,
                    "lastNight5MinHigh": 48.7,
                    "baseline": {
                        "lowUpper": 35.0,
                        "balancedLower": 36.0,
                        "balancedUpper": 55.0,
                        "markerValue": 45.0
                    }
                }
            ]
        """.trimIndent()
        
        val mockResponse = mockk<Response>()
        every { mockOkHttpClient.newCall(any()) } returns mockCall
        every { mockCall.execute() } returns mockResponse
        every { mockResponse.isSuccessful } returns true
        every { mockResponse.body } returns hrvResponseJson.toResponseBody("application/json".toMediaType())
        
        // When
        val result = garminConnectManager.syncHealthData(userId, startTime, endTime).first()
        
        // Then
        assertFalse("Should return HRV data", result.isEmpty())
        val hrvMetric = result.find { it.type == HealthMetricType.HRV }
        assertNotNull("Should contain HRV metric", hrvMetric)
        assertEquals("HRV value should match", 45.5, hrvMetric!!.value, 0.01)
        assertEquals("HRV unit should be ms", "ms", hrvMetric.unit)
        assertEquals("HRV source should be Garmin", DataSource.GARMIN, hrvMetric.source)
        assertTrue("HRV metadata should contain additional data", hrvMetric.metadata?.contains("lastNightAvg") == true)
    }

    @Test
    fun `syncRecoveryData should parse recovery data correctly`() = runTest {
        // Given
        setupAuthenticatedState()
        val userId = "test_user_id"
        val startTime = Instant.now().minusSeconds(86400)
        val endTime = Instant.now()
        
        val recoveryResponseJson = """
            [
                {
                    "calendarDate": "2024-01-15",
                    "recoveryScore": 85.0,
                    "sleepScore": 78.0,
                    "hrvScore": 92.0
                }
            ]
        """.trimIndent()
        
        val mockResponse = mockk<Response>()
        every { mockOkHttpClient.newCall(any()) } returns mockCall
        every { mockCall.execute() } returns mockResponse
        every { mockResponse.isSuccessful } returns true
        every { mockResponse.body } returns recoveryResponseJson.toResponseBody("application/json".toMediaType())
        
        // When
        val result = garminConnectManager.syncHealthData(userId, startTime, endTime).first()
        
        // Then
        val recoveryMetric = result.find { it.type == HealthMetricType.TRAINING_RECOVERY }
        assertNotNull("Should contain recovery metric", recoveryMetric)
        assertEquals("Recovery value should match", 85.0, recoveryMetric!!.value, 0.01)
        assertEquals("Recovery unit should be score", "score", recoveryMetric.unit)
        assertTrue("Recovery metadata should contain sleep and HRV scores", 
            recoveryMetric.metadata?.contains("sleepScore") == true &&
            recoveryMetric.metadata?.contains("hrvScore") == true)
    }

    @Test
    fun `syncStressData should parse stress data correctly`() = runTest {
        // Given
        setupAuthenticatedState()
        val userId = "test_user_id"
        val startTime = Instant.now().minusSeconds(86400)
        val endTime = Instant.now()
        
        val stressResponseJson = """
            [
                {
                    "calendarDate": "2024-01-15",
                    "overallStressLevel": 25.0,
                    "restStressLevel": 20.0,
                    "activityStressLevel": 35.0
                }
            ]
        """.trimIndent()
        
        val mockResponse = mockk<Response>()
        every { mockOkHttpClient.newCall(any()) } returns mockCall
        every { mockCall.execute() } returns mockResponse
        every { mockResponse.isSuccessful } returns true
        every { mockResponse.body } returns stressResponseJson.toResponseBody("application/json".toMediaType())
        
        // When
        val result = garminConnectManager.syncHealthData(userId, startTime, endTime).first()
        
        // Then
        val stressMetric = result.find { it.type == HealthMetricType.STRESS_SCORE }
        assertNotNull("Should contain stress metric", stressMetric)
        assertEquals("Stress value should match", 25.0, stressMetric!!.value, 0.01)
        assertEquals("Stress unit should be score", "score", stressMetric.unit)
        assertTrue("Stress metadata should contain rest and activity levels", 
            stressMetric.metadata?.contains("restStressLevel") == true &&
            stressMetric.metadata?.contains("activityStressLevel") == true)
    }

    @Test
    fun `syncBiologicalAgeData should parse biological age data correctly`() = runTest {
        // Given
        setupAuthenticatedState()
        val userId = "test_user_id"
        val startTime = Instant.now().minusSeconds(86400)
        val endTime = Instant.now()
        
        val biologicalAgeResponseJson = """
            {
                "fitnessAge": 28.5,
                "chronologicalAge": 32.0
            }
        """.trimIndent()
        
        val mockResponse = mockk<Response>()
        every { mockOkHttpClient.newCall(any()) } returns mockCall
        every { mockCall.execute() } returns mockResponse
        every { mockResponse.isSuccessful } returns true
        every { mockResponse.body } returns biologicalAgeResponseJson.toResponseBody("application/json".toMediaType())
        
        // When
        val result = garminConnectManager.syncHealthData(userId, startTime, endTime).first()
        
        // Then
        val biologicalAgeMetric = result.find { it.type == HealthMetricType.BIOLOGICAL_AGE }
        assertNotNull("Should contain biological age metric", biologicalAgeMetric)
        assertEquals("Biological age value should match", 28.5, biologicalAgeMetric!!.value, 0.01)
        assertEquals("Biological age unit should be years", "years", biologicalAgeMetric.unit)
        assertTrue("Biological age metadata should contain chronological age", 
            biologicalAgeMetric.metadata?.contains("chronologicalAge") == true)
    }

    // =============================================================================
    // Error Handling Tests
    // =============================================================================

    @Test
    fun `syncHealthData should handle API errors gracefully`() = runTest {
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
        assertTrue("Should return empty list on API error", result.isEmpty())
    }

    @Test
    fun `syncHealthData should handle network exceptions gracefully`() = runTest {
        // Given
        setupAuthenticatedState()
        val userId = "test_user_id"
        val startTime = Instant.now().minusSeconds(86400)
        val endTime = Instant.now()
        
        every { mockOkHttpClient.newCall(any()) } returns mockCall
        every { mockCall.execute() } throws java.net.SocketTimeoutException("Network timeout")
        
        // When
        val result = garminConnectManager.syncHealthData(userId, startTime, endTime).first()
        
        // Then
        assertTrue("Should return empty list on network exception", result.isEmpty())
    }

    @Test
    fun `syncHealthData should handle malformed JSON gracefully`() = runTest {
        // Given
        setupAuthenticatedState()
        val userId = "test_user_id"
        val startTime = Instant.now().minusSeconds(86400)
        val endTime = Instant.now()
        
        val malformedJson = "{ invalid json }"
        val mockResponse = mockk<Response>()
        every { mockOkHttpClient.newCall(any()) } returns mockCall
        every { mockCall.execute() } returns mockResponse
        every { mockResponse.isSuccessful } returns true
        every { mockResponse.body } returns malformedJson.toResponseBody("application/json".toMediaType())
        
        // When
        val result = garminConnectManager.syncHealthData(userId, startTime, endTime).first()
        
        // Then
        assertTrue("Should return empty list on malformed JSON", result.isEmpty())
    }

    // =============================================================================
    // PKCE Security Tests
    // =============================================================================

    @Test
    fun `generateCodeVerifier should create cryptographically secure verifier`() {
        // When
        val (_, codeVerifier1) = garminConnectManager.generateAuthorizationUrl()
        val (_, codeVerifier2) = garminConnectManager.generateAuthorizationUrl()
        
        // Then
        assertNotEquals("Code verifiers should be unique", codeVerifier1, codeVerifier2)
        assertTrue("Code verifier should be proper length", codeVerifier1.length >= 43)
        assertTrue("Code verifier should be base64url safe", codeVerifier1.matches(Regex("[A-Za-z0-9_-]+")))
    }

    @Test
    fun `generateCodeChallenge should create valid SHA256 challenge`() {
        // Given
        val (authUrl1, _) = garminConnectManager.generateAuthorizationUrl()
        val (authUrl2, _) = garminConnectManager.generateAuthorizationUrl()
        
        // Extract code challenges from URLs
        val challenge1 = extractCodeChallenge(authUrl1)
        val challenge2 = extractCodeChallenge(authUrl2)
        
        // Then
        assertNotNull("Code challenge should be present", challenge1)
        assertNotNull("Code challenge should be present", challenge2)
        assertNotEquals("Code challenges should be unique", challenge1, challenge2)
        assertTrue("Code challenge should be base64url encoded", challenge1!!.matches(Regex("[A-Za-z0-9_-]+")))
    }

    // =============================================================================
    // Integration Test Scenarios
    // =============================================================================

    @Test
    fun `full authentication and data sync flow should work end-to-end`() = runTest {
        // Given - Authentication flow
        val (authUrl, codeVerifier) = garminConnectManager.generateAuthorizationUrl()
        assertNotNull("Authorization URL should be generated", authUrl)
        
        // Mock successful token exchange
        val authCode = "test_auth_code"
        val tokenResponse = mockk<Response>()
        val tokenJson = """{"access_token": "test_token", "expires_in": 3600}"""
        
        every { mockOkHttpClient.newCall(any()) } returns mockCall
        every { mockCall.execute() } returns tokenResponse
        every { mockResponse.isSuccessful } returns true
        every { mockResponse.body } returns tokenJson.toResponseBody("application/json".toMediaType())
        
        val tokenResult = garminConnectManager.exchangeCodeForToken(authCode, codeVerifier)
        assertTrue("Token exchange should succeed", tokenResult.isSuccess)
        
        // Given - Data sync
        val userId = "test_user_id"
        val startTime = Instant.now().minusSeconds(86400)
        val endTime = Instant.now()
        
        // Mock successful data sync
        val dataResponse = mockk<Response>()
        val dataJson = """[{"calendarDate": "2024-01-15", "weeklyAvg": 45.5}]"""
        every { mockCall.execute() } returns dataResponse
        every { dataResponse.isSuccessful } returns true
        every { dataResponse.body } returns dataJson.toResponseBody("application/json".toMediaType())
        
        // When
        val syncResult = garminConnectManager.syncHealthData(userId, startTime, endTime).first()
        
        // Then
        assertFalse("Should return health data", syncResult.isEmpty())
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

    private fun extractCodeChallenge(authUrl: String): String? {
        val regex = Regex("code_challenge=([^&]+)")
        return regex.find(authUrl)?.groupValues?.get(1)
    }
}