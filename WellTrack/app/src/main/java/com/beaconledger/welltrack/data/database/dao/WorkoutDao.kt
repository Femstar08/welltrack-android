package com.beaconledger.welltrack.data.database.dao

import androidx.room.*
import com.beaconledger.welltrack.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    
    // Workout operations
    @Query("SELECT * FROM workouts WHERE userId = :userId ORDER BY completedAt DESC")
    fun getWorkoutsByUser(userId: String): Flow<List<Workout>>
    
    @Query("SELECT * FROM workouts WHERE userId = :userId AND type = :type ORDER BY completedAt DESC")
    fun getWorkoutsByType(userId: String, type: WorkoutType): Flow<List<Workout>>
    
    @Query("SELECT * FROM workouts WHERE userId = :userId AND category = :category ORDER BY completedAt DESC")
    fun getWorkoutsByCategory(userId: String, category: WorkoutCategory): Flow<List<Workout>>
    
    @Query("SELECT * FROM workouts WHERE userId = :userId AND completedAt BETWEEN :startDate AND :endDate ORDER BY completedAt DESC")
    fun getWorkoutsInDateRange(userId: String, startDate: String, endDate: String): Flow<List<Workout>>
    
    @Query("SELECT * FROM workouts WHERE userId = :userId AND type = :type AND completedAt BETWEEN :startDate AND :endDate ORDER BY completedAt DESC")
    fun getNorwegian4x4WorkoutsInDateRange(userId: String, type: WorkoutType = WorkoutType.NORWEGIAN_4X4, startDate: String, endDate: String): Flow<List<Workout>>
    
    @Query("SELECT * FROM workouts WHERE id = :workoutId AND userId = :userId")
    suspend fun getWorkoutById(workoutId: String, userId: String): Workout?
    
    @Query("SELECT COUNT(*) FROM workouts WHERE userId = :userId")
    suspend fun getWorkoutCountByUser(userId: String): Int
    
    @Query("SELECT COUNT(*) FROM workouts WHERE userId = :userId AND type = :type")
    suspend fun getWorkoutCountByType(userId: String, type: WorkoutType): Int
    
    @Query("SELECT AVG(duration) FROM workouts WHERE userId = :userId AND type = :type")
    suspend fun getAverageWorkoutDuration(userId: String, type: WorkoutType): Double?
    
    @Query("SELECT AVG(caloriesBurned) FROM workouts WHERE userId = :userId AND caloriesBurned IS NOT NULL")
    suspend fun getAverageCaloriesBurned(userId: String): Double?
    
    @Query("DELETE FROM workouts WHERE userId = :userId")
    suspend fun deleteAllWorkoutsByUser(userId: String)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: Workout)
    
    @Update
    suspend fun updateWorkout(workout: Workout)
    
    @Delete
    suspend fun deleteWorkout(workout: Workout)
    
    // Exercise template operations
    @Query("SELECT * FROM exercise_templates ORDER BY name ASC")
    fun getAllExerciseTemplates(): Flow<List<ExerciseTemplate>>
    
    @Query("SELECT * FROM exercise_templates WHERE category = :category ORDER BY name ASC")
    fun getExerciseTemplatesByCategory(category: ExerciseCategory): Flow<List<ExerciseTemplate>>
    
    @Query("SELECT * FROM exercise_templates WHERE isCardio = 1 ORDER BY name ASC")
    fun getCardioExerciseTemplates(): Flow<List<ExerciseTemplate>>
    
    @Query("SELECT * FROM exercise_templates WHERE isStrength = 1 ORDER BY name ASC")
    fun getStrengthExerciseTemplates(): Flow<List<ExerciseTemplate>>
    
    @Query("SELECT * FROM exercise_templates WHERE name LIKE '%Norwegian 4x4%' ORDER BY name ASC")
    fun getNorwegian4x4Templates(): Flow<List<ExerciseTemplate>>
    
    @Query("SELECT * FROM exercise_templates WHERE targetHeartRateZone = :zone ORDER BY name ASC")
    fun getExerciseTemplatesByHeartRateZone(zone: HeartRateZone): Flow<List<ExerciseTemplate>>
    
    @Query("SELECT * FROM exercise_templates WHERE id = :templateId")
    suspend fun getExerciseTemplateById(templateId: String): ExerciseTemplate?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExerciseTemplate(template: ExerciseTemplate)
    
    @Update
    suspend fun updateExerciseTemplate(template: ExerciseTemplate)
    
    @Delete
    suspend fun deleteExerciseTemplate(template: ExerciseTemplate)
    
    // VO2 Max specific queries
    @Query("""
        SELECT * FROM workouts 
        WHERE userId = :userId 
        AND (category = :vo2Category OR type = :norwegian4x4Type)
        ORDER BY completedAt DESC
    """)
    fun getVO2MaxWorkouts(
        userId: String, 
        vo2Category: WorkoutCategory = WorkoutCategory.VO2_MAX_TRAINING,
        norwegian4x4Type: WorkoutType = WorkoutType.NORWEGIAN_4X4
    ): Flow<List<Workout>>
    
    @Query("""
        SELECT COUNT(*) FROM workouts 
        WHERE userId = :userId 
        AND (category = :vo2Category OR type = :norwegian4x4Type)
        AND completedAt BETWEEN :startDate AND :endDate
    """)
    suspend fun getVO2MaxWorkoutCount(
        userId: String,
        startDate: String,
        endDate: String,
        vo2Category: WorkoutCategory = WorkoutCategory.VO2_MAX_TRAINING,
        norwegian4x4Type: WorkoutType = WorkoutType.NORWEGIAN_4X4
    ): Int
}