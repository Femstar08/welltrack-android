package com.beaconledger.welltrack.data.database.dao

import androidx.room.*
import com.beaconledger.welltrack.data.model.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface FamilyGroupDao {
    @Query("SELECT * FROM family_groups WHERE isActive = 1 ORDER BY createdAt DESC")
    fun getAllActiveFamilyGroups(): Flow<List<FamilyGroup>>

    @Query("SELECT * FROM family_groups WHERE id = :id")
    suspend fun getFamilyGroupById(id: String): FamilyGroup?

    @Query("""
        SELECT fg.* FROM family_groups fg
        INNER JOIN family_members fm ON fg.id = fm.familyGroupId
        WHERE fm.userId = :userId AND fm.isActive = 1 AND fg.isActive = 1
        ORDER BY fg.createdAt DESC
    """)
    fun getFamilyGroupsForUser(userId: String): Flow<List<FamilyGroup>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFamilyGroup(familyGroup: FamilyGroup): Long

    @Update
    suspend fun updateFamilyGroup(familyGroup: FamilyGroup)

    @Query("UPDATE family_groups SET isActive = 0 WHERE id = :id")
    suspend fun deactivateFamilyGroup(id: String)
}

@Dao
interface FamilyMemberDao {
    @Query("SELECT * FROM family_members WHERE familyGroupId = :familyGroupId AND isActive = 1")
    fun getFamilyMembers(familyGroupId: String): Flow<List<FamilyMember>>

    @Query("SELECT * FROM family_members WHERE userId = :userId AND isActive = 1")
    fun getUserFamilyMemberships(userId: String): Flow<List<FamilyMember>>

    @Query("SELECT * FROM family_members WHERE familyGroupId = :familyGroupId AND userId = :userId")
    suspend fun getFamilyMember(familyGroupId: String, userId: String): FamilyMember?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFamilyMember(familyMember: FamilyMember): Long

    @Update
    suspend fun updateFamilyMember(familyMember: FamilyMember)

    @Query("UPDATE family_members SET isActive = 0 WHERE familyGroupId = :familyGroupId AND userId = :userId")
    suspend fun removeFamilyMember(familyGroupId: String, userId: String)

    @Query("DELETE FROM family_members WHERE familyGroupId = :familyGroupId AND userId = :userId")
    suspend fun deleteFamilyMember(familyGroupId: String, userId: String)
}

@Dao
interface SharedMealPlanDao {
    @Query("SELECT * FROM shared_meal_plans WHERE familyGroupId = :familyGroupId AND isActive = 1 ORDER BY sharedAt DESC")
    fun getSharedMealPlans(familyGroupId: String): Flow<List<SharedMealPlan>>

    @Query("SELECT * FROM shared_meal_plans WHERE id = :id")
    suspend fun getSharedMealPlanById(id: String): SharedMealPlan?

    @Query("SELECT * FROM shared_meal_plans WHERE sharedBy = :userId AND isActive = 1 ORDER BY sharedAt DESC")
    fun getSharedMealPlansByUser(userId: String): Flow<List<SharedMealPlan>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSharedMealPlan(sharedMealPlan: SharedMealPlan): Long

    @Update
    suspend fun updateSharedMealPlan(sharedMealPlan: SharedMealPlan)

    @Query("UPDATE shared_meal_plans SET isActive = 0 WHERE id = :id")
    suspend fun deactivateSharedMealPlan(id: String)
}

@Dao
interface SharedRecipeDao {
    @Query("SELECT * FROM shared_recipes WHERE familyGroupId = :familyGroupId AND isActive = 1 ORDER BY sharedAt DESC")
    fun getSharedRecipes(familyGroupId: String): Flow<List<SharedRecipe>>

    @Query("SELECT * FROM shared_recipes WHERE id = :id")
    suspend fun getSharedRecipeById(id: String): SharedRecipe?

    @Query("SELECT * FROM shared_recipes WHERE sharedBy = :userId AND isActive = 1 ORDER BY sharedAt DESC")
    fun getSharedRecipesByUser(userId: String): Flow<List<SharedRecipe>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSharedRecipe(sharedRecipe: SharedRecipe): Long

    @Update
    suspend fun updateSharedRecipe(sharedRecipe: SharedRecipe)

    @Query("UPDATE shared_recipes SET isActive = 0 WHERE id = :id")
    suspend fun deactivateSharedRecipe(id: String)
}

@Dao
interface CollaborativeMealPrepDao {
    @Query("SELECT * FROM collaborative_meal_prep WHERE familyGroupId = :familyGroupId ORDER BY scheduledDate ASC")
    fun getCollaborativeMealPrep(familyGroupId: String): Flow<List<CollaborativeMealPrep>>

