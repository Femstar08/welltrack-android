package com.beaconledger.welltrack.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.data.database.dao.*
import com.beaconledger.welltrack.data.database.migrations.MIGRATION_1_2
import com.beaconledger.welltrack.data.compliance.DataDeletionRecord

private const val DATABASE_NAME = "welltrack_database"

@Database(
    entities = [
        User::class,
        UserProfile::class,
        Workout::class,
        ExerciseTemplate::class,
        Supplement::class,
        UserSupplement::class,
        SupplementIntake::class,
        ShoppingList::class,
        ShoppingListItem::class,
        NotificationEntity::class,
        BloodTestReminder::class,
        IngredientPrice::class,
        BudgetAlert::class,
        DailyTrackingEntry::class,
        DietaryRestriction::class,
        AuditLog::class,
        Goal::class,
        GoalProgress::class,
        GoalMilestone::class,
        GoalPrediction::class,
        DataExport::class,
        com.beaconledger.welltrack.data.compliance.DataDeletionRecord::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class WellTrackDatabase : RoomDatabase() {
    
    abstract fun userDao(): UserDao
    abstract fun recipeDao(): RecipeDao
    abstract fun mealDao(): MealDao
    abstract fun healthMetricDao(): HealthMetricDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun cookingSessionDao(): CookingSessionDao
    abstract fun mealPlanDao(): MealPlanDao
    abstract fun mealPrepDao(): MealPrepDao
    abstract fun supplementDao(): SupplementDao
    abstract fun pantryItemDao(): PantryItemDao
    abstract fun ingredientUsageDao(): IngredientUsageDao
    abstract fun shoppingListDao(): ShoppingListDao
    abstract fun shoppingListItemDao(): ShoppingListItemDao
    abstract fun biomarkerDao(): BiomarkerDao
    abstract fun costBudgetDao(): CostBudgetDao
    abstract fun syncStatusDao(): SyncStatusDao
    abstract fun macronutrientDao(): MacronutrientDao
    abstract fun socialDao(): SocialDao
    abstract fun achievementDao(): AchievementDao
    abstract fun sharedAchievementDao(): SharedAchievementDao
    abstract fun sharedShoppingListDao(): SharedShoppingListDao
    abstract fun sharedShoppingListItemDao(): SharedShoppingListItemDao
    abstract fun notificationDao(): NotificationDao
    abstract fun dailyTrackingDao(): DailyTrackingDao
    abstract fun dietaryRestrictionsDao(): DietaryRestrictionsDao
    abstract fun auditLogDao(): AuditLogDao
    abstract fun goalDao(): GoalDao
    abstract fun dataExportDao(): DataExportDao
    abstract fun dataDeletionDao(): DataDeletionDao
    
    // Additional DAO methods
    abstract fun recipeIngredientDao(): RecipeIngredientDao
    abstract fun pantryDao(): PantryDao
    abstract fun familyGroupDao(): FamilyGroupDao
    abstract fun familyMemberDao(): FamilyMemberDao
    abstract fun sharedMealPlanDao(): SharedMealPlanDao
    abstract fun sharedRecipeDao(): SharedRecipeDao
    abstract fun collaborativeMealPrepDao(): CollaborativeMealPrepDao
    abstract fun ingredientPreferenceDao(): IngredientPreferenceDao

    companion object {
        @Volatile
        private var INSTANCE: WellTrackDatabase? = null

        fun getDatabase(context: Context): WellTrackDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WellTrackDatabase::class.java,
                    DATABASE_NAME
                )
                .addMigrations(MIGRATION_1_2)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}