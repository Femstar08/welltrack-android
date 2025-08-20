package com.beaconledger.welltrack.data.database.dao

import androidx.room.*
import com.beaconledger.welltrack.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DietaryRestrictionsDao {
    
    // User Dietary Restrictions
    @Query("SELECT * FROM user_dietary_restrictions WHERE userId = :userId AND isActive = 1")
    fun getUserDietaryRestrictions(userId: String): Flow<List<UserDietaryRestriction>>
    
    @Query("SELECT * FROM user_dietary_restrictions WHERE userId = :userId AND restrictionType = :type AND isActive = 1")
    suspend fun getUserRestrictionByType(userId: String, type: DietaryRestrictionType): UserDietaryRestriction?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDietaryRestriction(restriction: UserDietaryRestriction)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDietaryRestrictions(restrictions: List<UserDietaryRestriction>)
    
    @Update
    suspend fun updateDietaryRestriction(restriction: UserDietaryRestriction)
    
    @Query("UPDATE user_dietary_restrictions SET isActive = 0 WHERE id = :restrictionId")
    suspend fun deactivateDietaryRestriction(restrictionId: String)
    
    @Query("DELETE FROM user_dietary_restrictions WHERE userId = :userId")
    suspend fun deleteAllUserDietaryRestrictions(userId: String)
    
    // User Allergies
    @Query("SELECT * FROM user_allergies WHERE userId = :userId AND isActive = 1")
    fun getUserAllergies(userId: String): Flow<List<UserAllergy>>
    
    @Query("SELECT * FROM user_allergies WHERE userId = :userId AND allergen = :allergen AND isActive = 1")
    suspend fun getUserAllergyByAllergen(userId: String, allergen: String): UserAllergy?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllergy(allergy: UserAllergy)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllergies(allergies: List<UserAllergy>)
    
    @Update
    suspend fun updateAllergy(allergy: UserAllergy)
    
    @Query("UPDATE user_allergies SET isActive = 0 WHERE id = :allergyId")
    suspend fun deactivateAllergy(allergyId: String)
    
    @Query("DELETE FROM user_allergies WHERE userId = :userId")
    suspend fun deleteAllUserAllergies(userId: String)
    
    // User Food Preferences
    @Query("SELECT * FROM user_food_preferences WHERE userId = :userId")
    fun getUserFoodPreferences(userId: String): Flow<List<UserFoodPreference>>
    
    @Query("SELECT * FROM user_food_preferences WHERE userId = :userId AND preferenceType = :type")
    fun getUserPreferencesByType(userId: String, type: FoodPreferenceType): Flow<List<UserFoodPreference>>
    
    @Query("SELECT * FROM user_food_preferences WHERE userId = :userId AND item = :item AND preferenceType = :type")
    suspend fun getUserPreferenceByItem(userId: String, item: String, type: FoodPreferenceType): UserFoodPreference?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoodPreference(preference: UserFoodPreference)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoodPreferences(preferences: List<UserFoodPreference>)
    
    @Update
    suspend fun updateFoodPreference(preference: UserFoodPreference)
    
    @Delete
    suspend fun deleteFoodPreference(preference: UserFoodPreference)
    
    @Query("DELETE FROM user_food_preferences WHERE userId = :userId")
    suspend fun deleteAllUserFoodPreferences(userId: String)
    
    // Meal Dietary Tags
    @Query("SELECT * FROM meal_dietary_tags WHERE mealId = :mealId")
    fun getMealDietaryTags(mealId: String): Flow<List<MealDietaryTag>>
    
    @Query("SELECT * FROM meal_dietary_tags WHERE mealId = :mealId AND tagType = :tagType")
    suspend fun getMealTagsByType(mealId: String, tagType: DietaryTagType): List<MealDietaryTag>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMealDietaryTag(tag: MealDietaryTag)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMealDietaryTags(tags: List<MealDietaryTag>)
    
    @Delete
    suspend fun deleteMealDietaryTag(tag: MealDietaryTag)
    
    @Query("DELETE FROM meal_dietary_tags WHERE mealId = :mealId")
    suspend fun deleteAllMealTags(mealId: String)
    
    // Recipe Dietary Tags
    @Query("SELECT * FROM recipe_dietary_tags WHERE recipeId = :recipeId")
    fun getRecipeDietaryTags(recipeId: String): Flow<List<RecipeDietaryTag>>
    
    @Query("SELECT * FROM recipe_dietary_tags WHERE recipeId = :recipeId AND tagType = :tagType")
    suspend fun getRecipeTagsByType(recipeId: String, tagType: DietaryTagType): List<RecipeDietaryTag>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipeDietaryTag(tag: RecipeDietaryTag)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipeDietaryTags(tags: List<RecipeDietaryTag>)
    
    @Delete
    suspend fun deleteRecipeDietaryTag(tag: RecipeDietaryTag)
    
    @Query("DELETE FROM recipe_dietary_tags WHERE recipeId = :recipeId")
    suspend fun deleteAllRecipeTags(recipeId: String)
    
    // Complex queries for dietary compatibility checking
    @Query("""
        SELECT DISTINCT r.* FROM recipes r
        LEFT JOIN recipe_dietary_tags rdt ON r.id = rdt.recipeId
        WHERE r.id NOT IN (
            SELECT DISTINCT rdt2.recipeId FROM recipe_dietary_tags rdt2
            INNER JOIN user_dietary_restrictions udr ON rdt2.tagValue = udr.restrictionType
            WHERE udr.userId = :userId AND udr.isActive = 1 AND udr.severity >= :minSeverity
        )
        AND r.id NOT IN (
            SELECT DISTINCT rdt3.recipeId FROM recipe_dietary_tags rdt3
            INNER JOIN user_allergies ua ON rdt3.tagValue = ua.allergen
            WHERE ua.userId = :userId AND ua.isActive = 1
        )
    """)
    suspend fun getCompatibleRecipes(userId: String, minSeverity: Int): List<Recipe>
    
    @Query("""
        SELECT COUNT(*) FROM recipe_dietary_tags rdt
        INNER JOIN user_dietary_restrictions udr ON rdt.tagValue = udr.restrictionType
        WHERE rdt.recipeId = :recipeId AND udr.userId = :userId AND udr.isActive = 1
    """)
    suspend fun countRestrictionViolations(recipeId: String, userId: String): Int
    
    @Query("""
        SELECT COUNT(*) FROM recipe_dietary_tags rdt
        INNER JOIN user_allergies ua ON rdt.tagValue = ua.allergen
        WHERE rdt.recipeId = :recipeId AND ua.userId = :userId AND ua.isActive = 1
    """)
    suspend fun countAllergenViolations(recipeId: String, userId: String): Int
    
    // Batch operations for performance
    @Transaction
    suspend fun replaceUserDietaryProfile(
        userId: String,
        restrictions: List<UserDietaryRestriction>,
        allergies: List<UserAllergy>,
        preferences: List<UserFoodPreference>
    ) {
        deleteAllUserDietaryRestrictions(userId)
        deleteAllUserAllergies(userId)
        deleteAllUserFoodPreferences(userId)
        
        insertDietaryRestrictions(restrictions)
        insertAllergies(allergies)
        insertFoodPreferences(preferences)
    }
}