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
    
    @Query("SELECT * FROM ingredient_usage_history WHERE userId = :userId AND usageType = :type ORDER BY usageDate DESC")
    fun getUsageHistoryByType(userId: String, type: UsageType): Flow<List<IngredientUsageHistory>>
    
    @Query("SELECT * FROM ingredient_usage_history WHERE userId = :userId AND date(usageDate) >= date('now', '-30 days') ORDER BY usageDate DESC")
    fun getRecentUsageHistory(userId: String): Flow<List<IngredientUsageHistory>>
    
    @Query("""
        SELECT ingredientName, COUNT(*) as usageCount, MAX(usageDate) as lastUsed
        FROM ingredient_usage_history 
        WHERE userId = :userId 
        GROUP BY ingredientName 
        ORDER BY usageCount DESC, lastUsed DESC
        LIMIT :limit
    """)
    suspend fun getMostUsedIngredients(userId: String, limit: Int = 20): List<IngredientUsageStats>
    
    @Query("""
        SELECT ingredientName, COUNT(*) as usageCount, MAX(usageDate) as lastUsed
        FROM ingredient_usage_history 
        WHERE userId = :userId AND date(usageDate) >= date('now', '-30 days')
        GROUP BY ingredientName 
        ORDER BY usageCount DESC, lastUsed DESC
        LIMIT :limit
    """)
    suspend fun getRecentlyUsedIngredients(userId: String, limit: Int = 20): List<IngredientUsageStats>
    
    @Query("""
        SELECT ingredientName, SUM(quantityUsed) as totalQuantity, COUNT(*) as usageCount
        FROM ingredient_usage_history 
        WHERE userId = :userId AND ingredientName = :ingredientName
        GROUP BY ingredientName
    """)
    suspend fun getIngredientUsageStats(userId: String, ingredientName: String): IngredientUsageStats?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsageHistory(usage: IngredientUsageHistory)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsageHistories(usages: List<IngredientUsageHistory>)
    
    @Delete
    suspend fun deleteUsageHistory(usage: IngredientUsageHistory)
    
    @Query("DELETE FROM ingredient_usage_history WHERE userId = :userId")
    suspend fun deleteAllUsageHistoryForUser(userId: String)
    
    @Query("DELETE FROM ingredient_usage_history WHERE userId = :userId AND date(usageDate) < date('now', '-365 days')")
    suspend fun deleteOldUsageHistory(userId: String)
    
    @Query("SELECT COUNT(*) FROM ingredient_usage_history WHERE userId = :userId")
    suspend fun getUsageHistoryCount(userId: String): Int
    
    @Query("SELECT DISTINCT ingredientName FROM ingredient_usage_history WHERE userId = :userId AND ingredientName LIKE :query || '%' ORDER BY ingredientName ASC LIMIT :limit")
    suspend fun searchUsedIngredients(userId: String, query: String, limit: Int = 10): List<String>
}

