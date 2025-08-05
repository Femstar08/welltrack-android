package com.beaconledger.welltrack.domain.repository

import android.net.Uri
import com.beaconledger.welltrack.data.model.UserProfile
import com.beaconledger.welltrack.data.model.ProfileCreationRequest
import com.beaconledger.welltrack.data.model.ProfileUpdateRequest
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    suspend fun createProfile(userId: String, request: ProfileCreationRequest): Result<UserProfile>
    suspend fun getProfile(userId: String): Result<UserProfile?>
    fun getProfileFlow(userId: String): Flow<UserProfile?>
    suspend fun updateProfile(userId: String, request: ProfileUpdateRequest): Result<UserProfile>
    suspend fun deleteProfile(userId: String): Result<Unit>
    suspend fun uploadProfilePhoto(userId: String, photoUri: Uri): Result<String>
    suspend fun updateProfilePhoto(userId: String, photoUrl: String): Result<Unit>
    fun getAllProfiles(): Flow<List<UserProfile>>
}