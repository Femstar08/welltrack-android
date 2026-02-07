package com.beaconledger.welltrack.data.database.dao

import androidx.room.*
import com.beaconledger.welltrack.data.model.UserPreferences
import kotlinx.coroutines.flow.Flow

@Dao
interface UserPreferencesDao {

    @Query("SELECT * FROM user_preferences WHERE userId = :userId")
    suspend fun getUserPreferences(userId: String): UserPreferences?

    @Query("SELECT * FROM user_preferences WHERE userId = :userId")
    fun getUserPreferencesFlow(userId: String): Flow<UserPreferences?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserPreferences(preferences: UserPreferences)

    @Update
    suspend fun updateUserPreferences(preferences: UserPreferences)

    @Delete
    suspend fun deleteUserPreferences(preferences: UserPreferences)

    @Query("DELETE FROM user_preferences WHERE userId = :userId")
    suspend fun deleteUserPreferencesByUserId(userId: String)
}