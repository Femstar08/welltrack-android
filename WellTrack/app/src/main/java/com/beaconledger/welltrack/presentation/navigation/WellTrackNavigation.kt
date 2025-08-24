package com.beaconledger.welltrack.presentation.navigation

// TEMPORARILY DISABLED FOR PHASE 1 BUILD

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController

// Minimal stub implementations for Phase 1
@Composable
fun WellTrackNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    // Stub implementation for Phase 1
    Box(modifier = modifier) {
        Text("Navigation - Phase 1 Stub")
    }
}

@Composable
fun BottomNavigationBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Stub implementation for Phase 1
    NavigationBar(modifier = modifier) {
        NavigationBarItem(
            selected = true,
            onClick = { },
            icon = { Text("Home") },
            label = { Text("Home") }
        )
    }
}