package com.beaconledger.welltrack.data.health

import android.content.Context
import com.beaconledger.welltrack.data.model.DataSource
import com.beaconledger.welltrack.data.model.HealthMetric
import com.beaconledger.welltrack.data.model.HealthMetricType
import com.samsung.android.sdk.healthdata.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SamsungHealthManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var healthDataStore: HealthDataStore? = null
    
    companion object {
        // Samsung Health data types
        private val REQUIRED_PERMISSIONS = setOf(
            HealthConstants.StepCount.HEALTH_DATA_TYPE,
            HealthConstants.HeartRate.HEALTH_DATA_TYPE,
            HealthConstants.Weight.HEALTH_DATA_TYPE,
            HealthConstants.BodyComposition.HEALTH_DATA_TYPE,
            HealthConstants.SleepStage.HEALTH_DATA_TYPE,
            HealthConstants.Exercise.HEALTH_DATA_TYPE,
            HealthConstants.BloodPressure.HEALTH_DATA_TYPE,
            HealthConstants.BloodGlucose.HEALTH_DATA_TYPE,
            HealthConstants.WaterIntake.HEALTH_DATA_TYPE,
            // ECG data type (if available)
            "com.samsung.health.electrocardiogram"
        )
    }
    
    /**
     * Initialize Samsung Health SDK
     */
    suspend fun initialize(): Result<Unit> {
        return try {
            val connectionListener = object : HealthDataStore.ConnectionListener() {
                override fun onConnected() {
                    // Connection successful
                }
                
                override fun onConnectionFailed(error: HealthConnectionErrorResult) {
                    // Handle connection error
                }
                
                override fun onDisconnected() {
                    // Handle disconnection
                }
            }
            
            healthDataStore = HealthDataStore(context, connectionListener)
            healthDataStore?.connectService()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Check if Samsung Health is available and connected
     */
    fun isAvailable(): Boolean {
        return healthDataStore?.isConnected() == true
    }
    
    /**
     * Check if all required permissions are granted
     */
    suspend fun hasAllPermissions(): Boolean {
        return try {
            val permissionManager = HealthPermissionManager(healthDataStore)
            val result = permissionManager.isPermissionAcquired(REQUIRED_PERMISSIONS)
            result.containsAll(REQUIRED_PERMISSIONS)
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Request permissions for Samsung Health data
     */
    suspend fun requestPermissions(): Result<Unit> {
        return try {
            val permissionManager = HealthPermissionManager(healthDataStore)
            permissionManager.requestPermissions(REQUIRED_PERMISSIONS, context as android.app.Activity)
                .get() // This would need proper async handling in real implementation
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Sync health data from Samsung Health
     */
    suspend fun syncHealthData(
        userId: String,
        startTime: Instant,
        endTime: Instant
    ): Flow<List<HealthMetric>> = flow {
        if (!isAvailable()) {
            emit(emptyList())
            return@flow
        }
        
        val allMetrics = mutableListOf<HealthMetric>()
        
        try {
            // Sync ECG data
            val ecgMetrics = syncECGData(userId, startTime, endTime)
            allMetrics.addAll(ecgMetrics)
            
            // Sync body composition data
            val bodyCompositionMetrics = syncBodyCompositionData(userId, startTime, endTime)
            allMetrics.addAll(bodyCompositionMetrics)
            
            // Sync advanced sleep data
            val sleepMetrics = syncAdvancedSleepData(userId, startTime, endTime)
            allMetrics.addAll(sleepMetrics)
            
            // Sync heart rate data
            val heartRateMetrics = syncHeartRateData(userId, startTime, endTime)
            allMetrics.addAll(heartRateMetrics)
            
            // Sync step data
            val stepMetrics = syncStepData(userId, startTime, endTime)
            allMetrics.addAll(stepMetrics)
            
            emit(allMetrics)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }
    
    private suspend fun syncECGData(
        userId: String,
        startTime: Instant,
        endTime: Instant
    ): List<HealthMetric> {
        return try {
            val request = HealthDataResolver.ReadRequest.Builder()
                .setDataType("com.samsung.health.electrocardiogram")
                .setProperties(arrayOf(
                    "start_time",
                    "end_time",
                    "heart_rate",
                    "ecg_data",
                    "result_status"
                ))
                .setLocalTimeRange(
                    "start_time",
                    "end_time",
                    startTime.toEpochMilli(),
                    endTime.toEpochMilli()
                )
                .build()
            
            val resolver = HealthDataResolver(healthDataStore, null)
            val result = resolver.read(request).get()
            
            val metrics = mutableListOf<HealthMetric>()
            result.use { iterator ->
                while (iterator.hasNext()) {
                    val data = iterator.next()
                    val timestamp = data.getLong("start_time")
                    val heartRate = data.getInt("heart_rate")
                    val resultStatus = data.getInt("result_status")
                    
                    metrics.add(
                        HealthMetric(
                            id = UUID.randomUUID().toString(),
                            userId = userId,
                            type = HealthMetricType.ECG,
                            value = heartRate.toDouble(),
                            unit = "bpm",
                            timestamp = Instant.ofEpochMilli(timestamp)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDateTime()
                                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                            source = DataSource.SAMSUNG_HEALTH,
                            metadata = "{\"resultStatus\":$resultStatus}"
                        )
                    )
                }
            }
            metrics
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    private suspend fun syncBodyCompositionData(
        userId: String,
        startTime: Instant,
        endTime: Instant
    ): List<HealthMetric> {
        return try {
            val request = HealthDataResolver.ReadRequest.Builder()
                .setDataType(HealthConstants.BodyComposition.HEALTH_DATA_TYPE)
                .setProperties(arrayOf(
                    HealthConstants.BodyComposition.START_TIME,
                    HealthConstants.BodyComposition.BODY_FAT_MASS,
                    HealthConstants.BodyComposition.SKELETAL_MUSCLE_MASS,
                    HealthConstants.BodyComposition.BODY_FAT_RATIO
                ))
                .setLocalTimeRange(
                    HealthConstants.BodyComposition.START_TIME,
                    HealthConstants.BodyComposition.START_TIME,
                    startTime.toEpochMilli(),
                    endTime.toEpochMilli()
                )
                .build()
            
            val resolver = HealthDataResolver(healthDataStore, null)
            val result = resolver.read(request).get()
            
            val metrics = mutableListOf<HealthMetric>()
            result.use { iterator ->
                while (iterator.hasNext()) {
                    val data = iterator.next()
                    val timestamp = data.getLong(HealthConstants.BodyComposition.START_TIME)
                    val bodyFatRatio = data.getFloat(HealthConstants.BodyComposition.BODY_FAT_RATIO)
                    val skeletalMuscleMass = data.getFloat(HealthConstants.BodyComposition.SKELETAL_MUSCLE_MASS)
                    
                    // Body fat percentage
                    if (bodyFatRatio > 0) {
                        metrics.add(
                            HealthMetric(
                                id = UUID.randomUUID().toString(),
                                userId = userId,
                                type = HealthMetricType.BODY_FAT_PERCENTAGE,
                                value = bodyFatRatio.toDouble(),
                                unit = "%",
                                timestamp = Instant.ofEpochMilli(timestamp)
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDateTime()
                                    .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                                source = DataSource.SAMSUNG_HEALTH,
                                metadata = null
                            )
                        )
                    }
                    
                    // Muscle mass
                    if (skeletalMuscleMass > 0) {
                        metrics.add(
                            HealthMetric(
                                id = UUID.randomUUID().toString(),
                                userId = userId,
                                type = HealthMetricType.MUSCLE_MASS,
                                value = skeletalMuscleMass.toDouble(),
                                unit = "kg",
                                timestamp = Instant.ofEpochMilli(timestamp)
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDateTime()
                                    .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                                source = DataSource.SAMSUNG_HEALTH,
                                metadata = null
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
    
    private suspend fun syncAdvancedSleepData(
        userId: String,
        startTime: Instant,
        endTime: Instant
    ): List<HealthMetric> {
        return try {
            val request = HealthDataResolver.ReadRequest.Builder()
                .setDataType(HealthConstants.SleepStage.HEALTH_DATA_TYPE)
                .setProperties(arrayOf(
                    HealthConstants.SleepStage.START_TIME,
                    HealthConstants.SleepStage.END_TIME,
                    HealthConstants.SleepStage.STAGE
                ))
                .setLocalTimeRange(
                    HealthConstants.SleepStage.START_TIME,
                    HealthConstants.SleepStage.END_TIME,
                    startTime.toEpochMilli(),
                    endTime.toEpochMilli()
                )
                .build()
            
            val resolver = HealthDataResolver(healthDataStore, null)
            val result = resolver.read(request).get()
            
            val sleepStages = mutableMapOf<String, MutableList<Pair<Long, Long>>>()
            
            result.use { iterator ->
                while (iterator.hasNext()) {
                    val data = iterator.next()
                    val startTime = data.getLong(HealthConstants.SleepStage.START_TIME)
                    val endTime = data.getLong(HealthConstants.SleepStage.END_TIME)
                    val stage = data.getInt(HealthConstants.SleepStage.STAGE)
                    
                    val stageKey = when (stage) {
                        HealthConstants.SleepStage.STAGE_DEEP -> "deep"
                        HealthConstants.SleepStage.STAGE_REM -> "rem"
                        HealthConstants.SleepStage.STAGE_LIGHT -> "light"
                        else -> "awake"
                    }
                    
                    sleepStages.getOrPut(stageKey) { mutableListOf() }
                        .add(Pair(startTime, endTime))
                }
            }
            
            // Calculate sleep stage durations and create metrics
            val metrics = mutableListOf<HealthMetric>()
            sleepStages.forEach { (stage, periods) ->
                val totalDuration = periods.sumOf { (start, end) -> end - start }
                val durationHours = totalDuration / (1000.0 * 60.0 * 60.0)
                
                if (durationHours > 0) {
                    val firstPeriod = periods.minByOrNull { it.first }
                    if (firstPeriod != null) {
                        metrics.add(
                            HealthMetric(
                                id = UUID.randomUUID().toString(),
                                userId = userId,
                                type = HealthMetricType.SLEEP_DURATION,
                                value = durationHours,
                                unit = "hours",
                                timestamp = Instant.ofEpochMilli(firstPeriod.first)
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDateTime()
                                    .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                                source = DataSource.SAMSUNG_HEALTH,
                                metadata = "{\"sleepStage\":\"$stage\"}"
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
    
    private suspend fun syncHeartRateData(
        userId: String,
        startTime: Instant,
        endTime: Instant
    ): List<HealthMetric> {
        return try {
            val request = HealthDataResolver.ReadRequest.Builder()
                .setDataType(HealthConstants.HeartRate.HEALTH_DATA_TYPE)
                .setProperties(arrayOf(
                    HealthConstants.HeartRate.START_TIME,
                    HealthConstants.HeartRate.HEART_RATE
                ))
                .setLocalTimeRange(
                    HealthConstants.HeartRate.START_TIME,
                    HealthConstants.HeartRate.START_TIME,
                    startTime.toEpochMilli(),
                    endTime.toEpochMilli()
                )
                .build()
            
            val resolver = HealthDataResolver(healthDataStore, null)
            val result = resolver.read(request).get()
            
            val metrics = mutableListOf<HealthMetric>()
            result.use { iterator ->
                while (iterator.hasNext()) {
                    val data = iterator.next()
                    val timestamp = data.getLong(HealthConstants.HeartRate.START_TIME)
                    val heartRate = data.getFloat(HealthConstants.HeartRate.HEART_RATE)
                    
                    metrics.add(
                        HealthMetric(
                            id = UUID.randomUUID().toString(),
                            userId = userId,
                            type = HealthMetricType.HEART_RATE,
                            value = heartRate.toDouble(),
                            unit = "bpm",
                            timestamp = Instant.ofEpochMilli(timestamp)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDateTime()
                                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                            source = DataSource.SAMSUNG_HEALTH,
                            metadata = null
                        )
                    )
                }
            }
            metrics
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    private suspend fun syncStepData(
        userId: String,
        startTime: Instant,
        endTime: Instant
    ): List<HealthMetric> {
        return try {
            val request = HealthDataResolver.ReadRequest.Builder()
                .setDataType(HealthConstants.StepCount.HEALTH_DATA_TYPE)
                .setProperties(arrayOf(
                    HealthConstants.StepCount.START_TIME,
                    HealthConstants.StepCount.END_TIME,
                    HealthConstants.StepCount.COUNT
                ))
                .setLocalTimeRange(
                    HealthConstants.StepCount.START_TIME,
                    HealthConstants.StepCount.END_TIME,
                    startTime.toEpochMilli(),
                    endTime.toEpochMilli()
                )
                .build()
            
            val resolver = HealthDataResolver(healthDataStore, null)
            val result = resolver.read(request).get()
            
            val metrics = mutableListOf<HealthMetric>()
            result.use { iterator ->
                while (iterator.hasNext()) {
                    val data = iterator.next()
                    val timestamp = data.getLong(HealthConstants.StepCount.END_TIME)
                    val stepCount = data.getInt(HealthConstants.StepCount.COUNT)
                    
                    metrics.add(
                        HealthMetric(
                            id = UUID.randomUUID().toString(),
                            userId = userId,
                            type = HealthMetricType.STEPS,
                            value = stepCount.toDouble(),
                            unit = "steps",
                            timestamp = Instant.ofEpochMilli(timestamp)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDateTime()
                                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                            source = DataSource.SAMSUNG_HEALTH,
                            metadata = null
                        )
                    )
                }
            }
            metrics
        } catch (e: Exception) {
            emptyList()
        }
    }
}