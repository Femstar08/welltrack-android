package com.beaconledger.welltrack.presentation.recipe.import

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.beaconledger.welltrack.data.recipe_import.ImportProgress

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeImportScreen(
    onRecipeImported: (String) -> Unit, // Recipe ID
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentStep by remember { mutableStateOf(ImportStep.METHOD_SELECTION) }
    
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { 
            // Handle gallery import
            currentStep = ImportStep.PROCESSING
        }
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        when (currentStep) {
            ImportStep.METHOD_SELECTION -> {
                ImportMethodSelectionScreen(
                    onUrlImportClick = { currentStep = ImportStep.URL_INPUT },
                    onCameraImportClick = { currentStep = ImportStep.CAMERA_CAPTURE },
                    onGalleryImportClick = { galleryLauncher.launch("image/*") },
                    onCancel = onCancel
                )
            }
            
            ImportStep.URL_INPUT -> {
                RecipeUrlImportScreen(
                    onImportUrl = { url -> 
                        // Handle URL import
                        currentStep = ImportStep.PROCESSING
                    },
                    onCancel = { currentStep = ImportStep.METHOD_SELECTION }
                )
            }
            
            ImportStep.CAMERA_CAPTURE -> {
                RecipeOcrCameraScreen(
                    onImageCaptured = { bitmap -> 
                        // Handle camera capture
                        currentStep = ImportStep.PROCESSING
                    },
                    onGalleryClick = { galleryLauncher.launch("image/*") },
                    onCancel = { currentStep = ImportStep.METHOD_SELECTION }
                )
            }
            
            ImportStep.PROCESSING -> {
                // Show a simple processing screen for now
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator()
                        Text("Processing recipe import...")
                        Button(
                            onClick = { currentStep = ImportStep.METHOD_SELECTION }
                        ) {
                            Text("Back")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ImportMethodSelectionScreen(
    onUrlImportClick: () -> Unit,
    onCameraImportClick: () -> Unit,
    onGalleryImportClick: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
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
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = "Import Recipe",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "Choose how you'd like to import your recipe",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // URL Import Option
            ImportOptionCard(
                icon = Icons.Default.Add,
                title = "From Website URL",
                description = "Import from recipe websites like AllRecipes, Food Network, and more",
                onClick = onUrlImportClick
            )
            
            // Camera Import Option
            ImportOptionCard(
                icon = Icons.Default.Add,
                title = "Scan Recipe Book",
                description = "Use your camera to scan recipes from cookbooks or printed pages",
                onClick = onCameraImportClick
            )
            
            // Gallery Import Option
            ImportOptionCard(
                icon = Icons.Default.Add,
                title = "From Photo",
                description = "Select a photo of a recipe from your gallery to scan",
                onClick = onGalleryImportClick
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancel")
            }
        }
    }
}

@Composable
private fun ImportOptionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

enum class ImportStep {
    METHOD_SELECTION,
    URL_INPUT,
    CAMERA_CAPTURE,
    PROCESSING
}