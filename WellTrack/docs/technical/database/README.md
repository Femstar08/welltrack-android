# Database Documentation

WellTrack uses Room as the local database abstraction layer over SQLite, providing compile-time query validation and seamless integration with Android Architecture Components.

## Database Overview

### Database Configuration
- **Framework**: Room with SQLite backend
- **Version**: 12 (current schema version)
- **Location**: `app/src/main/java/com/beaconledger/welltrack/data/database/`
- **Migration Strategy**: Incremental schema migrations with data preservation

### Key Features
- **Type Converters**: Custom converters for complex data types
- **Relationships**: Proper foreign key constraints and relationships
- **Indexing**: Optimized indexes for frequently queried columns
- **Backup Support**: Export/import capabilities for data portability

## Database Schema

### Core Entities

#### User Management
```kotlin
@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: String,
    val email: String,
    val displayName: String?,
    val profileImageUrl: String?,
    val createdAt: Long,
    val isActive: Boolean = true
)

@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey val userId: String,
    val age: Int?,
    val height: Float?,
    val weight: Float?,
    val activityLevel: String?,
    val dietaryPreferences: List<String>,
    val healthGoals: List<String>
)
```

#### Meal Management
```kotlin
@Entity(tableName = "meals")
data class Meal(
    @PrimaryKey val id: String,
    val userId: String,
    val name: String,
    val description: String?,
    val category: MealCategory,
    val servings: Int,
    val prepTimeMinutes: Int,
    val cookTimeMinutes: Int,
    val difficulty: DifficultyLevel,
    val rating: Float?,
    val nutritionGrade: String?,
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(tableName = "recipes")
data class Recipe(
    @PrimaryKey val id: String,
    val mealId: String,
    val instructions: List<String>,
    val ingredients: List<Ingredient>,
    val nutritionInfo: NutritionInfo?,
    val tags: List<String>,
    val sourceUrl: String?,
    val imageUrls: List<String>
)

@Entity(tableName = "meal_logs")
data class MealLog(
    @PrimaryKey val id: String,
    val userId: String,
    val mealId: String,
    val consumedAt: Long,
    val servings: Float,
    val notes: String?,
    val rating: Int?
)
```

#### Health Data
```kotlin
@Entity(tableName = "health_metrics")
data class HealthMetric(
    @PrimaryKey val id: String,
    val userId: String,
    val type: HealthMetricType,
    val value: Double,
    val unit: String,
    val timestamp: Long,
    val source: DataSource,
    val deviceInfo: String?,
    val metadata: Map<String, String>?
)

@Entity(tableName = "biomarkers")
data class Biomarker(
    @PrimaryKey val id: String,
    val userId: String,
    val type: BiomarkerType,
    val value: Double,
    val unit: String,
    val normalRange: String?,
    val testDate: Long,
    val labName: String?,
    val notes: String?
)

@Entity(tableName = "supplements")
data class Supplement(
    @PrimaryKey val id: String,
    val userId: String,
    val name: String,
    val dosage: String,
    val frequency: String,
    val startDate: Long,
    val endDate: Long?,
    val isActive: Boolean,
    val reminderEnabled: Boolean
)
```

#### Shopping and Pantry
```kotlin
@Entity(tableName = "shopping_lists")
data class ShoppingList(
    @PrimaryKey val id: String,
    val userId: String,
    val name: String,
    val createdAt: Long,
    val isCompleted: Boolean,
    val totalEstimatedCost: Double?,
    val actualCost: Double?
)

@Entity(tableName = "shopping_list_items")
data class ShoppingListItem(
    @PrimaryKey val id: String,
    val shoppingListId: String,
    val name: String,
    val quantity: String,
    val unit: String?,
    val estimatedCost: Double?,
    val actualCost: Double?,
    val isCompleted: Boolean,
    val notes: String?
)

@Entity(tableName = "pantry_items")
data class PantryItem(
    @PrimaryKey val id: String,
    val userId: String,
    val name: String,
    val quantity: String,
    val unit: String?,
    val purchaseDate: Long?,
    val expiryDate: Long?,
    val location: String?,
    val barcode: String?,
    val isLowStock: Boolean
)
```

