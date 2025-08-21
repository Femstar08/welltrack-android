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
import com.beaconledger.welltrack.presentation.mealplan.MealPlanScreen
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
    
    // Handle authentication state changes
    LaunchedEffect(authState) {
        when (authState) {
            AuthState.AUTHENTICATED -> {
                if (currentUser != null) {
                    // TODO: Check if user has a profile, for now go to profile creation
                    currentScreen = "profile"
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
                    // TODO: Save profile to database
                    // For now, go to dashboard
                    currentScreen = "dashboard"
                },
                onSkip = {
                    // Allow users to skip profile creation and go to dashboard
                    currentScreen = "dashboard"
                },
                isLoading = false,
                errorMessage = null
            )
        }
        "dashboard" -> {
            AuthenticatedApp(
                currentUser = currentUser,
                onSignOut = {
                    authViewModel.signOut()
                }
            )
        }
    }
}



@Composable
fun FeatureCard(
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
fun AuthenticatedApp(
    currentUser: com.beaconledger.welltrack.data.model.AuthUser?,
    onSignOut: () -> Unit
) {
    // Use the new navigation system
    com.beaconledger.welltrack.presentation.navigation.WellTrackNavigation()
}

@Composable
fun MainDashboard(
    currentUser: com.beaconledger.welltrack.data.model.AuthUser?,
    onSignOut: () -> Unit,
    onNavigateToMealPlan: () -> Unit,
    onNavigateToRecipes: () -> Unit
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
        
        // App Features
        Text(
            text = "WellTrack Features:",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Feature Cards
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FeatureCard(
                title = "Meal Planning",
                description = "Create weekly meal plans with calendar view and automated generation",
                onClick = onNavigateToMealPlan
            )
            
            FeatureCard(
                title = "Recipe Management",
                description = "Browse recipes, add new ones, and get step-by-step cooking guidance",
                onClick = onNavigateToRecipes
            )
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "More Features Coming Soon",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "Shopping lists, pantry management, health tracking, and more!",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}