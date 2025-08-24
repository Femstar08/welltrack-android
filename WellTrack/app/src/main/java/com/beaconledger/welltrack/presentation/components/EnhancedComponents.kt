package com.beaconledger.welltrack.presentation.components

// TEMPORARILY DISABLED FOR PHASE 1 BUILD
// This file contains advanced components that need additional dependencies
// Will be re-enabled in Phase 2

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// Minimal stub implementations for Phase 1
@Composable
fun EnhancedCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier,
        content = { content() }
    )
}

@Composable
fun SmartButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
    ) {
        Text(text)
    }
}