    @Query("SELECT * FROM collaborative_meal_prep WHERE assignedTo = :userId ORDER BY scheduledDate ASC")
    fun getMealPrepAssignedToUser(userId: String): Flow<List<CollaborativeMealPrep>>

    @Query("SELECT * FROM collaborative_meal_prep WHERE assignedBy = :userId ORDER BY scheduledDate ASC")
    fun getMealPrepAssignedByUser(userId: String): Flow<List<CollaborativeMealPrep>>

    @Query("SELECT * FROM collaborative_meal_prep WHERE id = :id")
    suspend fun getCollaborativeMealPrepById(id: String): CollaborativeMealPrep?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCollaborativeMealPrep(mealPrep: CollaborativeMealPrep): Long

    @Update
    suspend fun updateCollaborativeMealPrep(mealPrep: CollaborativeMealPrep)

    @Query("UPDATE collaborative_meal_prep SET status = :status, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateMealPrepStatus(id: String, status: MealPrepStatus, updatedAt: LocalDateTime)
}

@Dao
interface AchievementDao {
    @Query("SELECT * FROM achievements WHERE userId = :userId ORDER BY earnedAt DESC")
    fun getUserAchievements(userId: String): Flow<List<Achievement>>

    @Query("SELECT * FROM achievements WHERE userId = :userId AND isShared = 1 ORDER BY earnedAt DESC")
    fun getSharedAchievements(userId: String): Flow<List<Achievement>>

    @Query("SELECT * FROM achievements WHERE id = :id")
    suspend fun getAchievementById(id: String): Achievement?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievement(achievement: Achievement): Long

    @Update
    suspend fun updateAchievement(achievement: Achievement)

    @Query("UPDATE achievements SET isShared = :isShared, shareMessage = :shareMessage WHERE id = :id")
    suspend fun updateAchievementSharing(id: String, isShared: Boolean, shareMessage: String?)
}

@Dao
interface SharedAchievementDao {
    @Query("SELECT * FROM shared_achievements WHERE familyGroupId = :familyGroupId ORDER BY sharedAt DESC")
    fun getSharedAchievements(familyGroupId: String): Flow<List<SharedAchievement>>

    @Query("SELECT * FROM shared_achievements WHERE id = :id")
    suspend fun getSharedAchievementById(id: String): SharedAchievement?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSharedAchievement(sharedAchievement: SharedAchievement): Long

    @Update
    suspend fun updateSharedAchievement(sharedAchievement: SharedAchievement)
}

@Dao
interface SharedShoppingListDao {
    @Query("SELECT * FROM shared_shopping_lists WHERE familyGroupId = :familyGroupId AND isActive = 1 ORDER BY updatedAt DESC")
    fun getSharedShoppingLists(familyGroupId: String): Flow<List<SharedShoppingList>>

    @Query("SELECT * FROM shared_shopping_lists WHERE id = :id")
    suspend fun getSharedShoppingListById(id: String): SharedShoppingList?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSharedShoppingList(sharedShoppingList: SharedShoppingList): Long

    @Update
    suspend fun updateSharedShoppingList(sharedShoppingList: SharedShoppingList)

    @Query("UPDATE shared_shopping_lists SET isActive = 0 WHERE id = :id")
    suspend fun deactivateSharedShoppingList(id: String)
}

@Dao
interface SharedShoppingListItemDao {
    @Query("SELECT * FROM shared_shopping_list_items WHERE shoppingListId = :shoppingListId ORDER BY addedAt ASC")
    fun getSharedShoppingListItems(shoppingListId: String): Flow<List<SharedShoppingListItem>>

    @Query("SELECT * FROM shared_shopping_list_items WHERE id = :id")
    suspend fun getSharedShoppingListItemById(id: String): SharedShoppingListItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSharedShoppingListItem(item: SharedShoppingListItem): Long

    @Update
    suspend fun updateSharedShoppingListItem(item: SharedShoppingListItem)

    @Query("UPDATE shared_shopping_list_items SET isPurchased = :isPurchased, purchasedBy = :purchasedBy, purchasedAt = :purchasedAt WHERE id = :id")
    suspend fun updateItemPurchaseStatus(id: String, isPurchased: Boolean, purchasedBy: String?, purchasedAt: LocalDateTime?)

    @Query("DELETE FROM shared_shopping_list_items WHERE id = :id")
    suspend fun deleteSharedShoppingListItem(id: String)
}