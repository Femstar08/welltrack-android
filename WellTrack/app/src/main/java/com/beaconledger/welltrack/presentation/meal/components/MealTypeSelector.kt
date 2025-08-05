package com.beaconledger.welltrack.presentation.meal.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.beaconledger.welltrack.data.model.MealType

@Composable
fun MealTypeSelector(
    selectedMealType: MealType,
    onMealTypeSelected: (MealType) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Meal Type",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MealType.values().forEach { mealType ->
                if (mealType != MealType.SUPPLEMENT) { // Don't show supplement in meal logging
                    FilterChip(
                        selected = selectedMealType == mealType,
                        onClick = { onMealTypeSelected(mealType) },
                        label = {
                            Text(
                                text = mealType.name.lowercase().replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        modifier = Modifier.selectable(
                            selected = selectedMealType == mealType,
                            onClick = { onMealTypeSelected(mealType) },
                            role = Role.RadioButton
                        )
                    )
                }
            }
        }
    }
}