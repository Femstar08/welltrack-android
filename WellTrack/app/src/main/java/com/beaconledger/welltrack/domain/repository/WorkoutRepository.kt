package com.beaconledger.welltrack.domain.repository

import com.beaconledger.welltrack.data.model.*
import kotlinx.coroutines.flow.Flow

interface WorkoutRepository {
    // Workout operations
    suspend fun createWorkout(workout: Workout): Result<Unit>
    suspend fun getWorkout(workoutId: String, userId: String): Result<Workout?>
    fun getWorkoutsByUser(userId: String): Flow<List<Workout>>
    fun getWorkoutsByType(userId: String, type: WorkoutType): Flow<List<Workout>>
    fun getWorkoutsByCategory(userId: String, category: WorkoutCategory): Flow<List<Workout>>
    fun getWorkoutsInDateRange(userId: String, startDate: String, endDate: String): Flow<List<Workout>>
    fun getNorwegian4x4Workouts(userId: String, startDate: String, endDate: String): Flow<List<Workout>>
    suspend fun updateWorkout(workout: Workout): Result<Unit>
    suspend fun deleteWorkout(workoutId: String, userId: String): Result<Unit>
    
    // Exercise template operations
    fun getAllExerciseTemplates(): Flow<List<ExerciseTemplate>>
    fun getExerciseTemplatesByCategory(category: ExerciseCategory): Flow<List<ExerciseTemplate>>
    fun getCardioExerciseTemplates(): Flow<List<ExerciseTemplate>>
    fun getStrengthExerciseTemplates(): Flow<List<ExerciseTemplate>>
    fun getNorwegian4x4Templates(): Flow<List<ExerciseTemplate>>
    suspend fun getExerciseTemplate(templateId: String): Result<ExerciseTemplate?>
    suspend fun createExerciseTemplate(template: ExerciseTemplate): Result<Unit>
    suspend fun updateExerciseTemplate(template: ExerciseTemplate): Result<Unit>
    suspend fun deleteExerciseTemplate(templateId: String): Result<Unit>
    
    // Statistics
    suspend fun getWorkoutStats(userId: String): Result<WorkoutStats>
    suspend fun getVO2MaxWorkoutStats(userId: String, startDate: String, endDate: String): Result<VO2MaxWorkoutStats>
}

data class WorkoutStats(
    val totalWorkouts: Int,
    val totalDuration: Int, // minutes
    val averageDuration: Double,
    val totalCaloriesBurned: Int,
    val averageCaloriesBurned: Double,
    val workoutsByType: Map<WorkoutType, Int>,
    val workoutsByCategory: Map<WorkoutCategory, Int>
)

data class VO2MaxWorkoutStats(
    val norwegian4x4Sessions: Int,
    val totalVO2MaxWorkouts: Int,
    val averageHeartRate: Double?,
    val averageIntensityAchieved: Double,
    val sessionsPerWeek: Double,
    val estimatedVO2MaxImprovement: Double
)