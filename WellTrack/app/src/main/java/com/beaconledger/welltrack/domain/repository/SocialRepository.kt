package com.beaconledger.welltrack.domain.repository

import com.beaconledger.welltrack.data.model.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

interface SocialRepository {
    
    // Family Group Management
    suspend fun createFamilyGroup(name: String, description: String?, createdBy: String): Result<String>
    suspend fun getFamilyGroupsForUser(userId: String): Flow<List<FamilyGroupWithMembers>>
    suspend fun getFamilyGroupById(id: String): FamilyGroupWithMembers?
    suspend fun updateFamilyGroup(familyGroup: FamilyGroup): Result<Unit>
    suspend fun deleteFamilyGroup(id: String): Result<Unit>
    
    // Family Member Management
    suspend fun inviteFamilyMember(familyGroupId: String, userEmail: String, role: FamilyRole): Result<Unit>
    suspend fun addFamilyMember(familyGroupId: String, userId: String, role: FamilyRole): Result<Unit>
    suspend fun updateFamilyMemberRole(familyGroupId: String, userId: String, role: FamilyRole): Result<Unit>
    suspend fun removeFamilyMember(familyGroupId: String, userId: String): Result<Unit>
    suspend fun getFamilyMembers(familyGroupId: String): Flow<List<FamilyMemberInfo>>
    
    // Meal Plan Sharing
    suspend fun shareMealPlan(
        familyGroupId: String,
        mealPlanId: String,
        sharedBy: String,
        title: String,
        description: String?
    ): Result<String>
    suspend fun getSharedMealPlans(familyGroupId: String): Flow<List<SharedMealPlanWithDetails>>
    suspend fun getSharedMealPlanById(id: String): SharedMealPlanWithDetails?
    suspend fun unshareeMealPlan(id: String): Result<Unit>
    
    // Recipe Sharing
    suspend fun shareRecipe(
        familyGroupId: String,
        recipeId: String,
        sharedBy: String,
        message: String?
    ): Result<String>
    suspend fun getSharedRecipes(familyGroupId: String): Flow<List<SharedRecipeWithDetails>>
    suspend fun getSharedRecipeById(id: String): SharedRecipeWithDetails?
    suspend fun unshareRecipe(id: String): Result<Unit>
    
    // Collaborative Meal Prep
    suspend fun assignMealPrep(
        familyGroupId: String,
        recipeId: String,
        recipeName: String,
        assignedTo: String,
        assignedBy: String,
        scheduledDate: LocalDateTime,
        notes: String?
    ): Result<String>
    suspend fun getCollaborativeMealPrep(familyGroupId: String): Flow<List<CollaborativeMealPrepWithDetails>>
    suspend fun getMealPrepAssignedToUser(userId: String): Flow<List<CollaborativeMealPrepWithDetails>>
    suspend fun updateMealPrepStatus(id: String, status: MealPrepStatus): Result<Unit>
    suspend fun updateMealPrepNotes(id: String, notes: String): Result<Unit>
    
    // Achievement Sharing
    suspend fun createAchievement(
        userId: String,
        achievementType: AchievementType,
        title: String,
        description: String,
        iconName: String
    ): Result<String>
    suspend fun shareAchievement(achievementId: String, familyGroupId: String, shareMessage: String?): Result<String>
    suspend fun getSharedAchievements(familyGroupId: String): Flow<List<SharedAchievement>>
    suspend fun addAchievementReaction(sharedAchievementId: String, userId: String, reactionType: ReactionType): Result<Unit>
    suspend fun removeAchievementReaction(sharedAchievementId: String, userId: String): Result<Unit>
    
    // Shared Shopping Lists
    suspend fun createSharedShoppingList(familyGroupId: String, name: String, createdBy: String): Result<String>
    suspend fun getSharedShoppingLists(familyGroupId: String): Flow<List<SharedShoppingList>>
    suspend fun addItemToSharedShoppingList(
        shoppingListId: String,
        itemName: String,
        quantity: String,
        category: String?,
        addedBy: String
    ): Result<String>
    suspend fun updateShoppingListItemPurchaseStatus(
        itemId: String,
        isPurchased: Boolean,
        purchasedBy: String?
    ): Result<Unit>
    suspend fun getSharedShoppingListItems(shoppingListId: String): Flow<List<SharedShoppingListItem>>
    suspend fun deleteSharedShoppingListItem(itemId: String): Result<Unit>
    
    // Sync operations
    suspend fun syncFamilyData(userId: String): Result<Unit>
    suspend fun syncSharedContent(familyGroupId: String): Result<Unit>
}