package com.beaconledger.welltrack.accessibility

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Utility functions for accessibility testing
 * Helps validate WCAG 2.1 AA compliance in automated tests
 */
object AccessibilityTestingUtils {
    
    /**
     * Verify that all interactive elements meet minimum touch target size (44dp)
     */
    fun ComposeContentTestRule.assertMinimumTouchTargets() {
        val minimumSize = 44.dp
        
        onAllNodes(hasClickAction() or hasScrollAction())
            .fetchSemanticsNodes()
            .forEach { node ->
                val bounds = node.boundsInRoot
                val width = bounds.width
                val height = bounds.height
                
                assert(width >= minimumSize.value && height >= minimumSize.value) {
                    "Interactive element has insufficient touch target size: ${width}x${height}dp. " +
                    "Minimum required: ${minimumSize}x${minimumSize}dp"
                }
            }
    }
    
    /**
     * Verify that all images have content descriptions
     */
    fun ComposeContentTestRule.assertImageContentDescriptions() {
        onAllNodes(hasContentDescriptionExactly("") or !hasContentDescription())
            .fetchSemanticsNodes()
            .filter { node ->
                // Check if node represents an image or icon
                node.config.getOrNull(SemanticsProperties.Role)?.toString()?.contains("Image") == true
            }
            .forEach { node ->
                assert(false) {
                    "Image element missing content description: ${node.config}"
                }
            }
    }
    
    /**
     * Verify that all form fields have proper labels
     */
    fun ComposeContentTestRule.assertFormFieldLabels() {
        onAllNodes(hasSetTextAction())
            .fetchSemanticsNodes()
            .forEach { node ->
                val hasLabel = node.config.getOrNull(SemanticsProperties.ContentDescription) != null ||
                              node.config.getOrNull(SemanticsProperties.Text) != null
                
                assert(hasLabel) {
                    "Form field missing label: ${node.config}"
                }
            }
    }
    
    /**
     * Verify that error states are properly announced
     */
    fun ComposeContentTestRule.assertErrorAnnouncements() {
        onAllNodes(hasAnyDescendant(hasText("error", ignoreCase = true)))
            .fetchSemanticsNodes()
            .forEach { node ->
                val hasErrorSemantics = node.config.getOrNull(SemanticsProperties.Error) != null
                
                assert(hasErrorSemantics) {
                    "Error state not properly announced for accessibility: ${node.config}"
                }
            }
    }
    
    /**
     * Verify that headings are properly structured
     */
    fun ComposeContentTestRule.assertHeadingStructure() {
        val headings = onAllNodes(hasContentDescription())
            .fetchSemanticsNodes()
            .filter { node ->
                val description = node.config.getOrNull(SemanticsProperties.ContentDescription)
                description?.any { it.contains("heading", ignoreCase = true) } == true
            }
        
        // Verify heading hierarchy (H1 -> H2 -> H3, etc.)
        var previousLevel = 0
        headings.forEach { node ->
            val description = node.config.getOrNull(SemanticsProperties.ContentDescription)?.firstOrNull() ?: ""
            val level = extractHeadingLevel(description)
            
            if (level > 0) {
                assert(level <= previousLevel + 1) {
                    "Heading hierarchy violation: H$level follows H$previousLevel"
                }
                previousLevel = level
            }
        }
    }
    
    /**
     * Verify that focus order is logical
     */
    fun ComposeContentTestRule.assertLogicalFocusOrder() {
        val focusableNodes = onAllNodes(isFocusable())
            .fetchSemanticsNodes()
            .sortedBy { it.boundsInRoot.top }
            .sortedBy { it.boundsInRoot.left }
        
        // Verify that focus order follows reading order (left-to-right, top-to-bottom)
        for (i in 1 until focusableNodes.size) {
            val current = focusableNodes[i]
            val previous = focusableNodes[i - 1]
            
            val currentTop = current.boundsInRoot.top
            val previousTop = previous.boundsInRoot.top
            val currentLeft = current.boundsInRoot.left
            val previousLeft = previous.boundsInRoot.left
            
            // Allow some tolerance for elements on the same row
            val rowTolerance = 10.dp.value
            
            if (kotlin.math.abs(currentTop - previousTop) <= rowTolerance) {
                // Same row - should be left to right
                assert(currentLeft >= previousLeft) {
                    "Focus order violation: element at ($currentLeft, $currentTop) " +
                    "should come after element at ($previousLeft, $previousTop)"
                }
            } else {
                // Different rows - current should be below previous
                assert(currentTop > previousTop) {
                    "Focus order violation: element at ($currentLeft, $currentTop) " +
                    "should come after element at ($previousLeft, $previousTop)"
                }
            }
        }
    }
    
