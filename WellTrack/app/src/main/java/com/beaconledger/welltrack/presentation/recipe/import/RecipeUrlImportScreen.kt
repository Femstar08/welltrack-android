package com.beaconledger.welltrack.presentation.recipe.import

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeUrlImportScreen(
    onImportUrl: (String) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var url by remember { mutableStateOf("") }
    var isValidUrl by remember { mutableStateOf(false) }

    // Validate URL
    LaunchedEffect(url) {
        isValidUrl = isValidRecipeUrl(url)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Import from URL",
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Text(
                text = "Import Recipe from URL",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "Enter a URL from a recipe website to automatically import the recipe details.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // URL Input Field
            OutlinedTextField(
                value = url,
                onValueChange = { url = it },
                label = { Text("Recipe URL") },
                placeholder = { Text("https://example.com/recipe") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Uri,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (isValidUrl) {
                            onImportUrl(url)
                        }
                    }
                ),
                trailingIcon = {
                    Row {
                        if (url.isNotEmpty()) {
                            IconButton(
                                onClick = { url = "" }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear"
                                )
                            }
                        }
                        
                        IconButton(
                            onClick = {
                                // Simplified - just clear for now
                                url = ""
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Paste from clipboard"
                            )
                        }
                    }
                },
                isError = url.isNotEmpty() && !isValidUrl,
                supportingText = {
                    when {
                        url.isEmpty() -> Text("Enter a recipe URL to get started")
                        !isValidUrl -> Text("Please enter a valid URL")
                        else -> Text("URL looks good!")
                    }
                }
            )
            
            // Supported Sites Info
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Supported Sites",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• AllRecipes, Food Network, BBC Good Food\n" +
                                "• Serious Eats, Bon Appétit, Epicurious\n" +
                                "• Most recipe blogs with structured data\n" +
                                "• Sites using Recipe schema markup",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }
                
                Button(
                    onClick = { onImportUrl(url) },
                    enabled = isValidUrl,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Import Recipe")
                }
            }
        }
    }
}

private fun isValidRecipeUrl(url: String): Boolean {
    if (url.isBlank()) return false
    
    return try {
        val urlPattern = "^https?://[\\w\\-]+(\\.[\\w\\-]+)+([\\w\\-\\.,@?^=%&:/~\\+#]*[\\w\\-\\@?^=%&/~\\+#])?$".toRegex()
        urlPattern.matches(url)
    } catch (e: Exception) {
        false
    }
}