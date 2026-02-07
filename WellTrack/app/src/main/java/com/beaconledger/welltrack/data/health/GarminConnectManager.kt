package com.beaconledger.welltrack.data.health

import android.content.Context
import com.beaconledger.welltrack.config.EnvironmentConfig
import com.beaconledger.welltrack.config.SecureConfigLoader
import com.beaconledger.welltrack.data.model.DataSource
import com.beaconledger.welltrack.data.model.HealthMetric
import com.beaconledger.welltrack.data.model.HealthMetricType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class GarminConnectManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val environmentConfig: EnvironmentConfig,
    private val secureConfigLoader: SecureConfigLoader
) {
    private val client: OkHttpClient
    private val baseUrl = "https://apis.garmin.com"

    init {
        val certificatePinner = CertificatePinner.Builder()
            // TODO: Replace with the actual SHA-256 fingerprint of the apis.garmin.com certificate
            .add("apis.garmin.com", "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")
            .build()

        client = OkHttpClient.Builder()
            .certificatePinner(certificatePinner)
            .build()
    }
    
    // OAuth 2.0 PKCE configuration
    private var accessToken: String? = null
    private var refreshToken: String? = null
    private var tokenExpiryTime: Long = 0
    
    // Client credentials from environment configuration
    private val clientId = environmentConfig.garminClientId
    private val redirectUri = environmentConfig.garminRedirectUri
    
    /**
     * Generate OAuth 2.0 PKCE authorization URL
     */
    fun generateAuthorizationUrl(): Pair<String, String> {
        val codeVerifier = generateCodeVerifier()
        val codeChallenge = generateCodeChallenge(codeVerifier)
        val state = generateState()
        
        val authUrl = "$baseUrl/oauth-service/oauth/preauthorized" +
                "?client_id=$clientId" +
                "&response_type=code" +
                "&scope=ghs-read" +
                "&redirect_uri=$redirectUri" +
                "&state=$state" +
                "&code_challenge=$codeChallenge" +
                "&code_challenge_method=S256"
        
        return Pair(authUrl, codeVerifier)
    }
    
    /**
     * Exchange authorization code for access token
     */
    suspend fun exchangeCodeForToken(
        authorizationCode: String,
        codeVerifier: String
    ): Result<Unit> {
        return try {
            val requestBody = FormBody.Builder()
                .add("grant_type", "authorization_code")
                .add("client_id", clientId)
                .add("code", authorizationCode)
                .add("redirect_uri", redirectUri)
                .add("code_verifier", codeVerifier)
                .build()
            
            val request = Request.Builder()
                .url("$baseUrl/oauth-service/oauth/token")
                .post(requestBody)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build()
            
            val response = client.newCall(request).execute()
            
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                val json = JSONObject(responseBody ?: "")
                
                accessToken = json.getString("access_token")
                refreshToken = json.optString("refresh_token")
                val expiresIn = json.getInt("expires_in")
                tokenExpiryTime = System.currentTimeMillis() + (expiresIn * 1000)
                
                Result.success(Unit)
            } else {
                Result.failure(Exception("Token exchange failed: ${response.code}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Check if we have a valid access token
     */
    fun isAuthenticated(): Boolean {
        return accessToken != null && System.currentTimeMillis() < tokenExpiryTime
    }
    
    /**
     * Sync health data from Garmin Connect
     */
    suspend fun syncHealthData(
        userId: String,
        startTime: Instant,
        endTime: Instant
    ): Flow<List<HealthMetric>> = flow {
        if (!isAuthenticated()) {
            emit(emptyList())
            return@flow
        }
        
        val allMetrics = mutableListOf<HealthMetric>()
        
        try {
            // Sync HRV data
            val hrvMetrics = syncHRVData(userId, startTime, endTime)
            allMetrics.addAll(hrvMetrics)
            
            // Sync recovery data
            val recoveryMetrics = syncRecoveryData(userId, startTime, endTime)
            allMetrics.addAll(recoveryMetrics)
            
            // Sync stress score data
            val stressMetrics = syncStressData(userId, startTime, endTime)
            allMetrics.addAll(stressMetrics)
            
            // Sync biological age data
            val biologicalAgeMetrics = syncBiologicalAgeData(userId, startTime, endTime)
            allMetrics.addAll(biologicalAgeMetrics)
            
            // Sync additional fitness metrics
            val fitnessMetrics = syncFitnessMetrics(userId, startTime, endTime)
            allMetrics.addAll(fitnessMetrics)
            
            emit(allMetrics)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }
    
    private suspend fun syncHRVData(
        userId: String,
        startTime: Instant,
        endTime: Instant
    ): List<HealthMetric> {
        return try {
            val startDate = startTime.atZone(ZoneId.systemDefault()).toLocalDate()
            val endDate = endTime.atZone(ZoneId.systemDefault()).toLocalDate()
            
            val request = Request.Builder()
                .url("$baseUrl/wellness-api/rest/hrv/daily/$startDate/$endDate")
                .get()
                .addHeader("Authorization", "Bearer $accessToken")
                .build()
            
            val response = client.newCall(request).execute()
            
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                val json = JSONArray(responseBody ?: "[]")
                
                val metrics = mutableListOf<HealthMetric>()
                for (i in 0 until json.length()) {
                    val item = json.getJSONObject(i)
                    val calendarDate = item.getString("calendarDate")
                    val weeklyAvg = item.optDouble("weeklyAvg", 0.0)
                    val lastNightAvg = item.optDouble("lastNightAvg", 0.0)
                    val lastNight5MinHigh = item.optDouble("lastNight5MinHigh", 0.0)
                    val baseline = item.optJSONObject("baseline")
                    
                    if (weeklyAvg > 0) {
                        metrics.add(
                            HealthMetric(
                                id = UUID.randomUUID().toString(),
                                userId = userId,
                                type = HealthMetricType.HRV,
                                value = weeklyAvg,
                                unit = "ms",
                                timestamp = "${calendarDate}T00:00:00",
                                source = DataSource.GARMIN,
                                metadata = JSONObject().apply {
                                    put("type", "weekly_avg")
                                    put("lastNightAvg", lastNightAvg)
                                    put("lastNight5MinHigh", lastNight5MinHigh)
                                    baseline?.let { put("baseline", it) }
                                }.toString()
                            )
                        )
                    }
                }
                metrics
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    private suspend fun syncRecoveryData(
        userId: String,
        startTime: Instant,
        endTime: Instant
    ): List<HealthMetric> {
        return try {
            val startDate = startTime.atZone(ZoneId.systemDefault()).toLocalDate()
            val endDate = endTime.atZone(ZoneId.systemDefault()).toLocalDate()
            
            val request = Request.Builder()
                .url("$baseUrl/wellness-api/rest/recovery/daily/$startDate/$endDate")
                .get()
                .addHeader("Authorization", "Bearer $accessToken")
                .build()
            
            val response = client.newCall(request).execute()
            
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                val json = JSONArray(responseBody ?: "[]")
                
                val metrics = mutableListOf<HealthMetric>()
                for (i in 0 until json.length()) {
                    val item = json.getJSONObject(i)
                    val calendarDate = item.getString("calendarDate")
                    val recoveryScore = item.optDouble("recoveryScore", 0.0)
                    val sleepScore = item.optDouble("sleepScore", 0.0)
                    val hrvScore = item.optDouble("hrvScore", 0.0)
                    
                    if (recoveryScore > 0) {
                        metrics.add(
                            HealthMetric(
                                id = UUID.randomUUID().toString(),
                                userId = userId,
                                type = HealthMetricType.TRAINING_RECOVERY,
                                value = recoveryScore,
                                unit = "score",
                                timestamp = "${calendarDate}T00:00:00",
                                source = DataSource.GARMIN,
                                metadata = JSONObject().apply {
                                    put("sleepScore", sleepScore)
                                    put("hrvScore", hrvScore)
                                }.toString()
                            )
                        )
                    }
                }
                metrics
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    private suspend fun syncStressData(
        userId: String,
        startTime: Instant,
        endTime: Instant
    ): List<HealthMetric> {
        return try {
            val startDate = startTime.atZone(ZoneId.systemDefault()).toLocalDate()
            val endDate = endTime.atZone(ZoneId.systemDefault()).toLocalDate()
            
            val request = Request.Builder()
                .url("$baseUrl/wellness-api/rest/stress/daily/$startDate/$endDate")
                .get()
                .addHeader("Authorization", "Bearer $accessToken")
                .build()
            
            val response = client.newCall(request).execute()
            
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                val json = JSONArray(responseBody ?: "[]")
                
                val metrics = mutableListOf<HealthMetric>()
                for (i in 0 until json.length()) {
                    val item = json.getJSONObject(i)
                    val calendarDate = item.getString("calendarDate")
                    val overallStressLevel = item.optDouble("overallStressLevel", 0.0)
                    val restStressLevel = item.optDouble("restStressLevel", 0.0)
                    val activityStressLevel = item.optDouble("activityStressLevel", 0.0)
                    
                    if (overallStressLevel > 0) {
                        metrics.add(
                            HealthMetric(
                                id = UUID.randomUUID().toString(),
                                userId = userId,
                                type = HealthMetricType.STRESS_SCORE,
                                value = overallStressLevel,
                                unit = "score",
                                timestamp = "${calendarDate}T00:00:00",
                                source = DataSource.GARMIN,
                                metadata = JSONObject().apply {
                                    put("restStressLevel", restStressLevel)
                                    put("activityStressLevel", activityStressLevel)
                                }.toString()
                            )
                        )
                    }
                }
                metrics
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    private suspend fun syncBiologicalAgeData(
        userId: String,
        startTime: Instant,
        endTime: Instant
    ): List<HealthMetric> {
        return try {
            val request = Request.Builder()
                .url("$baseUrl/wellness-api/rest/fitness-age")
                .get()
                .addHeader("Authorization", "Bearer $accessToken")
                .build()
            
            val response = client.newCall(request).execute()
            
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                val json = JSONObject(responseBody ?: "{}")
                
                val fitnessAge = json.optDouble("fitnessAge", 0.0)
                val chronologicalAge = json.optDouble("chronologicalAge", 0.0)
                
                if (fitnessAge > 0) {
                    listOf(
                        HealthMetric(
                            id = UUID.randomUUID().toString(),
                            userId = userId,
                            type = HealthMetricType.BIOLOGICAL_AGE,
                            value = fitnessAge,
                            unit = "years",
                            timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                            source = DataSource.GARMIN,
                            metadata = JSONObject().apply {
                                put("chronologicalAge", chronologicalAge)
                                put("type", "fitness_age")
                            }.toString()
                        )
                    )
                } else {
                    emptyList()
                }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    private suspend fun syncFitnessMetrics(
        userId: String,
        startTime: Instant,
        endTime: Instant
    ): List<HealthMetric> {
        return try {
            val startDate = startTime.atZone(ZoneId.systemDefault()).toLocalDate()
            val endDate = endTime.atZone(ZoneId.systemDefault()).toLocalDate()
            
            val metrics = mutableListOf<HealthMetric>()
            
            // Sync VO2 Max data
            val vo2MaxRequest = Request.Builder()
                .url("$baseUrl/wellness-api/rest/vo2max/daily/$startDate/$endDate")
                .get()
                .addHeader("Authorization", "Bearer $accessToken")
                .build()
            
            val vo2MaxResponse = client.newCall(vo2MaxRequest).execute()
            
            if (vo2MaxResponse.isSuccessful) {
                val responseBody = vo2MaxResponse.body?.string()
                val json = JSONArray(responseBody ?: "[]")
                
                for (i in 0 until json.length()) {
                    val item = json.getJSONObject(i)
                    val calendarDate = item.getString("calendarDate")
                    val vo2Max = item.optDouble("vo2Max", 0.0)
                    val fitnessLevel = item.optString("fitnessLevel", "")
                    
                    if (vo2Max > 0) {
                        metrics.add(
                            HealthMetric(
                                id = UUID.randomUUID().toString(),
                                userId = userId,
                                type = HealthMetricType.VO2_MAX,
                                value = vo2Max,
                                unit = "ml/min/kg",
                                timestamp = "${calendarDate}T00:00:00",
                                source = DataSource.GARMIN,
                                metadata = JSONObject().apply {
                                    put("fitnessLevel", fitnessLevel)
                                }.toString()
                            )
                        )
                    }
                }
            }
            
            metrics
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    // PKCE helper methods
    private fun generateCodeVerifier(): String {
        val bytes = ByteArray(32)
        SecureRandom().nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }
    
    private fun generateCodeChallenge(codeVerifier: String): String {
        val bytes = codeVerifier.toByteArray(Charsets.US_ASCII)
        val digest = MessageDigest.getInstance("SHA-256").digest(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(digest)
    }
    
    private fun generateState(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }
}