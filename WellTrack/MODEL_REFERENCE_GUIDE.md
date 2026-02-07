# Model Reference Guide

Quick reference for key data models to prevent compilation errors.

## Core Models

### Meal
```kotlin
data class Meal(
    @PrimaryKey
    val id: String,
    val userId: String,
    val recipeId: String? = null,           // ⚠️ NOT recipeName
    val timestamp: String,                   // ⚠️ String, not LocalDateTime
    val mealType: MealType,
    val portions: Float = 1.0f,
    val nutritionInfo: String,              // ⚠️ JSON String, not object
    val score: MealScore,                   // ⚠️ Required field
    val status: MealStatus = MealStatus.PLANNED,
    val notes: String? = null,
    val rating: Float? = null,
    val isFavorite: Boolean = false
)
```

### Supplement (Library Entity)
```kotlin
data class Supplement(
    @PrimaryKey
    val id: String,
    val name: String,
    val brand: String? = null,
    val description: String? = null,
    val servingSize: String,                // ⚠️ String, not Double
    val servingUnit: String,
    val nutritionalInfo: String,            // ⚠️ JSON String
    val barcode: String? = null,
    val imageUrl: String? = null,
    val category: SupplementCategory,
    val createdAt: String,                  // ⚠️ String, not LocalDateTime
    val updatedAt: String                   // ⚠️ String, not LocalDateTime
)
// ⚠️ NO userId - it's a library entity
```

### UserSupplement (User's Supplement Schedule)
```kotlin
data class UserSupplement(
    @PrimaryKey
    val id: String,
    val userId: String,                     // ⚠️ userId is HERE, not in Supplement
    val supplementId: String,
    val customName: String? = null,
    val dosage: Double,
    val dosageUnit: String,
    val frequency: SupplementFrequency,
    val scheduledTimes: String,             // ⚠️ JSON array of times
    val isActive: Boolean = true,
    val notes: String? = null,
    val startDate: String,
    val endDate: String? = null,
    val createdAt: String,
    val updatedAt: String
)
```

### SupplementIntake (Actual Taken Records)
```kotlin
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
```

### HealthMetric
```kotlin
data class HealthMetric(
    @PrimaryKey
    val id: String,
    val userId: String,
    val type: HealthMetricType,
    val value: Double,
    val unit: String,
    val timestamp: String,                  // ⚠️ String, not LocalDateTime
    val source: DataSource,
    val metadata: String? = null,
    val accuracy: Float? = null,
    val deviceInfo: String? = null
)
```

### Goal
```kotlin
data class Goal(
    @PrimaryKey
    val id: String,
    val userId: String,
    val type: GoalType,
    val title: String,
    val description: String?,
    val targetValue: Double,
    val currentValue: Double = 0.0,
    val unit: String,
    val startDate: LocalDate,               // ⚠️ LocalDate, not String
    val targetDate: LocalDate,              // ⚠️ LocalDate, not String
    val isActive: Boolean = true,
    val priority: GoalPriority = GoalPriority.MEDIUM,
    val category: GoalCategory,
    val milestones: List<GoalMilestone> = emptyList(),
    val createdAt: LocalDateTime = LocalDateTime.now(),  // ⚠️ LocalDateTime with default
    val updatedAt: LocalDateTime = LocalDateTime.now()   // ⚠️ LocalDateTime with default
)
```

### GoalProgress
```kotlin
data class GoalProgress(
    @PrimaryKey
    val id: String,
    val goalId: String,
    val value: Double,
    val notes: String?,
    val recordedAt: LocalDateTime = LocalDateTime.now(),  // ⚠️ Has default value
    val source: ProgressSource = ProgressSource.MANUAL    // ⚠️ Has default value
)
// ⚠️ When creating, can omit recordedAt and source
```

## Important Date/Time Fields

### String Timestamps (ISO 8601 Format)
- Meal.timestamp
- Supplement.createdAt
- Supplement.updatedAt
- UserSupplement.startDate
- UserSupplement.endDate
- UserSupplement.createdAt
- UserSupplement.updatedAt
- SupplementIntake.takenAt
- SupplementIntake.scheduledAt
- SupplementIntake.createdAt
- HealthMetric.timestamp

### LocalDateTime (Object)
- Goal.createdAt (has default)
- Goal.updatedAt (has default)
- GoalProgress.recordedAt (has default)
- DateRange.startDate
- DateRange.endDate

### LocalDate (Object)
- Goal.startDate
- Goal.targetDate

## Common Pitfalls

### ❌ Wrong:
```kotlin
val meal = Meal(
    recipeName = "Chicken",              // Wrong property name
    timestamp = LocalDateTime.now(),     // Wrong type
    nutritionInfo = NutritionInfo(...)   // Wrong type
)
```

