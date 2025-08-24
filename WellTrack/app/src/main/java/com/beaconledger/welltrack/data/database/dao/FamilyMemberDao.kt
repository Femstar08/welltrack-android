package com.beaconledger.welltrack.data.database.dao

import androidx.room.*
import com.beaconledger.welltrack.data.model.FamilyMember
import kotlinx.coroutines.flow.Flow

@Dao
interface FamilyMemberDao {
    @Query("SELECT * FROM family_members WHERE familyGroupId = :groupId AND isActive = 1")
    fun getFamilyMembers(groupId: String): Flow<List<FamilyMember>>

    @Query("SELECT * FROM family_members WHERE userId = :userId AND isActive = 1")
    fun getUserFamilyMemberships(userId: String): Flow<List<FamilyMember>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFamilyMember(familyMember: FamilyMember)

    @Update
    suspend fun updateFamilyMember(familyMember: FamilyMember)

    @Delete
    suspend fun deleteFamilyMember(familyMember: FamilyMember)
}