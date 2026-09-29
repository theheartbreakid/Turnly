package com.crescentapps.turnly.presentation.gaussianblur.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class GaussianBlurColors(
    val isDark: Boolean,
    val background: Color,
    val surfaceTint: Color,
    val surfaceVariant: Color,
    val glassBorder: Color,
    val glassHighlight: Color,
    val accent: Color,
    val accentContainer: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val textOnAccent: Color,
    val iconTint: Color,
    val divider: Color,

    // Status
    val statusPending: Color,
    val statusCompleted: Color,
    val statusSkipped: Color,
    val statusMissed: Color,
    val statusAttention: Color
)

val DarkGaussianBlurColors = GaussianBlurColors(
    isDark = true,
    background = Color(0xFF0B0F19),
    surfaceTint = Color(0xFF121212),
    surfaceVariant = Color(0xFF1E293B),
    glassBorder = Color.White.copy(alpha = 0.12f),
    glassHighlight = Color.White.copy(alpha = 0.20f),
    accent = Color(0xFF818CF8),
    accentContainer = Color(0xFF3730A3),
    textPrimary = Color(0xFFF8FAFC),
    textSecondary = Color(0xFFCBD5E1),
    textMuted = Color(0xFF64748B),
    textOnAccent = Color(0xFFFFFFFF),
    iconTint = Color(0xFFF8FAFC),
    divider = Color.White.copy(alpha = 0.10f),

    statusPending = Color(0xFFF59E0B),
    statusCompleted = Color(0xFF10B981),
    statusSkipped = Color(0xFF94A3B8),
    statusMissed = Color(0xFFEF4444),
    statusAttention = Color(0xFFF97316)
)

val LightGaussianBlurColors = GaussianBlurColors(
    isDark = false,
    background = Color(0xFFF1F5F9),
    surfaceTint = Color(0xFFFAFAFA),
    surfaceVariant = Color(0xFFE2E8F0),
    glassBorder = Color.White.copy(alpha = 0.40f),
    glassHighlight = Color.White.copy(alpha = 0.60f),
    accent = Color(0xFF4F46E5),
    accentContainer = Color(0xFFE0E7FF),
    textPrimary = Color(0xFF0F172A),
    textSecondary = Color(0xFF334155),
    textMuted = Color(0xFF64748B),
    textOnAccent = Color(0xFFFFFFFF),
    iconTint = Color(0xFF0F172A),
    divider = Color.Black.copy(alpha = 0.08f),

    statusPending = Color(0xFFD97706),
    statusCompleted = Color(0xFF059669),
    statusSkipped = Color(0xFF64748B),
    statusMissed = Color(0xFFDC2626),
    statusAttention = Color(0xFFEA580C)
)

val LocalGaussianBlurColors = staticCompositionLocalOf { DarkGaussianBlurColors }
