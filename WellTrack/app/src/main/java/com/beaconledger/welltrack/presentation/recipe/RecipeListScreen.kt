package com.beaconledger.welltrack.presentation.recipe

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beaconledger.welltrack.data.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeListScreen(
    recipes: List<Recipe>,
    onRecipeClick: (String) -> Unit,
    onAddRecipeClick: () -> Unit,
    onSearchClick: () -> Unit,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Top Bar
        TopAppBar(
            title = {
                Text(
                    text = "My Recipes",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            actions = {
                IconButton(onClick = onSearchClick) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search Recipes"
                    )
                }
                IconButton(onClick = onAddRecipeClick) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Recipe"
                    )
                }
            }
        )

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (recipes.isEmpty()) {
            EmptyRecipeState(
                onAddRecipeClick = onAddRecipeClick,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(recipes) { recipe ->
                    RecipeCard(
                        recipe = recipe,
                        onClick = { onRecipeClick(recipe.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun RecipeCard(
    recipe: Recipe,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Recipe Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = recipe.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Recipe Info
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${recipe.prepTime + recipe.cookTime} min",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        
                        Text(
                            text = " • ",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        
                        Text(
                            text = "${recipe.servings} servings",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
                
                // Rating
                if (recipe.rating != null && recipe.rating > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating",
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFFFFD700) // Gold color
                        )
                        
                        Spacer(modifier = Modifier.width(4.dp))
                        
                        Text(
                            text = String.format("%.1f", recipe.rating),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Recipe Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Time breakdown
                Row {
                    TimeChip(
                        label = "Prep",
                        time = recipe.prepTime,
                        color = MaterialTheme.colorScheme.primaryContainer
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    TimeChip(
                        label = "Cook",
                        time = recipe.cookTime,
                        color = MaterialTheme.colorScheme.secondaryContainer
                    )
                }
                
                // Source indicator
                RecipeSourceChip(source = recipe.sourceType)
            }
        }
    }
}

@Composable
fun TimeChip(
    label: String,
    time: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = "$label ${time}m",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun RecipeSourceChip(
    source: RecipeSource,
    modifier: Modifier = Modifier
) {
    val (text, color) = when (source) {
        RecipeSource.MANUAL -> "Manual" to MaterialTheme.colorScheme.tertiaryContainer
        RecipeSource.URL_IMPORT -> "Web" to MaterialTheme.colorScheme.primaryContainer
        RecipeSource.OCR_SCAN -> "Scanned" to MaterialTheme.colorScheme.secondaryContainer
    }
    
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onTertiaryContainer,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun EmptyRecipeState(
    onAddRecipeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🍳",
            fontSize = 64.sp
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "No Recipes Yet",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Start building your recipe collection by adding your first recipe",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onAddRecipeClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text("Add Your First Recipe")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RecipeListScreenPreview() {
    MaterialTheme {
        RecipeListScreen(
            recipes = listOf(
                Recipe(
                    id = "1",
                    name = "Spaghetti Carbonara",
                    prepTime = 15,
                    cookTime = 20,
                    servings = 4,
                    instructions = "Cook pasta...",
                    nutritionInfo = "",
                    sourceType = RecipeSource.MANUAL,
                    rating = 4.5f,
                    createdAt = "",
                    updatedAt = ""
                ),
                Recipe(
                    id = "2",
                    name = "Chicken Tikka Masala",
                    prepTime = 30,
                    cookTime = 45,
                    servings = 6,
                    instructions = "Marinate chicken...",
                    nutritionInfo = "",
                    sourceType = RecipeSource.URL_IMPORT,
                    rating = 4.8f,
                    createdAt = "",
                    updatedAt = ""
                )
            ),
            onRecipeClick = {},
            onAddRecipeClick = {},
            onSearchClick = {},
            isLoading = false
        )
    }
}