package com.beaconledger.welltrack.presentation.automation

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

// Smart text field with auto-suggestions
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    suggestions: List<String> = emptyList(),
    onSuggestionSelected: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Done,
    onImeAction: () -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = { newValue ->
                onValueChange(newValue)
                expanded = newValue.isNotEmpty() && suggestions.isNotEmpty()
            },
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = imeAction
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    keyboardController?.hide()
                    onImeAction()
                }
            ),
            trailingIcon = if (suggestions.isNotEmpty()) {
                {
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(
                            imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "Show suggestions"
                        )
                    }
                }
            } else null
        )
        
        if (expanded && suggestions.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column {
                    suggestions.take(5).forEach { suggestion ->
                        TextButton(
                            onClick = {
                                onSuggestionSelected(suggestion)
                                onValueChange(suggestion)
                                expanded = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = suggestion,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

// Auto-complete ingredient input
@Composable
fun IngredientAutoComplete(
    value: String,
    onValueChange: (String) -> Unit,
    onIngredientSelected: (Ingredient) -> Unit,
    modifier: Modifier = Modifier
) {
    val commonIngredients = remember {
        listOf(
            Ingredient("Chicken Breast", "protein", 165),
            Ingredient("Brown Rice", "carbs", 112),
            Ingredient("Broccoli", "vegetable", 34),
            Ingredient("Salmon", "protein", 208),
            Ingredient("Sweet Potato", "carbs", 86),
            Ingredient("Spinach", "vegetable", 23),
            Ingredient("Quinoa", "carbs", 120),
            Ingredient("Avocado", "fat", 160),
            Ingredient("Greek Yogurt", "protein", 100),
            Ingredient("Almonds", "fat", 164)
        )
    }
    
    val filteredIngredients = remember(value) {
        if (value.length >= 2) {
            commonIngredients.filter { 
                it.name.contains(value, ignoreCase = true) 
            }
        } else emptyList()
    }
    
    SmartTextField(
        value = value,
        onValueChange = onValueChange,
        label = "Ingredient",
        suggestions = filteredIngredients.map { it.name },
        onSuggestionSelected = { selectedName ->
            val ingredient = commonIngredients.find { it.name == selectedName }
            ingredient?.let { onIngredientSelected(it) }
        },
        modifier = modifier
    )
}

// Quick quantity selector with common portions
@Composable
fun QuickQuantitySelector(
    selectedQuantity: Float,
    onQuantityChange: (Float) -> Unit,
    unit: String = "g",
    modifier: Modifier = Modifier
) {
    val commonQuantities = listOf(50f, 100f, 150f, 200f, 250f, 300f)
    
    Column(modifier = modifier) {
        Text(
            text = "Quick Select ($unit)",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            commonQuantities.forEach { quantity ->
                FilterChip(
                    onClick = { onQuantityChange(quantity) },
                    label = { Text("${quantity.toInt()}") },
                    selected = selectedQuantity == quantity,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

// Smart meal time detector
@Composable
fun SmartMealTimeSelector(
    selectedMealType: MealType,
    onMealTypeChange: (MealType) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentHour = remember { java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY) }
    
    val suggestedMealType = remember(currentHour) {
        when (currentHour) {
            in 5..10 -> MealType.BREAKFAST
            in 11..14 -> MealType.LUNCH
            in 15..17 -> MealType.SNACK
            in 18..22 -> MealType.DINNER
            else -> MealType.SNACK
        }
    }
    
    LaunchedEffect(suggestedMealType) {
        if (selectedMealType == MealType.BREAKFAST) { // Default value
            onMealTypeChange(suggestedMealType)
        }
    }
    
    Column(modifier = modifier) {
        Text(
            text = "Meal Type",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            MealType.values().forEach { mealType ->
                FilterChip(
                    onClick = { onMealTypeChange(mealType) },
                    label = { Text(mealType.displayName) },
                    selected = selectedMealType == mealType,
                    leadingIcon = if (mealType == suggestedMealType && selectedMealType != mealType) {
                        {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Suggested",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    } else null
                )
            }
        }
        
        if (suggestedMealType != selectedMealType) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "💡 Based on current time, ${suggestedMealType.displayName} is suggested",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

// Auto-save indicator
@Composable
fun AutoSaveIndicator(
    isSaving: Boolean,
    lastSaved: String? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        when {
            isSaving -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(12.dp),
                    strokeWidth = 1.dp
                )
                Text(
                    text = "Saving...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            lastSaved != null -> {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Saved",
                    modifier = Modifier.size(12.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Saved $lastSaved",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// Smart form with auto-save
@Composable
fun SmartForm(
    onSave: () -> Unit,
    autoSaveDelay: Long = 2000L,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    var hasChanges by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var lastSaved by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(hasChanges) {
        if (hasChanges) {
            delay(autoSaveDelay)
            isSaving = true
            onSave()
            delay(500) // Simulate save time
            isSaving = false
            lastSaved = "just now"
            hasChanges = false
        }
    }
    
    Column(modifier = modifier) {
        content()
        
        Spacer(modifier = Modifier.height(16.dp))
        
        AutoSaveIndicator(
            isSaving = isSaving,
            lastSaved = lastSaved,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

// Enhanced automation features for better UX
@Composable
fun SmartFormValidation(
    fields: Map<String, String>,
    validationRules: Map<String, (String) -> String?>,
    onValidationChange: (Map<String, String?>) -> Unit
) {
    val validationErrors = remember(fields) {
        fields.mapValues { (key, value) ->
            validationRules[key]?.invoke(value)
        }
    }
    
    LaunchedEffect(validationErrors) {
        onValidationChange(validationErrors)
    }
}

// Predictive text input with learning
@Composable
fun PredictiveTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    predictions: List<String> = emptyList(),
    onPredictionUsed: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showPredictions by remember { mutableStateOf(false) }
    
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = { newValue ->
                onValueChange(newValue)
                showPredictions = newValue.length >= 2 && predictions.isNotEmpty()
            },
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = if (predictions.isNotEmpty()) {
                {
                    IconButton(onClick = { showPredictions = !showPredictions }) {
                        Icon(
                            imageVector = Icons.Default.EmojiObjects,
                            contentDescription = "Smart suggestions"
                        )
                    }
                }
            } else null
        )
        
        AnimatedVisibility(
            visible = showPredictions,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column {
                    predictions.take(3).forEach { prediction ->
                        TextButton(
                            onClick = {
                                onValueChange(prediction)
                                onPredictionUsed(prediction)
                                showPredictions = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(prediction)
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "AI suggestion",
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Context-aware input suggestions
@Composable
fun ContextAwareInput(
    value: String,
    onValueChange: (String) -> Unit,
    context: InputContext,
    modifier: Modifier = Modifier
) {
    val suggestions = remember(context, value) {
        when (context) {
            InputContext.MEAL_NAME -> getMealSuggestions(value)
            InputContext.INGREDIENT -> getIngredientSuggestions(value)
            InputContext.EXERCISE -> getExerciseSuggestions(value)
            InputContext.SUPPLEMENT -> getSupplementSuggestions(value)
        }
    }
    
    PredictiveTextField(
        value = value,
        onValueChange = onValueChange,
        label = context.label,
        predictions = suggestions,
        modifier = modifier
    )
}

// Smart batch operations
@Composable
fun SmartBatchActions(
    selectedItems: List<String>,
    availableActions: List<BatchAction>,
    onActionExecuted: (BatchAction, List<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = selectedItems.isNotEmpty(),
        enter = slideInVertically() + fadeIn(),
        exit = slideOutVertically() + fadeOut()
    ) {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${selectedItems.size} items selected",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    availableActions.forEach { action ->
                        FilledTonalButton(
                            onClick = { onActionExecuted(action, selectedItems) }
                        ) {
                            Icon(
                                imageVector = action.icon,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(action.label)
                        }
                    }
                }
            }
        }
    }
}

// Gesture-based shortcuts
@Composable
fun GestureShortcuts(
    onSwipeLeft: () -> Unit = {},
    onSwipeRight: () -> Unit = {},
    onDoubleTap: () -> Unit = {},
    onLongPress: () -> Unit = {},
    content: @Composable () -> Unit
) {
    // Implementation would use gesture detection
    // For now, just render content with gesture hints
    Box {
        content()
        
        // Gesture hints overlay (can be toggled)
        var showHints by remember { mutableStateOf(false) }
        
        if (showHints) {
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = "Gesture Shortcuts",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text("← Swipe left: Previous", style = MaterialTheme.typography.bodySmall)
                    Text("→ Swipe right: Next", style = MaterialTheme.typography.bodySmall)
                    Text("⚡ Double tap: Quick action", style = MaterialTheme.typography.bodySmall)
                    Text("⏳ Long press: Options", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

// Helper functions for suggestions
private fun getMealSuggestions(input: String): List<String> {
    val commonMeals = listOf(
        "Grilled Chicken Salad", "Quinoa Bowl", "Salmon with Vegetables",
        "Greek Yogurt Parfait", "Avocado Toast", "Protein Smoothie",
        "Stir-fry Vegetables", "Lentil Soup", "Turkey Sandwich"
    )
    return commonMeals.filter { it.contains(input, ignoreCase = true) }
}

private fun getIngredientSuggestions(input: String): List<String> {
    val commonIngredients = listOf(
        "Chicken Breast", "Brown Rice", "Broccoli", "Salmon", "Sweet Potato",
        "Spinach", "Quinoa", "Avocado", "Greek Yogurt", "Almonds",
        "Olive Oil", "Garlic", "Onion", "Tomatoes", "Bell Peppers"
    )
    return commonIngredients.filter { it.contains(input, ignoreCase = true) }
}

private fun getExerciseSuggestions(input: String): List<String> {
    val commonExercises = listOf(
        "Push-ups", "Squats", "Lunges", "Plank", "Burpees",
        "Running", "Cycling", "Swimming", "Yoga", "Weight Training"
    )
    return commonExercises.filter { it.contains(input, ignoreCase = true) }
}

private fun getSupplementSuggestions(input: String): List<String> {
    val commonSupplements = listOf(
        "Vitamin D3", "Omega-3", "Magnesium", "Zinc", "Vitamin B12",
        "Probiotics", "Protein Powder", "Creatine", "Multivitamin"
    )
    return commonSupplements.filter { it.contains(input, ignoreCase = true) }
}

// Data classes for automation
data class Ingredient(
    val name: String,
    val category: String,
    val caloriesPer100g: Int
)

enum class MealType(val displayName: String) {
    BREAKFAST("Breakfast"),
    LUNCH("Lunch"),
    DINNER("Dinner"),
    SNACK("Snack")
}

enum class InputContext(val label: String) {
    MEAL_NAME("Meal Name"),
    INGREDIENT("Ingredient"),
    EXERCISE("Exercise"),
    SUPPLEMENT("Supplement")
}

data class BatchAction(
    val id: String,
    val label: String,
    val icon: ImageVector
)