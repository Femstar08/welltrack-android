package com.beaconledger.welltrack.presentation.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.beaconledger.welltrack.presentation.dashboard.DashboardScreen
import com.beaconledger.welltrack.presentation.meal.MealScreen
import com.beaconledger.welltrack.presentation.recipe.RecipeScreen
import com.beaconledger.welltrack.presentation.mealplan.MealPlanScreen
import com.beaconledger.welltrack.presentation.pantry.PantryScreen
import com.beaconledger.welltrack.presentation.shoppinglist.ShoppingListScreen
import com.beaconledger.welltrack.presentation.supplements.SupplementScreen
import com.beaconledger.welltrack.presentation.health.HealthConnectScreen
import com.beaconledger.welltrack.presentation.analytics.AnalyticsScreen
import com.beaconledger.welltrack.presentation.profile.ProfileScreen

sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector = icon
) {
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Outlined.Home, Icons.Filled.Home)
    object Meals : Screen("meals", "Meals", Icons.Outlined.Restaurant, Icons.Filled.Restaurant)
    object Recipes : Screen("recipes", "Recipes", Icons.Outlined.MenuBook, Icons.Filled.MenuBook)
    object MealPlan : Screen("meal_plan", "Meal Plan", Icons.Outlined.CalendarMonth, Icons.Filled.CalendarMonth)
    object Pantry : Screen("pantry", "Pantry", Icons.Outlined.Kitchen, Icons.Filled.Kitchen)
    object Shopping : Screen("shopping", "Shopping", Icons.Outlined.ShoppingCart, Icons.Filled.ShoppingCart)
    object Supplements : Screen("supplements", "Supplements", Icons.Outlined.Medication, Icons.Filled.Medication)
    object Health : Screen("health", "Health", Icons.Outlined.MonitorHeart, Icons.Filled.MonitorHeart)
    object Analytics : Screen("analytics", "Analytics", Icons.Outlined.Analytics, Icons.Filled.Analytics)
    object Profile : Screen("profile", "Profile", Icons.Outlined.Person, Icons.Filled.Person)
}

val bottomNavItems = listOf(
    Screen.Dashboard,
    Screen.Meals,
    Screen.MealPlan,
    Screen.Health,
    Screen.Analytics
)

