package com.beaconledger.welltrack.data.database

import androidx.room.TypeConverter
import com.beaconledger.welltrack.data.model.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Converters {
    private val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    @TypeConverter
    fun fromLocalDateTime(dateTime: LocalDateTime?): String? {
        return dateTime?.format(dateTimeFormatter)
    }

    @TypeConverter
    fun toLocalDateTime(dateTimeString: String?): LocalDateTime? {
        return dateTimeString?.let { LocalDateTime.parse(it, dateTimeFormatter) }
    }

    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return value.joinToString(",")
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        return if (value.isEmpty()) emptyList() else value.split(",")
    }

    @TypeConverter
    fun fromSupplementCategory(category: SupplementCategory): String {
        return category.name
    }

    @TypeConverter
    fun toSupplementCategory(categoryString: String): SupplementCategory {
        return SupplementCategory.valueOf(categoryString)
    }

    @TypeConverter
    fun fromSupplementFrequency(frequency: SupplementFrequency): String {
        return frequency.name
    }

    @TypeConverter
    fun toSupplementFrequency(frequencyString: String): SupplementFrequency {
        return SupplementFrequency.valueOf(frequencyString)
    }

    @TypeConverter
    fun fromIntakeStatus(status: IntakeStatus): String {
        return status.name
    }

    @TypeConverter
    fun toIntakeStatus(statusString: String): IntakeStatus {
        return IntakeStatus.valueOf(statusString)
    }

    @TypeConverter
    fun fromBloodTestType(testType: BloodTestType): String {
        return testType.name
    }

    @TypeConverter
    fun toBloodTestType(testTypeString: String): BloodTestType {
        return BloodTestType.valueOf(testTypeString)
    }

    @TypeConverter
    fun fromBiomarkerType(biomarkerType: BiomarkerType): String {
        return biomarkerType.name
    }

    @TypeConverter
    fun toBiomarkerType(biomarkerTypeString: String): BiomarkerType {
        return BiomarkerType.valueOf(biomarkerTypeString)
    }

    @TypeConverter
    fun fromReminderFrequency(frequency: ReminderFrequency): String {
        return frequency.name
    }

    @TypeConverter
    fun toReminderFrequency(frequencyString: String): ReminderFrequency {
        return ReminderFrequency.valueOf(frequencyString)
    }

    @TypeConverter
    fun fromTrendDirection(trend: TrendDirection): String {
        return trend.name
    }

    @TypeConverter
    fun toTrendDirection(trendString: String): TrendDirection {
        return TrendDirection.valueOf(trendString)
    }

    @TypeConverter
    fun fromUrgencyLevel(level: UrgencyLevel): String {
        return level.name
    }

    @TypeConverter
    fun toUrgencyLevel(levelString: String): UrgencyLevel {
        return UrgencyLevel.valueOf(levelString)
    }
}