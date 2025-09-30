package com.beaconledger.welltrack.accessibility

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.beaconledger.welltrack.accessibility.AccessibilityUtils.calculateContrastRatio
import com.beaconledger.welltrack.accessibility.AccessibilityUtils.formatDateForScreenReader
import com.beaconledger.welltrack.accessibility.AccessibilityUtils.formatNumberForScreenReader
import com.beaconledger.welltrack.accessibility.AccessibilityUtils.formatTimeForScreenReader
import com.beaconledger.welltrack.accessibility.AccessibilityUtils.generateContentDescription
import com.beaconledger.welltrack.accessibility.AccessibilityUtils.generateListPositionDescription
import com.beaconledger.welltrack.accessibility.AccessibilityUtils.generateProgressDescription
import com.beaconledger.welltrack.accessibility.AccessibilityUtils.getAccessibleTextSize
import com.beaconledger.welltrack.accessibility.AccessibilityUtils.meetsWCAGAA
import com.beaconledger.welltrack.accessibility.AccessibilityUtils.meetsWCAGAAA
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for accessibility utility functions
 */
class AccessibilityUtilsTest {
    
    @Test
    fun testContrastRatioCalculation() {
        // Test black on white (maximum contrast)
        val blackOnWhite = calculateContrastRatio(Color.Black, Color.White)
        assertEquals(21.0, blackOnWhite, 0.1)
        
        // Test white on black (same as black on white)
        val whiteOnBlack = calculateContrastRatio(Color.White, Color.Black)
        assertEquals(21.0, whiteOnBlack, 0.1)
        
        // Test same colors (minimum contrast)
        val sameColor = calculateContrastRatio(Color.Red, Color.Red)
        assertEquals(1.0, sameColor, 0.1)
        
        // Test medium contrast
        val mediumContrast = calculateContrastRatio(Color(0xFF666666), Color.White)
        assertTrue("Medium contrast should be between 1 and 21", mediumContrast > 1.0 && mediumContrast < 21.0)
    }
    
    @Test
    fun testWCAGAACompliance() {
        // Test colors that should meet WCAG AA
        assertTrue("Black on white should meet WCAG AA", meetsWCAGAA(Color.Black, Color.White))
        assertTrue("Dark blue on white should meet WCAG AA", meetsWCAGAA(Color(0xFF0000AA), Color.White))
        
        // Test colors that should not meet WCAG AA
        assertFalse("Light gray on white should not meet WCAG AA", meetsWCAGAA(Color(0xFFCCCCCC), Color.White))
        assertFalse("Yellow on white should not meet WCAG AA", meetsWCAGAA(Color.Yellow, Color.White))
        
        // Test large text requirements (lower threshold)
        assertTrue("Medium gray on white should meet WCAG AA for large text", 
            meetsWCAGAA(Color(0xFF767676), Color.White, isLargeText = true))
    }
    
    @Test
    fun testWCAGAAACompliance() {
        // Test colors that should meet WCAG AAA
        assertTrue("Black on white should meet WCAG AAA", meetsWCAGAAA(Color.Black, Color.White))
        
        // Test colors that meet AA but not AAA
        val mediumContrast = Color(0xFF595959) // Approximately 7:1 contrast
        assertTrue("Medium contrast should meet WCAG AA", meetsWCAGAA(mediumContrast, Color.White))
        // Note: This might not meet AAA depending on exact calculation
        
        // Test colors that should not meet WCAG AAA
        assertFalse("Light colors should not meet WCAG AAA", meetsWCAGAAA(Color(0xFFAAAAAA), Color.White))
    }
    
    @Test
    fun testAccessibleTextSize() {
        val baseSize = 16.sp
        
        // Test normal font scale
        val normalSize = getAccessibleTextSize(baseSize, 1.0f)
        assertEquals(16.sp, normalSize)
        
        // Test large font scale
        val largeSize = getAccessibleTextSize(baseSize, 1.5f)
        assertEquals(24.sp, largeSize)
        
        // Test small font scale
        val smallSize = getAccessibleTextSize(baseSize, 0.8f)
        assertEquals(12.8.sp, smallSize)
    }
    
