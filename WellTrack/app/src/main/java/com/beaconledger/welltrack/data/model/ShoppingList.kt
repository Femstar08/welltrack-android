package com.beaconledger.welltrack.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "shopping_lists")
data class ShoppingList(
    @PrimaryKey
    val id: String,
    val userId: String,
    val name: String,
    val description: String? = null,
    val isActive: Boolean = true,
    val generatedFromMealPlan: Boolean = false,
    val mealPlanId: String? = null,
    val weekStartDate: String? = null, // ISO 8601 date string for meal plan reference
    val weekEndDate: String? = null,   // ISO 8601 date string for meal plan reference
    val totalEstimatedCost: Double? = null,
    val createdAt: String = LocalDateTime.now().toString(),
    val updatedAt: String = LocalDateTime.now().toString()
)

@Entity(tableName = "shopping_list_items")
data class ShoppingListItem(
    @PrimaryKey
    val id: String,
    val shoppingListId: String,
    val name: String,
    val quantity: Double,
    val unit: String,
    val category: IngredientCategory = IngredientCategory.OTHER,
    val estimatedCost: Double? = null,
    val isPurchased: Boolean = false,
    val purchasedAt: String? = null,
    val notes: String? = null,
    val priority: ShoppingItemPriority = ShoppingItemPriority.NORMAL,
    val recipeIds: String = "[]", // JSON array of recipe IDs that require this ingredient
    val isManuallyAdded: Boolean = false, // true if added manually, false if generated from meal plan
    val consolidatedFromItems: String = "[]", // JSON array of original item IDs that were consolidated
    val createdAt: String = LocalDateTime.now().toString(),
    val updatedAt: String = LocalDateTime.now().toString()
)

data class ShoppingListWithItems(
    val shoppingList: ShoppingList,
    val items: List<ShoppingListItem>,
    val categorizedItems: Map<IngredientCategory, List<ShoppingListItem>> = emptyMap(),
    val totalItems: Int = items.size,
    val purchasedItems: Int = items.count { it.isPurchased },
    val totalEstimatedCost: Double = items.mapNotNull { it.estimatedCost }.sum()
)

data class ShoppingListGenerationRequest(
    val userId: String,
    val mealPlanId: String,
    val name: String? = null,
    val includeExistingPantryItems: Boolean = false,
    val consolidateSimilarItems: Boolean = true,
    val excludeCategories: List<IngredientCategory> = emptyList()
)

data class ShoppingListGenerationResult(
    val success: Boolean,
    val shoppingList: ShoppingListWithItems? = null,
    val consolidatedItems: List<ConsolidatedIngredient> = emptyList(),
    val warnings: List<String> = emptyList(),
    val error: String? = null
)

data class ConsolidatedIngredient(
    val name: String,
    val totalQuantity: Double,
    val unit: String,
    val category: IngredientCategory,
    val sourceRecipes: List<String>, // Recipe IDs
    val originalItems: List<IngredientConsolidationSource>
)

data class IngredientConsolidationSource(
    val recipeId: String,
    val recipeName: String,
    val quantity: Double,
    val unit: String
)

enum class ShoppingItemPriority {
    HIGH,
    NORMAL,
    LOW;
    
    val displayName: String
        get() = when (this) {
            HIGH -> "High Priority"
            NORMAL -> "Normal"
            LOW -> "Low Priority"
        }
    
    val sortOrder: Int
        get() = when (this) {
            HIGH -> 1
            NORMAL -> 2
            LOW -> 3
        }
}

data class ShoppingListCreateRequest(
    val name: String,
    val description: String? = null,
    val items: List<ShoppingListItemCreateRequest> = emptyList()
)

data class ShoppingListItemCreateRequest(
    val name: String,
    val quantity: Double,
    val unit: String,
    val category: IngredientCategory = IngredientCategory.OTHER,
    val estimatedCost: Double? = null,
    val notes: String? = null,
    val priority: ShoppingItemPriority = ShoppingItemPriority.NORMAL
)

data class ShoppingListUpdateRequest(
    val name: String?,
    val description: String?,
    val isActive: Boolean?
)

data class ShoppingListItemUpdateRequest(
    val name: String?,
    val quantity: Double?,
    val unit: String?,
    val category: IngredientCategory?,
    val estimatedCost: Double?,
    val notes: String?,
    val priority: ShoppingItemPriority?,
    val isPurchased: Boolean?
)