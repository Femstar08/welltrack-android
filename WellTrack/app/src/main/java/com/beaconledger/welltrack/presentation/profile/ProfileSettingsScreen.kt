package com.beaconledger.welltrack.presentation.profile

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
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
fun ProfileSettingsScreen(
    profile: UserProfile?,
    onBackClick: () -> Unit,
    onUpdateProfile: (ProfileUpdateRequest) -> Unit,
    onPhotoUpdate: () -> Unit,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf(profile?.name ?: "") }
    var age by remember { mutableStateOf(profile?.age?.toString() ?: "") }
    var height by remember { mutableStateOf(profile?.height?.toString() ?: "") }
    var weight by remember { mutableStateOf(profile?.weight?.toString() ?: "") }
    var selectedActivityLevel by remember { mutableStateOf(profile?.activityLevel ?: ActivityLevel.MODERATE) }
    var profilePhotoUri by remember { mutableStateOf<android.net.Uri?>(profile?.profilePhotoUrl?.let { android.net.Uri.parse(it) }) }
    
    var selectedFitnessGoals by remember { 
        mutableStateOf(
            profile?.fitnessGoals?.split(",")?.mapNotNull { goalStr ->
                try {
                    FitnessGoal.valueOf(goalStr.trim())
                } catch (e: IllegalArgumentException) {
                    null
                }
            }?.toSet() ?: emptySet()
        )
    }
    
    var selectedDietaryRestrictions by remember { 
        mutableStateOf(
            profile?.dietaryRestrictions?.split(",")?.mapNotNull { restrictionStr ->
                try {
                    DietaryRestriction.valueOf(restrictionStr.trim())
                } catch (e: IllegalArgumentException) {
                    null
                }
            }?.toSet() ?: emptySet()
        )
    }
    
    var allergies by remember { mutableStateOf(profile?.allergies ?: "") }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(scrollState)
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back"
                )
            }
            
            Text(
                text = "Profile Settings",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Profile Photo Section
        ProfilePhotoSelector(
            currentPhotoUri = profilePhotoUri,
            onPhotoSelected = { uri -> 
                profilePhotoUri = uri
                onPhotoUpdate()
            },
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

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                ActivityLevel.values().forEach { level ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
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
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Fitness Goals
        Text(
            text = "Fitness Goals",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 16.dp)
        )



        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
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
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Dietary Restrictions
        Text(
            text = "Dietary Restrictions",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 16.dp)
        )



        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
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

        Spacer(modifier = Modifier.height(32.dp))

        // Health Metrics Summary (Read-only for now)
        Text(
            text = "Health Summary",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                if (profile?.height != null && profile.weight != null) {
                    val bmi = profile.weight / ((profile.height / 100) * (profile.height / 100))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("BMI:")
                        Text(
                            text = String.format("%.1f", bmi),
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Activity Level:")
                    Text(
                        text = selectedActivityLevel.displayName.split(" ")[0],
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

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

        // Save Button
        Button(
            onClick = {
                val request = ProfileUpdateRequest(
                    name = if (name != profile?.name) name else null,
                    age = age.toIntOrNull().takeIf { it != profile?.age },
                    height = height.toFloatOrNull().takeIf { it != profile?.height },
                    weight = weight.toFloatOrNull().takeIf { it != profile?.weight },
                    activityLevel = selectedActivityLevel.takeIf { it != profile?.activityLevel },
                    fitnessGoals = selectedFitnessGoals.toList().takeIf { 
                        it.map { it.name } != profile?.fitnessGoals?.split(",")?.map { it.trim() } 
                    },
                    dietaryRestrictions = selectedDietaryRestrictions.toList().takeIf { 
                        it.map { it.name } != profile?.dietaryRestrictions?.split(",")?.map { it.trim() } 
                    },
                    allergies = if (allergies != profile?.allergies) allergies.split(",").map { it.trim() }.filter { it.isNotBlank() } else null,
                    preferredIngredients = null,
                    dislikedIngredients = null,
                    cuisinePreferences = null,
                    cookingMethods = null
                )
                onUpdateProfile(request)
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
                    text = "Save Changes",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileSettingsScreenPreview() {
    MaterialTheme {
        ProfileSettingsScreen(
            profile = UserProfile(
                userId = "1",
                name = "John Doe",
                age = 30,
                height = 175f,
                weight = 70f,
                activityLevel = ActivityLevel.MODERATE,
                fitnessGoals = "",
                dietaryRestrictions = "",
                allergies = "",
                preferredIngredients = "",
                dislikedIngredients = "",
                cuisinePreferences = "",
                cookingMethods = "",
                notificationSettings = "",
                createdAt = "",
                updatedAt = ""
            ),
            onBackClick = {},
            onUpdateProfile = {},
            onPhotoUpdate = {},
            isLoading = false,
            errorMessage = null
        )
    }
}