#### Meal Planning
```kotlin
@Entity(tableName = "meal_plans")
data class MealPlan(
    @PrimaryKey val id: String,
    val userId: String,
    val startDate: Long,
    val endDate: Long,
    val name: String?,
    val isActive: Boolean,
    val generatedAt: Long
)

@Entity(tableName = "meal_plan_entries")
data class MealPlanEntry(
    @PrimaryKey val id: String,
    val mealPlanId: String,
    val mealId: String,
    val scheduledDate: Long,
    val mealType: MealType,
    val servings: Int,
    val isCompleted: Boolean,
    val notes: String?
)
```

#### Analytics and Tracking
```kotlin
@Entity(tableName = "daily_tracking")
data class DailyTracking(
    @PrimaryKey val id: String,
    val userId: String,
    val date: Long,
    val totalCalories: Double?,
    val totalProtein: Double?,
    val totalCarbs: Double?,
    val totalFat: Double?,
    val waterIntake: Double?,
    val sleepHours: Double?,
    val exerciseMinutes: Int?,
    val mood: Int?,
    val energyLevel: Int?
)

@Entity(tableName = "goals")
data class Goal(
    @PrimaryKey val id: String,
    val userId: String,
    val type: GoalType,
    val title: String,
    val description: String?,
    val targetValue: Double,
    val currentValue: Double,
    val unit: String,
    val startDate: Long,
    val targetDate: Long,
    val isActive: Boolean,
    val priority: GoalPriority
)
```

#### Security and Audit
```kotlin
@Entity(tableName = "audit_logs")
data class AuditLog(
    @PrimaryKey val id: String,
    val userId: String?,
    val action: String,
    val resourceType: String,
    val resourceId: String?,
    val details: String?,
    val ipAddress: String?,
    val userAgent: String?,
    val timestamp: Long,
    val sessionId: String?
)

@Entity(tableName = "sync_status")
data class SyncStatus(
    @PrimaryKey val id: String,
    val userId: String,
    val dataType: String,
    val lastSyncTimestamp: Long,
    val syncStatus: String,
    val errorMessage: String?,
    val retryCount: Int,
    val nextRetryTimestamp: Long?
)
```

## Relationships and Foreign Keys

### Entity Relationships
```kotlin
// Meal to Recipe (One-to-One)
@Relation(
    parentColumn = "id",
    entityColumn = "mealId"
)

// User to Meals (One-to-Many)
@Relation(
    parentColumn = "id",
    entityColumn = "userId"
)

// Meal Plan to Entries (One-to-Many)
@Relation(
    parentColumn = "id",
    entityColumn = "mealPlanId"
)

// Shopping List to Items (One-to-Many)
@Relation(
    parentColumn = "id",
    entityColumn = "shoppingListId"
)
```

### Complex Relationships
```kotlin
data class MealWithRecipe(
    @Embedded val meal: Meal,
    @Relation(
        parentColumn = "id",
        entityColumn = "mealId"
    )
    val recipe: Recipe?
)

data class MealPlanWithEntries(
    @Embedded val mealPlan: MealPlan,
    @Relation(
        parentColumn = "id",
        entityColumn = "mealPlanId",
        entity = MealPlanEntry::class
    )
    val entries: List<MealPlanEntryWithMeal>
)

data class UserWithProfile(
    @Embedded val user: User,
    @Relation(
        parentColumn = "id",
        entityColumn = "userId"
    )
    val profile: UserProfile?
)
```

## Type Converters

### Custom Type Converters
```kotlin
class Converters {
    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        return Gson().fromJson(value, object : TypeToken<List<String>>() {}.type)
    }

    @TypeConverter
    fun fromIngredientList(value: List<Ingredient>): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toIngredientList(value: String): List<Ingredient> {
        return Gson().fromJson(value, object : TypeToken<List<Ingredient>>() {}.type)
    }

    @TypeConverter
    fun fromNutritionInfo(value: NutritionInfo?): String? {
        return value?.let { Gson().toJson(it) }
    }

    @TypeConverter
    fun toNutritionInfo(value: String?): NutritionInfo? {
        return value?.let { Gson().fromJson(it, NutritionInfo::class.java) }
    }

    @TypeConverter
    fun fromDataSource(value: DataSource): String {
        return value.name
    }

    @TypeConverter
    fun toDataSource(value: String): DataSource {
        return DataSource.valueOf(value)
    }
}
```

