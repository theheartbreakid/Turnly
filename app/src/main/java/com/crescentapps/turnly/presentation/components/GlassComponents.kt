package com.crescentapps.turnly.presentation.components

import android.view.ViewGroup
import androidx.compose.animation.Animatable
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.crescentapps.turnly.TurnlyApplication
import com.crescentapps.turnly.core.util.HapticManager
import com.crescentapps.turnly.data.preferences.UserPreferences
import com.crescentapps.turnly.presentation.catalog.utils.scale
import com.crescentapps.turnly.presentation.theme.LocalTurnlyColors
import com.matrix.prismal.DownsampleMode
import com.matrix.prismal.PrismalFrameLayout
import com.matrix.prismal.PrismalLiquidGlass
import kotlinx.coroutines.delay

/**
 * DHIKRCOUNTER VISUAL COMPONENT SUITE - PRISMAL MIGRATION
 * Adaptive Luminance, Optics Settings, Window Metrics, and Surface Architecture.
 */

val LocalPrismalAdaptiveColor = compositionLocalOf { Color.Unspecified }
val LocalPrismalCaptureHost = compositionLocalOf<ViewGroup?> { null }
val LocalPrismalSceneVersion = compositionLocalOf { 0L }
val LocalAdaptiveLuminanceActive = compositionLocalOf { false }
val LocalAdaptiveLuminanceEnabled = compositionLocalOf { true }
val LocalAdaptiveLuminanceInterval = compositionLocalOf { 1000 }
val LocalScrollInProgress = compositionLocalOf { false }
val LocalGlassIntensity = compositionLocalOf { 1.0f }
val LocalHapticIntensity = compositionLocalOf { 1.0f }
val LocalHapticEnabled = compositionLocalOf { true }
val LocalSoundFeedbackEnabled = compositionLocalOf { false }
val LocalFontTintFallbackMode = compositionLocalOf { 0 }
val LocalFontTintPaletteColor = compositionLocalOf { 0xFF6366F1.toInt() }
val LocalFontTintCustomColor = compositionLocalOf { 0xFF6366F1.toInt() }

@Composable
fun rememberPrismalHaptic(): () -> Unit {
    val context = LocalContext.current
    val hapticManager = remember(context) { HapticManager(context.applicationContext) }
    val intensity = LocalHapticIntensity.current
    val enabled = LocalHapticEnabled.current

    return remember(hapticManager, intensity, enabled) {
        {
            if (enabled) {
                hapticManager.vibrate(50L, intensity, enabled)
            }
        }
    }
}

@Composable
fun AdaptiveIcon(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    darkVariant: ImageVector = icon,
    contentDescription: String? = null,
    tint: Color = LocalPrismalAdaptiveColor.current
) {
    val isBackdropLight = tint.luminance() < 0.5f
    val targetIcon = if (isBackdropLight) darkVariant else icon

    Icon(
        imageVector = targetIcon,
        contentDescription = contentDescription,
        modifier = modifier,
        tint = tint
    )
}

/**
 * ADAPTIVE LUMINANCE PROVIDER
 * Samples backdrop pixels in real-time to compute optimal text contrast.
 * Fully supports fallback modes (Auto background derivation, Palette, Custom)
 * when Adaptive Luminance is toggled OFF.
 */
