package com.beaconledger.welltrack.data.database.dao

import androidx.room.*
import com.beaconledger.welltrack.data.model.SocialAchievement
import kotlinx.coroutines.flow.Flow

@Dao
interface AchievementDao {
    @Query("SELECT * FROM achievements WHERE userId = :userId")
    fun getUserAchievements(userId: String): Flow<List<SocialAchievement>>

    @Query("SELECT * FROM achievements WHERE id = :achievementId")
    suspend fun getAchievementById(achievementId: String): SocialAchievement?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievement(achievement: SocialAchievement)

    @Update
    suspend fun updateAchievement(achievement: SocialAchievement)

    @Delete
    suspend fun deleteAchievement(achievement: SocialAchievement)

    @Query("UPDATE achievements SET isShared = :isShared WHERE id = :achievementId")
    suspend fun updateAchievementSharing(achievementId: String, isShared: Boolean)

    @Query("DELETE FROM achievements WHERE userId = :userId")
    suspend fun deleteAllAchievementsForUser(userId: String)
}