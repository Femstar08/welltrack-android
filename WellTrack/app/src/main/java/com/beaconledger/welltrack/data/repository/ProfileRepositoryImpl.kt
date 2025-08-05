package com.beaconledger.welltrack.data.repository

import android.net.Uri
import com.beaconledger.welltrack.data.database.dao.ProfileDao
import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepositoryImpl @Inject constructor(
    private val profileDao: ProfileDao
) : ProfileRepository {

    override suspend fun createProfile(userId: String, request: ProfileCreationRequest): Result<UserProfile> {
        return try {
            val currentTime = LocalDateTime.now().toString()
            val profile = UserProfile(
                userId = userId,
                name = request.name,
                age = request.age,
                height = request.height,
                weight = request.weight,
                activityLevel = request.activityLevel,
                fitnessGoals = serializeFitnessGoals(request.fitnessGoals),
                dietaryRestrictions = serializeDietaryRestrictions(request.dietaryRestrictions),
                allergies = serializeStringList(request.allergies),
                createdAt = currentTime,
                updatedAt = currentTime
            )
            
            profileDao.insertProfile(profile)
            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getProfile(userId: String): Result<UserProfile?> {
        return try {
            val profile = profileDao.getProfileByUserId(userId)
            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getProfileFlow(userId: String): Flow<UserProfile?> {
        return profileDao.getProfileByUserIdFlow(userId)
    }

    override suspend fun updateProfile(userId: String, request: ProfileUpdateRequest): Result<UserProfile> {
        return try {
            val existingProfile = profileDao.getProfileByUserId(userId)
                ?: return Result.failure(Exception("Profile not found"))

            val updatedProfile = existingProfile.copy(
                name = request.name ?: existingProfile.name,
                age = request.age ?: existingProfile.age,
                height = request.height ?: existingProfile.height,
                weight = request.weight ?: existingProfile.weight,
                activityLevel = request.activityLevel ?: existingProfile.activityLevel,
                fitnessGoals = request.fitnessGoals?.let { serializeFitnessGoals(it) } ?: existingProfile.fitnessGoals,
                dietaryRestrictions = request.dietaryRestrictions?.let { serializeDietaryRestrictions(it) } ?: existingProfile.dietaryRestrictions,
                allergies = request.allergies?.let { serializeStringList(it) } ?: existingProfile.allergies,
                preferredIngredients = request.preferredIngredients?.let { serializeStringList(it) } ?: existingProfile.preferredIngredients,
                dislikedIngredients = request.dislikedIngredients?.let { serializeStringList(it) } ?: existingProfile.dislikedIngredients,
                cuisinePreferences = request.cuisinePreferences?.let { serializeStringList(it) } ?: existingProfile.cuisinePreferences,
                cookingMethods = request.cookingMethods?.let { serializeStringList(it) } ?: existingProfile.cookingMethods,
                updatedAt = LocalDateTime.now().toString()
            )

            profileDao.updateProfile(updatedProfile)
            Result.success(updatedProfile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteProfile(userId: String): Result<Unit> {
        return try {
            profileDao.deleteProfileByUserId(userId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun uploadProfilePhoto(userId: String, photoUri: Uri): Result<String> {
        return try {
            // TODO: Implement actual photo upload to Supabase Storage
            // For now, return a mock URL
            val mockPhotoUrl = "https://mock-storage.supabase.co/profile-photos/${userId}_${System.currentTimeMillis()}.jpg"
            Result.success(mockPhotoUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateProfilePhoto(userId: String, photoUrl: String): Result<Unit> {
        return try {
            val currentTime = LocalDateTime.now().toString()
            profileDao.updateProfilePhoto(userId, photoUrl, currentTime)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getAllProfiles(): Flow<List<UserProfile>> {
        return profileDao.getAllProfiles()
    }

    // Helper methods for serialization (simplified for now)
    private fun serializeFitnessGoals(goals: List<FitnessGoal>): String {
        return goals.joinToString(",") { it.name }
    }

    private fun serializeDietaryRestrictions(restrictions: List<DietaryRestriction>): String {
        return restrictions.joinToString(",") { it.name }
    }

    private fun serializeStringList(list: List<String>): String {
        return list.joinToString(",")
    }

    // Helper methods for deserialization
    fun deserializeFitnessGoals(serialized: String): List<FitnessGoal> {
        if (serialized.isEmpty()) return emptyList()
        return serialized.split(",").mapNotNull { goalName ->
            try {
                FitnessGoal.valueOf(goalName)
            } catch (e: IllegalArgumentException) {
                null
            }
        }
    }

    fun deserializeDietaryRestrictions(serialized: String): List<DietaryRestriction> {
        if (serialized.isEmpty()) return emptyList()
        return serialized.split(",").mapNotNull { restrictionName ->
            try {
                DietaryRestriction.valueOf(restrictionName)
            } catch (e: IllegalArgumentException) {
                null
            }
        }
    }

    fun deserializeStringList(serialized: String): List<String> {
        if (serialized.isEmpty()) return emptyList()
        return serialized.split(",")
    }
}