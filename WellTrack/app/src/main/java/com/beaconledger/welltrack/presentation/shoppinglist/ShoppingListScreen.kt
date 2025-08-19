package com.beaconledger.welltrack.presentation.shoppinglist

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.beaconledger.welltrack.data.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListScreen(
    userId: String,
    onNavigateBack: () -> Unit,
    viewModel: ShoppingListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    LaunchedEffect(userId) {
        viewModel.setCurrentUser(userId)
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Shopping Lists",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            Row {
                IconButton(onClick = { viewModel.showGenerateDialog() }) {
                    Icon(Icons.Default.Star, contentDescription = "Generate from meal plan")
                }
                IconButton(onClick = { viewModel.showCreateDialog() }) {
                    Icon(Icons.Default.Add, contentDescription = "Create shopping list")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Content
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            uiState.selectedShoppingList != null -> {
                ShoppingListDetailView(
                    shoppingListWithItems = uiState.selectedShoppingList!!,
                    analytics = uiState.selectedShoppingListAnalytics,
                    viewMode = uiState.viewMode,
                    filterCategory = uiState.filterCategory,
                    showPurchasedItems = uiState.showPurchasedItems,
                    onBackClick = { viewModel.loadShoppingListDetails("") },
                    onAddItem = { viewModel.showAddItemDialog() },
                    onToggleItemPurchase = { itemId, isPurchased ->
                        viewModel.toggleItemPurchaseStatus(itemId, isPurchased)
                    },
                    onDeleteItem = { itemId ->
                        viewModel.deleteShoppingListItem(itemId)
                    },
                    onUpdateItem = { itemId, request ->
                        viewModel.updateShoppingListItem(itemId, request)
                    },
                    onMarkAllPurchased = { isPurchased ->
                        viewModel.markAllItemsAsPurchased(uiState.selectedShoppingList!!.shoppingList.id, isPurchased)
                    },
                    onSetViewMode = { mode -> viewModel.setViewMode(mode) },
                    onSetFilterCategory = { category -> viewModel.setFilterCategory(category) },
                    onSetShowPurchasedItems = { show -> viewModel.setShowPurchasedItems(show) }
                )
            }
            
            else -> {
                ShoppingListOverview(
                    shoppingLists = uiState.shoppingLists,
                    onShoppingListClick = { shoppingListId ->
                        viewModel.loadShoppingListDetails(shoppingListId)
                    },
                    onDeleteShoppingList = { shoppingListId ->
                        viewModel.deleteShoppingList(shoppingListId)
                    },
                    onDuplicateShoppingList = { shoppingListId, newName ->
                        viewModel.duplicateShoppingList(shoppingListId, newName)
                    }
                )
            }
        }
    }
    
    // Dialogs
    if (uiState.showCreateDialog) {
        CreateShoppingListDialog(
            onDismiss = { viewModel.hideCreateDialog() },
            onConfirm = { request ->
                viewModel.createShoppingList(request)
            }
        )
    }
    
    if (uiState.showAddItemDialog) {
        AddShoppingListItemDialog(
            name = uiState.newItemName,
            quantity = uiState.newItemQuantity,
            unit = uiState.newItemUnit,
            category = uiState.newItemCategory,
            ingredientSuggestions = uiState.ingredientSuggestions,
            onNameChange = { viewModel.updateNewItemName(it) },
            onQuantityChange = { viewModel.updateNewItemQuantity(it) },
            onUnitChange = { viewModel.updateNewItemUnit(it) },
            onCategoryChange = { viewModel.updateNewItemCategory(it) },
            onSearchIngredients = { query -> viewModel.searchIngredients(query) },
            onDismiss = { viewModel.hideAddItemDialog() },
            onConfirm = { request ->
                uiState.selectedShoppingList?.shoppingList?.id?.let { shoppingListId ->
                    viewModel.addShoppingListItem(shoppingListId, request)
                }
            }
        )
    }
    
    if (uiState.showGenerateDialog) {
        GenerateShoppingListDialog(
            onDismiss = { viewModel.hideGenerateDialog() },
            onConfirm = { mealPlanId, name, options ->
                viewModel.generateShoppingListFromMealPlan(
                    mealPlanId = mealPlanId,
                    name = name,
                    includeExistingPantryItems = options.includeExistingPantryItems,
                    consolidateSimilarItems = options.consolidateSimilarItems,
                    excludeCategories = options.excludeCategories
                )
            }
        )
    }
    
    // Error handling
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // Show snackbar or handle error
            viewModel.clearError()
        }
    }
}