val allScreens = listOf(
    Screen.Dashboard,
    Screen.Meals,
    Screen.Recipes,
    Screen.MealPlan,
    Screen.Pantry,
    Screen.Shopping,
    Screen.Supplements,
    Screen.Health,
    Screen.Analytics,
    Screen.Profile
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WellTrackNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    // Enhanced navigation state management
    val currentScreen = remember(currentDestination) {
        allScreens.find { it.route == currentDestination?.route } ?: Screen.Dashboard
    }
    
    // Quick actions based on current screen
    val quickActions = remember(currentScreen) {
        when (currentScreen) {
            Screen.Dashboard -> listOf(
                QuickAction("log_meal", "Log Meal", Icons.Default.Restaurant) { 
                    navController.navigate(Screen.Meals.route) 
                },
                QuickAction("add_recipe", "Add Recipe", Icons.Default.MenuBook) { 
                    navController.navigate(Screen.Recipes.route) 
                }
            )
            Screen.Meals -> listOf(
                QuickAction("camera_meal", "Camera", Icons.Default.CameraEnhance) { /* Camera action */ },
                QuickAction("quick_log", "Quick Log", Icons.Default.Speed) { /* Quick log */ }
            )
            else -> emptyList()
        }
    }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            EnhancedBottomNavigation(
                currentScreen = currentScreen,
                onNavigate = { screen ->
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        },
        floatingActionButton = {
            if (quickActions.isNotEmpty()) {
                EnhancedFAB(
                    mainAction = quickActions.first(),
                    subActions = quickActions.drop(1)
                )
            }
        }
    ) { paddingValues ->
        // Enhanced navigation with performance optimizations
        EnhancedNavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(paddingValues),
            quickActions = quickActions
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    onNavigateToMealPlanner = { navController.navigate(Screen.MealPlan.route) },
                    onNavigateToRecipes = { navController.navigate(Screen.Recipes.route) },
                    onNavigateToHealth = { navController.navigate(Screen.Health.route) },
                    onNavigateToProfileCreation = { navController.navigate(Screen.Profile.route) },
                    onNavigateToProfileManagement = { navController.navigate(Screen.Profile.route) }
                )
            }
            
            composable(Screen.Meals.route) {
                MealScreen(
                    onNavigateToRecipes = { navController.navigate(Screen.Recipes.route) },
                    onNavigateToMealPlan = { navController.navigate(Screen.MealPlan.route) }
                )
            }
            
            composable(Screen.Recipes.route) {
                RecipeScreen(
                    onNavigateToMeals = { navController.navigate(Screen.Meals.route) },
                    onNavigateToPantry = { navController.navigate(Screen.Pantry.route) }
                )
            }
            
            composable(Screen.MealPlan.route) {
                MealPlanScreen(
                    onNavigateToRecipe = { navController.navigate(Screen.Recipes.route) },
                    onNavigateToShopping = { navController.navigate(Screen.Shopping.route) }
                )
            }
            
            composable(Screen.Pantry.route) {
                PantryScreen(
                    onNavigateToShopping = { navController.navigate(Screen.Shopping.route) },
                    onNavigateToRecipes = { navController.navigate(Screen.Recipes.route) }
                )
            }
            
            composable(Screen.Shopping.route) {
                ShoppingListScreen(
                    onNavigateToPantry = { navController.navigate(Screen.Pantry.route) },
                    onNavigateToMealPlan = { navController.navigate(Screen.MealPlan.route) }
                )
            }
            
            composable(Screen.Supplements.route) {
                SupplementScreen(
                    onNavigateToHealth = { navController.navigate(Screen.Health.route) }
                )
            }
            
            composable(Screen.Health.route) {
                HealthConnectScreen(
                    onNavigateToSupplements = { navController.navigate(Screen.Supplements.route) },
                    onNavigateToAnalytics = { navController.navigate(Screen.Analytics.route) }
                )
            }
            
            composable(Screen.Analytics.route) {
                AnalyticsScreen(
                    onNavigateToHealth = { navController.navigate(Screen.Health.route) },
                    onNavigateToMeals = { navController.navigate(Screen.Meals.route) }
                )
            }
            
            composable(Screen.Profile.route) {
                ProfileScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}

