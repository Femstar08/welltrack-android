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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListDetailHeader(
    shoppingList: ShoppingList,
    analytics: ShoppingListAnalytics?,
    onBackClick: () -> Unit,
    onAddItem: () -> Unit,
    onMarkAllPurchased: (Boolean) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                
                Column {
                    Text(
                        text = shoppingList.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    shoppingList.description?.let { description ->
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            
            Row {
                IconButton(onClick = onAddItem) {
                    Icon(Icons.Default.Add, contentDescription = "Add item")
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
                            text = { Text("Mark all as purchased") },
                            onClick = {
                                showMenu = false
                                onMarkAllPurchased(true)
                            },
                            leadingIcon = { Icon(Icons.Default.CheckCircle, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Mark all as unpurchased") },
                            onClick = {
                                showMenu = false
                                onMarkAllPurchased(false)
                            },
                            leadingIcon = { Icon(Icons.Default.Check, contentDescription = null) }
                        )
                    }
                }
            }
        }
        
        // Progress and analytics
        analytics?.let { stats ->
            Spacer(modifier = Modifier.height(16.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Progress",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${String.format("%.1f", stats.completionPercentage)}%",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    LinearProgressIndicator(
                        progress = stats.completionPercentage / 100f,
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem(
                            label = "Total",
                            value = stats.totalItems.toString(),
                            icon = Icons.Default.List
                        )
                        StatItem(
                            label = "Purchased",
                            value = stats.purchasedItems.toString(),
                            icon = Icons.Default.CheckCircle,
                            color = MaterialTheme.colorScheme.primary
                        )
                        StatItem(
                            label = "Remaining",
                            value = stats.remainingItems.toString(),
                            icon = Icons.Default.Info,
                            color = MaterialTheme.colorScheme.error
                        )
                        
                        stats.totalEstimatedCost?.let { cost ->
                            StatItem(
                                label = "Est. Cost",
                                value = "$${String.format("%.2f", cost)}",
                                icon = Icons.Default.Star
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListFilters(
    viewMode: ShoppingListViewMode,
    filterCategory: IngredientCategory?,
    showPurchasedItems: Boolean,
    onSetViewMode: (ShoppingListViewMode) -> Unit,
    onSetFilterCategory: (IngredientCategory?) -> Unit,
    onSetShowPurchasedItems: (Boolean) -> Unit
) {
    var showCategoryFilter by remember { mutableStateOf(false) }
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // View mode toggle
        SegmentedButton(
            options = listOf("List", "Categories"),
            selectedIndex = if (viewMode == ShoppingListViewMode.LIST) 0 else 1,
            onSelectionChange = { index ->
                onSetViewMode(
                    if (index == 0) ShoppingListViewMode.LIST 
                    else ShoppingListViewMode.CATEGORY_GROUPED
                )
            }
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Category filter
        FilterChip(
            onClick = { showCategoryFilter = true },
            label = { 
                Text(filterCategory?.displayName ?: "All Categories") 
            },
            selected = filterCategory != null,
            leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) }
        )
        
        // Show purchased toggle
        FilterChip(
            onClick = { onSetShowPurchasedItems(!showPurchasedItems) },
            label = { Text("Purchased") },
            selected = showPurchasedItems,
            leadingIcon = { 
                Icon(
                    if (showPurchasedItems) Icons.Default.Check 
                    else Icons.Default.Close, 
                    contentDescription = null
                ) 
            }
        )
    }
    
    if (showCategoryFilter) {
        CategoryFilterDialog(
            selectedCategory = filterCategory,
            onDismiss = { showCategoryFilter = false },
            onCategorySelected = { category ->
                onSetFilterCategory(category)
                showCategoryFilter = false
            }
        )
    }
}

@Composable
fun SegmentedButton(
    options: List<String>,
    selectedIndex: Int,
    onSelectionChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        options.forEachIndexed { index, option ->
            val isSelected = index == selectedIndex
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary
                        else Color.Transparent
                    )
                    .clickable { onSelectionChange(index) }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = option,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                           else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ShoppingListItemsList(
    items: List<ShoppingListItem>,
    onToggleItemPurchase: (String, Boolean) -> Unit,
    onDeleteItem: (String) -> Unit,
    onUpdateItem: (String, ShoppingListItemUpdateRequest) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items) { item ->
            ShoppingListItemCard(
                item = item,
                onTogglePurchase = { onToggleItemPurchase(item.id, !item.isPurchased) },
                onDelete = { onDeleteItem(item.id) },
                onUpdate = { request -> onUpdateItem(item.id, request) }
            )
        }
    }
}

@Composable
fun ShoppingListItemsGrouped(
    items: List<ShoppingListItem>,
    onToggleItemPurchase: (String, Boolean) -> Unit,
    onDeleteItem: (String) -> Unit,
    onUpdateItem: (String, ShoppingListItemUpdateRequest) -> Unit
) {
    val groupedItems = items.groupBy { it.category }
    
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        groupedItems.forEach { (category, categoryItems) ->
            item {
                CategorySection(
                    category = category,
                    items = categoryItems,
                    onToggleItemPurchase = onToggleItemPurchase,
                    onDeleteItem = onDeleteItem,
                    onUpdateItem = onUpdateItem
                )
            }
        }
    }
}

@Composable
fun CategorySection(
    category: IngredientCategory,
    items: List<ShoppingListItem>,
    onToggleItemPurchase: (String, Boolean) -> Unit,
    onDeleteItem: (String) -> Unit,
    onUpdateItem: (String, ShoppingListItemUpdateRequest) -> Unit
) {
    Column {
        Text(
            text = category.displayName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        items.forEach { item ->
            ShoppingListItemCard(
                item = item,
                onTogglePurchase = { onToggleItemPurchase(item.id, !item.isPurchased) },
                onDelete = { onDeleteItem(item.id) },
                onUpdate = { request -> onUpdateItem(item.id, request) }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}