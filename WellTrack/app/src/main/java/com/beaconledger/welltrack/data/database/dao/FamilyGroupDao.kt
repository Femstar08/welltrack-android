package com.beaconledger.welltrack.data.database.dao

import androidx.room.*
import com.beaconledger.welltrack.data.model.FamilyGroup
import kotlinx.coroutines.flow.Flow

@Dao
interface FamilyGroupDao {
    @Query("SELECT * FROM family_groups WHERE isActive = 1")
    fun getAllActiveFamilyGroups(): Flow<List<FamilyGroup>>

    @Query("SELECT * FROM family_groups WHERE id = :groupId")
    suspend fun getFamilyGroupById(groupId: String): FamilyGroup?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFamilyGroup(familyGroup: FamilyGroup)

    @Update
    suspend fun updateFamilyGroup(familyGroup: FamilyGroup)

    @Delete
    suspend fun deleteFamilyGroup(familyGroup: FamilyGroup)

    @Query("""
        SELECT fg.* FROM family_groups fg
        INNER JOIN family_members fm ON fg.id = fm.familyGroupId
        WHERE fm.userId = :userId AND fg.isActive = 1
    """)
    fun getFamilyGroupsForUser(userId: String): Flow<List<FamilyGroup>>

    @Query("UPDATE family_groups SET isActive = 0 WHERE id = :groupId")
    suspend fun deactivateFamilyGroup(groupId: String)
}