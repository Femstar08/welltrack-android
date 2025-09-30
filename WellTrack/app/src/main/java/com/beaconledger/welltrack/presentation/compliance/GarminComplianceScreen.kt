package com.beaconledger.welltrack.presentation.compliance

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.beaconledger.welltrack.R
import com.beaconledger.welltrack.data.compliance.ComplianceCheck
import com.beaconledger.welltrack.data.compliance.GarminComplianceResult
import com.beaconledger.welltrack.presentation.components.GarminAttributionComponents
import com.beaconledger.welltrack.presentation.components.WorksWithGarminBadge

/**
 * Screen for validating and displaying Garmin brand compliance status
 * Shows compliance with Garmin Connect Developer Program requirements
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GarminComplianceScreen(
    onNavigateBack: () -> Unit,
    viewModel: GarminComplianceViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    LaunchedEffect(Unit) {
        viewModel.validateCompliance()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Garmin Compliance",
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.validateCompliance() }
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Overall Compliance Status
            item {
                ComplianceOverviewCard(
                    complianceResult = uiState.complianceResult,
                    isLoading = uiState.isLoading
                )
            }
            
            // Works with Garmin Badge Demo
            item {
                GarminBadgeDemoCard()
            }
            
            // Attribution Examples
            item {
                AttributionExamplesCard()
            }
            
            // Individual Compliance Checks
            if (uiState.complianceResult != null) {
                items(uiState.complianceResult.checks) { check ->
                    ComplianceCheckCard(check = check)
                }
            }
            
            // Legal Requirements
            item {
                LegalRequirementsCard(
                    onOpenGarminPrivacy = { viewModel.openGarminPrivacyPolicy() },
                    onOpenDeveloperDocs = { viewModel.openGarminDeveloperDocs() }
                )
            }
            
            // Trademark Acknowledgments
            item {
                TrademarkAcknowledgmentsCard()
            }
        }
    }
}

@Composable
private fun ComplianceOverviewCard(
    complianceResult: GarminComplianceResult?,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isLoading -> MaterialTheme.colorScheme.surfaceVariant
                complianceResult?.isCompliant == true -> Color.Green.copy(alpha = 0.1f)
                complianceResult?.isCompliant == false -> Color.Red.copy(alpha = 0.1f)
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isLoading) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Validating Garmin compliance...",
                    style = MaterialTheme.typography.bodyMedium
                )
            } else if (complianceResult != null) {
                Icon(
                    imageVector = if (complianceResult.isCompliant) {
                        Icons.Default.CheckCircle
                    } else {
                        Icons.Default.Warning
                    },
                    contentDescription = null,
                    tint = if (complianceResult.isCompliant) Color.Green else Color.Red,
                    modifier = Modifier.size(48.dp)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = if (complianceResult.isCompliant) {
                        "Compliance Validated"
                    } else {
                        "Compliance Issues Found"
                    },
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = if (complianceResult.isCompliant) Color.Green else Color.Red
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = complianceResult.summary,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Compliance Statistics
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val passedChecks = complianceResult.checks.count { it.isCompliant }
                    val totalChecks = complianceResult.checks.size
                    
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$passedChecks/$totalChecks",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "Checks Passed",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                    
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${(passedChecks * 100 / totalChecks)}%",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "Compliance",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GarminBadgeDemoCard() {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Works with Garmin Badge",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Required badge display for Garmin Connect integration:",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    WorksWithGarminBadge(size = com.beaconledger.welltrack.presentation.components.BadgeSize.SMALL)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Small",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    WorksWithGarminBadge(size = com.beaconledger.welltrack.presentation.components.BadgeSize.MEDIUM)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Medium",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    WorksWithGarminBadge(size = com.beaconledger.welltrack.presentation.components.BadgeSize.LARGE)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Large",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

@Composable
private fun AttributionExamplesCard() {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Attribution Examples",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "All Garmin data displays must include proper attribution:",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Example attribution displays
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AttributionExample(
                    title = "Primary Display",
                    attribution = "Garmin Forerunner 945"
                )
                
                AttributionExample(
                    title = "Secondary Display", 
                    attribution = "Garmin"
                )
                
                AttributionExample(
                    title = "Combined Data",
                    attribution = "Data sources: Garmin Forerunner 945, Samsung Health"
                )
                
                AttributionExample(
                    title = "Export Format",
                    attribution = "Data provided by Garmin Forerunner 945"
                )
            }
        }
    }
}

@Composable
private fun AttributionExample(
    title: String,
    attribution: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = attribution,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ComplianceCheckCard(check: ComplianceCheck) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (check.isCompliant) {
                Color.Green.copy(alpha = 0.05f)
            } else {
                Color.Red.copy(alpha = 0.05f)
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = if (check.isCompliant) {
                    Icons.Default.CheckCircle
                } else {
                    Icons.Default.Error
                },
                contentDescription = null,
                tint = if (check.isCompliant) Color.Green else Color.Red,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = check.requirement,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = check.description,
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = check.details,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun LegalRequirementsCard(
    onOpenGarminPrivacy: () -> Unit,
    onOpenDeveloperDocs: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Legal Requirements",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Review important legal and compliance documentation:",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onOpenGarminPrivacy,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.OpenInNew, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Garmin Privacy Policy")
                }
                
                OutlinedButton(
                    onClick = onOpenDeveloperDocs,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Code, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Developer Program Docs")
                }
            }
        }
    }
}

@Composable
private fun TrademarkAcknowledgmentsCard() {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Trademark Acknowledgments",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            val trademarks = listOf(
                stringResource(R.string.garmin_trademark_notice),
                stringResource(R.string.garmin_connect_trademark),
                stringResource(R.string.connect_iq_trademark),
                stringResource(R.string.garmin_not_affiliated)
            )
            
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                trademarks.forEach { trademark ->
                    Text(
                        text = "• $trademark",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}