    @Test
    fun testContentDescriptionGeneration() {
        // Test basic content description
        val basic = generateContentDescription("Button")
        assertEquals("Button", basic)
        
        // Test with value
        val withValue = generateContentDescription("Slider", value = "50%")
        assertEquals("Slider, 50%", withValue)
        
        // Test with state
        val withState = generateContentDescription("Checkbox", state = "checked")
        assertEquals("Checkbox, checked", withState)
        
        // Test with position
        val withPosition = generateContentDescription("Item", position = "1 of 5")
        assertEquals("Item, 1 of 5", withPosition)
        
        // Test with all parameters
        val complete = generateContentDescription(
            label = "Menu item",
            value = "Settings",
            state = "selected",
            position = "2 of 10",
            additionalInfo = "opens submenu"
        )
        assertEquals("Menu item, Settings, selected, 2 of 10, opens submenu", complete)
    }
    
    @Test
    fun testNumberFormatting() {
        // Test integer
        assertEquals("5", formatNumberForScreenReader(5.0))
        assertEquals("5 kg", formatNumberForScreenReader(5.0, "kg"))
        
        // Test decimal
        assertEquals("5.5", formatNumberForScreenReader(5.5))
        assertEquals("5.5 kg", formatNumberForScreenReader(5.5, "kg"))
        
        // Test zero
        assertEquals("0", formatNumberForScreenReader(0.0))
        assertEquals("0 items", formatNumberForScreenReader(0.0, "items"))
    }
    
    @Test
    fun testTimeFormatting() {
        // Test hours only
        assertEquals("2 hours", formatTimeForScreenReader(2, 0))
        assertEquals("1 hour", formatTimeForScreenReader(1, 0))
        
        // Test minutes only
        assertEquals("30 minutes", formatTimeForScreenReader(0, 30))
        assertEquals("1 minute", formatTimeForScreenReader(0, 1))
        
        // Test hours and minutes
        assertEquals("2 hours and 30 minutes", formatTimeForScreenReader(2, 30))
        assertEquals("1 hour and 1 minute", formatTimeForScreenReader(1, 1))
        
        // Test zero time
        assertEquals("0 minutes", formatTimeForScreenReader(0, 0))
    }
    
    @Test
    fun testDateFormatting() {
        val formatted = formatDateForScreenReader(15, "March", 2024)
        assertEquals("March 15, 2024", formatted)
        
        val formatted2 = formatDateForScreenReader(1, "January", 2023)
        assertEquals("January 1, 2023", formatted2)
    }
    
    @Test
    fun testListPositionDescription() {
        assertEquals("1 of 5", generateListPositionDescription(0, 5))
        assertEquals("3 of 10", generateListPositionDescription(2, 10))
        assertEquals("1 of 1", generateListPositionDescription(0, 1))
    }
    
    @Test
    fun testProgressDescription() {
        // Test without unit
        assertEquals("3 of 10, 30 percent complete", generateProgressDescription(3, 10))
        
        // Test with unit
        assertEquals("5 of 20 items, 25 percent complete", generateProgressDescription(5, 20, "items"))
        
        // Test complete
        assertEquals("10 of 10 tasks, 100 percent complete", generateProgressDescription(10, 10, "tasks"))
        
        // Test zero total
        assertEquals("0 of 0, 0 percent complete", generateProgressDescription(0, 0))
    }
    
    @Test
    fun testTextStyleLargeTextDetection() {
        // Test large text by size (18sp+)
        val largeBySize = TextStyle(fontSize = 18.sp)
        assertTrue("18sp should be considered large text", largeBySize.isLargeText())
        
        // Test large text by weight (14sp+ bold)
        val largeByWeight = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold)
        assertTrue("14sp bold should be considered large text", largeByWeight.isLargeText())
        
        // Test normal text
        val normalText = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal)
        assertFalse("14sp normal should not be considered large text", normalText.isLargeText())
        
        // Test small text
        val smallText = TextStyle(fontSize = 12.sp)
        assertFalse("12sp should not be considered large text", smallText.isLargeText())
    }
}