@Composable
fun AdaptiveLuminanceProvider(
    layer: GraphicsLayer?,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    if (LocalAdaptiveLuminanceActive.current) {
        content()
        return
    }

    val globalEnabled = LocalAdaptiveLuminanceEnabled.current
    val turnlyColors = LocalTurnlyColors.current
    val isLightTheme = turnlyColors.isLight
    val parentAdaptiveColor = LocalPrismalAdaptiveColor.current

    val fallbackMode = LocalFontTintFallbackMode.current
    val paletteColor = LocalFontTintPaletteColor.current
    val customColor = LocalFontTintCustomColor.current
    val sceneVersion = LocalPrismalSceneVersion.current

    val contentColorAnimation = remember(isLightTheme, globalEnabled, fallbackMode, paletteColor, customColor, parentAdaptiveColor, turnlyColors) {
        val initialColor = if (globalEnabled) {
            // Respect parent adaptive context or resolve against current surface
            if (parentAdaptiveColor != Color.Unspecified && parentAdaptiveColor != Color.Transparent) {
                parentAdaptiveColor
            } else {
                turnlyColors.resolveForeground(turnlyColors.surface)
            }
        } else {
            when (fallbackMode) {
                1 -> Color(paletteColor)
                2 -> Color(customColor)
                else -> turnlyColors.resolveForeground(turnlyColors.surface)
            }
        }
        Animatable(initialColor)
    }

    var lastPosition by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Unspecified) }
    val refreshInterval = LocalAdaptiveLuminanceInterval.current
    val buffer = remember { IntArray(25) }
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    var isAppVisible by remember { mutableStateOf(true) }

    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            isAppVisible = event == androidx.lifecycle.Lifecycle.Event.ON_RESUME || event == androidx.lifecycle.Lifecycle.Event.ON_START
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val isScrollInProgress = LocalScrollInProgress.current

    LaunchedEffect(layer, globalEnabled, fallbackMode, paletteColor, customColor, sceneVersion, lastPosition, isAppVisible, isLightTheme, turnlyColors, isScrollInProgress) {
        if (globalEnabled && layer != null && isAppVisible) {
            while (true) {
                if (!isScrollInProgress) {
                    try {
                        val imageBitmap = layer.toImageBitmap()
                        val thumbnail = imageBitmap.scale(5, 5)
                        thumbnail.readPixels(buffer)

                        var luminanceSum = 0.0
                        for (argb in buffer) {
                            val r = (argb shr 16 and 0xFF) / 255f
                            val g = (argb shr 8 and 0xFF) / 255f
                            val b = (argb and 0xFF) / 255f
                            luminanceSum += 0.2126 * r + 0.7152 * g + 0.0722 * b
                        }
                        val averageLuminance = (luminanceSum / buffer.size).toFloat()
                        val targetColor = if (averageLuminance > 0.45f) Color.Black else Color.White
                        if (contentColorAnimation.targetValue != targetColor) {
                            contentColorAnimation.animateTo(targetColor, tween(300, easing = LinearOutSlowInEasing))
                        }
                    } catch (e: Exception) {
                        // Layer not yet drawn/ready; retain current contrast-safe color
                    }
                }
                delay(refreshInterval.toLong())
            }
        } else if (!globalEnabled) {
            // FALLBACK PATH (Adaptive Luminance OFF)
            if (fallbackMode == 0 && layer != null) { // AUTO
                while (true) {
                    if (!isScrollInProgress) {
                        try {
                            val imageBitmap = layer.toImageBitmap()
                            val thumbnail = imageBitmap.scale(5, 5)
                            thumbnail.readPixels(buffer)

                            var rSum = 0f; var gSum = 0f; var bSum = 0f
                            buffer.forEach { argb ->
                                rSum += (argb shr 16 and 0xFF) / 255f
                                gSum += (argb shr 8 and 0xFF) / 255f
                                bSum += (argb and 0xFF) / 255f
                            }
                            val avgColor = Color(rSum / 25, gSum / 25, bSum / 25)
                            val avgLuminance = 0.2126f * (rSum / 25) + 0.7152f * (gSum / 25) + 0.0722f * (bSum / 25)
                            val result = deriveReadableColor(avgColor, avgLuminance)
                            if (contentColorAnimation.targetValue != result) {
                                contentColorAnimation.animateTo(result, tween(300))
                            }
                        } catch (e: Exception) {
                            val defaultColor = turnlyColors.resolveForeground(turnlyColors.surface)
                            if (contentColorAnimation.targetValue != defaultColor) {
                                contentColorAnimation.animateTo(defaultColor, tween(200))
                            }
                        }
                    }
                    delay(refreshInterval.toLong().coerceAtLeast(300L))
                }
            } else {
                val baseColor = if (fallbackMode == 1) Color(paletteColor) else Color(customColor)
                if (layer != null) {
                    while (true) {
                        if (!isScrollInProgress) {
                            try {
                                val imageBitmap = layer.toImageBitmap()
                                val thumbnail = imageBitmap.scale(1, 1)
                                thumbnail.readPixels(buffer)
                                val argb = buffer[0]
                                val bgLuminance = (0.2126f * (argb shr 16 and 0xFF) + 0.7152f * (argb shr 8 and 0xFF) + 0.0722f * (argb and 0xFF)) / 255f
                                val contrastColor = ensureContrast(baseColor, bgLuminance)
                                if (contentColorAnimation.targetValue != contrastColor) {
                                    contentColorAnimation.animateTo(contrastColor, tween(200))
                                }
                            } catch (e: Exception) {
                                if (contentColorAnimation.targetValue != baseColor) {
                                    contentColorAnimation.animateTo(baseColor, tween(200))
                                }
                            }
                        }
                        delay(refreshInterval.toLong().coerceAtLeast(300L))
                    }
                } else {
                    contentColorAnimation.animateTo(baseColor, tween(200))
                }
            }
        }
    }

    Box(modifier = Modifier.onGloballyPositioned { coords ->
        lastPosition = coords.localToWindow(androidx.compose.ui.geometry.Offset.Zero)
    }) {
        CompositionLocalProvider(
            LocalPrismalAdaptiveColor provides contentColorAnimation.value,
            androidx.compose.material3.LocalContentColor provides contentColorAnimation.value,
            LocalAdaptiveLuminanceActive provides true
        ) {
            content()
        }
    }
}