// Enhanced Bottom Navigation with animations
@Composable
fun EnhancedBottomNavigation(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        bottomNavItems.forEach { screen ->
            val isSelected = currentScreen == screen
            
            NavigationBarItem(
                icon = {
                    AnimatedContent(
                        targetState = isSelected,
                        transitionSpec = {
                            scaleIn(animationSpec = tween(200)) + fadeIn() with
                            scaleOut(animationSpec = tween(200)) + fadeOut()
                        },
                        label = "nav_icon"
                    ) { selected ->
                        Icon(
                            imageVector = if (selected) screen.selectedIcon else screen.icon,
                            contentDescription = screen.title,
                            modifier = Modifier.size(if (selected) 26.dp else 24.dp)
                        )
                    }
                },
                label = { 
                    AnimatedVisibility(
                        visible = isSelected,
                        enter = fadeIn() + slideInVertically(),
                        exit = fadeOut() + slideOutVertically()
                    ) {
                        Text(
                            text = screen.title,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                selected = isSelected,
                onClick = { onNavigate(screen) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

// Enhanced NavHost with performance optimizations
@Composable
fun EnhancedNavHost(
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier = Modifier,
    quickActions: List<QuickAction> = emptyList(),
    content: NavGraphBuilder.() -> Unit
) {
    Box(modifier = modifier) {
        NavHost(
            navController = navController,
            startDestination = startDestination,
            enterTransition = {
                val isForward = when {
                    initialState.destination.route == Screen.Dashboard.route -> true
                    targetState.destination.route == Screen.Dashboard.route -> false
                    else -> true
                }
                
                if (isForward) {
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(350, easing = EaseOutCubic)
                    ) + fadeIn(animationSpec = tween(350))
                } else {
                    slideInHorizontally(
                        initialOffsetX = { -it },
                        animationSpec = tween(350, easing = EaseOutCubic)
                    ) + fadeIn(animationSpec = tween(350))
                }
            },
            exitTransition = {
                val isForward = when {
                    initialState.destination.route == Screen.Dashboard.route -> true
                    targetState.destination.route == Screen.Dashboard.route -> false
                    else -> true
                }
                
                if (isForward) {
                    slideOutHorizontally(
                        targetOffsetX = { -it },
                        animationSpec = tween(350, easing = EaseOutCubic)
                    ) + fadeOut(animationSpec = tween(350))
                } else {
                    slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(350, easing = EaseOutCubic)
                    ) + fadeOut(animationSpec = tween(350))
                }
            },
            content = content
        )
        
        // Quick actions overlay
        if (quickActions.isNotEmpty()) {
            SmartQuickActionsBar(
                quickActions = quickActions,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp)
            )
        }
    }
}

// Data classes for enhanced navigation
data class QuickAction(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val action: () -> Unit
)

// Enhanced FAB implementation
@Composable
fun EnhancedFAB(
    mainAction: QuickAction,
    subActions: List<QuickAction> = emptyList(),
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    
    Box(modifier = modifier) {
        // Sub actions
        subActions.forEachIndexed { index, action ->
            val offset by animateDpAsState(
                targetValue = if (isExpanded) (index + 1) * 72.dp else 0.dp,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "fab_offset_$index"
            )
            
            AnimatedVisibility(
                visible = isExpanded,
                enter = scaleIn(
                    animationSpec = tween(300, delayMillis = index * 50)
                ) + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                SmallFloatingActionButton(
                    onClick = {
                        action.action()
                        isExpanded = false
                    },
                    modifier = Modifier.offset(y = -offset),
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ) {
                    Icon(
                        imageVector = action.icon,
                        contentDescription = action.title
                    )
                }
            }
        }
        
        // Main FAB
        FloatingActionButton(
            onClick = {
                if (subActions.isNotEmpty()) {
                    isExpanded = !isExpanded
                } else {
                    mainAction.action()
                }
            },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            AnimatedContent(
                targetState = if (subActions.isNotEmpty() && isExpanded) Icons.Default.Close else mainAction.icon,
                transitionSpec = {
                    (scaleIn() + fadeIn()) with (scaleOut() + fadeOut())
                },
                label = "fab_icon"
            ) { icon ->
                Icon(
                    imageVector = icon,
                    contentDescription = if (isExpanded) "Close" else mainAction.title
                )
            }
        }
    }
}

// Smart Quick Actions Bar
@Composable
fun SmartQuickActionsBar(
    quickActions: List<QuickAction>,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = quickActions.isNotEmpty(),
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = tween(300)
        ) + fadeIn(),
        exit = slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = tween(300)
        ) + fadeOut()
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                quickActions.take(3).forEach { action ->
                    QuickActionChip(
                        action = action,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun QuickActionChip(
    action: QuickAction,
    modifier: Modifier = Modifier
) {
    FilterChip(
        onClick = action.action,
        label = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = action.icon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = action.title,
                    fontSize = 12.sp,
                    maxLines = 1
                )
            }
        },
        selected = false,
        modifier = modifier,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surface,
            labelColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

@Composable
fun NavigationDrawer(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    ModalDrawerSheet(
        modifier = modifier.width(280.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "WellTrack",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            
            allScreens.forEach { screen ->
                NavigationDrawerItem(
                    icon = {
                        Icon(
                            imageVector = if (currentScreen == screen) screen.selectedIcon else screen.icon,
                            contentDescription = screen.title
                        )
                    },
                    label = { Text(screen.title) },
                    selected = currentScreen == screen,
                    onClick = { onNavigate(screen) },
                    modifier = Modifier.padding(vertical = 4.dp),
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }
    }
}