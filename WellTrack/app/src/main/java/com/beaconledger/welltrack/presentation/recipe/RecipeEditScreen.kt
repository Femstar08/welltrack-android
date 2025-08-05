package com.beaconledger.welltrack.presentation.recipe

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beaconledger.welltrack.data.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeEditScreen(
    recipe: Recipe,
    ingredients: List<Ingredient>,
    onBackClick: () -> Unit,
    onSaveClick: (String, RecipeUpdateRequest) -> Unit,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    var recipeName by remember { mutableStateOf(recipe.name) }
    var description by remember { mutableStateOf("") }
    var prepTime by remember { mutableStateOf(recipe.prepTime.toString()) }
    var cookTime by remember { mutableStateOf(recipe.cookTime.toString()) }
    var servings by remember { mutableStateOf(recipe.servings.toString()) }
    var editableIngredients by remember { mutableStateOf(ingredients) }
    var steps by remember { mutableStateOf(deserializeSteps(recipe.instructions)) }
    var selectedTab by remember { mutableStateOf(0) }
    
    val tabs = listOf("Basic Info", "Ingredients", "Instructions")
    
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Top Bar
        TopAppBar(
            title = {
                Text(
                    text = "Edit Recipe",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
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
                Button(
                    onClick = {
                        val request = RecipeUpdateRequest(
                            name = recipeName.takeIf { it != recipe.name },
                            description = description.takeIf { it.isNotBlank() },
                            prepTime = prepTime.toIntOrNull()?.takeIf { it != recipe.prepTime },
                            cookTime = cookTime.toIntOrNull()?.takeIf { it != recipe.cookTime },
                            servings = servings.toIntOrNull()?.takeIf { it != recipe.servings },
                            ingredients = editableIngredients.takeIf { it != ingredients },
                            steps = steps.takeIf { it != deserializeSteps(recipe.instructions) },
                            tags = null,
                            difficulty = null,
                            cuisine = null,
                            rating = null
                        )
                        onSaveClick(recipe.id, request)
                    },
                    enabled = !isLoading && recipeName.isNotBlank() && editableIngredients.isNotEmpty() && steps.isNotEmpty(),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Save")
                    }
                }
            }
        )
        
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
            0 -> BasicInfoTab(
                recipeName = recipeName,
                onRecipeNameChange = { recipeName = it },
                description = description,
                onDescriptionChange = { description = it },
                prepTime = prepTime,
                onPrepTimeChange = { prepTime = it },
                cookTime = cookTime,
                onCookTimeChange = { cookTime = it },
                servings = servings,
                onServingsChange = { servings = it },
                modifier = Modifier.fillMaxSize()
            )
            1 -> IngredientsEditTab(
                ingredients = editableIngredients,
                onIngredientsChange = { editableIngredients = it },
                modifier = Modifier.fillMaxSize()
            )
            2 -> InstructionsEditTab(
                steps = steps,
                onStepsChange = { steps = it },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

// Helper function to deserialize steps from the stored format
private fun deserializeSteps(serialized: String): List<RecipeStep> {
    if (serialized.isEmpty()) return emptyList()
    return serialized.split("|||").mapNotNull { stepStr ->
        val parts = stepStr.split("::")
        if (parts.size >= 2) {
            RecipeStep(
                stepNumber = parts[0].toIntOrNull() ?: 1,
                instruction = parts[1],
                duration = parts.getOrNull(2)?.toIntOrNull(),
                temperature = parts.getOrNull(3)?.takeIf { it.isNotEmpty() },
                equipment = parts.getOrNull(4)?.split(",")?.filter { it.isNotEmpty() } ?: emptyList()
            )
        } else null
    }
}

@Preview(showBackground = true)
@Composable
fun RecipeEditScreenPreview() {
    MaterialTheme {
        RecipeEditScreen(
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
            onSaveClick = { _, _ -> },
            isLoading = false
        )
    }
}