## Database Access Objects (DAOs)

### Meal DAO
```kotlin
@Dao
interface MealDao {
    @Query("SELECT * FROM meals WHERE userId = :userId ORDER BY createdAt DESC")
    fun getMealsByUser(userId: String): Flow<List<Meal>>

    @Query("SELECT * FROM meals WHERE id = :mealId")
    suspend fun getMealById(mealId: String): Meal?

    @Transaction
    @Query("SELECT * FROM meals WHERE userId = :userId")
    suspend fun getMealsWithRecipes(userId: String): List<MealWithRecipe>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeal(meal: Meal): Long

    @Update
    suspend fun updateMeal(meal: Meal)

    @Delete
    suspend fun deleteMeal(meal: Meal)

    @Query("DELETE FROM meals WHERE userId = :userId")
    suspend fun deleteMealsByUser(userId: String)
}
```

### Health Metrics DAO
```kotlin
@Dao
interface HealthMetricDao {
    @Query("SELECT * FROM health_metrics WHERE userId = :userId AND type = :type ORDER BY timestamp DESC")
    fun getHealthMetricsByType(userId: String, type: HealthMetricType): Flow<List<HealthMetric>>

    @Query("SELECT * FROM health_metrics WHERE userId = :userId AND timestamp BETWEEN :startTime AND :endTime")
    suspend fun getHealthMetricsInRange(userId: String, startTime: Long, endTime: Long): List<HealthMetric>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHealthMetrics(metrics: List<HealthMetric>)

    @Query("DELETE FROM health_metrics WHERE userId = :userId AND source = :source")
    suspend fun deleteHealthMetricsBySource(userId: String, source: DataSource)
}
```

### Shopping List DAO
```kotlin
@Dao
interface ShoppingListDao {
    @Transaction
    @Query("SELECT * FROM shopping_lists WHERE userId = :userId ORDER BY createdAt DESC")
    fun getShoppingListsWithItems(userId: String): Flow<List<ShoppingListWithItems>>

    @Query("SELECT * FROM shopping_lists WHERE id = :listId")
    suspend fun getShoppingListById(listId: String): ShoppingList?

    @Insert
    suspend fun insertShoppingList(shoppingList: ShoppingList): Long

    @Insert
    suspend fun insertShoppingListItems(items: List<ShoppingListItem>)

    @Update
    suspend fun updateShoppingListItem(item: ShoppingListItem)

    @Query("UPDATE shopping_lists SET isCompleted = :isCompleted WHERE id = :listId")
    suspend fun updateShoppingListStatus(listId: String, isCompleted: Boolean)
}
```

## Database Migrations

### Migration Strategy
```kotlin
val MIGRATION_11_12 = object : Migration(11, 12) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Add goals table
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS goals (
                id TEXT PRIMARY KEY NOT NULL,
                userId TEXT NOT NULL,
                type TEXT NOT NULL,
                title TEXT NOT NULL,
                description TEXT,
                targetValue REAL NOT NULL,
                currentValue REAL NOT NULL,
                unit TEXT NOT NULL,
                startDate INTEGER NOT NULL,
                targetDate INTEGER NOT NULL,
                isActive INTEGER NOT NULL DEFAULT 1,
                priority TEXT NOT NULL
            )
        """)

        // Add index for efficient querying
        database.execSQL("CREATE INDEX index_goals_userId ON goals(userId)")
        database.execSQL("CREATE INDEX index_goals_type ON goals(type)")
    }
}
```

### Data Migration Utilities
```kotlin
class DatabaseMigrationHelper {
    suspend fun migrateUserData(oldVersion: Int, newVersion: Int) {
        when {
            oldVersion < 10 && newVersion >= 10 -> {
                // Migrate nutrition data format
                migrateNutritionData()
            }
            oldVersion < 11 && newVersion >= 11 -> {
                // Add default goal entries for existing users
                createDefaultGoals()
            }
        }
    }

    private suspend fun migrateNutritionData() {
        // Convert old nutrition format to new structure
    }

    private suspend fun createDefaultGoals() {
        // Create default health goals for existing users
    }
}
```

## Indexing Strategy

