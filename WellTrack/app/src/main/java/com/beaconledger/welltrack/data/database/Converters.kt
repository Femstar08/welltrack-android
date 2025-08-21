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
}