package com.beaconledger.welltrack.domain.usecase

import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.domain.repository.SocialRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SocialUseCase @Inject constructor(
    private val socialRepository: SocialRepository
) {
    
    // Family Group Operations
    suspend fun createFamilyGroup(name: String, description: String?, createdBy: String): Result<String> {
        return if (name.isBlank()) {
            Result.failure(IllegalArgumentException("Family group name cannot be empty"))
        } else {
            socialRepository.createFamilyGroup(name.trim(), description?.trim(), createdBy)
        }
    }
    
    suspend fun getFamilyGroupsForUser(userId: String): Flow<List<FamilyGroupWithMembers>> {
        return socialRepository.getFamilyGroupsForUser(userId)
    }
    
    suspend fun updateFamilyGroup(familyGroup: FamilyGroup): Result<Unit> {
        return socialRepository.updateFamilyGroup(familyGroup)
    }
    
    suspend fun deleteFamilyGroup(id: String): Result<Unit> {
        return socialRepository.deleteFamilyGroup(id)
    }
    
    // Family Member Operations
    suspend fun inviteFamilyMember(familyGroupId: String, userEmail: String, role: FamilyRole): Result<Unit> {
        return if (userEmail.isBlank() || !isValidEmail(userEmail)) {
            Result.failure(IllegalArgumentException("Invalid email address"))
        } else {
            socialRepository.inviteFamilyMember(familyGroupId, userEmail.trim(), role)
        }
    }
    
    suspend fun addFamilyMember(familyGroupId: String, userId: String, role: FamilyRole): Result<Unit> {
        return socialRepository.addFamilyMember(familyGroupId, userId, role)
    }
    
    suspend fun updateFamilyMemberRole(familyGroupId: String, userId: String, role: FamilyRole): Result<Unit> {
        return socialRepository.updateFamilyMemberRole(familyGroupId, userId, role)
    }
    
    suspend fun removeFamilyMember(familyGroupId: String, userId: String): Result<Unit> {
        return socialRepository.removeFamilyMember(familyGroupId, userId)
    }
    
    suspend fun getFamilyMembers(familyGroupId: String): Flow<List<FamilyMemberInfo>> {
        return socialRepository.getFamilyMembers(familyGroupId)
    }
    
    // Meal Plan Sharing Operations
    suspend fun shareMealPlan(
        familyGroupId: String,
        mealPlanId: String,
        sharedBy: String,
        title: String,
        description: String?
    ): Result<String> {
        return if (title.isBlank()) {
            Result.failure(IllegalArgumentException("Meal plan title cannot be empty"))
        } else {
            socialRepository.shareMealPlan(
                familyGroupId,
                mealPlanId,
                sharedBy,
                title.trim(),
                description?.trim()
            )
        }
    }
    
    suspend fun getSharedMealPlans(familyGroupId: String): Flow<List<SharedMealPlanWithDetails>> {
        return socialRepository.getSharedMealPlans(familyGroupId)
    }
    
    suspend fun getSharedMealPlanById(id: String): SharedMealPlanWithDetails? {
        return socialRepository.getSharedMealPlanById(id)
    }
    
    suspend fun unshareeMealPlan(id: String): Result<Unit> {
        return socialRepository.unshareeMealPlan(id)
    }
    
    // Recipe Sharing Operations
    suspend fun shareRecipe(
        familyGroupId: String,
        recipeId: String,
        sharedBy: String,
        message: String?
    ): Result<String> {
        return socialRepository.shareRecipe(familyGroupId, recipeId, sharedBy, message?.trim())
    }
    
    suspend fun getSharedRecipes(familyGroupId: String): Flow<List<SharedRecipeWithDetails>> {
        return socialRepository.getSharedRecipes(familyGroupId)
    }
    
    suspend fun getSharedRecipeById(id: String): SharedRecipeWithDetails? {
        return socialRepository.getSharedRecipeById(id)
    }
    
    suspend fun unshareRecipe(id: String): Result<Unit> {
        return socialRepository.unshareRecipe(id)
    }
    
    // Collaborative Meal Prep Operations
    suspend fun assignMealPrep(
        familyGroupId: String,
        recipeId: String,
        recipeName: String,
        assignedTo: String,
        assignedBy: String,
        scheduledDate: LocalDateTime,
        notes: String?
    ): Result<String> {
        return if (recipeName.isBlank()) {
            Result.failure(IllegalArgumentException("Recipe name cannot be empty"))
        } else if (scheduledDate.isBefore(LocalDateTime.now().minusDays(1))) {
            Result.failure(IllegalArgumentException("Scheduled date cannot be in the past"))
        } else {
            socialRepository.assignMealPrep(
                familyGroupId,
                recipeId,
                recipeName.trim(),
                assignedTo,
                assignedBy,
                scheduledDate,
                notes?.trim()
            )
        }
    }
    
    suspend fun getCollaborativeMealPrep(familyGroupId: String): Flow<List<CollaborativeMealPrepWithDetails>> {
        return socialRepository.getCollaborativeMealPrep(familyGroupId)
    }
    
    suspend fun getMealPrepAssignedToUser(userId: String): Flow<List<CollaborativeMealPrepWithDetails>> {
        return socialRepository.getMealPrepAssignedToUser(userId)
    }
    
    suspend fun updateMealPrepStatus(id: String, status: MealPrepStatus): Result<Unit> {
        return socialRepository.updateMealPrepStatus(id, status)
    }
    
    suspend fun updateMealPrepNotes(id: String, notes: String): Result<Unit> {
        return socialRepository.updateMealPrepNotes(id, notes.trim())
    }
    
    // Achievement Operations
    suspend fun createAchievement(
        userId: String,
        achievementType: AchievementType,
        title: String,
        description: String,
        iconName: String
    ): Result<String> {
        return if (title.isBlank()) {
            Result.failure(IllegalArgumentException("Achievement title cannot be empty"))
        } else if (description.isBlank()) {
            Result.failure(IllegalArgumentException("Achievement description cannot be empty"))
        } else {
            socialRepository.createAchievement(
                userId,
                achievementType,
                title.trim(),
                description.trim(),
                iconName
            )
        }
    }
    
    suspend fun shareAchievement(achievementId: String, familyGroupId: String, shareMessage: String?): Result<String> {
        return socialRepository.shareAchievement(achievementId, familyGroupId, shareMessage?.trim())
    }
    
    suspend fun getSharedAchievements(familyGroupId: String): Flow<List<SharedAchievement>> {
        return socialRepository.getSharedAchievements(familyGroupId)
    }
    
    suspend fun addAchievementReaction(sharedAchievementId: String, userId: String, reactionType: ReactionType): Result<Unit> {
        return socialRepository.addAchievementReaction(sharedAchievementId, userId, reactionType)
    }
    
    suspend fun removeAchievementReaction(sharedAchievementId: String, userId: String): Result<Unit> {
        return socialRepository.removeAchievementReaction(sharedAchievementId, userId)
    }
    
    // Shared Shopping List Operations
    suspend fun createSharedShoppingList(familyGroupId: String, name: String, createdBy: String): Result<String> {
        return if (name.isBlank()) {
            Result.failure(IllegalArgumentException("Shopping list name cannot be empty"))
        } else {
            socialRepository.createSharedShoppingList(familyGroupId, name.trim(), createdBy)
        }
    }
    
    suspend fun getSharedShoppingLists(familyGroupId: String): Flow<List<SharedShoppingList>> {
        return socialRepository.getSharedShoppingLists(familyGroupId)
    }
    
    suspend fun addItemToSharedShoppingList(
        shoppingListId: String,
        itemName: String,
        quantity: String,
        category: String?,
        addedBy: String
    ): Result<String> {
        return if (itemName.isBlank()) {
            Result.failure(IllegalArgumentException("Item name cannot be empty"))
        } else if (quantity.isBlank()) {
            Result.failure(IllegalArgumentException("Quantity cannot be empty"))
        } else {
            socialRepository.addItemToSharedShoppingList(
                shoppingListId,
                itemName.trim(),
                quantity.trim(),
                category?.trim(),
                addedBy
            )
        }
    }
    
    suspend fun updateShoppingListItemPurchaseStatus(
        itemId: String,
        isPurchased: Boolean,
        purchasedBy: String?
    ): Result<Unit> {
        return socialRepository.updateShoppingListItemPurchaseStatus(itemId, isPurchased, purchasedBy)
    }
    
    suspend fun getSharedShoppingListItems(shoppingListId: String): Flow<List<SharedShoppingListItem>> {
        return socialRepository.getSharedShoppingListItems(shoppingListId)
    }
    
    suspend fun deleteSharedShoppingListItem(itemId: String): Result<Unit> {
        return socialRepository.deleteSharedShoppingListItem(itemId)
    }
    
    // Sync Operations
    suspend fun syncFamilyData(userId: String): Result<Unit> {
        return socialRepository.syncFamilyData(userId)
    }
    
    suspend fun syncSharedContent(familyGroupId: String): Result<Unit> {
        return socialRepository.syncSharedContent(familyGroupId)
    }
    
    // Helper Functions
    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    
    // Achievement Generation Helpers
    suspend fun checkAndCreateMealStreakAchievement(userId: String, streakDays: Int): Result<String>? {
        return when (streakDays) {
            7 -> createAchievement(
                userId,
                AchievementType.MEAL_STREAK,
                "Week Warrior",
                "Logged meals for 7 consecutive days!",
                "ic_streak_week"
            )
            30 -> createAchievement(
                userId,
                AchievementType.MEAL_STREAK,
                "Monthly Master",
                "Logged meals for 30 consecutive days!",
                "ic_streak_month"
            )
            100 -> createAchievement(
                userId,
                AchievementType.MEAL_STREAK,
                "Century Champion",
                "Logged meals for 100 consecutive days!",
                "ic_streak_century"
            )
            else -> null
        }
    }
    
    suspend fun checkAndCreateNutritionGoalAchievement(userId: String, goalType: String): Result<String>? {
        return when (goalType) {
            "protein_target" -> createAchievement(
                userId,
                AchievementType.NUTRITION_GOAL,
                "Protein Pro",
                "Hit your protein target for 7 days straight!",
                "ic_protein_goal"
            )
            "fiber_target" -> createAchievement(
                userId,
                AchievementType.NUTRITION_GOAL,
                "Fiber Fighter",
                "Reached your fiber goal consistently!",
                "ic_fiber_goal"
            )
            "water_target" -> createAchievement(
                userId,
                AchievementType.NUTRITION_GOAL,
                "Hydration Hero",
                "Stayed hydrated for a full week!",
                "ic_water_goal"
            )
            else -> null
        }
    }
    
    suspend fun checkAndCreateRecipeMasteryAchievement(userId: String, recipeCount: Int): Result<String>? {
        return when (recipeCount) {
            10 -> createAchievement(
                userId,
                AchievementType.RECIPE_MASTERY,
                "Recipe Explorer",
                "Tried 10 different recipes!",
                "ic_recipe_explorer"
            )
            50 -> createAchievement(
                userId,
                AchievementType.RECIPE_MASTERY,
                "Culinary Adventurer",
                "Mastered 50 unique recipes!",
                "ic_recipe_master"
            )
            100 -> createAchievement(
                userId,
                AchievementType.RECIPE_MASTERY,
                "Kitchen Legend",
                "Conquered 100 recipes like a true chef!",
                "ic_recipe_legend"
            )
            else -> null
        }
    }
}