package com.beaconledger.welltrack.presentation.recipe

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beaconledger.welltrack.data.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    recipe: Recipe,
    ingredients: List<Ingredient>,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onShareClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    isFavorite: Boolean = false,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Ingredients", "Instructions", "Nutrition")

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Top Bar
        TopAppBar(
            title = {
                Text(
                    text = recipe.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            },
            actions = {
                IconButton(onClick = onFavoriteClick) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                        tint = if (isFavorite) Color.Red else MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(onClick = onShareClick) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share Recipe"
                    )
                }
                IconButton(onClick = onEditClick) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Recipe"
                    )
                }
            }
        )

        // Recipe Header
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                // Recipe Stats
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    RecipeStatItem(
                        label = "Prep Time",
                        value = "${recipe.prepTime} min",
                        modifier = Modifier.weight(1f)
                    )
                    
                    RecipeStatItem(
                        label = "Cook Time",
                        value = "${recipe.cookTime} min",
                        modifier = Modifier.weight(1f)
                    )
                    
                    RecipeStatItem(
                        label = "Servings",
                        value = recipe.servings.toString(),
                        modifier = Modifier.weight(1f)
                    )
                }
                
                if (recipe.rating != null && recipe.rating > 0) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating",
                            modifier = Modifier.size(20.dp),
                            tint = Color(0xFFFFD700)
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            text = String.format("%.1f", recipe.rating),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        
                        Text(
                            text = " / 5.0",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }

        // Tab Row
        TabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontSize = 14.sp,
                            fontWeight = if (selectedTab == index) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        // Tab Content
        when (selectedTab) {
            0 -> IngredientsTab(
                ingredients = ingredients,
                modifier = Modifier.fillMaxSize()
            )
            1 -> InstructionsTab(
                instructions = deserializeInstructions(recipe.instructions),
                modifier = Modifier.fillMaxSize()
            )
            2 -> NutritionTab(
                nutritionInfo = deserializeNutritionInfo(recipe.nutritionInfo),
                servings = recipe.servings,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
fun RecipeStatItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun IngredientsTab(
    ingredients: List<Ingredient>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(ingredients) { ingredient ->
            IngredientItem(ingredient = ingredient)
        }
    }
}

@Composable
fun IngredientItem(
    ingredient: Ingredient,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = ingredient.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (ingredient.notes != null) {
                    Text(
                        text = ingredient.notes,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
            
            Text(
                text = "${ingredient.quantity} ${ingredient.unit}",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun InstructionsTab(
    instructions: List<String>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        itemsIndexed(instructions) { index, instruction ->
            InstructionStep(
                stepNumber = index + 1,
                instruction = instruction
            )
        }
    }
}

@Composable
fun InstructionStep(
    stepNumber: Int,
    instruction: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Step number
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = stepNumber.toString(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Instruction text
            Text(
                text = instruction,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 22.sp,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun NutritionTab(
    nutritionInfo: NutritionInfo,
    servings: Int,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Per Serving (${servings} servings total)",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        item {
            NutritionCard(
                title = "Calories",
                value = "${(nutritionInfo.calories / servings).toInt()}",
                unit = "kcal",
                color = MaterialTheme.colorScheme.primaryContainer
            )
        }
        
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NutritionCard(
                    title = "Carbs",
                    value = String.format("%.1f", nutritionInfo.carbohydrates / servings),
                    unit = "g",
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.weight(1f)
                )
                
                NutritionCard(
                    title = "Protein",
                    value = String.format("%.1f", nutritionInfo.proteins / servings),
                    unit = "g",
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    modifier = Modifier.weight(1f)
                )
                
                NutritionCard(
                    title = "Fat",
                    value = String.format("%.1f", nutritionInfo.fats / servings),
                    unit = "g",
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NutritionCard(
                    title = "Fiber",
                    value = String.format("%.1f", nutritionInfo.fiber / servings),
                    unit = "g",
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                
                NutritionCard(
                    title = "Sodium",
                    value = "${(nutritionInfo.sodium / servings).toInt()}",
                    unit = "mg",
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun NutritionCard(
    title: String,
    value: String,
    unit: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            
            Text(
                text = unit,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

// Helper functions for deserialization
private fun deserializeInstructions(serialized: String): List<String> {
    if (serialized.isEmpty()) return emptyList()
    return serialized.split("|||").map { it.split("::")[1] }
}

private fun deserializeNutritionInfo(serialized: String): NutritionInfo {
    if (serialized.isEmpty()) return NutritionInfo(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
    val parts = serialized.split(",")
    return NutritionInfo(
        calories = parts.getOrNull(0)?.toDoubleOrNull() ?: 0.0,
        carbohydrates = parts.getOrNull(1)?.toDoubleOrNull() ?: 0.0,
        proteins = parts.getOrNull(2)?.toDoubleOrNull() ?: 0.0,
        fats = parts.getOrNull(3)?.toDoubleOrNull() ?: 0.0,
        fiber = parts.getOrNull(4)?.toDoubleOrNull() ?: 0.0,
        sodium = parts.getOrNull(5)?.toDoubleOrNull() ?: 0.0,
        potassium = parts.getOrNull(6)?.toDoubleOrNull() ?: 0.0
    )
}

@Preview(showBackground = true)
@Composable
fun RecipeDetailScreenPreview() {
    MaterialTheme {
        RecipeDetailScreen(
            recipe = Recipe(
                id = "1",
                name = "Spaghetti Carbonara",
                prepTime = 15,
                cookTime = 20,
                servings = 4,
                instructions = "1::Cook pasta in salted water||2::Fry pancetta until crispy||3::Mix eggs with cheese",
                nutritionInfo = "450,55,18,15,3,800,300",
                sourceType = RecipeSource.MANUAL,
                rating = 4.5f,
                createdAt = "",
                updatedAt = ""
            ),
            ingredients = listOf(
                Ingredient("Spaghetti", 400.0, "g", IngredientCategory.GRAINS),
                Ingredient("Pancetta", 150.0, "g", IngredientCategory.PROTEIN),
                Ingredient("Eggs", 3.0, "large", IngredientCategory.PROTEIN),
                Ingredient("Parmesan cheese", 100.0, "g", IngredientCategory.DAIRY)
            ),
            onBackClick = {},
            onEditClick = {},
            onShareClick = {},
            onFavoriteClick = {},
            isFavorite = true
        )
    }
}