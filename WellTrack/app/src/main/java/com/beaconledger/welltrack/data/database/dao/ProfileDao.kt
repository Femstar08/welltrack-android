package com.beaconledger.welltrack.data.database.dao

import androidx.room.*
import com.beaconledger.welltrack.data.model.UserProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    @Query("SELECT * FROM user_profiles WHERE userId = :userId")
    suspend fun getProfileByUserId(userId: String): UserProfile?

    @Query("SELECT * FROM user_profiles WHERE userId = :userId")
    fun getProfileByUserIdFlow(userId: String): Flow<UserProfile?>

    @Query("SELECT * FROM user_profiles")
    fun getAllProfiles(): Flow<List<UserProfile>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: UserProfile)

    @Update
    suspend fun updateProfile(profile: UserProfile)

    @Delete
    suspend fun deleteProfile(profile: UserProfile)

    @Query("DELETE FROM user_profiles WHERE userId = :userId")
    suspend fun deleteProfileByUserId(userId: String)

    @Query("UPDATE user_profiles SET profilePhotoUrl = :photoUrl, updatedAt = :updatedAt WHERE userId = :userId")
    suspend fun updateProfilePhoto(userId: String, photoUrl: String, updatedAt: String)

    @Query("UPDATE user_profiles SET name = :name, updatedAt = :updatedAt WHERE userId = :userId")
    suspend fun updateProfileName(userId: String, name: String, updatedAt: String)
}