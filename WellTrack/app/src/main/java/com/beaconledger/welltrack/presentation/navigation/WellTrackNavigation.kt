package com.beaconledger.welltrack.presentation.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.beaconledger.welltrack.presentation.dashboard.DashboardScreen
import com.beaconledger.welltrack.presentation.security.SecuritySettingsScreen
import com.beaconledger.welltrack.presentation.security.SecurityAuditScreen
import com.beaconledger.welltrack.presentation.security.AppLockScreen
import com.beaconledger.welltrack.presentation.goals.GoalScreen
import com.beaconledger.welltrack.presentation.goals.GoalDetailScreen

// Navigation routes
object Routes {
    const val DASHBOARD = "dashboard"
    const val SECURITY_SETTINGS = "security_settings"
    const val SECURITY_AUDIT = "security_audit"
    const val APP_LOCK = "app_lock"
    const val MEALS = "meals"
    const val RECIPES = "recipes"
    const val HEALTH = "health"
    const val ANALYTICS = "analytics"
    const val GOALS = "goals"
    const val GOAL_DETAIL = "goal_detail/{goalId}"
}

@Composable
fun WellTrackNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = Routes.DASHBOARD
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Routes.DASHBOARD) {
            DashboardScreen(
                onNavigateToSecurity = {
                    navController.navigate(Routes.SECURITY_SETTINGS)
                }
            )
        }
        
        composable(Routes.SECURITY_SETTINGS) {
            SecuritySettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Routes.SECURITY_AUDIT) {
            SecurityAuditScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Routes.APP_LOCK) {
            AppLockScreen(
                onUnlocked = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.APP_LOCK) { inclusive = true }
                    }
                }
            )
        }
        
        // Placeholder screens for other features
        composable(Routes.MEALS) {
            PlaceholderScreen("Meals", navController)
        }
        
        composable(Routes.RECIPES) {
            PlaceholderScreen("Recipes", navController)
        }
        
        composable(Routes.HEALTH) {
            PlaceholderScreen("Health", navController)
        }
        
        composable(Routes.ANALYTICS) {
            PlaceholderScreen("Analytics", navController)
        }
        
        composable(Routes.GOALS) {
            GoalScreen(
                onNavigateToGoalDetail = { goalId ->
                    navController.navigate("goal_detail/$goalId")
                }
            )
        }
        
        composable(Routes.GOAL_DETAIL) { backStackEntry ->
            val goalId = backStackEntry.arguments?.getString("goalId") ?: ""
            GoalDetailScreen(
                goalId = goalId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

@Composable
private fun PlaceholderScreen(
    title: String,
    navController: NavHostController
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(start = 8.dp, top = 8.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "$title Feature",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "This feature is currently under development. Security features are now fully implemented and ready for testing.",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = { navController.navigate(Routes.SECURITY_SETTINGS) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Security, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Test Security Features")
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(modifier = modifier) {
        NavigationBarItem(
            selected = currentRoute == Routes.DASHBOARD,
            onClick = { onNavigate(Routes.DASHBOARD) },
            icon = { Icon(Icons.Default.Dashboard, contentDescription = null) },
            label = { Text("Dashboard") }
        )
        
        NavigationBarItem(
            selected = currentRoute == Routes.MEALS,
            onClick = { onNavigate(Routes.MEALS) },
            icon = { Icon(Icons.Default.Restaurant, contentDescription = null) },
            label = { Text("Meals") }
        )
        
        NavigationBarItem(
            selected = currentRoute == Routes.RECIPES,
            onClick = { onNavigate(Routes.RECIPES) },
            icon = { Icon(Icons.Default.MenuBook, contentDescription = null) },
            label = { Text("Recipes") }
        )
        
        NavigationBarItem(
            selected = currentRoute == Routes.HEALTH,
            onClick = { onNavigate(Routes.HEALTH) },
            icon = { Icon(Icons.Default.Favorite, contentDescription = null) },
            label = { Text("Health") }
        )
        
        NavigationBarItem(
            selected = currentRoute == Routes.GOALS,
            onClick = { onNavigate(Routes.GOALS) },
            icon = { Icon(Icons.Default.Flag, contentDescription = null) },
            label = { Text("Goals") }
        )
    }
}

@Composable
fun getCurrentRoute(navController: NavHostController): String? {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    return navBackStackEntry?.destination?.route
}