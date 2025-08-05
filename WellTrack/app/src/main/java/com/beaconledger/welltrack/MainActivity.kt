package com.beaconledger.welltrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.clickable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.beaconledger.welltrack.presentation.auth.WelcomeScreen
import com.beaconledger.welltrack.presentation.auth.SignInScreen
import com.beaconledger.welltrack.presentation.auth.SignUpScreen
import com.beaconledger.welltrack.presentation.auth.AuthViewModel
import com.beaconledger.welltrack.data.model.AuthState
import com.beaconledger.welltrack.presentation.profile.ProfileCreationScreen
import com.beaconledger.welltrack.presentation.profile.ProfileSwitchingScreen
import com.beaconledger.welltrack.presentation.profile.ProfileSelectorComponent
import com.beaconledger.welltrack.presentation.profile.ProfileManagementScreen
import com.beaconledger.welltrack.presentation.profile.ProfileSettingsScreen
import com.beaconledger.welltrack.presentation.recipe.RecipeListScreen
import com.beaconledger.welltrack.presentation.recipe.RecipeDetailScreen
import com.beaconledger.welltrack.data.model.UserProfile
import com.beaconledger.welltrack.data.model.ActivityLevel
import com.beaconledger.welltrack.data.model.Recipe
import com.beaconledger.welltrack.data.model.RecipeSource
import com.beaconledger.welltrack.data.model.Ingredient
import com.beaconledger.welltrack.data.model.IngredientCategory
import com.beaconledger.welltrack.ui.theme.WellTrackTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WellTrackTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    WellTrackApp()
                }
            }
        }
    }
}

