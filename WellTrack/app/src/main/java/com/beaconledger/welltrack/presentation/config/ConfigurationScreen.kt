package com.beaconledger.welltrack.presentation.config

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigurationScreen(
    viewModel: ConfigurationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.validateConfiguration()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "App Configuration",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Configuration Status",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                ConfigurationStatusItem(
                    title = "Overall Status",
                    isValid = uiState.configStatus?.isValid == true,
                    description = if (uiState.configStatus?.isValid == true) {
                        "All required configurations are valid"
                    } else {
                        "Some configurations need attention"
                    }
                )

                ConfigurationStatusItem(
                    title = "Security System",
                    isValid = uiState.securityStatus?.isSecure == true,
                    description = if (uiState.securityStatus?.isSecure == true) {
                        "Security systems are functioning properly"
                    } else {
                        "Security system issues detected"
                    }
                )
            }
        }

        // Configuration Summary
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Configuration Summary",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                uiState.configSummary.forEach { (key, value) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = key,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = value,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (value.contains("true") || value.contains("Configured")) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
            }
        }

        // Issues and Warnings
        if (uiState.configStatus?.missingRequiredKeys?.isNotEmpty() == true ||
            uiState.configStatus?.missingOptionalKeys?.isNotEmpty() == true ||
            uiState.securityStatus?.securityIssues?.isNotEmpty() == true
        ) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Issues & Recommendations",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    LazyColumn {
                        // Required configuration issues
                        uiState.configStatus?.missingRequiredKeys?.let { issues ->
                            items(issues) { issue ->
                                IssueItem(
                                    title = "Missing Required Configuration",
                                    description = issue,
                                    severity = IssueSeverity.ERROR
                                )
                            }
                        }

                        // Optional configuration warnings
                        uiState.configStatus?.missingOptionalKeys?.let { warnings ->
                            items(warnings) { warning ->
                                IssueItem(
                                    title = "Optional Configuration",
                                    description = warning,
                                    severity = IssueSeverity.WARNING
                                )
                            }
                        }

                        // Security issues
                        uiState.securityStatus?.securityIssues?.let { securityIssues ->
                            items(securityIssues) { issue ->
                                IssueItem(
                                    title = "Security Issue",
                                    description = issue,
                                    severity = IssueSeverity.ERROR
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { viewModel.validateConfiguration() },
                modifier = Modifier.weight(1f)
            ) {
                Text("Refresh")
            }

            Button(
                onClick = { viewModel.openConfigurationGuide() },
                modifier = Modifier.weight(1f)
            ) {
                Text("Setup Guide")
            }
        }
    }
}

@Composable
private fun ConfigurationStatusItem(
    title: String,
    isValid: Boolean,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isValid) Icons.Default.CheckCircle else Icons.Default.Error,
            contentDescription = null,
            tint = if (isValid) Color.Green else MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(end = 12.dp)
        )

        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun IssueItem(
    title: String,
    description: String,
    severity: IssueSeverity
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = when (severity) {
                IssueSeverity.ERROR -> Icons.Default.Error
                IssueSeverity.WARNING -> Icons.Default.Warning
            },
            contentDescription = null,
            tint = when (severity) {
                IssueSeverity.ERROR -> MaterialTheme.colorScheme.error
                IssueSeverity.WARNING -> MaterialTheme.colorScheme.tertiary
            },
            modifier = Modifier.padding(end = 8.dp, top = 2.dp)
        )

        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private enum class IssueSeverity {
    ERROR, WARNING
}