private fun deriveReadableColor(averageColor: Color, backgroundLuminance: Float): Color {
    val isDarkBackground = backgroundLuminance < 0.5f
    return if (isDarkBackground) {
        if (averageColor.luminance() < 0.6f) lerp(averageColor, Color.White, 0.7f) else averageColor
    } else {
        if (averageColor.luminance() > 0.4f) lerp(averageColor, Color.Black, 0.7f) else averageColor
    }
}

private fun ensureContrast(color: Color, backgroundLuminance: Float): Color {
    val isDarkBackground = backgroundLuminance < 0.5f
    return if (isDarkBackground) {
        if (color.luminance() < 0.5f) lerp(color, Color.White, 0.7f) else color
    } else {
        if (color.luminance() > 0.5f) lerp(color, Color.Black, 0.7f) else color
    }
}

@Composable
fun PrismalScene(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val localView = LocalView.current
    val host = remember(localView) { localView.rootView as? ViewGroup }

    CompositionLocalProvider(
        LocalPrismalCaptureHost provides host,
        LocalPrismalSceneVersion provides 0L
    ) {
        Box(modifier = modifier) {
            content()
        }
    }
}

data class GlassSettings(
    val blurRadius: Float = 2.8f,
    val cornerRadius: Float = 28f,
    val refractionHeight: Float = 12f,
    val refractionAmount: Float = 24f,
    val chromaticAberration: Float = 0f,
    val ior: Float = 1.52f,
    val thickness: Float = 18f,
    val normalStrength: Float = 1.1f,
    val brightness: Float = 1.0f,
    val rimIntensity: Float = 0.85f,
    val specularIntensity: Float = 1.0f,
    val shininess: Float = 56f,
    val displacementScale: Float = 0.9f,
    val minSmoothing: Float = 1.8f,
    val highlightWidth: Float = 3.5f,
    val causticIntensity: Float = 0.1f,
    val liquidDome: Float = 0.7f,
    val transmittance: Float = 1.0f,
    val lightDirX: Float = -0.5f,
    val lightDirY: Float = -0.8f,
    val shadowColor: Int = 0x00000000,
    val shadowIntensity: Float = 0.18f,
    val shadowSoftness: Float = 0.2f,
    val captureDownsample: String = "balanced"
) {
    companion object {
        fun fromPreferences(prefs: com.crescentapps.turnly.data.preferences.UserPreferences): GlassSettings {
            return GlassSettings(
                blurRadius = prefs.glassBlurRadius,
                cornerRadius = prefs.glassCornerRadius,
                refractionHeight = prefs.glassRefractionHeight,
                refractionAmount = prefs.glassRefractionAmount,
                chromaticAberration = prefs.glassChromaticAberration,
                ior = prefs.glassIOR,
                thickness = prefs.glassThickness,
                normalStrength = prefs.glassNormalStrength,
                brightness = prefs.glassBrightness,
                rimIntensity = prefs.glassRimIntensity,
                specularIntensity = prefs.glassSpecularIntensity,
                shininess = prefs.glassShininess,
                displacementScale = prefs.glassDisplacementScale,
                minSmoothing = prefs.glassMinSmoothing,
                highlightWidth = prefs.glassHighlightWidth,
                causticIntensity = prefs.glassCausticIntensity,
                liquidDome = prefs.glassLiquidDome,
                transmittance = prefs.glassTransmittance,
                lightDirX = prefs.glassLightDirX,
                lightDirY = prefs.glassLightDirY,
                shadowColor = prefs.glassShadowColor,
                shadowIntensity = prefs.glassShadowIntensity,
                shadowSoftness = prefs.glassShadowSoftness,
                captureDownsample = prefs.glassCaptureDownsample
            )
        }
    }
}

