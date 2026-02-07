package com.beaconledger.welltrack.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index
import java.time.LocalDateTime

@Entity(
    tableName = "family_groups",
    indices = [Index(value = ["createdBy"])]
)
data class FamilyGroup(
    @PrimaryKey val id: String,
    val name: String,
    val description: String? = null,
    val createdBy: String, // User ID
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val isActive: Boolean = true
)

@Entity(
    tableName = "family_members",
    primaryKeys = ["familyGroupId", "userId"],
    foreignKeys = [
        ForeignKey(
            entity = FamilyGroup::class,
            parentColumns = ["id"],
            childColumns = ["familyGroupId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["familyGroupId"]), Index(value = ["userId"])]
)
data class FamilyMember(
    val familyGroupId: String,
    val userId: String,
    val role: FamilyRole,
    val joinedAt: LocalDateTime,
    val isActive: Boolean = true
)

enum class FamilyRole {
    ADMIN,
    MEMBER,
    VIEWER
}

@Entity(
    tableName = "shared_meal_plans",
    foreignKeys = [
        ForeignKey(
            entity = FamilyGroup::class,
            parentColumns = ["id"],
            childColumns = ["familyGroupId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["familyGroupId"]), Index(value = ["sharedBy"])]
)
data class SharedMealPlan(
    @PrimaryKey val id: String,
    val familyGroupId: String,
    val mealPlanId: String,
    val sharedBy: String, // User ID
    val title: String,
    val description: String? = null,
    val weekStartDate: LocalDateTime,
    val sharedAt: LocalDateTime,
    val isActive: Boolean = true
)

@Entity(
    tableName = "shared_recipes",
    foreignKeys = [
        ForeignKey(
            entity = FamilyGroup::class,
            parentColumns = ["id"],
            childColumns = ["familyGroupId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["familyGroupId"]), Index(value = ["sharedBy"])]
)
data class SharedRecipe(
    @PrimaryKey val id: String,
    val familyGroupId: String,
    val recipeId: String,
    val sharedBy: String, // User ID
    val message: String? = null,
    val sharedAt: LocalDateTime,
    val isActive: Boolean = true
)

@Entity(
    tableName = "collaborative_meal_prep",
    foreignKeys = [
        ForeignKey(
            entity = FamilyGroup::class,
            parentColumns = ["id"],
            childColumns = ["familyGroupId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["familyGroupId"]), Index(value = ["assignedTo"])]
)
data class CollaborativeMealPrep(
    @PrimaryKey val id: String,
    val familyGroupId: String,
    val recipeId: String,
    val recipeName: String,
    val assignedTo: String, // User ID
    val assignedBy: String, // User ID
    val scheduledDate: LocalDateTime,
    val status: MealPrepStatus,
    val notes: String? = null,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

enum class MealPrepStatus {
    ASSIGNED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
}

@Entity(
    tableName = "achievements",
    indices = [Index(value = ["userId"]), Index(value = ["achievementType"])]
)
data class SocialAchievement(
    @PrimaryKey val id: String,
    val userId: String,
    val achievementType: AchievementType,
    val title: String,
    val description: String,
    val iconName: String,
    val earnedAt: LocalDateTime,
    val isShared: Boolean = false,
    val shareMessage: String? = null
)

enum class AchievementType {
    MEAL_STREAK,
    NUTRITION_GOAL,
    RECIPE_MASTERY,
    HEALTH_MILESTONE,
    CONSISTENCY_BADGE,
    EXPLORATION_AWARD
}

@Entity(
    tableName = "shared_achievements",
    foreignKeys = [
        ForeignKey(
            entity = SocialAchievement::class,
            parentColumns = ["id"],
            childColumns = ["achievementId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = FamilyGroup::class,
            parentColumns = ["id"],
            childColumns = ["familyGroupId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["achievementId"]), Index(value = ["familyGroupId"])]
)
data class SharedAchievement(
    @PrimaryKey val id: String,
    val achievementId: String,
    val familyGroupId: String,
    val sharedAt: LocalDateTime,
    val reactions: List<AchievementReaction> = emptyList()
)

data class AchievementReaction(
    val userId: String,
    val reactionType: ReactionType,
    val reactedAt: LocalDateTime
)

enum class ReactionType {
    LIKE,
    LOVE,
    CELEBRATE,
    SUPPORT
}

@Entity(
    tableName = "shared_shopping_lists",
    foreignKeys = [
        ForeignKey(
            entity = FamilyGroup::class,
            parentColumns = ["id"],
            childColumns = ["familyGroupId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["familyGroupId"]), Index(value = ["createdBy"])]
)
data class SharedShoppingList(
    @PrimaryKey val id: String,
    val familyGroupId: String,
    val name: String,
    val createdBy: String, // User ID
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val isActive: Boolean = true
)

@Entity(
    tableName = "shared_shopping_list_items",
    foreignKeys = [
        ForeignKey(
            entity = SharedShoppingList::class,
            parentColumns = ["id"],
            childColumns = ["shoppingListId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["shoppingListId"])]
)
data class SharedShoppingListItem(
    @PrimaryKey val id: String,
    val shoppingListId: String,
    val itemName: String,
    val quantity: String,
    val category: String? = null,
    val isPurchased: Boolean = false,
    val purchasedBy: String? = null, // User ID
    val purchasedAt: LocalDateTime? = null,
    val addedBy: String, // User ID
    val addedAt: LocalDateTime
)

// Data transfer objects for API communication
data class FamilyGroupWithMembers(
    val familyGroup: FamilyGroup,
    val members: List<FamilyMemberInfo>
)

data class FamilyMemberInfo(
    val userId: String,
    val name: String,
    val profilePhoto: String? = null,
    val role: FamilyRole,
    val joinedAt: LocalDateTime,
    val isActive: Boolean
)

data class SharedMealPlanWithDetails(
    val sharedMealPlan: SharedMealPlan,
    val sharedByName: String,
    val mealPlanData: String // JSON representation of meal plan
)

data class SharedRecipeWithDetails(
    val sharedRecipe: SharedRecipe,
    val sharedByName: String,
    val recipeData: String // JSON representation of recipe
)

data class CollaborativeMealPrepWithDetails(
    val mealPrep: CollaborativeMealPrep,
    val assignedToName: String,
    val assignedByName: String
)