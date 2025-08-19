package com.beaconledger.welltrack.presentation.biomarkers

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
import com.beaconledger.welltrack.data.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BiomarkerScreen(
    modifier: Modifier = Modifier,
    viewModel: BiomarkerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = { Text("Biomarker Tracking") },
            actions = {
                IconButton(onClick = { viewModel.showCreateReminderDialog() }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Reminder")
                }
            }
        )
        
        // Tab Row
        TabRow(
            selectedTabIndex = uiState.selectedTab.ordinal
        ) {
            BiomarkerTab.values().forEach { tab ->
                Tab(
                    selected = uiState.selectedTab == tab,
                    onClick = { viewModel.selectTab(tab) },
                    text = { Text(tab.name.replace("_", " ")) }
                )
            }
        }
        
        // Content based on selected tab
        when (uiState.selectedTab) {
            BiomarkerTab.REMINDERS -> RemindersTab(
                reminders = uiState.reminders,
                overdueReminders = uiState.overdueReminders,
                onSkipReminder = viewModel::skipReminder,
                onCompleteReminder = viewModel::markReminderCompleted,
                onDeleteReminder = viewModel::deleteReminder,
                onAddEntry = { testType -> viewModel.showBulkEntryDialog(testType) }
            )
            BiomarkerTab.ENTRIES -> EntriesTab(
                entries = uiState.biomarkerEntries,
                onAddEntry = { viewModel.showAddEntryDialog() }
            )
            BiomarkerTab.TRENDS -> TrendsTab(
                entries = uiState.biomarkerEntries
            )
            BiomarkerTab.INSIGHTS -> InsightsTab(
                entries = uiState.biomarkerEntries
            )
        }
    }
    
    // Dialogs
    if (uiState.showCreateReminderDialog) {
        CreateReminderDialog(
            onDismiss = { viewModel.hideCreateReminderDialog() },
            onCreateReminder = { testType, name, description, frequency, dueDate ->
                viewModel.createReminder(testType, name, description, frequency, dueDate)
                viewModel.hideCreateReminderDialog()
            },
            getDefaultFrequency = viewModel::getDefaultReminderFrequency
        )
    }
    
    if (uiState.showAddEntryDialog) {
        AddBiomarkerEntryDialog(
            testType = uiState.selectedTestType,
            onDismiss = { viewModel.hideAddEntryDialog() },
            onSaveEntry = { testType, biomarkerType, value, unit, testDate, notes, labName, refMin, refMax ->
                viewModel.saveBiomarkerEntry(testType, biomarkerType, value, unit, testDate, notes, labName, refMin, refMax)
                viewModel.hideAddEntryDialog()
            },
            getBiomarkersForTestType = viewModel::getBiomarkersForTestType,
            getBiomarkerReference = viewModel::getBiomarkerReference,
            validateValue = viewModel::validateBiomarkerValue
        )
    }
    
    if (uiState.showBulkEntryDialog && uiState.selectedTestType != null) {
        BulkBiomarkerEntryDialog(
            testType = uiState.selectedTestType!!,
            onDismiss = { viewModel.hideBulkEntryDialog() },
            onSaveEntries = { testType, testDate, entries, labName, notes ->
                viewModel.saveBulkBiomarkerEntries(testType, testDate, entries, labName, notes)
                viewModel.hideBulkEntryDialog()
            },
            getBiomarkersForTestType = viewModel::getBiomarkersForTestType,
            getBiomarkerReference = viewModel::getBiomarkerReference,
            validateValue = viewModel::validateBiomarkerValue
        )
    }
    
    // Error handling
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // Show snackbar or handle error
            viewModel.clearError()
        }
    }
    
    // Loading indicator
    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}

@Composable
private fun RemindersTab(
    reminders: List<BloodTestReminder>,
    overdueReminders: List<BloodTestReminderWithStatus>,
    onSkipReminder: (String) -> Unit,
    onCompleteReminder: (String) -> Unit,
    onDeleteReminder: (String) -> Unit,
    onAddEntry: (BloodTestType) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Overdue reminders section
        if (overdueReminders.isNotEmpty()) {
            item {
                Text(
                    text = "Overdue Tests",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            }
            
            items(overdueReminders) { reminderWithStatus ->
                OverdueReminderCard(
                    reminderWithStatus = reminderWithStatus,
                    onSkip = { onSkipReminder(reminderWithStatus.reminder.id) },
                    onComplete = { onCompleteReminder(reminderWithStatus.reminder.id) },
                    onAddEntry = { onAddEntry(reminderWithStatus.reminder.testType) }
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
        
        // Active reminders section
        item {
            Text(
                text = "Upcoming Tests",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
        
        items(reminders) { reminder ->
            ReminderCard(
                reminder = reminder,
                onSkip = { onSkipReminder(reminder.id) },
                onComplete = { onCompleteReminder(reminder.id) },
                onDelete = { onDeleteReminder(reminder.id) },
                onAddEntry = { onAddEntry(reminder.testType) }
            )
        }
    }
}

@Composable
private fun EntriesTab(
    entries: List<BiomarkerEntry>,
    onAddEntry: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Add entry button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.End
        ) {
            FloatingActionButton(
                onClick = onAddEntry,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Entry")
            }
        }
        
        // Entries list
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(entries) { entry ->
                BiomarkerEntryCard(entry = entry)
            }
        }
    }
}

@Composable
private fun TrendsTab(
    entries: List<BiomarkerEntry>
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Biomarker Trends",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
        
        // Group entries by biomarker type and show trends
        val groupedEntries = entries.groupBy { it.biomarkerType }
        
        items(groupedEntries.keys.toList()) { biomarkerType ->
            val biomarkerEntries = groupedEntries[biomarkerType] ?: emptyList()
            if (biomarkerEntries.size >= 2) {
                BiomarkerTrendCard(
                    biomarkerType = biomarkerType,
                    entries = biomarkerEntries.sortedBy { it.testDate }
                )
            }
        }
    }
}

@Composable
private fun InsightsTab(
    entries: List<BiomarkerEntry>
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Health Insights",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
        
        item {
            InsightsSummaryCard(entries = entries)
        }
        
        // Add more insight cards as needed
    }
}