@Composable
fun WellTrackApp() {
    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.authState.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    val isLoading by authViewModel.isLoading.collectAsState()
    val errorMessage by authViewModel.errorMessage.collectAsState()
    
    var currentScreen by remember { mutableStateOf("welcome") }
    var demoMode by remember { mutableStateOf(false) }
    
    // Handle authentication state changes
    LaunchedEffect(authState) {
        when (authState) {
            AuthState.AUTHENTICATED -> {
                if (currentUser != null) {
                    currentScreen = "dashboard"
                }
            }
            AuthState.UNAUTHENTICATED -> {
                currentScreen = "welcome"
            }
            AuthState.LOADING -> {
                // Show loading state
            }
            AuthState.ERROR -> {
                // Error is handled by errorMessage state
            }
        }
    }

    // Clear error message when navigating between screens
    LaunchedEffect(currentScreen) {
        authViewModel.clearError()
    }

    // Demo profiles for testing
    val demoProfiles =  remember {
        listOf(
            UserProfile(
                userId = "demo_1",
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
            UserProfile(
                userId = "demo_2",
                name = "Jane Smith",
                age = 28,
                height = 165f,
                weight = 60f,
                activityLevel = ActivityLevel.ACTIVE,
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
            UserProfile(
                userId = "demo_3",
                name = "Alex Johnson",
                age = 16,
                height = 170f,
                weight = 65f,
                activityLevel = ActivityLevel.VERY_ACTIVE,
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
            )
        )
    }
    
    var activeProfileId by remember { mutableStateOf("demo_1") }
    
    if (demoMode) {
        DemoApp(
            profiles = demoProfiles,
            activeProfileId = activeProfileId,
            onProfileSwitch = { profileId -> activeProfileId = profileId },
            onExitDemo = { demoMode = false }
        )
    } else {
        when (currentScreen) {
            "welcome" -> {
                WelcomeScreen(
                    onSignUpClick = { currentScreen = "signup" },
                    onSignInClick = { currentScreen = "signin" }
                )
            }
            "signin" -> {
                SignInScreen(
                    onBackClick = { currentScreen = "welcome" },
                    onSignInClick = { email, password ->
                        authViewModel.signIn(email, password)
                    },
                    onForgotPasswordClick = { /* TODO: Implement forgot password */ },
                    onSocialSignInClick = { provider ->
                        authViewModel.signInWithProvider(provider)
                    },
                    isLoading = isLoading,
                    errorMessage = errorMessage
                )
            }
            "signup" -> {
                SignUpScreen(
                    onBackClick = { currentScreen = "welcome" },
                    onSignUpClick = { name, email, password ->
                        authViewModel.signUp(name, email, password)
                    },
                    onSocialSignUpClick = { provider ->
                        authViewModel.signInWithProvider(provider)
                    },
                    onSignInClick = { currentScreen = "signin" },
                    isLoading = isLoading,
                    errorMessage = errorMessage
                )
            }
            "profile" -> {
                ProfileCreationScreen(
                    onProfileCreate = { request ->
                        demoMode = true
                    },
                    onSkip = {
                        demoMode = true
                    },
                    isLoading = false,
                    errorMessage = null
                )
            }
            "dashboard" -> {
                MainDashboard(
                    currentUser = currentUser,
                    onSignOut = {
                        authViewModel.signOut()
                    }
                )
            }
        }
    }
}

@Composable
fun DemoApp(
    profiles: List<UserProfile>,
    activeProfileId: String,
    onProfileSwitch: (String) -> Unit,
    onExitDemo: () -> Unit
) {
    var currentDemoScreen by remember { mutableStateOf("dashboard") }
    var showProfileSwitcher by remember { mutableStateOf(false) }
    var selectedRecipeId by remember { mutableStateOf<String?>(null) }
    
    val activeProfile = profiles.find { it.userId == activeProfileId }
    
    // Demo recipes for testing
    val demoRecipes = remember {
        listOf(
            Recipe(
                id = "recipe_1",
                name = "Spaghetti Carbonara",
                prepTime = 15,
                cookTime = 20,
                servings = 4,
                instructions = "1::Cook pasta in salted water until al dente|||2::Fry pancetta until crispy|||3::Mix eggs with parmesan cheese|||4::Combine pasta with pancetta and egg mixture",
                nutritionInfo = "450,55,18,15,3,800,300",
                sourceType = RecipeSource.MANUAL,
                rating = 4.5f,
                tags = "italian,pasta,quick",
                createdAt = "",
                updatedAt = ""
            ),
            Recipe(
                id = "recipe_2",
                name = "Chicken Tikka Masala",
                prepTime = 30,
                cookTime = 45,
                servings = 6,
                instructions = "1::Marinate chicken in yogurt and spices|||2::Grill chicken until cooked|||3::Prepare tomato-based sauce|||4::Simmer chicken in sauce",
                nutritionInfo = "380,25,35,18,4,900,450",
                sourceType = RecipeSource.URL_IMPORT,
                rating = 4.8f,
                tags = "indian,chicken,curry",
                createdAt = "",
                updatedAt = ""
            ),
            Recipe(
                id = "recipe_3",
                name = "Avocado Toast",
                prepTime = 5,
                cookTime = 5,
                servings = 2,
                instructions = "1::Toast bread slices|||2::Mash avocado with lime and salt|||3::Spread avocado on toast|||4::Top with tomatoes and seasoning",
                nutritionInfo = "280,25,8,18,12,350,400",
                sourceType = RecipeSource.OCR_SCAN,
                rating = 4.2f,
                tags = "healthy,breakfast,quick",
                createdAt = "",
                updatedAt = ""
            )
        )
    }

    val demoIngredients = remember {
        mapOf(
            "recipe_1" to listOf(
                Ingredient("Spaghetti", 400.0, "g", IngredientCategory.GRAINS),
                Ingredient("Pancetta", 150.0, "g", IngredientCategory.PROTEIN),
                Ingredient("Eggs", 3.0, "large", IngredientCategory.PROTEIN),
                Ingredient("Parmesan cheese", 100.0, "g", IngredientCategory.DAIRY),
                Ingredient("Black pepper", 1.0, "tsp", IngredientCategory.SPICES)
            ),
            "recipe_2" to listOf(
                Ingredient("Chicken breast", 800.0, "g", IngredientCategory.PROTEIN),
                Ingredient("Greek yogurt", 200.0, "ml", IngredientCategory.DAIRY),
                Ingredient("Tomato sauce", 400.0, "ml", IngredientCategory.VEGETABLES),
                Ingredient("Heavy cream", 150.0, "ml", IngredientCategory.DAIRY),
                Ingredient("Garam masala", 2.0, "tsp", IngredientCategory.SPICES),
                Ingredient("Ginger", 1.0, "tbsp", IngredientCategory.SPICES)
            ),
            "recipe_3" to listOf(
                Ingredient("Bread slices", 4.0, "slices", IngredientCategory.GRAINS),
                Ingredient("Avocado", 2.0, "large", IngredientCategory.FRUITS),
                Ingredient("Cherry tomatoes", 100.0, "g", IngredientCategory.VEGETABLES),
                Ingredient("Lime", 1.0, "whole", IngredientCategory.FRUITS),
                Ingredient("Salt", 0.5, "tsp", IngredientCategory.SPICES)
            )
        )
    }
    
    when (currentDemoScreen) {
        "dashboard" -> {
            DemoDashboard(
                activeProfile = activeProfile,
                hasMultipleProfiles = profiles.size > 1,
                onProfileSelectorClick = { showProfileSwitcher = true },
                onNavigateToProfileManagement = { currentDemoScreen = "profile_management" },
                onNavigateToProfileSettings = { currentDemoScreen = "profile_settings" },
                onNavigateToRecipes = { currentDemoScreen = "recipes" },
                onExitDemo = onExitDemo
            )
        }
        "profile_management" -> {
            ProfileManagementScreen(
                profiles = profiles,
                activeProfileId = activeProfileId,
                onBackClick = { currentDemoScreen = "dashboard" },
                onAddProfile = { /* Demo: Show message */ },
                onEditProfile = { currentDemoScreen = "profile_settings" },
                onDeleteProfile = { /* Demo: Show message */ },
                onSwitchProfile = { profileId ->
                    onProfileSwitch(profileId)
                    currentDemoScreen = "dashboard"
                }
            )
        }
        "profile_settings" -> {
            ProfileSettingsScreen(
                profile = activeProfile,
                onBackClick = { currentDemoScreen = "dashboard" },
                onUpdateProfile = { /* Demo: Show message */ },
                onPhotoUpdate = { /* Demo: Show message */ },
                isLoading = false,
                errorMessage = null
            )
        }
        "recipes" -> {
            RecipeListScreen(
                recipes = demoRecipes,
                onRecipeClick = { recipeId ->
                    selectedRecipeId = recipeId
                    currentDemoScreen = "recipe_detail"
                },
                onAddRecipeClick = { /* Demo: Show message */ },
                onSearchClick = { /* Demo: Show message */ },
                isLoading = false
            )
        }
        "recipe_detail" -> {
            selectedRecipeId?.let { recipeId ->
                val recipe = demoRecipes.find { it.id == recipeId }
                val ingredients = demoIngredients[recipeId] ?: emptyList()
                
                if (recipe != null) {
                    RecipeDetailScreen(
                        recipe = recipe,
                        ingredients = ingredients,
                        onBackClick = { currentDemoScreen = "recipes" },
                        onEditClick = { /* Demo: Show message */ },
                        onShareClick = { /* Demo: Show message */ },
                        onFavoriteClick = { /* Demo: Show message */ },
                        isFavorite = false
                    )
                }
            }
        }
    }
    
    // Profile Switcher Bottom Sheet
    if (showProfileSwitcher) {
        // Using a simple Card instead of ModalBottomSheet for compatibility
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background.copy(alpha = 0.5f)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Bottom
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                ) {
                    ProfileSwitchingScreen(
                        profiles = profiles,
                        activeProfileId = activeProfileId,
                        onProfileSwitch = { profileId ->
                            onProfileSwitch(profileId)
                            showProfileSwitcher = false
                        },
                        onAddProfile = { showProfileSwitcher = false },
                        onEditProfile = { 
                            currentDemoScreen = "profile_settings"
                            showProfileSwitcher = false 
                        },
                        onCloseSheet = { showProfileSwitcher = false }
                    )
                }
            }
        }
    }
}

