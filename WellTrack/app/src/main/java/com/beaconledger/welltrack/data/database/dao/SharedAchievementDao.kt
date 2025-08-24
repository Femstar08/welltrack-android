package com.beaconledger.welltrack.data.database.dao

import androidx.room.*
import com.beaconledger.welltrack.data.model.SharedAchievement
import kotlinx.coroutines.flow.Flow

@Dao
interface SharedAchievementDao {
    @Query("SELECT * FROM shared_achievements WHERE familyGroupId = :groupId")
    fun getSharedAchievements(groupId: String): Flow<List<SharedAchievement>>

    @Query("SELECT * FROM shared_achievements WHERE id = :achievementId")
    suspend fun getSharedAchievementById(achievementId: String): SharedAchievement?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSharedAchievement(sharedAchievement: SharedAchievement)

    @Update
    suspend fun updateSharedAchievement(sharedAchievement: SharedAchievement)

    @Delete
    suspend fun deleteSharedAchievement(sharedAchievement: SharedAchievement)
}