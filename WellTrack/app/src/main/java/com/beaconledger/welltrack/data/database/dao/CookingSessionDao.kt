package com.beaconledger.welltrack.data.database.dao

import androidx.room.*
import com.beaconledger.welltrack.data.model.CookingSession
import kotlinx.coroutines.flow.Flow

@Dao
interface CookingSessionDao {
    
    @Query("SELECT * FROM cooking_sessions WHERE userId = :userId ORDER BY startedAt DESC")
    fun getCookingSessionsByUser(userId: String): Flow<List<CookingSession>>
    
    @Query("SELECT * FROM cooking_sessions WHERE id = :sessionId")
    suspend fun getCookingSessionById(sessionId: String): CookingSession?
    
    @Query("SELECT * FROM cooking_sessions WHERE recipeId = :recipeId AND userId = :userId ORDER BY startedAt DESC LIMIT 1")
    suspend fun getLatestCookingSessionForRecipe(recipeId: String, userId: String): CookingSession?
    
    @Query("SELECT * FROM cooking_sessions WHERE userId = :userId AND status = 'IN_PROGRESS' ORDER BY startedAt DESC")
    fun getActiveCookingSessions(userId: String): Flow<List<CookingSession>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCookingSession(session: CookingSession)
    
    @Update
    suspend fun updateCookingSession(session: CookingSession)
    
    @Query("DELETE FROM cooking_sessions WHERE id = :sessionId")
    suspend fun deleteCookingSession(sessionId: String)
    
    @Query("DELETE FROM cooking_sessions WHERE userId = :userId AND status = 'COMPLETED' AND startedAt < :cutoffDate")
    suspend fun deleteOldCompletedSessions(userId: String, cutoffDate: String)
}