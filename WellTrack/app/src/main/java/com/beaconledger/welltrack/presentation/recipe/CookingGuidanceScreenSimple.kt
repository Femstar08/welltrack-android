package com.beaconledger.welltrack.presentation.recipe

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun CookingGuidanceScreenSimple(
    recipeId: String,
    targetServings: Int = 1,
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Cooking Guidance",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text("Recipe ID: $recipeId")
        Text("Target Servings: $targetServings")
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(onClick = onNavigateBack) {
            Text("Back to Recipe")
        }
    }
}