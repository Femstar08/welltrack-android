package com.beaconledger.welltrack.accessibility

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.*
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * Manages keyboard navigation for accessibility compliance
 * Provides focus management and keyboard shortcuts
 */
class KeyboardNavigationManager {
    
    private var focusRequesters = mutableListOf<FocusRequester>()
    private var currentFocusIndex = 0
    
    /**
     * Register a focus requester for keyboard navigation
     */
    fun registerFocusRequester(focusRequester: FocusRequester) {
        focusRequesters.add(focusRequester)
    }
    
    /**
     * Clear all registered focus requesters
     */
    fun clearFocusRequesters() {
        focusRequesters.clear()
        currentFocusIndex = 0
    }
    
    /**
     * Move focus to next element
     */
    fun focusNext(): Boolean {
        if (focusRequesters.isEmpty()) return false
        
        currentFocusIndex = (currentFocusIndex + 1) % focusRequesters.size
        focusRequesters[currentFocusIndex].requestFocus()
        return true
    }
    
    /**
     * Move focus to previous element
     */
    fun focusPrevious(): Boolean {
        if (focusRequesters.isEmpty()) return false
        
        currentFocusIndex = if (currentFocusIndex > 0) currentFocusIndex - 1 else focusRequesters.size - 1
        focusRequesters[currentFocusIndex].requestFocus()
        return true
    }
    
    /**
     * Focus first element
     */
    fun focusFirst(): Boolean {
        if (focusRequesters.isEmpty()) return false
        
        currentFocusIndex = 0
        focusRequesters[currentFocusIndex].requestFocus()
        return true
    }
    
    /**
     * Focus last element
     */
    fun focusLast(): Boolean {
        if (focusRequesters.isEmpty()) return false
        
        currentFocusIndex = focusRequesters.size - 1
        focusRequesters[currentFocusIndex].requestFocus()
        return true
    }
}

/**
 * Composable that provides keyboard navigation support
 */
@Composable
fun KeyboardNavigationProvider(
    modifier: Modifier = Modifier,
    onEscape: (() -> Unit)? = null,
    onEnter: (() -> Unit)? = null,
    content: @Composable (KeyboardNavigationManager) -> Unit
) {
    val navigationManager = remember { KeyboardNavigationManager() }
    val focusManager = LocalFocusManager.current
    
    // Clear focus requesters when composition is disposed
    DisposableEffect(navigationManager) {
        onDispose {
            navigationManager.clearFocusRequesters()
        }
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .focusable()
            .onKeyEvent { keyEvent ->
                when {
                    keyEvent.type == KeyEventType.KeyDown -> {
                        when (keyEvent.key) {
                            Key.Tab -> {
                                if (keyEvent.isShiftPressed) {
                                    navigationManager.focusPrevious()
                                } else {
                                    navigationManager.focusNext()
                                }
                                true
                            }
                            Key.Escape -> {
                                onEscape?.invoke() ?: run {
                                    focusManager.clearFocus()
                                }
                                true
                            }
                            Key.Enter, Key.NumPadEnter -> {
                                onEnter?.invoke()
                                true
                            }
                            Key.Home -> {
                                if (keyEvent.isCtrlPressed) {
                                    navigationManager.focusFirst()
                                    true
                                } else false
                            }
                            Key.MoveEnd -> {
                                if (keyEvent.isCtrlPressed) {
                                    navigationManager.focusLast()
                                    true
                                } else false
                            }
                            else -> false
                        }
                    }
                    else -> false
                }
            }
    ) {
        content(navigationManager)
    }
}

/**
 * Modifier for keyboard-focusable elements
 */
@Composable
fun Modifier.keyboardFocusable(
    navigationManager: KeyboardNavigationManager,
    onFocus: (() -> Unit)? = null,
    onBlur: (() -> Unit)? = null
): Modifier {
    val focusRequester = remember { FocusRequester() }
    
    // Register focus requester with navigation manager
    LaunchedEffect(focusRequester) {
        navigationManager.registerFocusRequester(focusRequester)
    }
    
    return this
        .focusRequester(focusRequester)
        .onFocusChanged { focusState ->
            if (focusState.isFocused) {
                onFocus?.invoke()
            } else {
                onBlur?.invoke()
            }
        }
        .focusable()
}

/**
 * Skip link component for keyboard navigation
 */
@Composable
fun SkipLink(
    text: String,
    targetFocusRequester: FocusRequester,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }
    val accessibilitySettings = rememberAccessibilitySettings()
    
    if (isVisible) {
        androidx.compose.material3.TextButton(
            onClick = {
                targetFocusRequester.requestFocus()
                isVisible = false
            },
            modifier = modifier
                .onFocusChanged { focusState ->
                    isVisible = focusState.isFocused
                }
                .focusable()
        ) {
            androidx.compose.material3.Text(
                text = text,
                fontSize = AccessibilityUtils.getAccessibleTextSize(14.sp, accessibilitySettings.fontScale)
            )
        }
    } else {
        // Invisible but focusable skip link
        Box(
            modifier = modifier
                .onFocusChanged { focusState ->
                    isVisible = focusState.isFocused
                }
                .focusable()
        )
    }
}

/**
 * Focus trap component to contain focus within a specific area
 */
@Composable
fun FocusTrap(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val focusManager = LocalFocusManager.current
    var trapActive by remember { mutableStateOf(true) }
    
    Box(
        modifier = modifier
            .onKeyEvent { keyEvent ->
                if (trapActive && keyEvent.type == KeyEventType.KeyDown && keyEvent.key == Key.Tab) {
                    // Handle tab navigation within the trap
                    if (keyEvent.isShiftPressed) {
                        // Shift+Tab - move to previous focusable element
                        focusManager.moveFocus(FocusDirection.Previous)
                    } else {
                        // Tab - move to next focusable element
                        focusManager.moveFocus(FocusDirection.Next)
                    }
                    true
                } else {
                    false
                }
            }
    ) {
        content()
    }
    
    // Activate trap when component is composed
    LaunchedEffect(Unit) {
        trapActive = true
    }
    
    // Deactivate trap when component is disposed
    DisposableEffect(Unit) {
        onDispose {
            trapActive = false
        }
    }
}

/**
 * Auto-focus component that focuses an element when it becomes visible
 */
@Composable
fun AutoFocus(
    delay: Long = 100L,
    content: @Composable (FocusRequester) -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    
    LaunchedEffect(focusRequester) {
        delay(delay)
        focusRequester.requestFocus()
    }
    
    content(focusRequester)
}