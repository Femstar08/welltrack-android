package com.beaconledger.welltrack.data.health

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.*
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.beaconledger.welltrack.data.model.DataSource
import com.beaconledger.welltrack.data.model.HealthMetric
import com.beaconledger.welltrack.data.model.HealthMetricType
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
class HealthConnectManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val healthConnectClient by lazy { HealthConnectClient.getOrCreate(context) }
    
    // Define required permissions for Health Connect
    val permissions = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getReadPermission(WeightRecord::class),
        HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class),
        HealthPermission.getReadPermission(BloodPressureRecord::class),
        HealthPermission.getReadPermission(BloodGlucoseRecord::class),
        HealthPermission.getReadPermission(BodyFatRecord::class),
        HealthPermission.getReadPermission(SleepSessionRecord::class),
        HealthPermission.getReadPermission(ExerciseSessionRecord::class),
        HealthPermission.getReadPermission(HydrationRecord::class),
        HealthPermission.getReadPermission(Vo2MaxRecord::class),
        HealthPermission.getReadPermission(LeanBodyMassRecord::class),
        HealthPermission.getReadPermission(BasalMetabolicRateRecord::class)
    )
    
    /**
     * Check if Health Connect is available on this device
     */
    suspend fun isAvailable(): Boolean {
        return HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE
    }
    
    /**
     * Check if all required permissions are granted
     */
    suspend fun hasAllPermissions(): Boolean {
        val grantedPermissions = healthConnectClient.permissionController.getGrantedPermissions()
        return permissions.all { it in grantedPermissions }
    }
    
    /**
     * Get permission controller for requesting permissions
     */
    fun getPermissionController(): PermissionController {
        return healthConnectClient.permissionController
    }
    
    /**
     * Sync all health data for a specific time range
     */
    suspend fun syncHealthData(
        userId: String,
        startTime: Instant,
        endTime: Instant
    ): Flow<List<HealthMetric>> = flow {
        val allMetrics = mutableListOf<HealthMetric>()
        
        try {
            // Sync steps data
            val stepsMetrics = syncStepsData(userId, startTime, endTime)
            allMetrics.addAll(stepsMetrics)
            
            // Sync heart rate data
            val heartRateMetrics = syncHeartRateData(userId, startTime, endTime)
            allMetrics.addAll(heartRateMetrics)
            
            // Sync weight data
            val weightMetrics = syncWeightData(userId, startTime, endTime)
            allMetrics.addAll(weightMetrics)
            
            // Sync calories burned data
            val caloriesMetrics = syncCaloriesData(userId, startTime, endTime)
            allMetrics.addAll(caloriesMetrics)
            
            // Sync blood pressure data
            val bloodPressureMetrics = syncBloodPressureData(userId, startTime, endTime)
            allMetrics.addAll(bloodPressureMetrics)
            
            // Sync blood glucose data
            val bloodGlucoseMetrics = syncBloodGlucoseData(userId, startTime, endTime)
            allMetrics.addAll(bloodGlucoseMetrics)
            
            // Sync body composition data
            val bodyCompositionMetrics = syncBodyCompositionData(userId, startTime, endTime)
            allMetrics.addAll(bodyCompositionMetrics)
            
            // Sync sleep data
            val sleepMetrics = syncSleepData(userId, startTime, endTime)
            allMetrics.addAll(sleepMetrics)
            
            // Sync exercise data
            val exerciseMetrics = syncExerciseData(userId, startTime, endTime)
            allMetrics.addAll(exerciseMetrics)
            
            // Sync hydration data
            val hydrationMetrics = syncHydrationData(userId, startTime, endTime)
            allMetrics.addAll(hydrationMetrics)
            
            // Sync VO2 Max data
            val vo2MaxMetrics = syncVo2MaxData(userId, startTime, endTime)
            allMetrics.addAll(vo2MaxMetrics)
            
            emit(allMetrics)
        } catch (e: Exception) {
            // Log error and emit empty list
            emit(emptyList())
        }
    }
    
    private suspend fun syncStepsData(
        userId: String,
        startTime: Instant,
        endTime: Instant
    ): List<HealthMetric> {
        return try {
            val request = ReadRecordsRequest(
                recordType = StepsRecord::class,
                timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
            )
            val response = healthConnectClient.readRecords(request)
            
            response.records.map { record ->
                HealthMetric(
                    id = UUID.randomUUID().toString(),
                    userId = userId,
                    type = HealthMetricType.STEPS,
                    value = record.count.toDouble(),
                    unit = "steps",
                    timestamp = record.endTime.atZone(ZoneId.systemDefault())
                        .toLocalDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                    source = DataSource.HEALTH_CONNECT,
                    metadata = null
                )
            }
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
            val request = ReadRecordsRequest(
                recordType = HeartRateRecord::class,
                timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
            )
            val response = healthConnectClient.readRecords(request)
            
            response.records.flatMap { record ->
                record.samples.map { sample ->
                    HealthMetric(
                        id = UUID.randomUUID().toString(),
                        userId = userId,
                        type = HealthMetricType.HEART_RATE,
                        value = sample.beatsPerMinute.toDouble(),
                        unit = "bpm",
                        timestamp = sample.time.atZone(ZoneId.systemDefault())
                            .toLocalDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                        source = DataSource.HEALTH_CONNECT,
                        metadata = null
                    )
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    private suspend fun syncWeightData(
        userId: String,
        startTime: Instant,
        endTime: Instant
    ): List<HealthMetric> {
        return try {
            val request = ReadRecordsRequest(
                recordType = WeightRecord::class,
                timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
            )
            val response = healthConnectClient.readRecords(request)
            
            response.records.map { record ->
                HealthMetric(
                    id = UUID.randomUUID().toString(),
                    userId = userId,
                    type = HealthMetricType.WEIGHT,
                    value = record.weight.inKilograms,
                    unit = "kg",
                    timestamp = record.time.atZone(ZoneId.systemDefault())
                        .toLocalDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                    source = DataSource.HEALTH_CONNECT,
                    metadata = null
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    private suspend fun syncCaloriesData(
        userId: String,
        startTime: Instant,
        endTime: Instant
    ): List<HealthMetric> {
        return try {
            val request = ReadRecordsRequest(
                recordType = TotalCaloriesBurnedRecord::class,
                timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
            )
            val response = healthConnectClient.readRecords(request)
            
            response.records.map { record ->
                HealthMetric(
                    id = UUID.randomUUID().toString(),
                    userId = userId,
                    type = HealthMetricType.CALORIES_BURNED,
                    value = record.energy.inCalories,
                    unit = "cal",
                    timestamp = record.endTime.atZone(ZoneId.systemDefault())
                        .toLocalDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                    source = DataSource.HEALTH_CONNECT,
                    metadata = null
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    private suspend fun syncBloodPressureData(
        userId: String,
        startTime: Instant,
        endTime: Instant
    ): List<HealthMetric> {
        return try {
            val request = ReadRecordsRequest(
                recordType = BloodPressureRecord::class,
                timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
            )
            val response = healthConnectClient.readRecords(request)
            
            response.records.flatMap { record ->
                listOf(
                    HealthMetric(
                        id = UUID.randomUUID().toString(),
                        userId = userId,
                        type = HealthMetricType.BLOOD_PRESSURE,
                        value = record.systolic.inMillimetersOfMercury,
                        unit = "mmHg",
                        timestamp = record.time.atZone(ZoneId.systemDefault())
                            .toLocalDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                        source = DataSource.HEALTH_CONNECT,
                        metadata = "{\"type\":\"systolic\"}"
                    ),
                    HealthMetric(
                        id = UUID.randomUUID().toString(),
                        userId = userId,
                        type = HealthMetricType.BLOOD_PRESSURE,
                        value = record.diastolic.inMillimetersOfMercury,
                        unit = "mmHg",
                        timestamp = record.time.atZone(ZoneId.systemDefault())
                            .toLocalDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                        source = DataSource.HEALTH_CONNECT,
                        metadata = "{\"type\":\"diastolic\"}"
                    )
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    private suspend fun syncBloodGlucoseData(
        userId: String,
        startTime: Instant,
        endTime: Instant
    ): List<HealthMetric> {
        return try {
            val request = ReadRecordsRequest(
                recordType = BloodGlucoseRecord::class,
                timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
            )
            val response = healthConnectClient.readRecords(request)
            
            response.records.map { record ->
                HealthMetric(
                    id = UUID.randomUUID().toString(),
                    userId = userId,
                    type = HealthMetricType.BLOOD_GLUCOSE,
                    value = record.level.inMillimolesPerLiter,
                    unit = "mmol/L",
                    timestamp = record.time.atZone(ZoneId.systemDefault())
                        .toLocalDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                    source = DataSource.HEALTH_CONNECT,
                    metadata = "{\"specimenSource\":\"${record.specimenSource}\",\"mealType\":\"${record.mealType}\"}"
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    private suspend fun syncBodyCompositionData(
        userId: String,
        startTime: Instant,
        endTime: Instant
    ): List<HealthMetric> {
        val metrics = mutableListOf<HealthMetric>()
        
        try {
            // Body Fat Percentage
            val bodyFatRequest = ReadRecordsRequest(
                recordType = BodyFatRecord::class,
                timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
            )
            val bodyFatResponse = healthConnectClient.readRecords(bodyFatRequest)
            
            bodyFatResponse.records.forEach { record ->
                metrics.add(
                    HealthMetric(
                        id = UUID.randomUUID().toString(),
                        userId = userId,
                        type = HealthMetricType.BODY_FAT_PERCENTAGE,
                        value = record.percentage.value,
                        unit = "%",
                        timestamp = record.time.atZone(ZoneId.systemDefault())
                            .toLocalDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                        source = DataSource.HEALTH_CONNECT,
                        metadata = null
                    )
                )
            }
            
            // Lean Body Mass
            val leanBodyMassRequest = ReadRecordsRequest(
                recordType = LeanBodyMassRecord::class,
                timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
            )
            val leanBodyMassResponse = healthConnectClient.readRecords(leanBodyMassRequest)
            
            leanBodyMassResponse.records.forEach { record ->
                metrics.add(
                    HealthMetric(
                        id = UUID.randomUUID().toString(),
                        userId = userId,
                        type = HealthMetricType.MUSCLE_MASS,
                        value = record.mass.inKilograms,
                        unit = "kg",
                        timestamp = record.time.atZone(ZoneId.systemDefault())
                            .toLocalDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                        source = DataSource.HEALTH_CONNECT,
                        metadata = null
                    )
                )
            }
        } catch (e: Exception) {
            // Continue with empty metrics if error occurs
        }
        
        return metrics
    }
    
    private suspend fun syncSleepData(
        userId: String,
        startTime: Instant,
        endTime: Instant
    ): List<HealthMetric> {
        return try {
            val request = ReadRecordsRequest(
                recordType = SleepSessionRecord::class,
                timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
            )
            val response = healthConnectClient.readRecords(request)
            
            response.records.map { record ->
                val durationHours = java.time.Duration.between(record.startTime, record.endTime).toMinutes() / 60.0
                HealthMetric(
                    id = UUID.randomUUID().toString(),
                    userId = userId,
                    type = HealthMetricType.SLEEP_DURATION,
                    value = durationHours,
                    unit = "hours",
                    timestamp = record.endTime.atZone(ZoneId.systemDefault())
                        .toLocalDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                    source = DataSource.HEALTH_CONNECT,
                    metadata = "{\"startTime\":\"${record.startTime}\",\"endTime\":\"${record.endTime}\"}"
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    private suspend fun syncExerciseData(
        userId: String,
        startTime: Instant,
        endTime: Instant
    ): List<HealthMetric> {
        return try {
            val request = ReadRecordsRequest(
                recordType = ExerciseSessionRecord::class,
                timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
            )
            val response = healthConnectClient.readRecords(request)
            
            response.records.map { record ->
                val durationMinutes = java.time.Duration.between(record.startTime, record.endTime).toMinutes().toDouble()
                HealthMetric(
                    id = UUID.randomUUID().toString(),
                    userId = userId,
                    type = HealthMetricType.EXERCISE_DURATION,
                    value = durationMinutes,
                    unit = "minutes",
                    timestamp = record.endTime.atZone(ZoneId.systemDefault())
                        .toLocalDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                    source = DataSource.HEALTH_CONNECT,
                    metadata = "{\"exerciseType\":\"${record.exerciseType}\",\"title\":\"${record.title}\"}"
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    private suspend fun syncHydrationData(
        userId: String,
        startTime: Instant,
        endTime: Instant
    ): List<HealthMetric> {
        return try {
            val request = ReadRecordsRequest(
                recordType = HydrationRecord::class,
                timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
            )
            val response = healthConnectClient.readRecords(request)
            
            response.records.map { record ->
                HealthMetric(
                    id = UUID.randomUUID().toString(),
                    userId = userId,
                    type = HealthMetricType.HYDRATION,
                    value = record.volume.inLiters,
                    unit = "L",
                    timestamp = record.endTime.atZone(ZoneId.systemDefault())
                        .toLocalDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                    source = DataSource.HEALTH_CONNECT,
                    metadata = null
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    private suspend fun syncVo2MaxData(
        userId: String,
        startTime: Instant,
        endTime: Instant
    ): List<HealthMetric> {
        return try {
            val request = ReadRecordsRequest(
                recordType = Vo2MaxRecord::class,
                timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
            )
            val response = healthConnectClient.readRecords(request)
            
            response.records.map { record ->
                HealthMetric(
                    id = UUID.randomUUID().toString(),
                    userId = userId,
                    type = HealthMetricType.VO2_MAX,
                    value = record.vo2MillilitersPerMinuteKilogram,
                    unit = "ml/min/kg",
                    timestamp = record.time.atZone(ZoneId.systemDefault())
                        .toLocalDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                    source = DataSource.HEALTH_CONNECT,
                    metadata = "{\"measurementMethod\":\"${record.measurementMethod}\"}"
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}