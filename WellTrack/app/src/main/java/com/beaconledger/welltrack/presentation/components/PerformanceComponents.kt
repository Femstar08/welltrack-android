package com.beaconledger.welltrack.presentation.components

// TEMPORARILY DISABLED FOR PHASE 1 BUILD

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

// Minimal stub implementations for Phase 1
@Composable
fun OptimizedLazyColumn(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    LazyColumn(modifier = modifier) {
        item { content() }
    }
}

@Composable
fun PerformanceMonitor(
    modifier: Modifier = Modifier
) {
    // Stub implementation
    Box(modifier = modifier)
}