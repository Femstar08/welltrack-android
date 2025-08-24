package com.beaconledger.welltrack.data.database.dao

import androidx.room.*
import com.beaconledger.welltrack.data.model.SharedMealPlan
import kotlinx.coroutines.flow.Flow

@Dao
interface SharedMealPlanDao {
    @Query("SELECT * FROM shared_meal_plans WHERE familyGroupId = :groupId")
    fun getSharedMealPlans(groupId: String): Flow<List<SharedMealPlan>>

    @Query("SELECT * FROM shared_meal_plans WHERE id = :planId")
    suspend fun getSharedMealPlanById(planId: String): SharedMealPlan?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSharedMealPlan(sharedMealPlan: SharedMealPlan)

    @Update
    suspend fun updateSharedMealPlan(sharedMealPlan: SharedMealPlan)

    @Delete
    suspend fun deleteSharedMealPlan(sharedMealPlan: SharedMealPlan)
}