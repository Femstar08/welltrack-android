package com.beaconledger.welltrack.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "health_metrics")
data class HealthMetric(
    @PrimaryKey
    val id: String,
    val userId: String,
    val type: HealthMetricType,
    val value: Double,
    val unit: String,
    val timestamp: String,
    val source: DataSource,
    val metadata: String? = null // JSON string for additional data
)

enum class HealthMetricType {
    // Fitness Data
    STEPS,
    ACTIVE_MINUTES,
    HEART_RATE,
    WEIGHT,
    CALORIES_BURNED,
    BLOOD_PRESSURE,
    BLOOD_GLUCOSE,
    BODY_COMPOSITION,
    SLEEP_DURATION,
    EXERCISE_DURATION,
    HYDRATION,
    VO2_MAX,
    
    // Biomarkers - Hormonal
    TESTOSTERONE,
    ESTRADIOL,
    CORTISOL,
    THYROID_TSH,
    THYROID_T3,
    THYROID_T4,
    
    // Biomarkers - Micronutrients
    VITAMIN_D3,
    VITAMIN_B12,
    VITAMIN_B6,
    FOLATE,
    IRON,
    FERRITIN,
    ZINC,
    MAGNESIUM,
    OMEGA_3,
    NITRIC_OXIDE,
    
    // Biomarkers - General Health
    LIPID_PANEL_TOTAL_CHOLESTEROL,
    LIPID_PANEL_HDL,
    LIPID_PANEL_LDL,
    LIPID_PANEL_TRIGLYCERIDES,
    HBA1C,
    RBC_COUNT,
    HEMOGLOBIN,
    
    // Garmin Specific
    HRV,
    TRAINING_RECOVERY,
    STRESS_SCORE,
    BIOLOGICAL_AGE,
    
    // Samsung Health Specific
    ECG,
    BODY_FAT_PERCENTAGE,
    MUSCLE_MASS,
    
    // Custom Habits
    CUSTOM_HABIT
}

enum class DataSource {
    HEALTH_CONNECT,
    GARMIN,
    SAMSUNG_HEALTH,
    MANUAL_ENTRY,
    BLOOD_TEST,
    CUSTOM
}

