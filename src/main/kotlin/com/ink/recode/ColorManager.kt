package com.ink.recode

import java.awt.Color

/**
 * Unified color management class for dark theme
 * Contains all theme color definitions and color conversion utilities
 */
object ColorManager {

    // Primary color palette - Main accent colors for the interface
    val primary: Color = getColor("#D2BCFD")          // Main accent color (light purple) for buttons, selected states
    val surfaceTint: Color = getColor("#D2BCFD")      // Surface tint color for adding primary hue to surface elements
    val onPrimary: Color = getColor("#38265C")        // Text color on primary (dark purple) for readability
    val primaryContainer: Color = getColor("#4F3D74") // Container background (dark purple) for cards, panels
    val onPrimaryContainer: Color = getColor("#EADDFF") // Text color on primary container (light purple)

    // Secondary color palette - Auxiliary colors to primary palette
    val secondary: Color = getColor("#CDC2DB")        // Secondary accent (light purple-gray) for secondary buttons
    val onSecondary: Color = getColor("#342D40")      // Text color on secondary (dark purple-gray)
    val secondaryContainer: Color = getColor("#4B4358") // Secondary container background (dark purple-gray)
    val onSecondaryContainer: Color = getColor("#E9DEF8") // Text color on secondary container

    // Tertiary color palette - For special states/elements highlighting
    val tertiary: Color = getColor("#F0B7C5")         // Tertiary accent (light pink-purple) for alerts, special tags
    val onTertiary: Color = getColor("#4A2530")       // Text color on tertiary (dark pink-purple)
    val tertiaryContainer: Color = getColor("#643B46") // Tertiary container background (dark pink-purple)
    val onTertiaryContainer: Color = getColor("#FFD9E1") // Text color on tertiary container

    // Error color palette - For error states and notifications
    val error: Color = getColor("#FFB4AB")            // Error color (light red) for error prompts, failure states
    val onError: Color = getColor("#690005")          // Text color on error (dark red)
    val errorContainer: Color = getColor("#93000A")   // Error container background (dark red)
    val onErrorContainer: Color = getColor("#FFDAD6") // Text color on error container

    // Background & surface colors - Base background for interface
    val background: Color = getColor("#151218")       // Global background (dark black-purple) for app overall
    val onBackground: Color = getColor("#E7E0E8")     // Text color on background (light gray-purple)
    val surface: Color = getColor("#151218")          // Surface color for cards, components (same as background)
    val onSurface: Color = getColor("#E7E0E8")        // Text color on surface (same as onBackground)
    val surfaceVariant: Color = getColor("#49454E")   // Surface variant for secondary container background
    val onSurfaceVariant: Color = getColor("#CBC4CF") // Text color on surface variant

    // Border & shadow colors - For borders, shadows and dividers
    val outline: Color = getColor("#948F99")          // Outline color for buttons, input borders
    val outlineVariant: Color = getColor("#49454E")   // Variant outline color for secondary borders
    val shadow: Color = getColor("#000000")           // Shadow color for component shadows
    val scrim: Color = getColor("#000000")            // Scrim color for dialog overlays

    // Inverse colors - For reversed contrast scenarios
    val inverseSurface: Color = getColor("#E7E0E8")   // Inverse surface color (reversed background)
    val inverseOnSurface: Color = getColor("#322F35") // Text color on inverse surface
    val inversePrimary: Color = getColor("#67548E")   // Inverse primary color (reversed primary)

    // Fixed colors - Unchanged across theme switches
    val primaryFixed: Color = getColor("#EADDFF")     // Fixed primary color
    val onPrimaryFixed: Color = getColor("#230F46")   // Text color on fixed primary
    val primaryFixedDim: Color = getColor("#D2BCFD")  // Fixed primary color (dim version)
    val onPrimaryFixedVariant: Color = getColor("#4F3D74") // Text color on fixed primary variant

    // Surface container colors - Different depth for layer distinction
    val surfaceDim: Color = getColor("#151218")       // Dim surface (lowest layer background)
    val surfaceBright: Color = getColor("#3B383E")    // Bright surface (highlighted container background)
    val surfaceContainerLowest: Color = getColor("#0F0D13") // Lowest surface container (darkest)
    val surfaceContainerLow: Color = getColor("#1D1B20")    // Low surface container (darker)
    val surfaceContainer: Color = getColor("#211F24")       // Default surface container
    val surfaceContainerHigh: Color = getColor("#2B292F")   // High surface container (brighter)
    val surfaceContainerHighest: Color = getColor("#36343A") // Highest surface container (brightest)

    /**
     * Get Color object by HEX string (camelCase function name)
     * @param hex HEX color value (format: #RRGGBB)
     * @return Color object, returns black if parsing fails
     */
    fun getColor(hex: String): Color {
        return try {
            val cleanHex = if (hex.startsWith("#")) hex.substring(1) else hex
            val r = cleanHex.substring(0, 2).toInt(16)
            val g = cleanHex.substring(2, 4).toInt(16)
            val b = cleanHex.substring(4, 6).toInt(16)
            Color(r, g, b)
        } catch (e: Exception) {
            Color.BLACK
        }
    }

    /**
     * Get Color object by RGB values (camelCase function name)
     * @param r Red component (0-255)
     * @param g Green component (0-255)
     * @param b Blue component (0-255)
     * @return Color object with validated RGB values
     */
    fun getColor(r: Int, g: Int, b: Int): Color {
        val red = r.coerceIn(0, 255)
        val green = g.coerceIn(0, 255)
        val blue = b.coerceIn(0, 255)
        return Color(red, green, blue)
    }

    /**
     * Get HEX string by Color object (camelCase function name)
     * @param color Color object to convert
     * @return HEX string in #RRGGBB format (black if color is null)
     */
    fun getColorHex(color: Color?): String {
        color ?: return "#000000"
        return String.format("#%02X%02X%02X", color.red, color.green, color.blue)
    }
}