package com.beaconledger.welltrack.presentation.recipe.import

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.beaconledger.welltrack.data.recipe_import.ImportProgress
import com.beaconledger.welltrack.data.recipe_import.ImportSource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeImportProgressScreen(
    importProgress: ImportProgress,
    onRetry: () -> Unit,
    onCancel: () -> Unit,
    onContinue: () -> Unit,
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (importProgress) {
                is ImportProgress.Started -> {
                    ImportStartedContent(importProgress.message)
                }
                is ImportProgress.InProgress -> {
                    ImportInProgressContent(
                        percentage = importProgress.percentage,
                        message = importProgress.message
                    )
                }
                is ImportProgress.Success -> {
                    ImportSuccessContent(
                        importProgress = importProgress,
                        onContinue = onContinue
                    )
                }
                is ImportProgress.Failed -> {
                    ImportFailedContent(
                        error = importProgress.error,
                        onRetry = onRetry,
                        onCancel = onCancel
                    )
                }
            }
        }
    }
}

@Composable
private fun ImportStartedContent(message: String) {
    CircularProgressIndicator(
        modifier = Modifier.size(48.dp),
        color = MaterialTheme.colorScheme.primary
    )
    
    Text(
        text = "Starting Import",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold
    )
    
    Text(
        text = message,
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun ImportInProgressContent(
    percentage: Int,
    message: String
) {
    LinearProgressIndicator(
        progress = { percentage / 100f },
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp),
        color = MaterialTheme.colorScheme.primary,
        trackColor = MaterialTheme.colorScheme.surfaceVariant,
    )
    
    Text(
        text = "$percentage%",
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )
    
    Text(
        text = message,
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun ImportSuccessContent(
    importProgress: ImportProgress.Success,
    onContinue: () -> Unit
) {
    Icon(
        imageVector = Icons.Default.CheckCircle,
        contentDescription = "Success",
        modifier = Modifier.size(64.dp),
        tint = Color(0xFF4CAF50)
    )
    
    Text(
        text = "Import Successful!",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF4CAF50)
    )
    
    Text(
        text = "Recipe \"${importProgress.recipe.name}\" has been imported successfully.",
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    
    // Show validation warnings if any
    if (importProgress.validationResult.warnings.isNotEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.warningContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = "Warnings:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onWarningContainer
                )
                importProgress.validationResult.warnings.forEach { warning ->
                    Text(
                        text = "• ${warning.message}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onWarningContainer
                    )
                }
            }
        }
    }
    
    // Show suggestions if any
    if (importProgress.validationResult.suggestions.isNotEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = "Suggestions:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                importProgress.validationResult.suggestions.forEach { suggestion ->
                    Text(
                        text = "• $suggestion",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
    
    Button(
        onClick = onContinue,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Continue to Recipe")
    }
}

@Composable
private fun ImportFailedContent(
    error: String,
    onRetry: () -> Unit,
    onCancel: () -> Unit
) {
    Icon(
        imageVector = Icons.Default.Warning,
        contentDescription = "Error",
        modifier = Modifier.size(64.dp),
        tint = MaterialTheme.colorScheme.error
    )
    
    Text(
        text = "Import Failed",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.error
    )
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Text(
            text = error,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(12.dp),
            color = MaterialTheme.colorScheme.onErrorContainer
        )
    }
    
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
            onClick = onRetry,
            modifier = Modifier.weight(1f)
        ) {
            Text("Retry")
        }
    }
}

// Extension property for warning container color
private val ColorScheme.warningContainer: Color
    get() = Color(0xFFFFF3CD)

private val ColorScheme.onWarningContainer: Color
    get() = Color(0xFF856404)