package com.beaconledger.welltrack.data.database.dao

import androidx.room.*
import com.beaconledger.welltrack.data.model.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface GoalDao {
    
    // Goal CRUD operations
    @Query("SELECT * FROM goals WHERE userId = :userId AND isActive = 1 ORDER BY priority DESC, createdAt DESC")
    fun getActiveGoalsForUser(userId: String): Flow<List<Goal>>
    
    @Query("SELECT * FROM goals WHERE userId = :userId ORDER BY createdAt DESC")
    fun getAllGoalsForUser(userId: String): Flow<List<Goal>>
    
    @Query("SELECT * FROM goals WHERE id = :goalId")
    suspend fun getGoalById(goalId: String): Goal?
    
    @Query("SELECT * FROM goals WHERE userId = :userId AND type = :type AND isActive = 1")
    fun getGoalsByType(userId: String, type: GoalType): Flow<List<Goal>>
    
    @Query("SELECT * FROM goals WHERE userId = :userId AND category = :category AND isActive = 1")
    fun getGoalsByCategory(userId: String, category: GoalCategory): Flow<List<Goal>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: Goal): Long
    
    @Update
    suspend fun updateGoal(goal: Goal)
    
    @Query("UPDATE goals SET isActive = 0 WHERE id = :goalId")
    suspend fun deactivateGoal(goalId: String)
    
    @Query("DELETE FROM goals WHERE id = :goalId")
    suspend fun deleteGoal(goalId: String)
    
    // Goal Progress operations
    @Query("SELECT * FROM goal_progress WHERE goalId = :goalId ORDER BY recordedAt DESC")
    fun getProgressForGoal(goalId: String): Flow<List<GoalProgress>>
    
    @Query("SELECT * FROM goal_progress WHERE goalId = :goalId ORDER BY recordedAt DESC LIMIT :limit")
    suspend fun getRecentProgressForGoal(goalId: String, limit: Int = 10): List<GoalProgress>
    
    @Query("SELECT * FROM goal_progress WHERE goalId = :goalId AND recordedAt >= :startDate ORDER BY recordedAt ASC")
    suspend fun getProgressInDateRange(goalId: String, startDate: LocalDate): List<GoalProgress>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgress(progress: GoalProgress): Long
    
    @Update
    suspend fun updateProgress(progress: GoalProgress)
    
    @Delete
    suspend fun deleteProgress(progress: GoalProgress)
    
    // Goal Milestones operations
    @Query("SELECT * FROM goal_milestones WHERE goalId = :goalId ORDER BY `order` ASC")
    fun getMilestonesForGoal(goalId: String): Flow<List<GoalMilestone>>
    
    @Query("SELECT * FROM goal_milestones WHERE goalId = :goalId AND isCompleted = 0 ORDER BY `order` ASC LIMIT 1")
    suspend fun getNextMilestone(goalId: String): GoalMilestone?
    
    @Query("SELECT COUNT(*) FROM goal_milestones WHERE goalId = :goalId AND isCompleted = 1")
    suspend fun getCompletedMilestonesCount(goalId: String): Int
    
    @Query("SELECT COUNT(*) FROM goal_milestones WHERE goalId = :goalId")
    suspend fun getTotalMilestonesCount(goalId: String): Int
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMilestone(milestone: GoalMilestone): Long
    
    @Update
    suspend fun updateMilestone(milestone: GoalMilestone)
    
    @Delete
    suspend fun deleteMilestone(milestone: GoalMilestone)
    
    // Goal Predictions operations
    @Query("SELECT * FROM goal_predictions WHERE goalId = :goalId ORDER BY calculatedAt DESC LIMIT 1")
    suspend fun getLatestPredictionForGoal(goalId: String): GoalPrediction?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrediction(prediction: GoalPrediction): Long
    
    @Update
    suspend fun updatePrediction(prediction: GoalPrediction)
    
    // Complex queries for goal with related data
    @Transaction
    @Query("SELECT * FROM goals WHERE id = :goalId")
    suspend fun getGoalWithProgress(goalId: String): GoalWithProgress?
    
    @Transaction
    @Query("SELECT * FROM goals WHERE userId = :userId AND isActive = 1")
    suspend fun getActiveGoalsWithProgress(userId: String): List<GoalWithProgress>
    
    // Statistics queries
    @Query("""
        SELECT AVG(value) as avgProgress 
        FROM goal_progress 
        WHERE goalId = :goalId 
        AND recordedAt >= :startDate
    """)
    suspend fun getAverageProgressInPeriod(goalId: String, startDate: LocalDate): Double?
    
    @Query("""
        SELECT COUNT(*) 
        FROM goals 
        WHERE userId = :userId 
        AND isActive = 1 
        AND targetDate < :currentDate
        AND currentValue < targetValue
    """)
    suspend fun getOverdueGoalsCount(userId: String, currentDate: LocalDate): Int
    
    @Query("""
        SELECT COUNT(*) 
        FROM goals 
        WHERE userId = :userId 
        AND isActive = 1 
        AND currentValue >= targetValue
    """)
    suspend fun getCompletedGoalsCount(userId: String): Int
    
    @Query("SELECT * FROM goals WHERE userId = :userId")
    suspend fun getAllGoalsForUser(userId: String): List<Goal>

    @Query("DELETE FROM goals WHERE userId = :userId")
    suspend fun deleteAllGoalsForUser(userId: String)

    // Additional methods for progress tracking by ID
    @Query("SELECT * FROM goal_progress WHERE id = :progressId")
    suspend fun getProgressById(progressId: String): GoalProgress?

    @Query("SELECT * FROM goal_milestones WHERE id = :milestoneId")
    suspend fun getMilestoneById(milestoneId: String): GoalMilestone?
}