### Performance Indexes
```kotlin
@Entity(
    tableName = "health_metrics",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["type"]),
        Index(value = ["timestamp"]),
        Index(value = ["userId", "type", "timestamp"])
    ]
)

@Entity(
    tableName = "meals",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["category"]),
        Index(value = ["createdAt"])
    ]
)

@Entity(
    tableName = "meal_logs",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["consumedAt"]),
        Index(value = ["userId", "consumedAt"])
    ]
)
```

## Database Optimization

### Query Optimization
```kotlin
// Efficient pagination
@Query("""
    SELECT * FROM meals
    WHERE userId = :userId
    ORDER BY createdAt DESC
    LIMIT :limit OFFSET :offset
""")
suspend fun getMealsPaginated(userId: String, limit: Int, offset: Int): List<Meal>

// Aggregated queries
@Query("""
    SELECT
        DATE(consumedAt / 1000, 'unixepoch') as date,
        SUM(calories) as totalCalories,
        COUNT(*) as mealCount
    FROM meal_logs
    WHERE userId = :userId
    GROUP BY DATE(consumedAt / 1000, 'unixepoch')
    ORDER BY date DESC
""")
suspend fun getDailyCalorieSummary(userId: String): List<DailyCaloriesSummary>
```

### Memory Management
```kotlin
class DatabaseManager {
    fun optimizeDatabase() {
        // Clean up old data
        cleanupExpiredData()

        // Vacuum database
        database.openHelper.writableDatabase.execSQL("VACUUM")

        // Update statistics
        database.openHelper.writableDatabase.execSQL("ANALYZE")
    }

    private fun cleanupExpiredData() {
        val thirtyDaysAgo = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30)

        // Clean up old sync status records
        database.syncStatusDao().deleteOldSyncRecords(thirtyDaysAgo)

        // Clean up old audit logs (keep 90 days)
        val ninetyDaysAgo = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(90)
        database.auditLogDao().deleteOldLogs(ninetyDaysAgo)
    }
}
```

## Backup and Export

### Data Export
```kotlin
class DatabaseExportManager {
    suspend fun exportUserData(userId: String): DatabaseExport {
        return DatabaseExport(
            user = userDao.getUserById(userId),
            meals = mealDao.getMealsByUser(userId).first(),
            healthMetrics = healthMetricDao.getAllHealthMetrics(userId),
            shoppingLists = shoppingListDao.getShoppingListsByUser(userId),
            // ... other data
        )
    }

    suspend fun importUserData(export: DatabaseExport) {
        database.withTransaction {
            // Import user data with conflict resolution
            export.meals.forEach { meal ->
                mealDao.insertMeal(meal)
            }
            // ... import other data
        }
    }
}
```

### Backup Strategy
- **Local Backup**: Room database backup to internal storage
- **Cloud Backup**: Encrypted export to Supabase storage
- **Incremental Sync**: Only sync changed data since last backup
- **Conflict Resolution**: Timestamp-based conflict resolution

## Security Considerations

### Data Encryption
- **At Rest**: Room database files encrypted using SQLCipher
- **In Transit**: All sync operations use TLS encryption
- **Key Management**: Encryption keys stored in Android Keystore

### Access Control
```kotlin
class SecureDatabaseAccess {
    fun requireAuthentication(): Boolean {
        return when {
            biometricManager.isAuthenticationRequired() -> {
                biometricManager.authenticate()
            }
            else -> true
        }
    }

    suspend fun <T> secureQuery(query: suspend () -> T): T {
        if (!requireAuthentication()) {
            throw SecurityException("Authentication required")
        }

        return auditLogger.logDataAccess("database_query") {
            query()
        }
    }
}
```

## Testing Strategy

### Database Testing
```kotlin
@RunWith(AndroidJUnit4::class)
class DatabaseTest {
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: WellTrackDatabase
    private lateinit var mealDao: MealDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, WellTrackDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        mealDao = database.mealDao()
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun insertAndRetrieveMeal() = runTest {
        val meal = TestDataFactory.createMeal()
        mealDao.insertMeal(meal)

        val retrieved = mealDao.getMealById(meal.id)
        assertThat(retrieved).isEqualTo(meal)
    }
}
```

---

**Last Updated**: January 2025
**Schema Version**: 12
**Next Review**: February 2025