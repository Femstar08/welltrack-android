package com.beaconledger.welltrack.data.repository

import com.beaconledger.welltrack.data.database.dao.WorkoutDao
import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.domain.repository.WorkoutRepository
import com.beaconledger.welltrack.domain.repository.WorkoutStats
import com.beaconledger.welltrack.domain.repository.VO2MaxWorkoutStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkoutRepositoryImpl @Inject constructor(
    private val workoutDao: WorkoutDao
) : WorkoutRepository {

    override suspend fun createWorkout(workout: Workout): Result<Unit> {
        return try {
            workoutDao.insertWorkout(workout)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getWorkout(workoutId: String, userId: String): Result<Workout?> {
        return try {
            val workout = workoutDao.getWorkoutById(workoutId, userId)
            Result.success(workout)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getWorkoutsByUser(userId: String): Flow<List<Workout>> {
        return workoutDao.getWorkoutsByUser(userId)
    }

    override fun getWorkoutsByType(userId: String, type: WorkoutType): Flow<List<Workout>> {
        return workoutDao.getWorkoutsByType(userId, type)
    }

    override fun getWorkoutsByCategory(userId: String, category: WorkoutCategory): Flow<List<Workout>> {
        return workoutDao.getWorkoutsByCategory(userId, category)
    }

    override fun getWorkoutsInDateRange(userId: String, startDate: String, endDate: String): Flow<List<Workout>> {
        return workoutDao.getWorkoutsInDateRange(userId, startDate, endDate)
    }

    override fun getNorwegian4x4Workouts(userId: String, startDate: String, endDate: String): Flow<List<Workout>> {
        return workoutDao.getNorwegian4x4WorkoutsInDateRange(userId, WorkoutType.NORWEGIAN_4X4, startDate, endDate)
    }

    override suspend fun updateWorkout(workout: Workout): Result<Unit> {
        return try {
            workoutDao.updateWorkout(workout)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteWorkout(workoutId: String, userId: String): Result<Unit> {
        return try {
            val workout = workoutDao.getWorkoutById(workoutId, userId)
            if (workout != null) {
                workoutDao.deleteWorkout(workout)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Workout not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getAllExerciseTemplates(): Flow<List<ExerciseTemplate>> {
        return workoutDao.getAllExerciseTemplates()
    }

    override fun getExerciseTemplatesByCategory(category: ExerciseCategory): Flow<List<ExerciseTemplate>> {
        return workoutDao.getExerciseTemplatesByCategory(category)
    }

    override fun getCardioExerciseTemplates(): Flow<List<ExerciseTemplate>> {
        return workoutDao.getCardioExerciseTemplates()
    }

    override fun getStrengthExerciseTemplates(): Flow<List<ExerciseTemplate>> {
        return workoutDao.getStrengthExerciseTemplates()
    }

    override fun getNorwegian4x4Templates(): Flow<List<ExerciseTemplate>> {
        return workoutDao.getNorwegian4x4Templates()
    }

    override suspend fun getExerciseTemplate(templateId: String): Result<ExerciseTemplate?> {
        return try {
            val template = workoutDao.getExerciseTemplateById(templateId)
            Result.success(template)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createExerciseTemplate(template: ExerciseTemplate): Result<Unit> {
        return try {
            workoutDao.insertExerciseTemplate(template)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateExerciseTemplate(template: ExerciseTemplate): Result<Unit> {
        return try {
            workoutDao.updateExerciseTemplate(template)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteExerciseTemplate(templateId: String): Result<Unit> {
        return try {
            val template = workoutDao.getExerciseTemplateById(templateId)
            if (template != null) {
                workoutDao.deleteExerciseTemplate(template)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Exercise template not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getWorkoutStats(userId: String): Result<WorkoutStats> {
        return try {
            val workouts = workoutDao.getWorkoutsByUser(userId).first()
            val totalWorkouts = workouts.size
            val totalDuration = workouts.sumOf { it.duration }
            val averageDuration = if (totalWorkouts > 0) totalDuration.toDouble() / totalWorkouts else 0.0
            val totalCaloriesBurned = workouts.mapNotNull { it.caloriesBurned }.sum()
            val averageCaloriesBurned = if (totalWorkouts > 0) totalCaloriesBurned.toDouble() / totalWorkouts else 0.0
            
            val workoutsByType = workouts.groupBy { it.type }.mapValues { it.value.size }
            val workoutsByCategory = workouts.groupBy { it.category }.mapValues { it.value.size }
            
            val stats = WorkoutStats(
                totalWorkouts = totalWorkouts,
                totalDuration = totalDuration,
                averageDuration = averageDuration,
                totalCaloriesBurned = totalCaloriesBurned,
                averageCaloriesBurned = averageCaloriesBurned,
                workoutsByType = workoutsByType,
                workoutsByCategory = workoutsByCategory
            )
            
            Result.success(stats)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getVO2MaxWorkoutStats(userId: String, startDate: String, endDate: String): Result<VO2MaxWorkoutStats> {
        return try {
            val vo2MaxWorkouts = workoutDao.getVO2MaxWorkouts(userId).first()
            val norwegian4x4Workouts = vo2MaxWorkouts.filter { it.type == WorkoutType.NORWEGIAN_4X4 }
            
            val norwegian4x4Sessions = norwegian4x4Workouts.size
            val totalVO2MaxWorkouts = vo2MaxWorkouts.size
            
            val averageHeartRate = vo2MaxWorkouts.mapNotNull { it.averageHeartRate }.average().takeIf { !it.isNaN() }
            
            // Calculate average intensity achieved (simplified)
            val averageIntensityAchieved = norwegian4x4Workouts.mapNotNull { workout ->
                workout.averageHeartRate?.let { hr ->
                    // Assume target was 85% of max HR, calculate achievement percentage
                    (hr / (220 - 30)) * 100 // Simplified calculation assuming 30 years old
                }
            }.average().takeIf { !it.isNaN() } ?: 0.0
            
            // Calculate sessions per week
            val daysDifference = 30 // Simplified - should calculate from date range
            val sessionsPerWeek = (norwegian4x4Sessions.toDouble() / daysDifference) * 7
            
            // Estimate VO2 max improvement (simplified)
            val estimatedVO2MaxImprovement = when {
                sessionsPerWeek >= 3 -> 0.15
                sessionsPerWeek >= 2 -> 0.12
                else -> 0.08
            }
            
            val stats = VO2MaxWorkoutStats(
                norwegian4x4Sessions = norwegian4x4Sessions,
                totalVO2MaxWorkouts = totalVO2MaxWorkouts,
                averageHeartRate = averageHeartRate,
                averageIntensityAchieved = averageIntensityAchieved,
                sessionsPerWeek = sessionsPerWeek,
                estimatedVO2MaxImprovement = estimatedVO2MaxImprovement
            )
            
            Result.success(stats)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}