package com.beaconledger.welltrack.data.repository

import com.beaconledger.welltrack.data.database.dao.*
import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.data.remote.SupabaseClient
import com.beaconledger.welltrack.domain.repository.SocialRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SocialRepositoryImpl @Inject constructor(
    private val familyGroupDao: FamilyGroupDao,
    private val familyMemberDao: FamilyMemberDao,
    private val sharedMealPlanDao: SharedMealPlanDao,
    private val sharedRecipeDao: SharedRecipeDao,
    private val collaborativeMealPrepDao: CollaborativeMealPrepDao,
    private val achievementDao: AchievementDao,
    private val sharedAchievementDao: SharedAchievementDao,
    private val sharedShoppingListDao: SharedShoppingListDao,
    private val sharedShoppingListItemDao: SharedShoppingListItemDao,
    private val supabaseClient: SupabaseClient
) : SocialRepository {

    override suspend fun createFamilyGroup(name: String, description: String?, createdBy: String): Result<String> {
        return try {
            val id = UUID.randomUUID().toString()
            val now = LocalDateTime.now()
            
            val familyGroup = FamilyGroup(
                id = id,
                name = name,
                description = description,
                createdBy = createdBy,
                createdAt = now,
                updatedAt = now
            )
            
            // Insert family group
            familyGroupDao.insertFamilyGroup(familyGroup)
            
            // Add creator as admin
            val familyMember = FamilyMember(
                familyGroupId = id,
                userId = createdBy,
                role = FamilyRole.ADMIN,
                joinedAt = now
            )
            familyMemberDao.insertFamilyMember(familyMember)
            
            // Sync to cloud
            supabaseClient.insertFamilyGroup(familyGroup)
            supabaseClient.insertFamilyMember(familyMember)
            
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getFamilyGroupsForUser(userId: String): Flow<List<FamilyGroupWithMembers>> {
        return familyGroupDao.getFamilyGroupsForUser(userId).map { groups ->
            groups.map { group ->
                val members = getMemberInfoForGroup(group.id)
                FamilyGroupWithMembers(group, members)
            }
        }
    }

    override suspend fun getFamilyGroupById(id: String): FamilyGroupWithMembers? {
        return try {
            val group = familyGroupDao.getFamilyGroupById(id)
            if (group != null) {
                val members = getMemberInfoForGroup(id)
                FamilyGroupWithMembers(group, members)
            } else null
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun getMemberInfoForGroup(familyGroupId: String): List<FamilyMemberInfo> {
        // This would typically fetch user info from user repository
        // For now, returning basic structure
        return emptyList()
    }

    override suspend fun updateFamilyGroup(familyGroup: FamilyGroup): Result<Unit> {
        return try {
            val updatedGroup = familyGroup.copy(updatedAt = LocalDateTime.now())
            familyGroupDao.updateFamilyGroup(updatedGroup)
            supabaseClient.updateFamilyGroup(updatedGroup)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteFamilyGroup(id: String): Result<Unit> {
        return try {
            familyGroupDao.deactivateFamilyGroup(id)
            supabaseClient.deactivateFamilyGroup(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun inviteFamilyMember(familyGroupId: String, userEmail: String, role: FamilyRole): Result<Unit> {
        return try {
            // This would typically send an invitation email
            // For now, just return success
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addFamilyMember(familyGroupId: String, userId: String, role: FamilyRole): Result<Unit> {
        return try {
            val familyMember = FamilyMember(
                familyGroupId = familyGroupId,
                userId = userId,
                role = role,
                joinedAt = LocalDateTime.now()
            )
            
            familyMemberDao.insertFamilyMember(familyMember)
            supabaseClient.insertFamilyMember(familyMember)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateFamilyMemberRole(familyGroupId: String, userId: String, role: FamilyRole): Result<Unit> {
        return try {
            val existingMember = familyMemberDao.getFamilyMember(familyGroupId, userId)
            if (existingMember != null) {
                val updatedMember = existingMember.copy(role = role)
                familyMemberDao.updateFamilyMember(updatedMember)
                supabaseClient.updateFamilyMember(updatedMember)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun removeFamilyMember(familyGroupId: String, userId: String): Result<Unit> {
        return try {
            familyMemberDao.removeFamilyMember(familyGroupId, userId)
            supabaseClient.removeFamilyMember(familyGroupId, userId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getFamilyMembers(familyGroupId: String): Flow<List<FamilyMemberInfo>> {
        return familyMemberDao.getFamilyMembers(familyGroupId).map { members ->
            // Convert to FamilyMemberInfo with user details
            members.map { member ->
                FamilyMemberInfo(
                    userId = member.userId,
                    name = "User ${member.userId}", // Would fetch from user repository
                    profilePhoto = null,
                    role = member.role,
                    joinedAt = member.joinedAt,
                    isActive = member.isActive
                )
            }
        }
    }

    override suspend fun shareMealPlan(
        familyGroupId: String,
        mealPlanId: String,
        sharedBy: String,
        title: String,
        description: String?
    ): Result<String> {
        return try {
            val id = UUID.randomUUID().toString()
            val sharedMealPlan = SharedMealPlan(
                id = id,
                familyGroupId = familyGroupId,
                mealPlanId = mealPlanId,
                sharedBy = sharedBy,
                title = title,
                description = description,
                weekStartDate = LocalDateTime.now(), // Would get from actual meal plan
                sharedAt = LocalDateTime.now()
            )
            
            sharedMealPlanDao.insertSharedMealPlan(sharedMealPlan)
            supabaseClient.insertSharedMealPlan(sharedMealPlan)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getSharedMealPlans(familyGroupId: String): Flow<List<SharedMealPlanWithDetails>> {
        return sharedMealPlanDao.getSharedMealPlans(familyGroupId).map { sharedPlans ->
            sharedPlans.map { plan ->
                SharedMealPlanWithDetails(
                    sharedMealPlan = plan,
                    sharedByName = "User ${plan.sharedBy}", // Would fetch from user repository
                    mealPlanData = "{}" // Would fetch actual meal plan data
                )
            }
        }
    }

    override suspend fun getSharedMealPlanById(id: String): SharedMealPlanWithDetails? {
        return try {
            val sharedPlan = sharedMealPlanDao.getSharedMealPlanById(id)
            if (sharedPlan != null) {
                SharedMealPlanWithDetails(
                    sharedMealPlan = sharedPlan,
                    sharedByName = "User ${sharedPlan.sharedBy}",
                    mealPlanData = "{}"
                )
            } else null
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun unshareeMealPlan(id: String): Result<Unit> {
        return try {
            sharedMealPlanDao.deactivateSharedMealPlan(id)
            supabaseClient.deactivateSharedMealPlan(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun shareRecipe(
        familyGroupId: String,
        recipeId: String,
        sharedBy: String,
        message: String?
    ): Result<String> {
        return try {
            val id = UUID.randomUUID().toString()
            val sharedRecipe = SharedRecipe(
                id = id,
                familyGroupId = familyGroupId,
                recipeId = recipeId,
                sharedBy = sharedBy,
                message = message,
                sharedAt = LocalDateTime.now()
            )
            
            sharedRecipeDao.insertSharedRecipe(sharedRecipe)
            supabaseClient.insertSharedRecipe(sharedRecipe)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getSharedRecipes(familyGroupId: String): Flow<List<SharedRecipeWithDetails>> {
        return sharedRecipeDao.getSharedRecipes(familyGroupId).map { sharedRecipes ->
            sharedRecipes.map { recipe ->
                SharedRecipeWithDetails(
                    sharedRecipe = recipe,
                    sharedByName = "User ${recipe.sharedBy}",
                    recipeData = "{}" // Would fetch actual recipe data
                )
            }
        }
    }

    override suspend fun getSharedRecipeById(id: String): SharedRecipeWithDetails? {
        return try {
            val sharedRecipe = sharedRecipeDao.getSharedRecipeById(id)
            if (sharedRecipe != null) {
                SharedRecipeWithDetails(
                    sharedRecipe = sharedRecipe,
                    sharedByName = "User ${sharedRecipe.sharedBy}",
                    recipeData = "{}"
                )
            } else null
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun unshareRecipe(id: String): Result<Unit> {
        return try {
            sharedRecipeDao.deactivateSharedRecipe(id)
            supabaseClient.deactivateSharedRecipe(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun assignMealPrep(
        familyGroupId: String,
        recipeId: String,
        recipeName: String,
        assignedTo: String,
        assignedBy: String,
        scheduledDate: LocalDateTime,
        notes: String?
    ): Result<String> {
        return try {
            val id = UUID.randomUUID().toString()
            val now = LocalDateTime.now()
            
            val mealPrep = CollaborativeMealPrep(
                id = id,
                familyGroupId = familyGroupId,
                recipeId = recipeId,
                recipeName = recipeName,
                assignedTo = assignedTo,
                assignedBy = assignedBy,
                scheduledDate = scheduledDate,
                status = MealPrepStatus.ASSIGNED,
                notes = notes,
                createdAt = now,
                updatedAt = now
            )
            
            collaborativeMealPrepDao.insertCollaborativeMealPrep(mealPrep)
            supabaseClient.insertCollaborativeMealPrep(mealPrep)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCollaborativeMealPrep(familyGroupId: String): Flow<List<CollaborativeMealPrepWithDetails>> {
        return collaborativeMealPrepDao.getCollaborativeMealPrep(familyGroupId).map { mealPreps ->
            mealPreps.map { prep ->
                CollaborativeMealPrepWithDetails(
                    mealPrep = prep,
                    assignedToName = "User ${prep.assignedTo}",
                    assignedByName = "User ${prep.assignedBy}"
                )
            }
        }
    }

    override suspend fun getMealPrepAssignedToUser(userId: String): Flow<List<CollaborativeMealPrepWithDetails>> {
        return collaborativeMealPrepDao.getMealPrepAssignedToUser(userId).map { mealPreps ->
            mealPreps.map { prep ->
                CollaborativeMealPrepWithDetails(
                    mealPrep = prep,
                    assignedToName = "User ${prep.assignedTo}",
                    assignedByName = "User ${prep.assignedBy}"
                )
            }
        }
    }

    override suspend fun updateMealPrepStatus(id: String, status: MealPrepStatus): Result<Unit> {
        return try {
            val now = LocalDateTime.now()
            collaborativeMealPrepDao.updateMealPrepStatus(id, status, now)
            supabaseClient.updateMealPrepStatus(id, status, now)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateMealPrepNotes(id: String, notes: String): Result<Unit> {
        return try {
            val mealPrep = collaborativeMealPrepDao.getCollaborativeMealPrepById(id)
            if (mealPrep != null) {
                val updated = mealPrep.copy(notes = notes, updatedAt = LocalDateTime.now())
                collaborativeMealPrepDao.updateCollaborativeMealPrep(updated)
                supabaseClient.updateCollaborativeMealPrep(updated)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createAchievement(
        userId: String,
        achievementType: AchievementType,
        title: String,
        description: String,
        iconName: String
    ): Result<String> {
        return try {
            val id = UUID.randomUUID().toString()
            val achievement = Achievement(
                id = id,
                userId = userId,
                achievementType = achievementType,
                title = title,
                description = description,
                iconName = iconName,
                earnedAt = LocalDateTime.now()
            )
            
            achievementDao.insertAchievement(achievement)
            supabaseClient.insertAchievement(achievement)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun shareAchievement(achievementId: String, familyGroupId: String, shareMessage: String?): Result<String> {
        return try {
            // Update achievement to mark as shared
            achievementDao.updateAchievementSharing(achievementId, true, shareMessage)
            
            // Create shared achievement record
            val id = UUID.randomUUID().toString()
            val sharedAchievement = SharedAchievement(
                id = id,
                achievementId = achievementId,
                familyGroupId = familyGroupId,
                sharedAt = LocalDateTime.now()
            )
            
            sharedAchievementDao.insertSharedAchievement(sharedAchievement)
            supabaseClient.insertSharedAchievement(sharedAchievement)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getSharedAchievements(familyGroupId: String): Flow<List<SharedAchievement>> {
        return sharedAchievementDao.getSharedAchievements(familyGroupId)
    }

    override suspend fun addAchievementReaction(sharedAchievementId: String, userId: String, reactionType: ReactionType): Result<Unit> {
        return try {
            val sharedAchievement = sharedAchievementDao.getSharedAchievementById(sharedAchievementId)
            if (sharedAchievement != null) {
                val reaction = AchievementReaction(userId, reactionType, LocalDateTime.now())
                val updatedReactions = sharedAchievement.reactions.filter { it.userId != userId } + reaction
                val updated = sharedAchievement.copy(reactions = updatedReactions)
                
                sharedAchievementDao.updateSharedAchievement(updated)
                supabaseClient.updateSharedAchievement(updated)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun removeAchievementReaction(sharedAchievementId: String, userId: String): Result<Unit> {
        return try {
            val sharedAchievement = sharedAchievementDao.getSharedAchievementById(sharedAchievementId)
            if (sharedAchievement != null) {
                val updatedReactions = sharedAchievement.reactions.filter { it.userId != userId }
                val updated = sharedAchievement.copy(reactions = updatedReactions)
                
                sharedAchievementDao.updateSharedAchievement(updated)
                supabaseClient.updateSharedAchievement(updated)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createSharedShoppingList(familyGroupId: String, name: String, createdBy: String): Result<String> {
        return try {
            val id = UUID.randomUUID().toString()
            val now = LocalDateTime.now()
            
            val sharedShoppingList = SharedShoppingList(
                id = id,
                familyGroupId = familyGroupId,
                name = name,
                createdBy = createdBy,
                createdAt = now,
                updatedAt = now
            )
            
            sharedShoppingListDao.insertSharedShoppingList(sharedShoppingList)
            supabaseClient.insertSharedShoppingList(sharedShoppingList)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getSharedShoppingLists(familyGroupId: String): Flow<List<SharedShoppingList>> {
        return sharedShoppingListDao.getSharedShoppingLists(familyGroupId)
    }

    override suspend fun addItemToSharedShoppingList(
        shoppingListId: String,
        itemName: String,
        quantity: String,
        category: String?,
        addedBy: String
    ): Result<String> {
        return try {
            val id = UUID.randomUUID().toString()
            val item = SharedShoppingListItem(
                id = id,
                shoppingListId = shoppingListId,
                itemName = itemName,
                quantity = quantity,
                category = category,
                addedBy = addedBy,
                addedAt = LocalDateTime.now()
            )
            
            sharedShoppingListItemDao.insertSharedShoppingListItem(item)
            supabaseClient.insertSharedShoppingListItem(item)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateShoppingListItemPurchaseStatus(
        itemId: String,
        isPurchased: Boolean,
        purchasedBy: String?
    ): Result<Unit> {
        return try {
            val purchasedAt = if (isPurchased) LocalDateTime.now() else null
            sharedShoppingListItemDao.updateItemPurchaseStatus(itemId, isPurchased, purchasedBy, purchasedAt)
            supabaseClient.updateShoppingListItemPurchaseStatus(itemId, isPurchased, purchasedBy, purchasedAt)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getSharedShoppingListItems(shoppingListId: String): Flow<List<SharedShoppingListItem>> {
        return sharedShoppingListItemDao.getSharedShoppingListItems(shoppingListId)
    }

    override suspend fun deleteSharedShoppingListItem(itemId: String): Result<Unit> {
        return try {
            sharedShoppingListItemDao.deleteSharedShoppingListItem(itemId)
            supabaseClient.deleteSharedShoppingListItem(itemId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun syncFamilyData(userId: String): Result<Unit> {
        return try {
            // Sync family groups and memberships from cloud
            supabaseClient.syncFamilyData(userId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun syncSharedContent(familyGroupId: String): Result<Unit> {
        return try {
            // Sync all shared content for family group
            supabaseClient.syncSharedContent(familyGroupId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}