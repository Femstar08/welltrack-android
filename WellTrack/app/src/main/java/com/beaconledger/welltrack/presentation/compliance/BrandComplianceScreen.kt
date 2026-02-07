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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.beaconledger.welltrack.R
import com.beaconledger.welltrack.data.compliance.BrandComplianceCheck
import com.beaconledger.welltrack.data.compliance.BrandComplianceSeverity
import com.beaconledger.welltrack.presentation.components.*

/**
 * Brand compliance validation screen
 * 
 * Displays comprehensive brand compliance status for all third-party integrations
 * including Garmin, Samsung Health, Google Health Connect, and app store requirements
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrandComplianceScreen(
    onNavigateBack: () -> Unit,
    viewModel: BrandComplianceViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.validateBrandCompliance()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = stringResource(R.string.brand_compliance_title),
                        fontWeight = FontWeight.SemiBold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.navigate_back)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.validateBrandCompliance() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.refresh_compliance)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Loading state
            if (uiState.isLoading) {
                item {
                    ComplianceLoadingCard()
                }
            }
            
            // Compliance summary
            uiState.complianceResult?.let { result ->
                item {
                    ComplianceSummaryCard(
                        result = result,
                        onRunValidation = { viewModel.validateBrandCompliance() }
                    )
                }
                
                // Health platform badges demonstration
                item {
                    HealthPlatformBadgesCard()
                }
                
                // Individual compliance checks
                items(result.checks) { check ->
                    ComplianceCheckCard(
                        check = check,
                        onExpandDetails = { viewModel.toggleCheckDetails(check.requirement) },
                        isExpanded = uiState.expandedChecks.contains(check.requirement)
                    )
                }
                
                // Recommendations
                if (result.recommendations.isNotEmpty()) {
                    item {
                        ComplianceRecommendationsCard(
                            recommendations = result.recommendations
                        )
                    }
                }
            }
            
            // Error state
            uiState.error?.let { error ->
                item {
                    ComplianceErrorCard(
                        error = error,
                        onRetry = { viewModel.validateBrandCompliance() }
                    )
                }
            }
        }
    }
}

@Composable
private fun ComplianceLoadingCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator()
            Text(
                text = stringResource(R.string.validating_brand_compliance),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ComplianceSummaryCard(
    result: com.beaconledger.welltrack.data.compliance.BrandComplianceResult,
    onRunValidation: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (result.isCompliant) {
                Color(0xFFE8F5E8)
            } else {
                Color(0xFFFFEBEE)
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (result.isCompliant) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (result.isCompliant) Color(0xFF4CAF50) else Color(0xFFF44336)
                    )
                    Text(
                        text = stringResource(
                            if (result.isCompliant) R.string.compliance_status_compliant 
                            else R.string.compliance_status_non_compliant
                        ),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (result.isCompliant) Color(0xFF2E7D32) else Color(0xFFD32F2F)
                    )
                }
                
                OutlinedButton(
                    onClick = onRunValidation
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.revalidate))
                }
            }
            
            Text(
                text = result.summary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (result.hasCriticalIssues) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFCDD2)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = Color(0xFFD32F2F)
                        )
                        Text(
                            text = stringResource(R.string.critical_issues_warning),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFD32F2F)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HealthPlatformBadgesCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.health_platform_badges_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            Text(
                text = stringResource(R.string.health_platform_badges_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // Display all platform badges
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Garmin badge
                WorksWithGarminBadge(size = GarminBadgeSize.Medium)
                
                // Samsung Health badge
                WorksWithSamsungHealthBadge(size = HealthPlatformBadgeSize.Medium)
                
                // Health Connect badge
                WorksWithHealthConnectBadge(size = HealthPlatformBadgeSize.Medium)
            }
        }
    }
}

@Composable
private fun ComplianceCheckCard(
    check: BrandComplianceCheck,
    onExpandDetails: () -> Unit,
    isExpanded: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        onClick = onExpandDetails
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (check.isCompliant) Icons.Default.CheckCircle else Icons.Default.Error,
                        contentDescription = null,
                        tint = if (check.isCompliant) Color(0xFF4CAF50) else getSeverityColor(check.severity),
                        modifier = Modifier.size(20.dp)
                    )
                    
                    Column {
                        Text(
                            text = check.requirement,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = check.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    SeverityChip(severity = check.severity)
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            if (isExpanded) {
                Divider()
                
                Text(
                    text = stringResource(R.string.compliance_check_details),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                Text(
                    text = check.details,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                check.remediation?.let { remediation ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.recommended_action),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = remediation,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SeverityChip(severity: BrandComplianceSeverity) {
    val (color, text) = when (severity) {
        BrandComplianceSeverity.CRITICAL -> Color(0xFFD32F2F) to stringResource(R.string.severity_critical)
        BrandComplianceSeverity.HIGH -> Color(0xFFFF9800) to stringResource(R.string.severity_high)
        BrandComplianceSeverity.MEDIUM -> Color(0xFFFFC107) to stringResource(R.string.severity_medium)
        BrandComplianceSeverity.LOW -> Color(0xFF4CAF50) to stringResource(R.string.severity_low)
    }
    
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.1f),
        contentColor = color
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ComplianceRecommendationsCard(
    recommendations: List<String>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFF3E0)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = Color(0xFFFF9800)
                )
                Text(
                    text = stringResource(R.string.compliance_recommendations),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            recommendations.forEach { recommendation ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowRight,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = recommendation,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ComplianceErrorCard(
    error: String,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFEBEE)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = null,
                    tint = Color(0xFFD32F2F)
                )
                Text(
                    text = stringResource(R.string.compliance_validation_error),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFD32F2F)
                )
            }
            
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD32F2F)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(stringResource(R.string.retry_validation))
            }
        }
    }
}

private fun getSeverityColor(severity: BrandComplianceSeverity): Color {
    return when (severity) {
        BrandComplianceSeverity.CRITICAL -> Color(0xFFD32F2F)
        BrandComplianceSeverity.HIGH -> Color(0xFFFF9800)
        BrandComplianceSeverity.MEDIUM -> Color(0xFFFFC107)
        BrandComplianceSeverity.LOW -> Color(0xFF4CAF50)
    }
}