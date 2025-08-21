package com.beaconledger.welltrack.presentation.ux

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UXOptimizationViewModel @Inject constructor() : ViewModel() {
    
    private val _navigationState = MutableStateFlow(NavigationState())
    val navigationState: StateFlow<NavigationState> = _navigationState.asStateFlow()
    
    private val _performanceMetrics = MutableStateFlow(PerformanceMetrics())
    val performanceMetrics: StateFlow<PerformanceMetrics> = _performanceMetrics.asStateFlow()
    
    private val _automationSettings = MutableStateFlow(AutomationSettings())
    val automationSettings: StateFlow<AutomationSettings> = _automationSettings.asStateFlow()
    
    fun updateNavigationState(newState: NavigationState) {
        _navigationState.value = newState
    }
    
    fun trackPerformanceMetric(metric: String, value: Long) {
        viewModelScope.launch {
            val current = _performanceMetrics.value
            _performanceMetrics.value = current.copy(
                metrics = current.metrics + (metric to value),
                lastUpdated = System.currentTimeMillis()
            )
        }
    }
    
    fun updateAutomationSettings(settings: AutomationSettings) {
        _automationSettings.value = settings
    }
}

data class NavigationState(
    val currentScreen: String = "dashboard",
    val navigationHistory: List<String> = emptyList(),
    val isTransitioning: Boolean = false,
    val quickActions: List<QuickAction> = emptyList()
)

data class PerformanceMetrics(
    val metrics: Map<String, Long> = emptyMap(),
    val lastUpdated: Long = 0L,
    val averageLoadTime: Long = 0L,
    val memoryUsage: Long = 0L
)

data class AutomationSettings(
    val autoSaveEnabled: Boolean = true,
    val smartSuggestionsEnabled: Boolean = true,
    val quickActionsEnabled: Boolean = true,
    val gestureNavigationEnabled: Boolean = true
)

data class QuickAction(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val action: () -> Unit
)

// Enhanced Navigation Manager
@Composable
fun EnhancedNavigationManager(
    viewModel: UXOptimizationViewModel = hiltViewModel(),
    content: @Composable (NavigationState) -> Unit
) {
    val navigationState by viewModel.navigationState.collectAsState()
    val context = LocalContext.current
    
    // Track navigation performance
    LaunchedEffect(navigationState.currentScreen) {
        val startTime = System.currentTimeMillis()
        delay(100) // Simulate navigation time
        val endTime = System.currentTimeMillis()
        viewModel.trackPerformanceMetric("navigation_time", endTime - startTime)
    }
    
    content(navigationState)
}

// Seamless Screen Transitions
@Composable
fun SeamlessTransition(
    targetState: String,
    modifier: Modifier = Modifier,
    content: @Composable AnimatedVisibilityScope.(String) -> Unit
) {
    AnimatedContent(
        targetState = targetState,
        modifier = modifier,
        transitionSpec = {
            slideInHorizontally(
                initialOffsetX = { width -> width },
                animationSpec = tween(300, easing = EaseInOutCubic)
            ) + fadeIn(
                animationSpec = tween(300)
            ) with slideOutHorizontally(
                targetOffsetX = { width -> -width },
                animationSpec = tween(300, easing = EaseInOutCubic)
            ) + fadeOut(
                animationSpec = tween(300)
            )
        },
        label = "screen_transition"
    ) { screen ->
        content(screen)
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
            initialOffsetY = { it },
            animationSpec = tween(300)
        ) + fadeIn(),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(300)
        ) + fadeOut()
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                quickActions.take(4).forEach { action ->
                    QuickActionButton(
                        action = action,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun QuickActionButton(
    action: QuickAction,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        FilledIconButton(
            onClick = action.action,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Icon(
                imageVector = action.icon,
                contentDescription = action.title
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = action.title,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            maxLines = 1
        )
    }
}

// Performance Monitor
@Composable
fun PerformanceMonitor(
    viewModel: UXOptimizationViewModel = hiltViewModel(),
    showMetrics: Boolean = false
) {
    val performanceMetrics by viewModel.performanceMetrics.collectAsState()
    
    if (showMetrics) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = "Performance Metrics",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                performanceMetrics.metrics.forEach { (key, value) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = key.replace("_", " ").capitalize(),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${value}ms",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (value < 100) Color.Green else if (value < 300) Color(0xFFFF9800) else Color.Red
                        )
                    }
                }
            }
        }
    }
}

// Gesture Navigation Helper
@Composable
fun GestureNavigationHelper(
    onSwipeLeft: () -> Unit = {},
    onSwipeRight: () -> Unit = {},
    onSwipeUp: () -> Unit = {},
    onSwipeDown: () -> Unit = {},
    content: @Composable () -> Unit
) {
    // Implementation would use gesture detection
    // For now, just render content
    content()
}

// Smart Loading States
@Composable
fun SmartLoadingState(
    isLoading: Boolean,
    loadingMessage: String = "Loading...",
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier) {
        content()
        
        AnimatedVisibility(
            visible = isLoading,
            enter = fadeIn() + scaleIn(initialScale = 0.8f),
            exit = fadeOut() + scaleOut(targetScale = 0.8f)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                )
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = loadingMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}