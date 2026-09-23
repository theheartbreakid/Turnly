package com.crescentapps.turnly.presentation.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

object LiquidColors {
    // Backgrounds
    val DarkBackground = Color(0xFF0D0F18)
    val AmoledBackground = Color(0xFF000000)
    val LightBackground = Color(0xFFF3F5FA)

    // Glass Base Surfaces
    val DarkGlassSurface = Color(0x381E2438)
    val DarkGlassCard = Color(0x48242C44)
    val DarkGlassBorder = Color(0x33FFFFFF)
    val DarkGlassHighlight = Color(0x22FFFFFF)

    val AmoledGlassSurface = Color(0x40161616)
    val AmoledGlassCard = Color(0x55222222)
    val AmoledGlassBorder = Color(0x30FFFFFF)
    val AmoledGlassHighlight = Color(0x20FFFFFF)

    val LightGlassSurface = Color(0x70FFFFFF)
    val LightGlassCard = Color(0x8CFFFFFF)
    val LightGlassBorder = Color(0x40FFFFFF)
    val LightGlassHighlight = Color(0x60FFFFFF)

    // Liquid Gradients for dynamic canvas
    val DarkFluidBlob1 = Color(0x4D6366F1) // Indigo
    val DarkFluidBlob2 = Color(0x40A855F7) // Purple
    val DarkFluidBlob3 = Color(0x3806B6D4) // Cyan
    val DarkFluidBlob4 = Color(0x33EC4899) // Pink

    val LightFluidBlob1 = Color(0x33818CF8)
    val LightFluidBlob2 = Color(0x2E06B6D4)
    val LightFluidBlob3 = Color(0x29C084FC)
    val LightFluidBlob4 = Color(0x24F472B6)

    // Status Colors
    val StatusPending = Color(0xFFF59E0B) // Amber
    val StatusCompleted = Color(0xFF10B981) // Emerald
    val StatusSkipped = Color(0xFF6B7280) // Gray
    val StatusMissed = Color(0xFFEF4444) // Red
    val StatusOverride = Color(0xFF8B5CF6) // Violet

    // Participant Palette (Vibrant, high-contrast, beautiful)
    val ParticipantPalette = listOf(
        "#3B82F6", // Sky Blue
        "#10B981", // Emerald
        "#8B5CF6", // Violet
        "#EC4899", // Pink
        "#F59E0B", // Amber
        "#06B6D4", // Cyan
        "#6366F1", // Indigo
        "#F97316", // Coral Orange
        "#14B8A6", // Teal
        "#A855F7", // Purple
        "#E11D48", // Rose
        "#84CC16"  // Lime
    )

    fun getParticipantColor(colorHex: String?): Color {
        if (colorHex.isNullOrBlank()) return Color(0xFF3B82F6)
        return runCatching {
            Color(android.graphics.Color.parseColor(colorHex))
        }.getOrDefault(Color(0xFF3B82F6))
    }
}