### ✅ Correct:
```kotlin
val meal = Meal(
    recipeId = "recipe-123",             // Correct property
    timestamp = LocalDateTime.now().toString(),  // String
    nutritionInfo = gson.toJson(nutritionInfo),  // JSON string
    score = MealScore.B                  // Don't forget required fields
)
```

### ❌ Wrong:
```kotlin
val supplement = Supplement(
    userId = "user-123",                 // Supplement has NO userId
    dosage = 500.0,                      // Wrong property name
    frequency = SupplementFrequency.DAILY // Not in Supplement
)
```

### ✅ Correct:
```kotlin
val supplement = Supplement(
    name = "Vitamin D",
    servingSize = "500",                 // String, not number
    servingUnit = "IU",
    nutritionalInfo = "{}",
    category = SupplementCategory.VITAMIN,
    createdAt = LocalDateTime.now().toString(),
    updatedAt = LocalDateTime.now().toString()
)

// Then create UserSupplement to link to user
val userSupplement = UserSupplement(
    userId = "user-123",                 // userId goes here
    supplementId = supplement.id,
    dosage = 500.0,                      // Dosage goes here
    dosageUnit = "IU",
    frequency = SupplementFrequency.DAILY // Frequency goes here
)
```

## DAO Method Signatures

### HealthMetricDao
```kotlin
suspend fun getMetricByTypeAndTimestamp(
    userId: String,
    type: HealthMetricType,
    timestamp: LocalDateTime           // ⚠️ LocalDateTime, not String
): HealthMetric?
```

### MealDao
```kotlin
suspend fun getMealByTimestamp(
    userId: String,
    timestamp: LocalDateTime           // ⚠️ LocalDateTime, not String
): Meal?
```

### SupplementDao
```kotlin
suspend fun getSupplementByNameAndDate(
    name: String,                      // ⚠️ NO userId parameter
    date: String
): Supplement?

suspend fun deleteAllUserSupplementsForUser(userId: String)  // ⚠️ Different from deleteAllSupplementsForUser
```

## Type Conversion Helpers

### String to LocalDateTime
```kotlin
val timestamp: String = "2025-01-15T10:30:00"
val dateTime = LocalDateTime.parse(timestamp, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
```

### LocalDateTime to String
```kotlin
val dateTime = LocalDateTime.now()
val timestamp: String = dateTime.toString()  // ISO 8601 format
```

### Object to JSON String
```kotlin
val nutritionInfo = NutritionInfo(calories = 500.0, protein = 30.0)
val jsonString: String = gson.toJson(nutritionInfo)
```

### JSON String to Object
```kotlin
val jsonString = "{\"calories\":500.0,\"protein\":30.0}"
val nutritionInfo = gson.fromJson(jsonString, NutritionInfo::class.java)
```

## Enums Reference

### MealType
- BREAKFAST
- LUNCH
- DINNER
- SNACK
- SUPPLEMENT

### MealScore
- A (grade: "A")
- B (grade: "B")
- C (grade: "C")
- D (grade: "D")
- E (grade: "E")

### MealStatus
- PLANNED
- IN_PROGRESS
- COMPLETED
- SKIPPED

### SupplementCategory
- VITAMIN
- MINERAL
- PROTEIN
- AMINO_ACID
- HERBAL
- PROBIOTIC
- OMEGA_3
- PRE_WORKOUT
- POST_WORKOUT
- DIGESTIVE
- IMMUNE
- ENERGY
- SLEEP
- JOINT
- OTHER

### SupplementFrequency
- ONCE_DAILY
- TWICE_DAILY
- THREE_TIMES_DAILY
- FOUR_TIMES_DAILY
- EVERY_OTHER_DAY
- WEEKLY
- AS_NEEDED
- CUSTOM

### IntakeStatus
- TAKEN
- SKIPPED
- MISSED
- PARTIAL

### DataSource
- MANUAL_ENTRY
- HEALTH_CONNECT
- GARMIN_CONNECT
- SAMSUNG_HEALTH

### ProgressSource
- MANUAL
- HEALTH_CONNECT
- GARMIN
- SAMSUNG_HEALTH
- MEAL_LOGGING
- HABIT_TRACKING
- AUTOMATIC

## Best Practices

1. **Always check the model definition** before creating instances
2. **Use explicit type parameters** for maps and collections when type inference might fail
3. **Convert timestamps** when calling DAO methods that expect LocalDateTime
4. **Remember defaults** - don't pass parameters that have default values unless you need to override
5. **Understand the entity relationships** - Supplement vs UserSupplement vs SupplementIntake
6. **Use JSON for complex data** - nutritionInfo, metadata, etc. are JSON strings
7. **Check DAO signatures** - some methods take LocalDateTime, others take String
