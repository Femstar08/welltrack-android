package com.beaconledger.welltrack.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "supplements")
data class Supplement(
    @PrimaryKey
    val id: String,
    val name: String,
    val brand: String? = null,
    val description: String? = null,
    val servingSize: String,
    val servingUnit: String,
    val nutritionalInfo: String, // JSON string containing nutritional data
    val barcode: String? = null,
    val imageUrl: String? = null,
    val category: SupplementCategory,
    val createdAt: String,
    val updatedAt: String
)

@Entity(tableName = "user_supplements")
data class UserSupplement(
    @PrimaryKey
    val id: String,
    val userId: String,
    val supplementId: String,
    val customName: String? = null, // User can override supplement name
    val dosage: Double,
    val dosageUnit: String,
    val frequency: SupplementFrequency,
    val scheduledTimes: String, // JSON array of time strings
    val isActive: Boolean = true,
    val notes: String? = null,
    val startDate: String,
    val endDate: String? = null,
    val createdAt: String,
    val updatedAt: String
)

@Entity(tableName = "supplement_intakes")
data class SupplementIntake(
    @PrimaryKey
    val id: String,
    val userId: String,
    val userSupplementId: String,
    val actualDosage: Double,
    val dosageUnit: String,
    val takenAt: String,
    val scheduledAt: String? = null,
    val status: IntakeStatus,
    val notes: String? = null,
    val createdAt: String
)

enum class SupplementCategory {
    VITAMIN,
    MINERAL,
    PROTEIN,
    AMINO_ACID,
    HERBAL,
    PROBIOTIC,
    OMEGA_3,
    PRE_WORKOUT,
    POST_WORKOUT,
    DIGESTIVE,
    IMMUNE,
    ENERGY,
    SLEEP,
    JOINT,
    OTHER
}

enum class SupplementFrequency {
    ONCE_DAILY,
    TWICE_DAILY,
    THREE_TIMES_DAILY,
    FOUR_TIMES_DAILY,
    EVERY_OTHER_DAY,
    WEEKLY,
    AS_NEEDED,
    CUSTOM
}

enum class IntakeStatus {
    TAKEN,
    SKIPPED,
    MISSED,
    PARTIAL
}

data class SupplementNutrition(
    val calories: Double? = null,
    val protein: Double? = null,
    val carbs: Double? = null,
    val fat: Double? = null,
    val fiber: Double? = null,
    val sugar: Double? = null,
    val sodium: Double? = null,
    val potassium: Double? = null,
    val calcium: Double? = null,
    val iron: Double? = null,
    val vitaminD: Double? = null,
    val vitaminB12: Double? = null,
    val vitaminC: Double? = null,
    val magnesium: Double? = null,
    val zinc: Double? = null,
    val omega3: Double? = null,
    val customNutrients: Map<String, Double>? = null
)

data class SupplementSchedule(
    val time: String, // HH:mm format
    val label: String // e.g., "Morning", "With breakfast", "Before workout"
)

data class SupplementDailySummary(
    val date: String,
    val totalTaken: Int,
    val totalScheduled: Int,
    val totalMissed: Int,
    val adherencePercentage: Float
)

data class SupplementReminder(
    val id: String,
    val userSupplementId: String,
    val supplementName: String,
    val dosage: Double,
    val dosageUnit: String,
    val scheduledTime: String,
    val isOverdue: Boolean
)