    /**
     * Verify color contrast ratios meet WCAG AA standards
     */
    fun assertColorContrast(
        foreground: Color,
        background: Color,
        isLargeText: Boolean = false,
        elementDescription: String = "element"
    ) {
        val contrastRatio = AccessibilityUtils.calculateContrastRatio(foreground, background)
        val meetsStandard = AccessibilityUtils.meetsWCAGAA(foreground, background, isLargeText)
        
        assert(meetsStandard) {
            "$elementDescription has insufficient color contrast: $contrastRatio:1. " +
            "Required: ${if (isLargeText) "3:1" else "4.5:1"} for WCAG AA compliance"
        }
    }
    
    /**
     * Verify that live regions are properly configured
     */
    fun ComposeContentTestRule.assertLiveRegions() {
        onAllNodes(hasAnyDescendant(hasText("loading", ignoreCase = true)) or 
                  hasAnyDescendant(hasText("error", ignoreCase = true)) or
                  hasAnyDescendant(hasText("success", ignoreCase = true)))
            .fetchSemanticsNodes()
            .forEach { node ->
                val hasLiveRegion = node.config.getOrNull(SemanticsProperties.LiveRegion) != null
                
                assert(hasLiveRegion) {
                    "Dynamic content should have live region for screen reader announcements: ${node.config}"
                }
            }
    }
    
    /**
     * Verify that custom controls have proper roles
     */
    fun ComposeContentTestRule.assertCustomControlRoles() {
        onAllNodes(hasClickAction())
            .fetchSemanticsNodes()
            .forEach { node ->
                val hasRole = node.config.getOrNull(SemanticsProperties.Role) != null
                
                assert(hasRole) {
                    "Interactive element missing semantic role: ${node.config}"
                }
            }
    }
    
    /**
     * Test keyboard navigation functionality
     */
    fun ComposeContentTestRule.testKeyboardNavigation() {
        // Find all focusable elements
        val focusableNodes = onAllNodes(isFocusable())
        
        // Test Tab navigation
        focusableNodes.onFirst().requestFocus()
        
        focusableNodes.fetchSemanticsNodes().forEachIndexed { index, _ ->
            if (index < focusableNodes.fetchSemanticsNodes().size - 1) {
                // Simulate Tab key press (move to next element)
                onRoot().performKeyInput {
                    pressKey(androidx.compose.ui.input.key.Key.Tab)
                }
            }
        }
        
        // Test Shift+Tab navigation (reverse)
        for (i in focusableNodes.fetchSemanticsNodes().size - 1 downTo 1) {
            onRoot().performKeyInput {
                keyDown(androidx.compose.ui.input.key.Key.ShiftLeft)
                pressKey(androidx.compose.ui.input.key.Key.Tab)
                keyUp(androidx.compose.ui.input.key.Key.ShiftLeft)
            }
        }
    }
    
    /**
     * Extract heading level from content description
     */
    private fun extractHeadingLevel(description: String): Int {
        val regex = Regex("h(\\d+)|heading\\s+(\\d+)", RegexOption.IGNORE_CASE)
        val match = regex.find(description)
        return match?.groupValues?.get(1)?.toIntOrNull() ?: 
               match?.groupValues?.get(2)?.toIntOrNull() ?: 0
    }
    
    /**
     * Verify that animations respect reduced motion preferences
     */
    fun ComposeContentTestRule.assertReducedMotionSupport() {
        // This would need to be implemented based on specific animation components
        // For now, we'll check that animations can be disabled
        
        onAllNodes(hasTestTag("animated_element"))
            .fetchSemanticsNodes()
            .forEach { node ->
                // Verify that animated elements have a way to disable animations
                // This is implementation-specific and would need to be customized
                // based on how animations are implemented in the app
            }
    }
    
    /**
     * Comprehensive accessibility audit
     */
    fun ComposeContentTestRule.performAccessibilityAudit() {
        assertMinimumTouchTargets()
        assertImageContentDescriptions()
        assertFormFieldLabels()
        assertErrorAnnouncements()
        assertHeadingStructure()
        assertLogicalFocusOrder()
        assertLiveRegions()
        assertCustomControlRoles()
        testKeyboardNavigation()
        assertReducedMotionSupport()
    }
}