@Composable
fun DemoDashboard(
    activeProfile: UserProfile?,
    hasMultipleProfiles: Boolean,
    onProfileSelectorClick: () -> Unit,
    onNavigateToProfileManagement: () -> Unit,
    onNavigateToProfileSettings: () -> Unit,
    onNavigateToRecipes: () -> Unit,
    onExitDemo: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header with Profile Selector
        ProfileSelectorComponent(
            activeProfile = activeProfile,
            hasMultipleProfiles = hasMultipleProfiles,
            onProfileSelectorClick = onProfileSelectorClick
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Demo Content
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "🎉 Demo Mode Active",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Welcome to WellTrack! You're now in demo mode where you can explore all the profile features we've built.",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Demo Actions
        Text(
            text = "Try These Features:",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Feature Cards
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DemoFeatureCard(
                title = "Switch Profiles",
                description = "Tap the profile selector above to switch between family members",
                onClick = onProfileSelectorClick
            )
            
            DemoFeatureCard(
                title = "Manage Profiles",
                description = "View and manage all family member profiles",
                onClick = onNavigateToProfileManagement
            )
            
            DemoFeatureCard(
                title = "Profile Settings",
                description = "Edit the current profile's information and preferences",
                onClick = onNavigateToProfileSettings
            )
            
            DemoFeatureCard(
                title = "View Recipes",
                description = "Browse and view recipe details with ingredients",
                onClick = onNavigateToRecipes
            )
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Exit Demo Button
        OutlinedButton(
            onClick = onExitDemo,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Exit Demo Mode")
        }
    }
}

@Composable
fun DemoFeatureCard(
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = description,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun MainDashboard(
    currentUser: com.beaconledger.welltrack.data.model.AuthUser?,
    onSignOut: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Welcome back!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                currentUser?.let { user ->
                    Text(
                        text = user.email,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                }
            }
            
            OutlinedButton(onClick = onSignOut) {
                Text("Sign Out")
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Authentication Success Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "🎉 Authentication Complete!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "You have successfully authenticated with Supabase! The real authentication system is now working.",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                currentUser?.let { user ->
                    Text(
                        text = "User ID: ${user.id}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Email Confirmed: ${if (user.emailConfirmed) "Yes" else "No"}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Next Steps
        Text(
            text = "Next Steps:",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
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
                    text = "1. Configure Supabase",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Update BuildConfig.kt with your actual Supabase URL and anon key",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "2. Set up Database Tables",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Create user profiles, recipes, and meals tables in Supabase",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "3. Continue Implementation",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Move on to the next tasks in your implementation plan",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}