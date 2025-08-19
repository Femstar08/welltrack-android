package com.beaconledger.welltrack.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blood_test_reminders")
data class BloodTestReminder(
    @PrimaryKey
    val id: String,
    val userId: String,
    val testType: BloodTestType,
    val reminderName: String,
    val description: String? = null,
    val frequency: ReminderFrequency,
    val nextDueDate: String, // ISO date string
    val lastCompletedDate: String? = null,
    val isActive: Boolean = true,
    val canSkip: Boolean = true,
    val skipCount: Int = 0,
    val maxSkips: Int = 3,
    val createdAt: String,
    val updatedAt: String
)

@Entity(tableName = "biomarker_entries")
data class BiomarkerEntry(
    @PrimaryKey
    val id: String,
    val userId: String,
    val testType: BloodTestType,
    val biomarkerType: BiomarkerType,
    val value: Double,
    val unit: String,
    val referenceRangeMin: Double? = null,
    val referenceRangeMax: Double? = null,
    val testDate: String, // ISO date string
    val entryDate: String, // When user entered the data
    val notes: String? = null,
    val labName: String? = null,
    val isWithinRange: Boolean? = null,
    val createdAt: String
)

@Entity(tableName = "biomarker_test_sessions")
data class BiomarkerTestSession(
    @PrimaryKey
    val id: String,
    val userId: String,
    val testDate: String,
    val labName: String? = null,
    val notes: String? = null,
    val totalMarkers: Int,
    val enteredMarkers: Int,
    val isComplete: Boolean = false,
    val createdAt: String,
    val updatedAt: String
)

enum class BloodTestType {
    HORMONAL_PANEL,
    MICRONUTRIENT_PANEL,
    GENERAL_HEALTH_PANEL,
    LIPID_PANEL,
    THYROID_PANEL,
    VITAMIN_PANEL,
    MINERAL_PANEL,
    COMPREHENSIVE_METABOLIC_PANEL,
    CUSTOM_TEST
}

enum class BiomarkerType {
    // Hormonal Markers
    TESTOSTERONE,
    ESTRADIOL,
    CORTISOL,
    THYROID_TSH,
    THYROID_T3,
    THYROID_T4,
    THYROID_FREE_T3,
    THYROID_FREE_T4,
    INSULIN,
    GROWTH_HORMONE,
    
    // Micronutrients - Vitamins
    VITAMIN_D3,
    VITAMIN_B12,
    VITAMIN_B6,
    FOLATE,
    VITAMIN_C,
    VITAMIN_A,
    VITAMIN_E,
    VITAMIN_K,
    THIAMINE_B1,
    RIBOFLAVIN_B2,
    NIACIN_B3,
    BIOTIN,
    
    // Micronutrients - Minerals
    IRON,
    FERRITIN,
    ZINC,
    MAGNESIUM,
    CALCIUM,
    PHOSPHORUS,
    SELENIUM,
    COPPER,
    MANGANESE,
    CHROMIUM,
    
    // Essential Fatty Acids
    OMEGA_3,
    OMEGA_6,
    EPA,
    DHA,
    
    // General Health Panel
    TOTAL_CHOLESTEROL,
    HDL_CHOLESTEROL,
    LDL_CHOLESTEROL,
    TRIGLYCERIDES,
    HBA1C,
    GLUCOSE_FASTING,
    RBC_COUNT,
    HEMOGLOBIN,
    HEMATOCRIT,
    WHITE_BLOOD_CELLS,
    PLATELETS,
    
    // Metabolic Markers
    CREATININE,
    BUN,
    URIC_ACID,
    ALT,
    AST,
    BILIRUBIN,
    
    // Other Important Markers
    NITRIC_OXIDE,
    HOMOCYSTEINE,
    CRP,
    ESR,
    
    // Custom marker for user-defined tests
    CUSTOM
}

enum class ReminderFrequency {
    MONTHLY,
    QUARTERLY,
    SEMI_ANNUALLY,
    ANNUALLY,
    CUSTOM
}

data class BiomarkerReference(
    val biomarkerType: BiomarkerType,
    val unit: String,
    val normalRangeMin: Double,
    val normalRangeMax: Double,
    val optimalRangeMin: Double? = null,
    val optimalRangeMax: Double? = null,
    val description: String
)

data class BiomarkerCategory(
    val name: String,
    val biomarkers: List<BiomarkerType>,
    val description: String
)

data class BloodTestReminderWithStatus(
    val reminder: BloodTestReminder,
    val isOverdue: Boolean,
    val daysSinceLastTest: Int?,
    val daysUntilNext: Int
)

data class BiomarkerTrend(
    val biomarkerType: BiomarkerType,
    val entries: List<BiomarkerEntry>,
    val trend: TrendDirection,
    val changePercentage: Double?
)

enum class TrendDirection {
    IMPROVING,
    STABLE,
    DECLINING,
    INSUFFICIENT_DATA
}

data class BiomarkerInsight(
    val biomarkerType: BiomarkerType,
    val currentValue: Double,
    val previousValue: Double?,
    val isWithinRange: Boolean,
    val trend: TrendDirection,
    val recommendation: String?,
    val urgencyLevel: UrgencyLevel
)

enum class UrgencyLevel {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}