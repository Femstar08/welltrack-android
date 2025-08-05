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
fun RecipeCreateScreen(
    onBackClick: () -> Unit,
    onSaveClick: (RecipeCreateRequest) -> Unit,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    var recipeName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var prepTime by remember { mutableStateOf("") }
    var cookTime by remember { mutableStateOf("") }
    var servings by remember { mutableStateOf("") }
    var ingredients by remember { mutableStateOf(listOf<Ingredient>()) }
    var steps by remember { mutableStateOf(listOf<RecipeStep>()) }
    var selectedTab by remember { mutableStateOf(0) }
    
    val tabs = listOf("Basic Info", "Ingredients", "Instructions")
    
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Top Bar
        TopAppBar(
            title = {
                Text(
                    text = "Create Recipe",
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
                        val request = RecipeCreateRequest(
                            name = recipeName,
                            description = description.takeIf { it.isNotBlank() },
                            prepTime = prepTime.toIntOrNull() ?: 0,
                            cookTime = cookTime.toIntOrNull() ?: 0,
                            servings = servings.toIntOrNull() ?: 1,
                            ingredients = ingredients,
                            steps = steps
                        )
                        onSaveClick(request)
                    },
                    enabled = !isLoading && recipeName.isNotBlank() && ingredients.isNotEmpty() && steps.isNotEmpty(),
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
                ingredients = ingredients,
                onIngredientsChange = { ingredients = it },
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

@Composable
fun BasicInfoTab(
    recipeName: String,
    onRecipeNameChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    prepTime: String,
    onPrepTimeChange: (String) -> Unit,
    cookTime: String,
    onCookTimeChange: (String) -> Unit,
    servings: String,
    onServingsChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            OutlinedTextField(
                value = recipeName,
                onValueChange = onRecipeNameChange,
                label = { Text("Recipe Name *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
        
        item {
            OutlinedTextField(
                value = description,
                onValueChange = onDescriptionChange,
                label = { Text("Description (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )
        }
        
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = prepTime,
                    onValueChange = onPrepTimeChange,
                    label = { Text("Prep Time (min)") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = cookTime,
                    onValueChange = onCookTimeChange,
                    label = { Text("Cook Time (min)") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = servings,
                    onValueChange = onServingsChange,
                    label = { Text("Servings") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }
        }
    }
}

@Composable
fun IngredientsEditTab(
    ingredients: List<Ingredient>,
    onIngredientsChange: (List<Ingredient>) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        // Add Ingredient Button
        Button(
            onClick = {
                val newIngredient = Ingredient(
                    name = "",
                    quantity = 0.0,
                    unit = "",
                    category = IngredientCategory.OTHER
                )
                onIngredientsChange(ingredients + newIngredient)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Ingredient")
        }
        
        // Ingredients List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(ingredients) { index, ingredient ->
                IngredientEditItem(
                    ingredient = ingredient,
                    onIngredientChange = { updatedIngredient ->
                        val updatedList = ingredients.toMutableList()
                        updatedList[index] = updatedIngredient
                        onIngredientsChange(updatedList)
                    },
                    onDeleteClick = {
                        val updatedList = ingredients.toMutableList()
                        updatedList.removeAt(index)
                        onIngredientsChange(updatedList)
                    }
                )
            }
        }
    }
}

@Composable
fun IngredientEditItem(
    ingredient: Ingredient,
    onIngredientChange: (Ingredient) -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ingredient",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete ingredient",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedTextField(
                value = ingredient.name,
                onValueChange = { onIngredientChange(ingredient.copy(name = it)) },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = if (ingredient.quantity == 0.0) "" else ingredient.quantity.toString(),
                    onValueChange = { 
                        onIngredientChange(ingredient.copy(quantity = it.toDoubleOrNull() ?: 0.0))
                    },
                    label = { Text("Quantity") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = ingredient.unit,
                    onValueChange = { onIngredientChange(ingredient.copy(unit = it)) },
                    label = { Text("Unit") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }
        }
    }
}

@Composable
fun InstructionsEditTab(
    steps: List<RecipeStep>,
    onStepsChange: (List<RecipeStep>) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        // Add Step Button
        Button(
            onClick = {
                val newStep = RecipeStep(
                    stepNumber = steps.size + 1,
                    instruction = ""
                )
                onStepsChange(steps + newStep)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Step")
        }
        
        // Steps List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(steps) { index, step ->
                StepEditItem(
                    step = step,
                    onStepChange = { updatedStep ->
                        val updatedList = steps.toMutableList()
                        updatedList[index] = updatedStep.copy(stepNumber = index + 1)
                        onStepsChange(updatedList)
                    },
                    onDeleteClick = {
                        val updatedList = steps.toMutableList()
                        updatedList.removeAt(index)
                        // Renumber steps
                        val renumberedList = updatedList.mapIndexed { i, s -> s.copy(stepNumber = i + 1) }
                        onStepsChange(renumberedList)
                    }
                )
            }
        }
    }
}

@Composable
fun StepEditItem(
    step: RecipeStep,
    onStepChange: (RecipeStep) -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Step ${step.stepNumber}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete step",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedTextField(
                value = step.instruction,
                onValueChange = { onStepChange(step.copy(instruction = it)) },
                label = { Text("Instruction") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RecipeCreateScreenPreview() {
    MaterialTheme {
        RecipeCreateScreen(
            onBackClick = {},
            onSaveClick = {},
            isLoading = false
        )
    }
}