data class DockSettings(
    val blurRadius: Float = 8f,
    val cornerRadius: Float = 32f,
    val refractionHeight: Float = 24f,
    val refractionAmount: Float = 24f,
    val chromaticAberration: Float = 0.01f
)

val LocalGlassSettings = staticCompositionLocalOf { GlassSettings() }
val LocalDockSettings = staticCompositionLocalOf { DockSettings() }

@Composable
fun PrismalSurface(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.medium,
    tonalColor: Color? = null,
    thickness: Float? = null,
    blurRadius: Float? = null,
    onClick: (() -> Unit)? = null,
    adaptiveLuminance: Boolean = true,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val captureHost = LocalPrismalCaptureHost.current
    val turnlyColors = LocalTurnlyColors.current
    val isDark = turnlyColors.isDark
    val glassSettings = LocalGlassSettings.current
    val glassIntensity = LocalGlassIntensity.current

    val rim = if (isDark) 0.75f else 0.95f
    val specular = if (isDark) 0.9f else 1.15f

    val cornerRadiusPx = remember(shape, density) {
        if (shape is RoundedCornerShape) {
            shape.topStart.toPx(androidx.compose.ui.geometry.Size(1000f, 1000f), density)
        } else {
            0f
        }
    }

    val isLuminanceRoot = !LocalAdaptiveLuminanceActive.current
    val layer = if (isLuminanceRoot) rememberGraphicsLayer() else null
    val backdrop = com.crescentapps.turnly.presentation.catalog.utils.LocalBackdrop.current

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    var isAppVisible by remember { mutableStateOf(true) }
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            isAppVisible = event == androidx.lifecycle.Lifecycle.Event.ON_RESUME || event == androidx.lifecycle.Lifecycle.Event.ON_START
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val effectiveSurfaceBg = tonalColor ?: turnlyColors.surface

    AdaptiveLuminanceProvider(layer = layer, enabled = adaptiveLuminance) {
        Box(
            modifier = modifier.drawBehind {
                if (isLuminanceRoot && isAppVisible) {
                    val w = size.width.toInt().coerceAtLeast(1)
                    val h = size.height.toInt().coerceAtLeast(1)
                    layer?.record(
                        density = this,
                        layoutDirection = layoutDirection,
                        size = androidx.compose.ui.unit.IntSize(w, h)
                    ) {
                        if (backdrop != null) {
                            with(backdrop) {
                                drawBackdrop(this@drawBehind, null, null)
                            }
                            tonalColor?.let { drawRect(it) }
                        } else {
                            drawRect(effectiveSurfaceBg)
                        }
                    }
                }
            },
            contentAlignment = Alignment.Center
        ) {
            AndroidView(
                modifier = Modifier.matchParentSize(),
                factory = { ctx ->
                    SafePrismalFrameLayout(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        setCaptureHost(captureHost)
                        PrismalLiquidGlass.applyBase(this)
                        applyCalibratedSettings(this, glassSettings, density, rim, specular, glassIntensity)
                        thickness?.let { setThickness(density.run { it.dp.toPx() }) }
                        blurRadius?.let { setBlurRadius(density.run { it.dp.toPx() }) }
                        tonalColor?.let { setGlassColor(it.toArgb()) }
                        setCornerRadius(cornerRadiusPx)
                        if (onClick != null) { setOnClickWithAnimationListener { onClick() } }
                    }
                },
                update = { view ->
                    view.setCaptureHost(captureHost)
                    applyCalibratedSettings(view, glassSettings, density, rim, specular, glassIntensity)
                    thickness?.let { view.setThickness(density.run { it.dp.toPx() }) }
                    blurRadius?.let { view.setBlurRadius(density.run { it.dp.toPx() }) }
                    tonalColor?.let { view.setGlassColor(it.toArgb()) }
                    view.setCornerRadius(cornerRadiusPx)
                    if (onClick != null) {
                        view.setOnClickWithAnimationListener { onClick() }
                    } else {
                        view.setOnClickListener(null)
                        view.isClickable = false
                    }
                }
            )
            content()
        }
    }
}

