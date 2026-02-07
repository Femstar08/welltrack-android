package com.beaconledger.welltrack.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.data.database.dao.*
import com.beaconledger.welltrack.data.database.migrations.MIGRATION_1_2
import com.beaconledger.welltrack.data.database.migrations.MIGRATION_2_3
import com.beaconledger.welltrack.data.compliance.DataDeletionRecord

const val DATABASE_NAME = "welltrack_database"

@Database(
    entities = [
        // Core user management
        User::class,
        UserProfile::class,
        UserPreferences::class,

        // Recipes and meals
        Recipe::class,
        RecipeIngredient::class,
        Meal::class,

        // Pantry management
        PantryItem::class,
        IngredientUsageHistory::class,
        IngredientPreference::class,

        // Shopping lists
        ShoppingList::class,
        ShoppingListItem::class,

        // Meal planning
        MealPlan::class,
        PlannedMeal::class,
        PlannedSupplement::class,

        // Meal prep
        MealPrepInstruction::class,
        StorageRecommendation::class,
        Leftover::class,
        LeftoverCombination::class,

        // Supplements
        Supplement::class,
        UserSupplement::class,
        SupplementIntake::class,

        // Health metrics and biomarkers
        HealthMetric::class,
        BiomarkerEntry::class,
        BiomarkerTestSession::class,
        BloodTestReminder::class,

        // Fitness and workouts
        Workout::class,
        ExerciseTemplate::class,

        // Nutrition tracking
        MacronutrientTarget::class,
        MacronutrientIntake::class,
        CustomNutrient::class,

        // Daily tracking
        DailyTrackingEntry::class,

        // Dietary restrictions and preferences
        UserDietaryRestriction::class,
        UserAllergy::class,
        UserFoodPreference::class,
        MealDietaryTag::class,
        RecipeDietaryTag::class,

        // Goals and achievements
        Goal::class,
        GoalProgress::class,
        GoalMilestone::class,
        GoalPrediction::class,

        // Social features
        FamilyGroup::class,
        FamilyMember::class,
        SharedMealPlan::class,
        SharedRecipe::class,
        CollaborativeMealPrep::class,
        SocialAchievement::class,
        SharedAchievement::class,
        SharedShoppingList::class,
        SharedShoppingListItem::class,

        // Budget and cost tracking
        IngredientPrice::class,
        MealCost::class,
        BudgetSettings::class,
        BudgetTracking::class,

        // Habits
        CustomHabit::class,
        HabitCompletion::class,

        // Cooking sessions
        CookingSession::class,

        // Notifications
        NotificationEntity::class,

        // Sync and data management
        SyncStatus::class,
        DataExport::class,
        AuditLog::class,
        com.beaconledger.welltrack.data.compliance.DataDeletionRecord::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class WellTrackDatabase : RoomDatabase() {
    
    abstract fun userDao(): UserDao
    abstract fun recipeDao(): RecipeDao
    abstract fun mealDao(): MealDao
    abstract fun healthMetricDao(): HealthMetricDao
    abstract fun userProfileDao(): ProfileDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun cookingSessionDao(): CookingSessionDao
    abstract fun mealPlanDao(): MealPlanDao
    abstract fun mealPrepDao(): MealPrepDao
    abstract fun supplementDao(): SupplementDao
    abstract fun pantryItemDao(): PantryDao
    abstract fun ingredientUsageDao(): IngredientUsageDao
    abstract fun shoppingListDao(): ShoppingListDao
    abstract fun biomarkerDao(): BiomarkerDao
    abstract fun costBudgetDao(): CostBudgetDao
    abstract fun syncStatusDao(): SyncStatusDao
    abstract fun macronutrientDao(): MacronutrientDao
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
    abstract fun userPreferencesDao(): UserPreferencesDao
    
    // Additional DAO methods
    abstract fun recipeIngredientDao(): RecipeIngredientDao
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
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}