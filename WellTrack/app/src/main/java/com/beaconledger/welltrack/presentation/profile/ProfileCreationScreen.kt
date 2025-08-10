package com.beaconledger.welltrack.presentation.profile

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beaconledger.welltrack.data.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileCreationScreen(
    onProfileCreate: (ProfileCreationRequest) -> Unit,
    onSkip: () -> Unit,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var selectedActivityLevel by remember { mutableStateOf(ActivityLevel.MODERATE) }
    var selectedFitnessGoals by remember { mutableStateOf(setOf<FitnessGoal>()) }
    var selectedDietaryRestrictions by remember { mutableStateOf(setOf<DietaryRestriction>()) }
    var allergies by remember { mutableStateOf("") }
    var profilePhotoUri by remember { mutableStateOf<Uri?>(null) }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(scrollState)
    ) {
        // Header
        Text(
            text = "Create Your Profile",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Text(
            text = "Help us personalize your wellness journey",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
        )

        // Profile Photo Section
        ProfilePhotoSelector(
            currentPhotoUri = profilePhotoUri,
            onPhotoSelected = { uri -> profilePhotoUri = uri },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Basic Information
        Text(
            text = "Basic Information",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Full Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = age,
                onValueChange = { age = it },
                label = { Text("Age") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                singleLine = true
            )

            OutlinedTextField(
                value = height,
                onValueChange = { height = it },
                label = { Text("Height (cm)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                singleLine = true
            )

            OutlinedTextField(
                value = weight,
                onValueChange = { weight = it },
                label = { Text("Weight (kg)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Activity Level
        Text(
            text = "Activity Level",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        ActivityLevel.values().forEach { level ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = selectedActivityLevel == level,
                        onClick = { selectedActivityLevel = level }
                    )
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selectedActivityLevel == level,
                    onClick = { selectedActivityLevel = level }
                )
                Column(modifier = Modifier.padding(start = 16.dp)) {
                    Text(
                        text = level.displayName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Fitness Goals
        Text(
            text = "Fitness Goals (Select all that apply)",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        FitnessGoal.values().forEach { goal ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = selectedFitnessGoals.contains(goal),
                    onCheckedChange = { checked ->
                        selectedFitnessGoals = if (checked) {
                            selectedFitnessGoals + goal
                        } else {
                            selectedFitnessGoals - goal
                        }
                    }
                )
                Text(
                    text = goal.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Dietary Restrictions
        Text(
            text = "Dietary Restrictions (Optional)",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        DietaryRestriction.values().forEach { restriction ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = selectedDietaryRestrictions.contains(restriction),
                    onCheckedChange = { checked ->
                        selectedDietaryRestrictions = if (checked) {
                            selectedDietaryRestrictions + restriction
                        } else {
                            selectedDietaryRestrictions - restriction
                        }
                    }
                )
                Text(
                    text = restriction.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Allergies
        OutlinedTextField(
            value = allergies,
            onValueChange = { allergies = it },
            label = { Text("Allergies (comma-separated)") },
            placeholder = { Text("e.g., nuts, shellfish, dairy") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3
        )

        // Error Message
        if (errorMessage != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Action Buttons
        Button(
            onClick = {
                val request = ProfileCreationRequest(
                    name = name,
                    age = age.toIntOrNull(),
                    height = height.toFloatOrNull(),
                    weight = weight.toFloatOrNull(),
                    activityLevel = selectedActivityLevel,
                    fitnessGoals = selectedFitnessGoals.toList(),
                    dietaryRestrictions = selectedDietaryRestrictions.toList(),
                    allergies = if (allergies.isBlank()) emptyList() else allergies.split(",").map { it.trim() },
                    profilePhotoUri = profilePhotoUri?.toString()
                )
                onProfileCreate(request)
            },
            enabled = !isLoading && name.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(
                    text = "Create Profile",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = onSkip,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Skip for now")
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}



@Preview(showBackground = true)
@Composable
fun ProfileCreationScreenPreview() {
    MaterialTheme {
        ProfileCreationScreen(
            onProfileCreate = {},
            onSkip = {},
            isLoading = false,
            errorMessage = null
        )
    }
}