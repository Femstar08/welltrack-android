package com.beaconledger.welltrack.presentation.notifications

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.beaconledger.welltrack.data.model.NotificationEntity
import com.beaconledger.welltrack.data.model.NotificationType
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    onNavigateBack: () -> Unit,
    viewModel: NotificationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // Show error snackbar
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // Show snackbar
            viewModel.clearError()
        }
    }
    
    // Show success message snackbar
    uiState.message?.let { message ->
        LaunchedEffect(message) {
            // Show snackbar
            viewModel.clearMessage()
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            
            Text(
                text = "Notification Settings",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            IconButton(
                onClick = { viewModel.setupAllNotifications() }
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Setup All")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Notification Type Settings
                item {
                    NotificationTypeSettings(
                        preferences = uiState.preferences,
                        onMealRemindersChanged = viewModel::updateMealRemindersEnabled,
                        onSupplementRemindersChanged = viewModel::updateSupplementRemindersEnabled,
                        onPantryAlertsChanged = viewModel::updatePantryAlertsEnabled,
                        onMotivationalChanged = viewModel::updateMotivationalNotificationsEnabled,
                        onWaterRemindersChanged = viewModel::updateWaterRemindersEnabled,
                        onBloodTestRemindersChanged = viewModel::updateBloodTestRemindersEnabled,
                        onMealPrepRemindersChanged = viewModel::updateMealPrepRemindersEnabled
                    )
                }
                
                // Quiet Hours Settings
                item {
                    QuietHoursSettings(
                        preferences = uiState.preferences,
                        onQuietHoursChanged = viewModel::updateQuietHours
                    )
                }
                
                // Snooze Settings
                item {
                    SnoozeSettings(
                        preferences = uiState.preferences,
                        onSnoozeMinutesChanged = viewModel::updateSnoozeMinutes
                    )
                }
                
                // Active Notifications
                item {
                    ActiveNotificationsSection(
                        notifications = uiState.notifications,
                        onSnoozeNotification = viewModel::snoozeNotification,
                        onMarkComplete = viewModel::markNotificationComplete,
                        onDismissNotification = viewModel::dismissNotification
                    )
                }
                
                // Action Buttons
                item {
                    NotificationActionButtons(
                        onSetupAll = viewModel::setupAllNotifications,
                        onCancelAll = viewModel::cancelAllNotifications
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationTypeSettings(
    preferences: com.beaconledger.welltrack.data.model.NotificationPreferences,
    onMealRemindersChanged: (Boolean) -> Unit,
    onSupplementRemindersChanged: (Boolean) -> Unit,
    onPantryAlertsChanged: (Boolean) -> Unit,
    onMotivationalChanged: (Boolean) -> Unit,
    onWaterRemindersChanged: (Boolean) -> Unit,
    onBloodTestRemindersChanged: (Boolean) -> Unit,
    onMealPrepRemindersChanged: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Notification Types",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            NotificationToggleItem(
                title = "Meal Reminders",
                subtitle = "Get reminded about planned meals",
                icon = Icons.Default.Restaurant,
                checked = preferences.mealRemindersEnabled,
                onCheckedChange = onMealRemindersChanged
            )
            
            NotificationToggleItem(
                title = "Supplement Reminders",
                subtitle = "Never miss your supplements",
                icon = Icons.Default.Medication,
                checked = preferences.supplementRemindersEnabled,
                onCheckedChange = onSupplementRemindersChanged
            )
            
            NotificationToggleItem(
                title = "Pantry Expiry Alerts",
                subtitle = "Get notified when ingredients expire",
                icon = Icons.Default.Warning,
                checked = preferences.pantryAlertsEnabled,
                onCheckedChange = onPantryAlertsChanged
            )
            
            NotificationToggleItem(
                title = "Motivational Notifications",
                subtitle = "Stay motivated with health goal updates",
                icon = Icons.Default.TrendingUp,
                checked = preferences.motivationalNotificationsEnabled,
                onCheckedChange = onMotivationalChanged
            )
            
            NotificationToggleItem(
                title = "Water Reminders",
                subtitle = "Stay hydrated throughout the day",
                icon = Icons.Default.WaterDrop,
                checked = preferences.waterRemindersEnabled,
                onCheckedChange = onWaterRemindersChanged
            )
            
            NotificationToggleItem(
                title = "Blood Test Reminders",
                subtitle = "Remember scheduled blood tests",
                icon = Icons.Default.Bloodtype,
                checked = preferences.bloodTestRemindersEnabled,
                onCheckedChange = onBloodTestRemindersChanged
            )
            
            NotificationToggleItem(
                title = "Meal Prep Reminders",
                subtitle = "Get reminded about meal preparation",
                icon = Icons.Default.AccessTime,
                checked = preferences.mealPrepRemindersEnabled,
                onCheckedChange = onMealPrepRemindersChanged
            )
        }
    }
}

@Composable
private fun NotificationToggleItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun QuietHoursSettings(
    preferences: com.beaconledger.welltrack.data.model.NotificationPreferences,
    onQuietHoursChanged: (LocalTime?, LocalTime?) -> Unit
) {
    var showTimePicker by remember { mutableStateOf(false) }
    var isSelectingStartTime by remember { mutableStateOf(true) }
    
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Quiet Hours",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "No notifications will be sent during quiet hours",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = {
                        isSelectingStartTime = true
                        showTimePicker = true
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Start Time")
                        Text(
                            text = preferences.quietHoursStart ?: "Not set",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                OutlinedButton(
                    onClick = {
                        isSelectingStartTime = false
                        showTimePicker = true
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("End Time")
                        Text(
                            text = preferences.quietHoursEnd ?: "Not set",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
            
            if (preferences.quietHoursStart != null && preferences.quietHoursEnd != null) {
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = { onQuietHoursChanged(null, null) }
                ) {
                    Text("Clear Quiet Hours")
                }
            }
        }
    }
}

@Composable
private fun SnoozeSettings(
    preferences: com.beaconledger.welltrack.data.model.NotificationPreferences,
    onSnoozeMinutesChanged: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Snooze Duration",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Default snooze time")
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf(5, 10, 15, 30).forEach { minutes ->
                        FilterChip(
                            onClick = { onSnoozeMinutesChanged(minutes) },
                            label = { Text("${minutes}m") },
                            selected = preferences.snoozeMinutes == minutes,
                            modifier = Modifier.padding(horizontal = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActiveNotificationsSection(
    notifications: List<NotificationEntity>,
    onSnoozeNotification: (String, Int) -> Unit,
    onMarkComplete: (String, String) -> Unit,
    onDismissNotification: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Active Notifications",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (notifications.isEmpty()) {
                Text(
                    text = "No active notifications",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                notifications.forEach { notification ->
                    NotificationItem(
                        notification = notification,
                        onSnooze = { onSnoozeNotification(notification.id, 15) },
                        onMarkComplete = { onMarkComplete(notification.id, notification.id) },
                        onDismiss = { onDismissNotification(notification.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationItem(
    notification: NotificationEntity,
    onSnooze: () -> Unit,
    onMarkComplete: () -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = notification.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = notification.scheduledTime.format(DateTimeFormatter.ofPattern("MMM dd, HH:mm")),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Row {
                    if (notification.type == NotificationType.SUPPLEMENT_REMINDER) {
                        IconButton(onClick = onMarkComplete) {
                            Icon(Icons.Default.Check, contentDescription = "Mark Complete")
                        }
                    }
                    
                    IconButton(onClick = onSnooze) {
                        Icon(Icons.Default.Snooze, contentDescription = "Snooze")
                    }
                    
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Dismiss")
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationActionButtons(
    onSetupAll: () -> Unit,
    onCancelAll: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onSetupAll,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Setup All")
                }
                
                OutlinedButton(
                    onClick = onCancelAll,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Cancel, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cancel All")
                }
            }
        }
    }
}