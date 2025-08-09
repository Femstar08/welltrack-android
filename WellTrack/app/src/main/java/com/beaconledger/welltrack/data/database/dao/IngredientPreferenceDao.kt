package com.beaconledger.welltrack.data.database.dao

import androidx.room.*
import com.beaconledger.welltrack.data.model.IngredientPreference
import com.beaconledger.welltrack.data.model.PreferenceType
import kotlinx.coroutines.flow.Flow

@Dao
interface IngredientPreferenceDao {
    
    @Query("SELECT * FROM ingredient_preferences WHERE userId = :userId ORDER BY priority DESC, ingredientName ASC")
    fun getPreferencesForUser(userId: String): Flow<List<IngredientPreference>>
    
    @Query("SELECT * FROM ingredient_preferences WHERE userId = :userId AND preferenceType = :type ORDER BY priority DESC, ingredientName ASC")
    fun getPreferencesByType(userId: String, type: PreferenceType): Flow<List<IngredientPreference>>
    
    @Query("SELECT * FROM ingredient_preferences WHERE userId = :userId AND ingredientName = :ingredientName LIMIT 1")
    suspend fun getPreferenceForIngredient(userId: String, ingredientName: String): IngredientPreference?
    
    @Query("SELECT * FROM ingredient_preferences WHERE userId = :userId AND preferenceType = 'PREFERRED' ORDER BY priority DESC")
    suspend fun getPreferredIngredients(userId: String): List<IngredientPreference>
    
    @Query("SELECT * FROM ingredient_preferences WHERE userId = :userId AND preferenceType = 'DISLIKED'")
    suspend fun getDislikedIngredients(userId: String): List<IngredientPreference>
    
    @Query("SELECT * FROM ingredient_preferences WHERE userId = :userId AND preferenceType = 'ALLERGIC'")
    suspend fun getAllergicIngredients(userId: String): List<IngredientPreference>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreference(preference: IngredientPreference)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreferences(preferences: List<IngredientPreference>)
    
    @Update
    suspend fun updatePreference(preference: IngredientPreference)
    
    @Delete
    suspend fun deletePreference(preference: IngredientPreference)
    
    @Query("DELETE FROM ingredient_preferences WHERE userId = :userId AND ingredientName = :ingredientName")
    suspend fun deletePreferenceForIngredient(userId: String, ingredientName: String)
    
    @Query("DELETE FROM ingredient_preferences WHERE userId = :userId")
    suspend fun deleteAllPreferencesForUser(userId: String)
    
    @Query("SELECT COUNT(*) FROM ingredient_preferences WHERE userId = :userId AND preferenceType = 'PREFERRED'")
    suspend fun getPreferredIngredientsCount(userId: String): Int
    
    @Query("SELECT DISTINCT ingredientName FROM ingredient_preferences WHERE userId = :userId AND ingredientName LIKE :query || '%' ORDER BY priority DESC, ingredientName ASC LIMIT :limit")
    suspend fun searchIngredients(userId: String, query: String, limit: Int = 10): List<String>
}