/**
 * SAFE PRISMAL FRAME LAYOUT
 *
 * ROOT CAUSE FIX FOR:
 * java.lang.IllegalArgumentException: Cannot coerce value to an empty range:
 * maximum 1.9499999 is less than minimum 3.2.
 * at com.matrix.prismal.renderer.PrismalGlassRenderer.onDrawFrame(PrismalGlassRenderer.kt:366)
 *
 * Inside PrismalGlassRenderer.onDrawFrame, Kotlin evaluates:
 *   val minDim = Math.min(glassWidth, glassHeight)
 *   val factor = 0.65f + 0.10f * smoothstep(...)
 *   val maxRefractPx = minDim * factor
 *   uLensRefractionPx = (calcVal).coerceIn(3.2f, maxRefractPx)
 *
 * When minDim < 4.92f (e.g. 3.0f during initial layout, window animations, or zero-size frames),
 * maxRefractPx evaluates to 1.95f, creating an empty range 3.2f .. 1.95f, which throws an IAE.
 *
 * SafePrismalFrameLayout intercepts dimensions and enforces that glass size reported to the
 * renderer is never less than 16.0f (safe well above the 4.92f threshold), and seeds the initial
 * glass size so that the first draw frame before layout pass has a guaranteed valid dimension range.
 */
class SafePrismalFrameLayout(context: android.content.Context) : PrismalFrameLayout(context) {

    companion object {
        const val SAFE_MIN_DIM = 24.0f
    }

    init {
        // Seed initial glass size immediately so initial GL render passes never execute with 0px dimensions
        setGlassSize(SAFE_MIN_DIM, SAFE_MIN_DIM)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        // Ensure measured dimensions are never sub-threshold before Prismal's internal layoutListener reads them
        val measuredW = measuredWidth
        val measuredH = measuredHeight
        val safeMinPx = SAFE_MIN_DIM.toInt()
        val finalW = if (measuredW < safeMinPx) safeMinPx else measuredW
        val finalH = if (measuredH < safeMinPx) safeMinPx else measuredH
        if (finalW != measuredW || finalH != measuredH) {
            setMeasuredDimension(finalW, finalH)
        }
        setGlassSize(maxOf(finalW.toFloat(), SAFE_MIN_DIM), maxOf(finalH.toFloat(), SAFE_MIN_DIM))
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val safeW = if (w > 0) maxOf(w.toFloat(), SAFE_MIN_DIM) else SAFE_MIN_DIM
        val safeH = if (h > 0) maxOf(h.toFloat(), SAFE_MIN_DIM) else SAFE_MIN_DIM
        setGlassSize(safeW, safeH)
    }
}

