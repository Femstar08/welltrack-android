package com.beaconledger.welltrack.data.database

import androidx.room.TypeConverter
import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.data.compliance.DeletionStatus
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

    // Macronutrient tracking converters
    @TypeConverter
    fun fromNutrientSource(source: NutrientSource): String {
        return source.name
    }

    @TypeConverter
    fun toNutrientSource(sourceString: String): NutrientSource {
        return NutrientSource.valueOf(sourceString)
    }

    @TypeConverter
    fun fromNutrientPriority(priority: NutrientPriority): String {
        return priority.name
    }

    @TypeConverter
    fun toNutrientPriority(priorityString: String): NutrientPriority {
        return NutrientPriority.valueOf(priorityString)
    }

    @TypeConverter
    fun fromNutrientCategory(category: NutrientCategory): String {
        return category.name
    }

    @TypeConverter
    fun toNutrientCategory(categoryString: String): NutrientCategory {
        return NutrientCategory.valueOf(categoryString)
    }

    @TypeConverter
    fun fromActivityLevel(level: ActivityLevel): String {
        return level.name
    }

    @TypeConverter
    fun toActivityLevel(levelString: String): ActivityLevel {
        return ActivityLevel.valueOf(levelString)
    }

    @TypeConverter
    fun fromGender(gender: Gender): String {
        return gender.name
    }

    @TypeConverter
    fun toGender(genderString: String): Gender {
        return Gender.valueOf(genderString)
    }

    @TypeConverter
    fun fromStringDoubleMap(map: Map<String, Double>): String {
        return map.entries.joinToString(";") { "${it.key}:${it.value}" }
    }

    @TypeConverter
    fun toStringDoubleMap(mapString: String): Map<String, Double> {
        if (mapString.isEmpty()) return emptyMap()
        return mapString.split(";").associate { entry ->
            val (key, value) = entry.split(":")
            key to value.toDouble()
        }
    }

    @TypeConverter
    fun fromLocalDate(date: java.time.LocalDate?): String? {
        return date?.toString()
    }

    @TypeConverter
    fun toLocalDate(dateString: String?): java.time.LocalDate? {
        return dateString?.let { java.time.LocalDate.parse(it) }
    }

    // Dietary Restrictions converters
    @TypeConverter
    fun fromDietaryRestrictionType(type: DietaryRestrictionType): String {
        return type.name
    }

    @TypeConverter
    fun toDietaryRestrictionType(typeString: String): DietaryRestrictionType {
        return DietaryRestrictionType.valueOf(typeString)
    }

    @TypeConverter
    fun fromRestrictionSeverity(severity: RestrictionSeverity): String {
        return severity.name
    }

    @TypeConverter
    fun toRestrictionSeverity(severityString: String): RestrictionSeverity {
        return RestrictionSeverity.valueOf(severityString)
    }

    @TypeConverter
    fun fromAllergySeverity(severity: AllergySeverity): String {
        return severity.name
    }

    @TypeConverter
    fun toAllergySeverity(severityString: String): AllergySeverity {
        return AllergySeverity.valueOf(severityString)
    }

    @TypeConverter
    fun fromFoodPreferenceType(type: FoodPreferenceType): String {
        return type.name
    }

    @TypeConverter
    fun toFoodPreferenceType(typeString: String): FoodPreferenceType {
        return FoodPreferenceType.valueOf(typeString)
    }

    @TypeConverter
    fun fromPreferenceLevel(level: PreferenceLevel): String {
        return level.name
    }

    @TypeConverter
    fun toPreferenceLevel(levelString: String): PreferenceLevel {
        return PreferenceLevel.valueOf(levelString)
    }

    @TypeConverter
    fun fromDietaryTagType(type: DietaryTagType): String {
        return type.name
    }

    @TypeConverter
    fun toDietaryTagType(typeString: String): DietaryTagType {
        return DietaryTagType.valueOf(typeString)
    }

    @TypeConverter
    fun fromTagSource(source: TagSource): String {
        return source.name
    }

    @TypeConverter
    fun toTagSource(sourceString: String): TagSource {
        return TagSource.valueOf(sourceString)
    }

    // Social features converters
    @TypeConverter
    fun fromFamilyRole(role: FamilyRole): String {
        return role.name
    }

    @TypeConverter
    fun toFamilyRole(roleString: String): FamilyRole {
        return FamilyRole.valueOf(roleString)
    }

    @TypeConverter
    fun fromMealPrepStatus(status: MealPrepStatus): String {
        return status.name
    }

    @TypeConverter
    fun toMealPrepStatus(statusString: String): MealPrepStatus {
        return MealPrepStatus.valueOf(statusString)
    }

    @TypeConverter
    fun fromAchievementType(type: AchievementType): String {
        return type.name
    }

    @TypeConverter
    fun toAchievementType(typeString: String): AchievementType {
        return AchievementType.valueOf(typeString)
    }

    @TypeConverter
    fun fromReactionType(type: ReactionType): String {
        return type.name
    }

    @TypeConverter
    fun toReactionType(typeString: String): ReactionType {
        return ReactionType.valueOf(typeString)
    }

    @TypeConverter
    fun fromAchievementReactionList(reactions: List<AchievementReaction>): String {
        return reactions.joinToString(";") { "${it.userId}:${it.reactionType.name}:${it.reactedAt.format(dateTimeFormatter)}" }
    }

    @TypeConverter
    fun toAchievementReactionList(reactionsString: String): List<AchievementReaction> {
        if (reactionsString.isEmpty()) return emptyList()
        return reactionsString.split(";").map { entry ->
            val parts = entry.split(":")
            AchievementReaction(
                userId = parts[0],
                reactionType = ReactionType.valueOf(parts[1]),
                reactedAt = LocalDateTime.parse(parts[2], dateTimeFormatter)
            )
        }
    }

    // Cost Budget converters
    @TypeConverter
    fun fromBudgetPeriod(period: BudgetPeriod): String {
        return period.name
    }

    @TypeConverter
    fun toBudgetPeriod(periodString: String): BudgetPeriod {
        return BudgetPeriod.valueOf(periodString)
    }

    @TypeConverter
    fun fromOptimizationType(type: OptimizationType): String {
        return type.name
    }

    @TypeConverter
    fun toOptimizationType(typeString: String): OptimizationType {
        return OptimizationType.valueOf(typeString)
    }

    @TypeConverter
    fun fromSuggestionPriority(priority: SuggestionPriority): String {
        return priority.name
    }

    @TypeConverter
    fun toSuggestionPriority(priorityString: String): SuggestionPriority {
        return SuggestionPriority.valueOf(priorityString)
    }

    @TypeConverter
    fun fromAlertType(type: AlertType): String {
        return type.name
    }

    @TypeConverter
    fun toAlertType(typeString: String): AlertType {
        return AlertType.valueOf(typeString)
    }

    @TypeConverter
    fun fromIngredientCostBreakdownList(costs: List<IngredientCostBreakdown>): String {
        return costs.joinToString(";") { "${it.ingredientName}:${it.quantity}:${it.unit}:${it.unitPrice}:${it.totalCost}:${it.isEstimated}" }
    }

    @TypeConverter
    fun toIngredientCostBreakdownList(costsString: String): List<IngredientCostBreakdown> {
        if (costsString.isEmpty()) return emptyList()
        return costsString.split(";").map { entry ->
            val parts = entry.split(":")
            IngredientCostBreakdown(
                ingredientName = parts[0],
                quantity = parts[1].toDouble(),
                unit = parts[2],
                unitPrice = parts[3].toDouble(),
                totalCost = parts[4].toDouble(),
                isEstimated = parts[5].toBoolean()
            )
        }
    }

    // Goal tracking converters
    @TypeConverter
    fun fromGoalType(type: GoalType): String {
        return type.name
    }

    @TypeConverter
    fun toGoalType(typeString: String): GoalType {
        return GoalType.valueOf(typeString)
    }

    @TypeConverter
    fun fromGoalCategory(category: GoalCategory): String {
        return category.name
    }

    @TypeConverter
    fun toGoalCategory(categoryString: String): GoalCategory {
        return GoalCategory.valueOf(categoryString)
    }

    @TypeConverter
    fun fromGoalPriority(priority: GoalPriority): String {
        return priority.name
    }

    @TypeConverter
    fun toGoalPriority(priorityString: String): GoalPriority {
        return GoalPriority.valueOf(priorityString)
    }

    @TypeConverter
    fun fromProgressSource(source: ProgressSource): String {
        return source.name
    }

    @TypeConverter
    fun toProgressSource(sourceString: String): ProgressSource {
        return ProgressSource.valueOf(sourceString)
    }

    @TypeConverter
    fun fromTrendAnalysisEnum(trend: com.beaconledger.welltrack.data.model.GoalTrend): String {
        return trend.name
    }

    @TypeConverter
    fun toTrendAnalysisEnum(trendString: String): com.beaconledger.welltrack.data.model.GoalTrend {
        return com.beaconledger.welltrack.data.model.GoalTrend.valueOf(trendString)
    }

    @TypeConverter
    fun fromGoalMilestoneList(milestones: List<GoalMilestone>): String {
        return milestones.joinToString(";") { milestone ->
            "${milestone.id}:${milestone.goalId}:${milestone.title}:${milestone.targetValue}:${milestone.order}:${milestone.isCompleted}"
        }
    }

    @TypeConverter
    fun toGoalMilestoneList(milestonesString: String): List<GoalMilestone> {
        if (milestonesString.isEmpty()) return emptyList()
        return milestonesString.split(";").map { entry ->
            val parts = entry.split(":")
            GoalMilestone(
                id = parts[0],
                goalId = parts[1],
                title = parts[2],
                description = null,
                targetValue = parts[3].toDouble(),
                targetDate = null,
                isCompleted = parts[5].toBoolean(),
                completedAt = null,
                order = parts[4].toInt()
            )
        }
    }

    // Accessibility Settings converter
    @TypeConverter
    fun fromAccessibilitySettings(settings: AccessibilitySettings): String {
        return "${settings.highContrastEnabled}," +
                "${settings.reduceAnimationsEnabled}," +
                "${settings.largeTextEnabled}," +
                "${settings.screenReaderOptimizationEnabled}," +
                "${settings.audioDescriptionsEnabled}," +
                "${settings.largeTouchTargetsEnabled}," +
                "${settings.reduceMotionEnabled}," +
                "${settings.simplifiedUIEnabled}," +
                "${settings.extendedTimeoutsEnabled}"
    }

    @TypeConverter
    fun toAccessibilitySettings(settingsString: String): AccessibilitySettings {
        val parts = settingsString.split(",")
        return AccessibilitySettings(
            highContrastEnabled = parts[0].toBoolean(),
            reduceAnimationsEnabled = parts[1].toBoolean(),
            largeTextEnabled = parts[2].toBoolean(),
            screenReaderOptimizationEnabled = parts[3].toBoolean(),
            audioDescriptionsEnabled = parts[4].toBoolean(),
            largeTouchTargetsEnabled = parts[5].toBoolean(),
            reduceMotionEnabled = parts[6].toBoolean(),
            simplifiedUIEnabled = parts[7].toBoolean(),
            extendedTimeoutsEnabled = parts[8].toBoolean()
        )
    }

    @TypeConverter
    fun fromDateRange(dateRange: ExportDateRange?): String? {
        return dateRange?.let { "${it.startDate}|${it.endDate}" }
    }

    @TypeConverter
    fun toDateRange(dateRangeString: String?): ExportDateRange? {
        return dateRangeString?.let {
            val parts = it.split("|")
            if (parts.size == 2) {
                ExportDateRange(
                    startDate = LocalDateTime.parse(parts[0]),
                    endDate = LocalDateTime.parse(parts[1])
                )
            } else null
        }
    }

    @TypeConverter
    fun fromStringStringMap(map: Map<String, String>): String {
        return map.entries.joinToString(",") { "${it.key}:${it.value}" }
    }

    @TypeConverter
    fun toStringStringMap(mapString: String): Map<String, String> {
        return if (mapString.isEmpty()) {
            emptyMap()
        } else {
            mapString.split(",").associate {
                val parts = it.split(":")
                if (parts.size == 2) parts[0] to parts[1] else parts[0] to ""
            }
        }
    }

    @TypeConverter
    fun fromStringIntMap(map: Map<String, Int>): String {
        return map.entries.joinToString(",") { "${it.key}:${it.value}" }
    }

    @TypeConverter
    fun toStringIntMap(mapString: String): Map<String, Int> {
        return if (mapString.isEmpty()) {
            emptyMap()
        } else {
            mapString.split(",").associate {
                val parts = it.split(":")
                if (parts.size == 2) parts[0] to (parts[1].toIntOrNull() ?: 0) else parts[0] to 0
            }
        }
    }

    // Data Export converters
    @TypeConverter
    fun fromExportType(type: ExportType): String {
        return type.name
    }

    @TypeConverter
    fun toExportType(typeString: String): ExportType {
        return ExportType.valueOf(typeString)
    }

    @TypeConverter
    fun fromExportFormat(format: ExportFormat): String {
        return format.name
    }

    @TypeConverter
    fun toExportFormat(formatString: String): ExportFormat {
        return ExportFormat.valueOf(formatString)
    }

    @TypeConverter
    fun fromExportStatus(status: ExportStatus): String {
        return status.name
    }

    @TypeConverter
    fun toExportStatus(statusString: String): ExportStatus {
        return ExportStatus.valueOf(statusString)
    }

    // Notification converters
    @TypeConverter
    fun fromNotificationType(type: NotificationType): String {
        return type.name
    }

    @TypeConverter
    fun toNotificationType(typeString: String): NotificationType {
        return NotificationType.valueOf(typeString)
    }

    // Data Deletion converters
    @TypeConverter
    fun fromDeletionStatus(status: DeletionStatus): String {
        return status.name
    }

    @TypeConverter
    fun toDeletionStatus(statusString: String): DeletionStatus {
        return DeletionStatus.valueOf(statusString)
    }
}
