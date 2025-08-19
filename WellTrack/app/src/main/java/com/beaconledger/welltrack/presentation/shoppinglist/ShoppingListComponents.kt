package com.beaconledger.welltrack.presentation.shoppinglist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.domain.repository.ShoppingListAnalytics

@Composable
fun ShoppingListOverview(
    shoppingLists: List<ShoppingListWithItems>,
    onShoppingListClick: (String) -> Unit,
    onDeleteShoppingList: (String) -> Unit,
    onDuplicateShoppingList: (String, String) -> Unit
) {
    if (shoppingLists.isEmpty()) {
        EmptyShoppingListState()
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(shoppingLists) { shoppingListWithItems ->
                ShoppingListCard(
                    shoppingListWithItems = shoppingListWithItems,
                    onClick = { onShoppingListClick(shoppingListWithItems.shoppingList.id) },
                    onDelete = { onDeleteShoppingList(shoppingListWithItems.shoppingList.id) },
                    onDuplicate = { newName -> 
                        onDuplicateShoppingList(shoppingListWithItems.shoppingList.id, newName) 
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListCard(
    shoppingListWithItems: ShoppingListWithItems,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onDuplicate: (String) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var showDuplicateDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = shoppingListWithItems.shoppingList.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    shoppingListWithItems.shoppingList.description?.let { description ->
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More options")
                    }
                    
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Duplicate") },
                            onClick = {
                                showMenu = false
                                showDuplicateDialog = true
                            },
                            leadingIcon = { Icon(Icons.Default.Add, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = {
                                showMenu = false
                                onDelete()
                            },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Progress indicator
            val progress = if (shoppingListWithItems.totalItems > 0) {
                shoppingListWithItems.purchasedItems.toFloat() / shoppingListWithItems.totalItems
            } else 0f
            
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${shoppingListWithItems.purchasedItems}/${shoppingListWithItems.totalItems} items",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                shoppingListWithItems.totalEstimatedCost.takeIf { it > 0 }?.let { cost ->
                    Text(
                        text = "$${String.format("%.2f", cost)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Status indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (shoppingListWithItems.shoppingList.generatedFromMealPlan) {
                    AssistChip(
                        onClick = { },
                        label = { Text("From Meal Plan", style = MaterialTheme.typography.labelSmall) },
                        leadingIcon = { Icon(Icons.Default.Star, contentDescription = null) }
                    )
                }
                
                if (!shoppingListWithItems.shoppingList.isActive) {
                    AssistChip(
                        onClick = { },
                        label = { Text("Archived", style = MaterialTheme.typography.labelSmall) },
                        leadingIcon = { Icon(Icons.Default.Star, contentDescription = null) }
                    )
                }
            }
        }
    }
    
    if (showDuplicateDialog) {
        DuplicateShoppingListDialog(
            originalName = shoppingListWithItems.shoppingList.name,
            onDismiss = { showDuplicateDialog = false },
            onConfirm = { newName ->
                onDuplicate(newName)
                showDuplicateDialog = false
            }
        )
    }
}

@Composable
fun EmptyShoppingListState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.ShoppingCart,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "No Shopping Lists",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Create a shopping list or generate one from your meal plan",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ShoppingListDetailView(
    shoppingListWithItems: ShoppingListWithItems,
    analytics: ShoppingListAnalytics?,
    viewMode: ShoppingListViewMode,
    filterCategory: IngredientCategory?,
    showPurchasedItems: Boolean,
    onBackClick: () -> Unit,
    onAddItem: () -> Unit,
    onToggleItemPurchase: (String, Boolean) -> Unit,
    onDeleteItem: (String) -> Unit,
    onUpdateItem: (String, ShoppingListItemUpdateRequest) -> Unit,
    onMarkAllPurchased: (Boolean) -> Unit,
    onSetViewMode: (ShoppingListViewMode) -> Unit,
    onSetFilterCategory: (IngredientCategory?) -> Unit,
    onSetShowPurchasedItems: (Boolean) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        ShoppingListDetailHeader(
            shoppingList = shoppingListWithItems.shoppingList,
            analytics = analytics,
            onBackClick = onBackClick,
            onAddItem = onAddItem,
            onMarkAllPurchased = onMarkAllPurchased
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Filters and view options
        ShoppingListFilters(
            viewMode = viewMode,
            filterCategory = filterCategory,
            showPurchasedItems = showPurchasedItems,
            onSetViewMode = onSetViewMode,
            onSetFilterCategory = onSetFilterCategory,
            onSetShowPurchasedItems = onSetShowPurchasedItems
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Items list
        val filteredItems = shoppingListWithItems.items.filter { item ->
            (filterCategory == null || item.category == filterCategory) &&
            (showPurchasedItems || !item.isPurchased)
        }
        
        when (viewMode) {
            ShoppingListViewMode.LIST -> {
                ShoppingListItemsList(
                    items = filteredItems,
                    onToggleItemPurchase = onToggleItemPurchase,
                    onDeleteItem = onDeleteItem,
                    onUpdateItem = onUpdateItem
                )
            }
            ShoppingListViewMode.CATEGORY_GROUPED -> {
                ShoppingListItemsGrouped(
                    items = filteredItems,
                    onToggleItemPurchase = onToggleItemPurchase,
                    onDeleteItem = onDeleteItem,
                    onUpdateItem = onUpdateItem
                )
            }
        }
    }
}