@Composable
fun PrismalCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(28.dp),
    tonalColor: Color? = null,
    thickness: Float? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    PrismalSurface(
        modifier = modifier,
        shape = shape,
        tonalColor = tonalColor,
        thickness = thickness,
        onClick = onClick,
        content = content
    )
}

@Composable
fun PrismalChip(
    modifier: Modifier = Modifier,
    tonalColor: Color? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    PrismalSurface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        tonalColor = tonalColor,
        thickness = 6f,
        blurRadius = 2.5f,
        onClick = onClick,
        content = content
    )
}

@Composable
fun PrismalButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tonalColor: Color? = null,
    shape: Shape = MaterialTheme.shapes.large,
    content: @Composable () -> Unit
) {
    PrismalSurface(
        modifier = modifier,
        shape = shape,
        tonalColor = tonalColor,
        thickness = 5f,
        onClick = onClick
    ) {
        Box(modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)) {
            content()
        }
    }
}

@Composable
fun PrismalIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tonalColor: Color? = null,
    content: @Composable () -> Unit
) {
    PrismalSurface(
        modifier = modifier.size(48.dp),
        shape = CircleShape,
        tonalColor = tonalColor,
        thickness = 5f,
        blurRadius = 2.5f,
        onClick = onClick
    ) {
        content()
    }
}

@Composable
fun PrismalDialog(
    onDismissRequest: () -> Unit,
    tonalColor: Color? = null,
    title: String = "",
    message: String = "",
    positiveText: String = "Confirm",
    negativeText: String? = "Cancel",
    onPositive: () -> Unit = {},
    isDestructive: Boolean = false,
    content: @Composable (ColumnScope.() -> Unit)? = null
) {
    val captureHost = LocalPrismalCaptureHost.current

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = true
        )
    ) {
        var showDialog by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) { showDialog = true }

        val scale by animateFloatAsState(
            targetValue = if (showDialog) 1f else 0.96f,
            animationSpec = spring(dampingRatio = 0.55f, stiffness = 380f),
            label = "DialogScale"
        )
        val alpha by animateFloatAsState(
            targetValue = if (showDialog) 1f else 0f,
            animationSpec = tween(280),
            label = "DialogAlpha"
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.45f * alpha))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismissRequest
                ),
            contentAlignment = Alignment.Center
        ) {
            val sizeDetails = LocalAppWindowSizeDetails.current
            val dialogWidth = when (sizeDetails.widthClass) {
                AppWindowWidthSizeClass.COMPACT -> sizeDetails.widthDp.dp * 0.92f
                AppWindowWidthSizeClass.MEDIUM -> 340.dp
                AppWindowWidthSizeClass.EXPANDED -> 460.dp
            }

            CompositionLocalProvider(LocalPrismalCaptureHost provides captureHost) {
                PrismalSurface(
                    modifier = Modifier
                        .width(dialogWidth)
                        .padding(horizontal = if (sizeDetails.widthClass == AppWindowWidthSizeClass.COMPACT) 8.dp else 24.dp)
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            alpha = alpha
                        )
                        .clickable(enabled = false) {},
                    shape = RoundedCornerShape(38.dp),
                    tonalColor = tonalColor ?: Color(0x1AFFFFFF),
                    thickness = 0.8f,
                    blurRadius = 18f
                ) {
                    Column(
                        modifier = Modifier
                            .padding(28.dp)
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp,
                                letterSpacing = (-0.5).sp
                            ),
                            color = LocalPrismalAdaptiveColor.current
                        )

                        if (message.isNotEmpty()) {
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = message,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 15.sp,
                                    lineHeight = 20.sp
                                ),
                                color = LocalPrismalAdaptiveColor.current.copy(alpha = 0.7f)
                            )
                        }

                        if (content != null) {
                            Spacer(Modifier.height(20.dp))
                            content()
                        }

                        Spacer(Modifier.height(28.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            if (negativeText != null) {
                                PrismalDialogButton(
                                    text = negativeText,
                                    onClick = onDismissRequest,
                                    modifier = Modifier.weight(1f),
                                    tonalColor = Color.White.copy(alpha = 0.08f),
                                    contentColor = LocalPrismalAdaptiveColor.current
                                )
                            }

                            val posColor = if (isDestructive) Color.Transparent else MaterialTheme.colorScheme.primary
                            val posTextColor = if (isDestructive) Color(0xFFFF5252) else (if (posColor.luminance() > 0.5f) Color.Black else Color.White)

                            PrismalDialogButton(
                                text = positiveText,
                                onClick = {
                                    onPositive()
                                    onDismissRequest()
                                },
                                modifier = Modifier.weight(1f),
                                tonalColor = posColor,
                                contentColor = posTextColor
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PrismalDialogButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tonalColor: Color? = null,
    contentColor: Color? = null
) {
    PrismalSurface(
        modifier = modifier.height(52.dp),
        shape = CircleShape,
        tonalColor = tonalColor,
        onClick = onClick,
        thickness = 1.0f,
        blurRadius = 6f
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                ),
                color = contentColor ?: LocalPrismalAdaptiveColor.current
            )
        }
    }
}

