package com.crescentapps.turnly.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/**
 * COMPLETE TURNLY VISUAL TOKENS
 * Semantic tokens mapping Light, Dark, AMOLED, and dynamic adaptive contexts.
 * Ensures no UI element relies on raw hardcoded Color.White/Color.Black.
 */
data class TurnlyColors(
    val isDark: Boolean,
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val glassCard: Color,
    val glassCardBorder: Color,
    val glassHighlight: Color,

    // Comprehensive Semantic Text Hierarchy
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val textMuted: Color,
    val textDisabled: Color,
    val textOnAccent: Color,
    val textOnError: Color,

    val accent: Color,
    val accentContainer: Color,
    val iconTint: Color,
    val divider: Color,

    // Controls: Toggles, Sliders, Checkboxes
    val controlActive: Color,
    val controlInactive: Color,
    val controlBorder: Color,
    val toggleTrackActive: Color,
    val toggleTrackInactive: Color,
    val toggleThumb: Color,
    val toggleThumbBorder: Color,
    val sliderTrackActive: Color,
    val sliderTrackInactive: Color,
    val sliderThumb: Color,
    val sliderThumbBorder: Color,
    val sliderThumbShadow: Color,

    // Calendar Specific Semantic Colors
    val calendarDayNormal: Color,
    val calendarDaySelectedText: Color,
    val calendarDaySelectedBg: Color,
    val calendarDayTodayBorder: Color,
    val calendarDayAdjacentMonth: Color,

    // Status Colors & Containers
    val statusPending: Color,
    val statusPendingContainer: Color,
    val statusCompleted: Color,
    val statusCompletedContainer: Color,
    val statusSkipped: Color,
    val statusSkippedContainer: Color,
    val statusMissed: Color,
    val statusMissedContainer: Color,
    val statusUpcoming: Color,
    val statusUpcomingContainer: Color,
    val statusAttention: Color,
    val statusAttentionContainer: Color
) {
    val isLight: Boolean get() = !isDark
    val primary: Color get() = accent

    /**
     * Resolves an optimal, readable foreground color for any background color
     * with high contrast ratio.
     */
    fun resolveForeground(backgroundColor: Color): Color {
        val bgLum = backgroundColor.luminance()
        return if (bgLum > 0.45f) Color(0xFF0F172A) else Color(0xFFF8FAFC)
    }
}

val DarkTurnlyColors = TurnlyColors(
    isDark = true,
    background = Color(0xFF0F172A),
    surface = Color(0xFF1E293B),
    surfaceVariant = Color(0xFF334155),
    glassCard = Color(0x381E2438),
    glassCardBorder = Color(0x38FFFFFF),
    glassHighlight = Color(0x28FFFFFF),

    textPrimary = Color(0xFFF8FAFC),
    textSecondary = Color(0xFFCBD5E1),
    textTertiary = Color(0xFF94A3B8),
    textMuted = Color(0xFF64748B),
    textDisabled = Color(0x6694A3B8),
    textOnAccent = Color(0xFFFFFFFF),
    textOnError = Color(0xFFFFFFFF),

    accent = Color(0xFF818CF8),
    accentContainer = Color(0xFF3730A3),
    iconTint = Color(0xFFF8FAFC),
    divider = Color(0x1FFFFFFF),

    controlActive = Color(0xFF818CF8),
    controlInactive = Color(0x33FFFFFF),
    controlBorder = Color(0x40FFFFFF),

    toggleTrackActive = Color(0xFF6366F1),
    toggleTrackInactive = Color(0x3D787880),
    toggleThumb = Color(0xFFFFFFFF),
    toggleThumbBorder = Color(0x40000000),

    sliderTrackActive = Color(0xFF818CF8),
    sliderTrackInactive = Color(0x2EFFFFFF),
    sliderThumb = Color(0xFFFFFFFF),
    sliderThumbBorder = Color(0x33000000),
    sliderThumbShadow = Color(0x66000000),

    calendarDayNormal = Color(0xFFF8FAFC),
    calendarDaySelectedText = Color(0xFFFFFFFF),
    calendarDaySelectedBg = Color(0xFF4F46E5),
    calendarDayTodayBorder = Color(0xFF818CF8),
    calendarDayAdjacentMonth = Color(0xFF64748B),

    statusPending = Color(0xFFF59E0B),
    statusPendingContainer = Color(0x33F59E0B),
    statusCompleted = Color(0xFF10B981),
    statusCompletedContainer = Color(0x3310B981),
    statusSkipped = Color(0xFF94A3B8),
    statusSkippedContainer = Color(0x3394A3B8),
    statusMissed = Color(0xFFEF4444),
    statusMissedContainer = Color(0x33EF4444),
    statusUpcoming = Color(0xFF38BDF8),
    statusUpcomingContainer = Color(0x3338BDF8),
    statusAttention = Color(0xFFF97316),
    statusAttentionContainer = Color(0x33F97316)
)

