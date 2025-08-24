package com.beaconledger.welltrack.data.database.dao

import androidx.room.*
import com.beaconledger.welltrack.data.model.CollaborativeMealPrep
import kotlinx.coroutines.flow.Flow

@Dao
interface CollaborativeMealPrepDao {
    @Query("SELECT * FROM collaborative_meal_prep WHERE familyGroupId = :groupId")
    fun getCollaborativeMealPreps(groupId: String): Flow<List<CollaborativeMealPrep>>

    @Query("SELECT * FROM collaborative_meal_prep WHERE id = :prepId")
    suspend fun getCollaborativeMealPrepById(prepId: String): CollaborativeMealPrep?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCollaborativeMealPrep(collaborativeMealPrep: CollaborativeMealPrep)

    @Update
    suspend fun updateCollaborativeMealPrep(collaborativeMealPrep: CollaborativeMealPrep)

    @Delete
    suspend fun deleteCollaborativeMealPrep(collaborativeMealPrep: CollaborativeMealPrep)
}