package com.beaconledger.welltrack.data.repository

import com.beaconledger.welltrack.data.database.WellTrackDatabase
import com.beaconledger.welltrack.data.model.User
import com.beaconledger.welltrack.data.model.UserPreferences
import com.beaconledger.welltrack.data.model.AccessibilitySettings
import com.beaconledger.welltrack.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val database: WellTrackDatabase
) : UserRepository {

    override fun getAllUsers(): Flow<List<User>> {
        return database.userDao().getAllUsers()
    }

    override suspend fun getUserById(userId: String): Result<User?> {
        return try {
            val user = database.userDao().getUserById(userId)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserByEmail(email: String): Result<User?> {
        return try {
            val user = database.userDao().getUserByEmail(email)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createUser(user: User): Result<String> {
        return try {
            database.userDao().insertUser(user)
            Result.success(user.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateUser(user: User): Result<Unit> {
        return try {
            database.userDao().updateUser(user)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteUser(userId: String): Result<Unit> {
        return try {
            database.userDao().deleteUser(userId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserPreferences(userId: String): Result<UserPreferences> {
        return try {
            val preferences = database.userPreferencesDao().getUserPreferences(userId)
            if (preferences != null) {
                Result.success(preferences)
            } else {
                // Return default preferences if none exist
                val defaultPreferences = UserPreferences(
                    userId = userId,
                    notificationsEnabled = true,
                    darkModeEnabled = false,
                    language = "en",
                    accessibilitySettings = AccessibilitySettings(
                        highContrastEnabled = false,
                        reduceAnimationsEnabled = false,
                        largeTextEnabled = false,
                        screenReaderOptimizationEnabled = true,
                        audioDescriptionsEnabled = false,
                        largeTouchTargetsEnabled = false,
                        reduceMotionEnabled = false,
                        simplifiedUIEnabled = false,
                        extendedTimeoutsEnabled = false
                    )
                )
                // Insert default preferences
                database.userPreferencesDao().insertUserPreferences(defaultPreferences)
                Result.success(defaultPreferences)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateUserPreferences(userId: String, preferences: UserPreferences): Result<Unit> {
        return try {
            database.userPreferencesDao().updateUserPreferences(preferences)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}