package com.beaconledger.welltrack.data.database.dao

import androidx.room.*
import com.beaconledger.welltrack.data.model.IngredientUsageHistory
import com.beaconledger.welltrack.data.model.IngredientUsageStats
import com.beaconledger.welltrack.data.model.UsageType
import kotlinx.coroutines.flow.Flow

@Dao
interface IngredientUsageDao {
    
    @Query("SELECT * FROM ingredient_usage_history WHERE userId = :userId ORDER BY usageDate DESC")
    fun getUsageHistoryForUser(userId: String): Flow<List<IngredientUsageHistory>>
    
    @Query("SELECT * FROM ingredient_usage_history WHERE userId = :userId AND ingredientName = :ingredientName ORDER BY usageDate DESC")
    fun getUsageHistoryForIngredient(userId: String, ingredientName: String): Flow<List<IngredientUsageHistory>>
    
    @Query("SELECT * FROM ingredient_usage_history WHERE userId = :userId AND recipeId = :recipeId")
    suspend fun getUsageHistoryForRecipe(userId: String, recipeId: String): List<IngredientUsageHistory>
    
    @Query("SELECT * FROM ingredient_usage_history WHERE userId = :userId AND mealId = :mealId")
    suspend fun getUsageHistoryForMeal(userId: String, mealId: String): List<IngredientUsageHistory>
    
    @Query("SELECT * FROM ingredient_usage_history WHERE userId = :userId AND usageType = :usageType ORDER BY usageDate DESC")
    fun getUsageHistoryByType(userId: String, usageType: UsageType): Flow<List<IngredientUsageHistory>>
    
    @Query("""
        SELECT * FROM ingredient_usage_history 
        WHERE userId = :userId 
        AND date(usageDate) BETWEEN date(:startDate) AND date(:endDate)
        ORDER BY usageDate DESC
    """)
    suspend fun getUsageHistoryInDateRange(userId: String, startDate: String, endDate: String): List<IngredientUsageHistory>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsageHistory(usage: IngredientUsageHistory)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsageHistoryBatch(usages: List<IngredientUsageHistory>)
    
    @Update
    suspend fun updateUsageHistory(usage: IngredientUsageHistory)
    
    @Delete
    suspend fun deleteUsageHistory(usage: IngredientUsageHistory)
    
    @Query("DELETE FROM ingredient_usage_history WHERE userId = :userId AND ingredientName = :ingredientName")
    suspend fun deleteUsageHistoryForIngredient(userId: String, ingredientName: String)
    
    @Query("DELETE FROM ingredient_usage_history WHERE userId = :userId")
    suspend fun deleteAllUsageHistoryForUser(userId: String)
    
    // Analytics queries
    @Query("""
        SELECT 
            ingredientName,
            COUNT(*) as usageCount,
            MAX(usageDate) as lastUsed,
            MIN(usageDate) as firstUsed,
            SUM(quantityUsed) as totalQuantityUsed,
            AVG(quantityUsed) as averageQuantityPerUse,
            unit as mostCommonUnit
        FROM ingredient_usage_history 
        WHERE userId = :userId AND ingredientName = :ingredientName
        GROUP BY ingredientName, unit
        ORDER BY COUNT(*) DESC
        LIMIT 1
    """)
    suspend fun getIngredientUsageStats(userId: String, ingredientName: String): IngredientUsageStats?
    
    @Query("""
        SELECT ingredientName, COUNT(*) as usageCount
        FROM ingredient_usage_history 
        WHERE userId = :userId 
        AND date(usageDate) >= date('now', '-30 days')
        GROUP BY ingredientName
        ORDER BY COUNT(*) DESC
        LIMIT :limit
    """)
    suspend fun getMostUsedIngredients(userId: String, limit: Int = 10): List<IngredientUsageFrequency>
    
    @Query("""
        SELECT DISTINCT ingredientName
        FROM ingredient_usage_history 
        WHERE userId = :userId 
        AND date(usageDate) >= date('now', '-7 days')
        ORDER BY usageDate DESC
    """)
    suspend fun getRecentlyUsedIngredients(userId: String): List<String>
    
    @Query("""
        SELECT COUNT(DISTINCT ingredientName) 
        FROM ingredient_usage_history 
        WHERE userId = :userId
    """)
    suspend fun getUniqueIngredientsCount(userId: String): Int
    
    @Query("""
        SELECT SUM(quantityUsed) 
        FROM ingredient_usage_history 
        WHERE userId = :userId 
        AND ingredientName = :ingredientName
        AND date(usageDate) >= date('now', '-30 days')
    """)
    suspend fun getMonthlyUsageQuantity(userId: String, ingredientName: String): Double?
    
    @Query("""
        SELECT AVG(dailyUsage.totalQuantity) as averageDailyUsage
        FROM (
            SELECT date(usageDate) as usageDay, SUM(quantityUsed) as totalQuantity
            FROM ingredient_usage_history 
            WHERE userId = :userId 
            AND ingredientName = :ingredientName
            AND date(usageDate) >= date('now', '-30 days')
            GROUP BY date(usageDate)
        ) as dailyUsage
    """)
    suspend fun getAverageDailyUsage(userId: String, ingredientName: String): Double?
    
    @Query("""
        SELECT usageType, COUNT(*) as count
        FROM ingredient_usage_history 
        WHERE userId = :userId 
        AND ingredientName = :ingredientName
        GROUP BY usageType
        ORDER BY COUNT(*) DESC
    """)
    suspend fun getUsageTypeBreakdown(userId: String, ingredientName: String): List<UsageTypeCount>
    
    @Query("""
        SELECT * FROM ingredient_usage_history 
        WHERE userId = :userId 
        AND date(usageDate) >= date('now', '-30 days')
        ORDER BY usageDate DESC
    """)
    suspend fun getRecentUsageHistory(userId: String): List<IngredientUsageHistory>
    
    @Query("""
        SELECT DISTINCT ingredientName
        FROM ingredient_usage_history 
        WHERE userId = :userId 
        AND ingredientName LIKE '%' || :query || '%'
        ORDER BY usageDate DESC
        LIMIT :limit
    """)
    suspend fun searchUsedIngredients(userId: String, query: String, limit: Int = 10): List<String>
}

data class IngredientUsageFrequency(
    val ingredientName: String,
    val usageCount: Int
)

data class UsageTypeCount(
    val usageType: UsageType,
    val count: Int
)