val AmoledDarkTurnlyColors = DarkTurnlyColors.copy(
    background = Color(0xFF000000),
    surface = Color(0xFF0A0A0A),
    surfaceVariant = Color(0xFF121212),
    glassCard = Color(0x40161616),
    glassCardBorder = Color(0x28FFFFFF)
)

val LightTurnlyColors = TurnlyColors(
    isDark = false,
    background = Color(0xFFF8FAFC),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFE2E8F0),
    glassCard = Color(0x8CFFFFFF),
    glassCardBorder = Color(0x52FFFFFF),
    glassHighlight = Color(0x66FFFFFF),

    textPrimary = Color(0xFF0F172A),
    textSecondary = Color(0xFF334155),
    textTertiary = Color(0xFF64748B),
    textMuted = Color(0xFF94A3B8),
    textDisabled = Color(0x6664748B),
    textOnAccent = Color(0xFFFFFFFF),
    textOnError = Color(0xFFFFFFFF),

    accent = Color(0xFF4F46E5),
    accentContainer = Color(0xFFE0E7FF),
    iconTint = Color(0xFF0F172A),
    divider = Color(0x190F172A),

    controlActive = Color(0xFF4F46E5),
    controlInactive = Color(0x260F172A),
    controlBorder = Color(0x330F172A),

    toggleTrackActive = Color(0xFF4F46E5),
    toggleTrackInactive = Color(0x2E64748B),
    toggleThumb = Color(0xFFFFFFFF),
    toggleThumbBorder = Color(0x26000000),

    sliderTrackActive = Color(0xFF4F46E5),
    sliderTrackInactive = Color(0x1F0F172A),
    sliderThumb = Color(0xFFFFFFFF),
    sliderThumbBorder = Color(0x29000000),
    sliderThumbShadow = Color(0x29000000),

    calendarDayNormal = Color(0xFF0F172A),
    calendarDaySelectedText = Color(0xFFFFFFFF),
    calendarDaySelectedBg = Color(0xFF4F46E5),
    calendarDayTodayBorder = Color(0xFF4F46E5),
    calendarDayAdjacentMonth = Color(0xFF94A3B8),

    statusPending = Color(0xFFD97706),
    statusPendingContainer = Color(0x26D97706),
    statusCompleted = Color(0xFF059669),
    statusCompletedContainer = Color(0x26059669),
    statusSkipped = Color(0xFF64748B),
    statusSkippedContainer = Color(0x2664748B),
    statusMissed = Color(0xFFDC2626),
    statusMissedContainer = Color(0x26DC2626),
    statusUpcoming = Color(0xFF0284C7),
    statusUpcomingContainer = Color(0x260284C7),
    statusAttention = Color(0xFFEA580C),
    statusAttentionContainer = Color(0x26EA580C)
)

val LocalTurnlyColors = staticCompositionLocalOf { DarkTurnlyColors }

/**
 * Accessor for Turnly Design Tokens
 */
object TurnlyThemeTokens {
    val colors: TurnlyColors
        @Composable
        @ReadOnlyComposable
        get() = LocalTurnlyColors.current
}

