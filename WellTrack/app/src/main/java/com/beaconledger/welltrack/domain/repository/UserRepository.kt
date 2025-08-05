package com.beaconledger.welltrack.domain.repository

import com.beaconledger.welltrack.data.model.User
import com.beaconledger.welltrack.data.model.UserPreferences
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getAllUsers(): Flow<List<User>>
    suspend fun getUserById(userId: String): Result<User?>
    suspend fun getUserByEmail(email: String): Result<User?>
    suspend fun createUser(user: User): Result<String>
    suspend fun updateUser(user: User): Result<Unit>
    suspend fun deleteUser(userId: String): Result<Unit>
    suspend fun getUserPreferences(userId: String): Result<UserPreferences>
    suspend fun updateUserPreferences(userId: String, preferences: UserPreferences): Result<Unit>
}