private fun applyCalibratedSettings(
    view: PrismalFrameLayout,
    settings: GlassSettings,
    density: androidx.compose.ui.unit.Density,
    rim: Float,
    specular: Float,
    intensityScale: Float = 1.0f
) {
    // STABILITY PASS: Explicitly sanitize and clamp all values to prevent library internal coerceIn failures.
    val d = if (density.density.isFinite() && density.density > 0.1f) density.density else 1.0f
    val safeIntensity = if (intensityScale.isFinite() && intensityScale >= 0f) intensityScale else 1.0f
    
    val safeIor = if (settings.ior.isFinite()) settings.ior.coerceIn(1.0f, 2.5f) else 1.52f
    view.setIOR(safeIor)
    
    val rawBlur = if (settings.blurRadius.isFinite()) settings.blurRadius else 2.8f
    val blurRadiusPx = (rawBlur * safeIntensity).dpToPx(d)
    view.setBlurRadius(blurRadiusPx.coerceIn(1.0f, 200.0f))
    
    val safeNormal = if (settings.normalStrength.isFinite()) settings.normalStrength.coerceIn(0f, 5f) else 1.1f
    view.setNormalStrength(safeNormal)
    
    val rawThickness = if (settings.thickness.isFinite()) settings.thickness else 18f
    val thicknessPx = rawThickness.dpToPx(d)
    view.setThickness(thicknessPx.coerceIn(1.0f, 500.0f))
    
    val safeDisp = if (settings.displacementScale.isFinite()) settings.displacementScale.coerceIn(0f, 2f) else 0.9f
    view.setDisplacementScale(safeDisp)
    
    // Aligned with DhikrCounter scaling: Brightness * 1.5 * intensity
    val rawBrightness = if (settings.brightness.isFinite()) settings.brightness else 1.0f
    val finalBrightness = (rawBrightness * 1.5f * safeIntensity).coerceIn(0.1f, 3.0f)
    view.setBrightness(finalBrightness)
    
    val safeRim = if (rim.isFinite()) (rim * safeIntensity).coerceIn(0f, 2f) else 0.85f
    view.setRimStrength(safeRim)

    val safeSpecular = if (specular.isFinite()) (specular * safeIntensity).coerceIn(0f, 2f) else 1.0f
    val safeShininess = if (settings.shininess.isFinite()) settings.shininess.coerceIn(1f, 128f) else 56f
    view.setSpecular(safeSpecular, safeShininess)
    
    val safeHighlightWidth = if (settings.highlightWidth.isFinite()) settings.highlightWidth.coerceIn(0f, 20f) else 3.5f
    view.setHighlightWidth(safeHighlightWidth)

    val safeCaustic = if (settings.causticIntensity.isFinite()) (settings.causticIntensity * safeIntensity).coerceIn(0f, 2f) else 0.1f
    view.setCausticIntensity(safeCaustic)

    val safeDome = if (settings.liquidDome.isFinite()) settings.liquidDome.coerceIn(0f, 2f) else 0.7f
    view.setLiquidDomeStrength(safeDome)
    view.setFresnelReflectStrength(1.0f)
    
    val rawRefractAmount = if (settings.refractionAmount.isFinite()) settings.refractionAmount else 24f
    view.setLensRefractionScale((rawRefractAmount / 24f).coerceIn(0f, 5f))

    val safeLightX = if (settings.lightDirX.isFinite()) settings.lightDirX.coerceIn(-1f, 1f) else -0.5f
    val safeLightY = if (settings.lightDirY.isFinite()) settings.lightDirY.coerceIn(-1f, 1f) else -0.8f
    view.setLightDirection(safeLightX, safeLightY)

    val safeTransmittance = if (settings.transmittance.isFinite()) settings.transmittance.coerceIn(0f, 1f) else 1.0f
    view.setTransmittance(safeTransmittance)

    val safeSmoothing = if (settings.minSmoothing.isFinite()) settings.minSmoothing.coerceIn(0f, 10f) else 1.8f
    view.setMinSmoothing(safeSmoothing)
    
    val rawChroma = if (settings.chromaticAberration.isFinite()) settings.chromaticAberration else 0.0f
    view.setChromaticAberration((rawChroma * safeIntensity).coerceIn(0f, 1f))
    
    val rawShadowIntensity = if (settings.shadowIntensity.isFinite()) settings.shadowIntensity else 0.18f
    val shadowAlpha = (rawShadowIntensity * 255).toInt().coerceIn(0, 255)
    val colorWithAlpha = (shadowAlpha shl 24) or (settings.shadowColor and 0x00FFFFFF)
    val safeShadowSoftness = if (settings.shadowSoftness.isFinite()) settings.shadowSoftness.coerceIn(0f, 1f) else 0.2f
    view.setShadowProperties(colorWithAlpha, safeShadowSoftness)
    
    val mode = when (settings.captureDownsample.lowercase()) {
        "off" -> DownsampleMode.OFF
        "subtle" -> DownsampleMode.SUBTLE
        "balanced" -> DownsampleMode.BALANCED
        "aggressive" -> DownsampleMode.AGGRESSIVE
        else -> DownsampleMode.BALANCED
    }
    view.setCaptureDownsample(mode)
    view.setClickAnimationPressScale(0.97f)
}

private fun Float.dpToPx(density: Float): Float = this * density

enum class AppWindowWidthSizeClass { COMPACT, MEDIUM, EXPANDED }
enum class AppWindowHeightSizeClass { COMPACT, MEDIUM, EXPANDED }

data class AppWindowSizeDetails(
    val widthClass: AppWindowWidthSizeClass,
    val heightClass: AppWindowHeightSizeClass,
    val isLandscape: Boolean,
    val widthDp: Int,
    val heightDp: Int
)

val LocalAppWindowSizeDetails = staticCompositionLocalOf {
    AppWindowSizeDetails(
        widthClass = AppWindowWidthSizeClass.MEDIUM,
        heightClass = AppWindowHeightSizeClass.MEDIUM,
        isLandscape = false,
        widthDp = 360,
        heightDp = 640
    )
}
