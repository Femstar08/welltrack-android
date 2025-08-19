package com.beaconledger.welltrack.presentation.shoppinglist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.domain.usecase.ShoppingListUseCase
import com.beaconledger.welltrack.domain.repository.ShoppingListAnalytics
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShoppingListViewModel @Inject constructor(
    private val shoppingListUseCase: ShoppingListUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ShoppingListUiState())
    val uiState: StateFlow<ShoppingListUiState> = _uiState.asStateFlow()
    
    private val _currentUserId = MutableStateFlow("")
    
    fun setCurrentUser(userId: String) {
        _currentUserId.value = userId
        loadShoppingLists()
    }
    
    private fun loadShoppingLists() {
        viewModelScope.launch {
            _currentUserId.value.let { userId ->
                if (userId.isNotEmpty()) {
                    shoppingListUseCase.getShoppingListsWithItems(userId)
                        .catch { exception ->
                            _uiState.update { it.copy(
                                isLoading = false,
                                error = exception.message ?: "Failed to load shopping lists"
                            )}
                        }
                        .collect { shoppingLists ->
                            _uiState.update { it.copy(
                                isLoading = false,
                                shoppingLists = shoppingLists,
                                error = null
                            )}
                        }
                }
            }
        }
    }
    
    fun loadShoppingListDetails(shoppingListId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val shoppingListWithItems = shoppingListUseCase.getShoppingListWithItems(shoppingListId)
                val analytics = shoppingListUseCase.getShoppingListAnalytics(shoppingListId)
                
                _uiState.update { it.copy(
                    isLoading = false,
                    selectedShoppingList = shoppingListWithItems,
                    selectedShoppingListAnalytics = analytics,
                    error = null
                )}
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load shopping list details"
                )}
            }
        }
    }
    
    fun createShoppingList(request: ShoppingListCreateRequest) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val result = shoppingListUseCase.createShoppingList(request, _currentUserId.value)
            
            if (result.isSuccess) {
                _uiState.update { it.copy(
                    isLoading = false,
                    showCreateDialog = false,
                    error = null
                )}
                loadShoppingLists()
            } else {
                _uiState.update { it.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Failed to create shopping list"
                )}
            }
        }
    }
    
    fun updateShoppingList(shoppingListId: String, request: ShoppingListUpdateRequest) {
        viewModelScope.launch {
            val result = shoppingListUseCase.updateShoppingList(shoppingListId, request)
            
            if (result.isSuccess) {
                loadShoppingLists()
                if (_uiState.value.selectedShoppingList?.shoppingList?.id == shoppingListId) {
                    loadShoppingListDetails(shoppingListId)
                }
            } else {
                _uiState.update { it.copy(
                    error = result.exceptionOrNull()?.message ?: "Failed to update shopping list"
                )}
            }
        }
    }
    
    fun deleteShoppingList(shoppingListId: String) {
        viewModelScope.launch {
            val result = shoppingListUseCase.deleteShoppingList(shoppingListId)
            
            if (result.isSuccess) {
                _uiState.update { it.copy(
                    selectedShoppingList = if (_uiState.value.selectedShoppingList?.shoppingList?.id == shoppingListId) null else _uiState.value.selectedShoppingList
                )}
                loadShoppingLists()
            } else {
                _uiState.update { it.copy(
                    error = result.exceptionOrNull()?.message ?: "Failed to delete shopping list"
                )}
            }
        }
    }
    
    fun addShoppingListItem(shoppingListId: String, request: ShoppingListItemCreateRequest) {
        viewModelScope.launch {
            val result = shoppingListUseCase.addShoppingListItem(shoppingListId, request)
            
            if (result.isSuccess) {
                _uiState.update { it.copy(
                    showAddItemDialog = false,
                    newItemName = "",
                    newItemQuantity = "",
                    newItemUnit = "",
                    newItemCategory = IngredientCategory.OTHER
                )}
                loadShoppingListDetails(shoppingListId)
            } else {
                _uiState.update { it.copy(
                    error = result.exceptionOrNull()?.message ?: "Failed to add item"
                )}
            }
        }
    }
    
    fun updateShoppingListItem(itemId: String, request: ShoppingListItemUpdateRequest) {
        viewModelScope.launch {
            val result = shoppingListUseCase.updateShoppingListItem(itemId, request)
            
            if (result.isSuccess) {
                _uiState.value.selectedShoppingList?.shoppingList?.id?.let { shoppingListId ->
                    loadShoppingListDetails(shoppingListId)
                }
            } else {
                _uiState.update { it.copy(
                    error = result.exceptionOrNull()?.message ?: "Failed to update item"
                )}
            }
        }
    }
    
    fun deleteShoppingListItem(itemId: String) {
        viewModelScope.launch {
            val result = shoppingListUseCase.deleteShoppingListItem(itemId)
            
            if (result.isSuccess) {
                _uiState.value.selectedShoppingList?.shoppingList?.id?.let { shoppingListId ->
                    loadShoppingListDetails(shoppingListId)
                }
            } else {
                _uiState.update { it.copy(
                    error = result.exceptionOrNull()?.message ?: "Failed to delete item"
                )}
            }
        }
    }
    
    fun toggleItemPurchaseStatus(itemId: String, isPurchased: Boolean) {
        viewModelScope.launch {
            val result = shoppingListUseCase.markItemAsPurchased(itemId, isPurchased)
            
            if (result.isSuccess) {
                _uiState.value.selectedShoppingList?.shoppingList?.id?.let { shoppingListId ->
                    loadShoppingListDetails(shoppingListId)
                }
            } else {
                _uiState.update { it.copy(
                    error = result.exceptionOrNull()?.message ?: "Failed to update item status"
                )}
            }
        }
    }
    
    fun markAllItemsAsPurchased(shoppingListId: String, isPurchased: Boolean) {
        viewModelScope.launch {
            val result = shoppingListUseCase.markAllItemsAsPurchased(shoppingListId, isPurchased)
            
            if (result.isSuccess) {
                loadShoppingListDetails(shoppingListId)
            } else {
                _uiState.update { it.copy(
                    error = result.exceptionOrNull()?.message ?: "Failed to update all items"
                )}
            }
        }
    }
    
    fun generateShoppingListFromMealPlan(
        mealPlanId: String,
        name: String? = null,
        includeExistingPantryItems: Boolean = false,
        consolidateSimilarItems: Boolean = true,
        excludeCategories: List<IngredientCategory> = emptyList()
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val result = shoppingListUseCase.generateShoppingListFromMealPlan(
                userId = _currentUserId.value,
                mealPlanId = mealPlanId,
                name = name,
                includeExistingPantryItems = includeExistingPantryItems,
                consolidateSimilarItems = consolidateSimilarItems,
                excludeCategories = excludeCategories
            )
            
            if (result.isSuccess) {
                val generationResult = result.getOrNull()
                if (generationResult?.success == true) {
                    _uiState.update { it.copy(
                        isLoading = false,
                        showGenerateDialog = false,
                        error = null
                    )}
                    loadShoppingLists()
                } else {
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = generationResult?.error ?: "Failed to generate shopping list"
                    )}
                }
            } else {
                _uiState.update { it.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Failed to generate shopping list"
                )}
            }
        }
    }
    
    fun duplicateShoppingList(shoppingListId: String, newName: String) {
        viewModelScope.launch {
            val result = shoppingListUseCase.duplicateShoppingList(shoppingListId, newName)
            
            if (result.isSuccess) {
                loadShoppingLists()
            } else {
                _uiState.update { it.copy(
                    error = result.exceptionOrNull()?.message ?: "Failed to duplicate shopping list"
                )}
            }
        }
    }
    
    fun searchIngredients(query: String) {
        viewModelScope.launch {
            if (query.length >= 2) {
                try {
                    val suggestions = shoppingListUseCase.searchIngredientNames(query)
                    _uiState.update { it.copy(ingredientSuggestions = suggestions) }
                } catch (e: Exception) {
                    // Ignore search errors
                }
            } else {
                _uiState.update { it.copy(ingredientSuggestions = emptyList()) }
            }
        }
    }
    
    // UI State management
    fun showCreateDialog() {
        _uiState.update { it.copy(showCreateDialog = true) }
    }
    
    fun hideCreateDialog() {
        _uiState.update { it.copy(showCreateDialog = false) }
    }
    
    fun showAddItemDialog() {
        _uiState.update { it.copy(showAddItemDialog = true) }
    }
    
    fun hideAddItemDialog() {
        _uiState.update { it.copy(
            showAddItemDialog = false,
            newItemName = "",
            newItemQuantity = "",
            newItemUnit = "",
            newItemCategory = IngredientCategory.OTHER
        )}
    }
    
    fun showGenerateDialog() {
        _uiState.update { it.copy(showGenerateDialog = true) }
    }
    
    fun hideGenerateDialog() {
        _uiState.update { it.copy(showGenerateDialog = false) }
    }
    
    fun updateNewItemName(name: String) {
        _uiState.update { it.copy(newItemName = name) }
    }
    
    fun updateNewItemQuantity(quantity: String) {
        _uiState.update { it.copy(newItemQuantity = quantity) }
    }
    
    fun updateNewItemUnit(unit: String) {
        _uiState.update { it.copy(newItemUnit = unit) }
    }
    
    fun updateNewItemCategory(category: IngredientCategory) {
        _uiState.update { it.copy(newItemCategory = category) }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    fun setViewMode(viewMode: ShoppingListViewMode) {
        _uiState.update { it.copy(viewMode = viewMode) }
    }
    
    fun setFilterCategory(category: IngredientCategory?) {
        _uiState.update { it.copy(filterCategory = category) }
    }
    
    fun setShowPurchasedItems(show: Boolean) {
        _uiState.update { it.copy(showPurchasedItems = show) }
    }
}

data class ShoppingListUiState(
    val isLoading: Boolean = false,
    val shoppingLists: List<ShoppingListWithItems> = emptyList(),
    val selectedShoppingList: ShoppingListWithItems? = null,
    val selectedShoppingListAnalytics: ShoppingListAnalytics? = null,
    val error: String? = null,
    
    // Dialog states
    val showCreateDialog: Boolean = false,
    val showAddItemDialog: Boolean = false,
    val showGenerateDialog: Boolean = false,
    
    // New item form state
    val newItemName: String = "",
    val newItemQuantity: String = "",
    val newItemUnit: String = "",
    val newItemCategory: IngredientCategory = IngredientCategory.OTHER,
    
    // Search and suggestions
    val ingredientSuggestions: List<String> = emptyList(),
    
    // View options
    val viewMode: ShoppingListViewMode = ShoppingListViewMode.LIST,
    val filterCategory: IngredientCategory? = null,
    val showPurchasedItems: Boolean = true
)

enum class ShoppingListViewMode {
    LIST,
    CATEGORY_GROUPED
}