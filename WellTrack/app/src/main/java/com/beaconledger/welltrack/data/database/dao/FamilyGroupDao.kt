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
}