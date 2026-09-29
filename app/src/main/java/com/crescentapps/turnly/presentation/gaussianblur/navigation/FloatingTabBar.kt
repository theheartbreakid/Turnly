@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.crescentapps.turnly.presentation.gaussianblur.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Indication
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.roundToInt

private val DefaultTabBarContentModifier: @Composable () -> Modifier = { Modifier }
private val GlassSpring: AnimationSpec<Float> = spring(stiffness = Spring.StiffnessMediumLow)
private const val SQUASH = 0.15f
private const val STRETCH = 0.20f

@Composable
fun FloatingTabBar(
    isInline: Boolean,
    selectedTabKey: Any?,
    modifier: Modifier = Modifier,
    tabBarContentModifier: @Composable () -> Modifier = DefaultTabBarContentModifier,
    inlineAccessory: (@Composable SharedTransitionScope.(Modifier, AnimatedVisibilityScope) -> Unit)? = null,
    expandedAccessory: (@Composable SharedTransitionScope.(Modifier, AnimatedVisibilityScope) -> Unit)? = null,
    colors: FloatingTabBarColors = FloatingTabBarDefaults.colors(),
    shapes: FloatingTabBarShapes = FloatingTabBarDefaults.shapes(),
    sizes: FloatingTabBarSizes = FloatingTabBarDefaults.sizes(),
    elevations: FloatingTabBarElevations = FloatingTabBarDefaults.elevations(),
    contentKey: Any? = null,
    content: FloatingTabBarScope.() -> Unit
) {
    val scrollConnection = rememberFloatingTabBarScrollConnection(
        initialIsInline = isInline,
        inlineBehavior = FloatingTabBarInlineBehavior.Never
    )

    LaunchedEffect(isInline) {
        if (isInline) scrollConnection.inline() else scrollConnection.expand()
    }

    FloatingTabBar(
        selectedTabKey = selectedTabKey,
        scrollConnection = scrollConnection,
        modifier = modifier,
        tabBarContentModifier = tabBarContentModifier,
        inlineAccessory = inlineAccessory,
        expandedAccessory = expandedAccessory,
        colors = colors,
        shapes = shapes,
        sizes = sizes,
        elevations = elevations,
        contentKey = contentKey,
        content = content
    )
}

@Composable
fun FloatingTabBar(
    selectedTabKey: Any?,
    scrollConnection: FloatingTabBarScrollConnection,
    modifier: Modifier = Modifier,
    tabBarContentModifier: @Composable () -> Modifier = DefaultTabBarContentModifier,
    inlineAccessory: (@Composable SharedTransitionScope.(Modifier, AnimatedVisibilityScope) -> Unit)? = null,
    expandedAccessory: (@Composable SharedTransitionScope.(Modifier, AnimatedVisibilityScope) -> Unit)? = null,
    colors: FloatingTabBarColors = FloatingTabBarDefaults.colors(),
    shapes: FloatingTabBarShapes = FloatingTabBarDefaults.shapes(),
    sizes: FloatingTabBarSizes = FloatingTabBarDefaults.sizes(),
    elevations: FloatingTabBarElevations = FloatingTabBarDefaults.elevations(),
    contentKey: Any? = null,
    content: FloatingTabBarScope.() -> Unit
) {
    val scope = remember(contentKey) { FloatingTabBarScopeImpl().apply { content() } }
    val isAccessoryShared = inlineAccessory != null && expandedAccessory != null

    SharedTransitionLayout(modifier = modifier) {
        AnimatedContent(
            targetState = scrollConnection.isInline,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            contentAlignment = Alignment.BottomCenter
        ) { isInline ->
            if (isInline) {
                InlineBar(
                    scope = scope,
                    selectedTabKey = selectedTabKey,
                    accessory = inlineAccessory,
                    isAccessoryShared = isAccessoryShared,
                    onInlineTabClick = { scrollConnection.expand() },
                    colors = colors,
                    shapes = shapes,
                    sizes = sizes,
                    elevations = elevations,
                    tabBarContentModifier = tabBarContentModifier,
                    animatedVisibilityScope = this@AnimatedContent
                )
            } else {
                ExpandedBar(
                    scope = scope,
                    selectedTabKey = selectedTabKey,
                    accessory = expandedAccessory,
                    isAccessoryShared = isAccessoryShared,
                    colors = colors,
                    shapes = shapes,
                    sizes = sizes,
                    elevations = elevations,
                    tabBarContentModifier = tabBarContentModifier,
                    animatedVisibilityScope = this@AnimatedContent
                )
            }
        }
    }
}

class FloatingTabBarScrollConnection(
    initialIsInline: Boolean = false,
    private val scrollThresholdPx: Float,
    private val inlineBehavior: FloatingTabBarInlineBehavior = FloatingTabBarInlineBehavior.OnScrollDown
) : NestedScrollConnection {
    var isInline by mutableStateOf(initialIsInline)
        private set

    private var accumulatedScroll = 0f

    fun expand() {
        isInline = false
        accumulatedScroll = 0f
    }

    fun inline() {
        isInline = true
        accumulatedScroll = 0f
    }

    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
        if (inlineBehavior == FloatingTabBarInlineBehavior.Never) return Offset.Zero

        val scrollDelta = available.y
        if ((accumulatedScroll > 0 && scrollDelta < 0) || (accumulatedScroll < 0 && scrollDelta > 0)) {
            accumulatedScroll = 0f
        }
        accumulatedScroll += scrollDelta

        when (inlineBehavior) {
            FloatingTabBarInlineBehavior.OnScrollDown -> {
                if (accumulatedScroll <= -scrollThresholdPx && !isInline) {
                    isInline = true
                    accumulatedScroll = 0f
                } else if (accumulatedScroll >= scrollThresholdPx && isInline) {
                    isInline = false
                    accumulatedScroll = 0f
                }
            }
            FloatingTabBarInlineBehavior.OnScrollUp -> {
                if (accumulatedScroll >= scrollThresholdPx && !isInline) {
                    isInline = true
                    accumulatedScroll = 0f
                } else if (accumulatedScroll <= -scrollThresholdPx && isInline) {
                    isInline = false
                    accumulatedScroll = 0f
                }
            }
            FloatingTabBarInlineBehavior.Never -> {}
        }
        return Offset.Zero
    }
}

@Composable
fun rememberFloatingTabBarScrollConnection(
    initialIsInline: Boolean = false,
    scrollThreshold: Dp = 50.dp,
    inlineBehavior: FloatingTabBarInlineBehavior = FloatingTabBarInlineBehavior.OnScrollDown
): FloatingTabBarScrollConnection = with(LocalDensity.current) {
    val scrollThresholdPx = scrollThreshold.toPx()
    remember(scrollThresholdPx, inlineBehavior, initialIsInline) {
        FloatingTabBarScrollConnection(initialIsInline, scrollThresholdPx, inlineBehavior)
    }
}

enum class FloatingTabBarInlineBehavior {
    Never,
    OnScrollDown,
    OnScrollUp
}

interface FloatingTabBarScope {
    fun tab(
        key: Any,
        title: @Composable () -> Unit,
        icon: @Composable () -> Unit,
        onClick: () -> Unit,
        indication: (@Composable () -> Indication)? = { LocalIndication.current }
    )

    fun standaloneTab(
        key: Any,
        icon: @Composable () -> Unit,
        onClick: () -> Unit,
        indication: (@Composable () -> Indication)? = { LocalIndication.current }
    )
}

@Composable
private fun SharedTransitionScope.InlineBar(
    scope: FloatingTabBarScopeImpl,
    selectedTabKey: Any?,
    accessory: (@Composable SharedTransitionScope.(Modifier, AnimatedVisibilityScope) -> Unit)?,
    isAccessoryShared: Boolean,
    onInlineTabClick: () -> Unit,
    colors: FloatingTabBarColors,
    shapes: FloatingTabBarShapes,
    sizes: FloatingTabBarSizes,
    elevations: FloatingTabBarElevations,
    tabBarContentModifier: @Composable () -> Modifier,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    val inlineTab = scope.getInlineTab(selectedTabKey)
    val standaloneTab = scope.standaloneTab
    val hasInlineTab = inlineTab != null

    Row(
        horizontalArrangement = Arrangement.spacedBy(sizes.componentSpacing),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .then(if (accessory == null) Modifier.wrapContentWidth() else Modifier)
            .height(IntrinsicSize.Max)
    ) {
        if (hasInlineTab) {
            InlineTab(
                inlineTab = inlineTab,
                onInlineTabClick = onInlineTabClick,
                shapes = shapes,
                sizes = sizes,
                colors = colors,
                elevations = elevations,
                animatedVisibilityScope = animatedVisibilityScope,
                tabBarContentModifier = tabBarContentModifier,
                modifier = Modifier
            )
        }

        if (accessory != null) {
            InlineAccessory(
                accessory = accessory,
                isAccessoryShared = isAccessoryShared,
                shapes = shapes,
                colors = colors,
                elevations = elevations,
                animatedVisibilityScope = animatedVisibilityScope,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }

        if (standaloneTab != null) {
            InlineStandaloneTab(
                standaloneTab = standaloneTab,
                shapes = shapes,
                colors = colors,
                elevations = elevations,
                animatedVisibilityScope = animatedVisibilityScope,
                tabBarContentModifier = tabBarContentModifier,
                modifier = Modifier
                    .fillMaxHeight()
                    .aspectRatio(1f)
            )
        }
    }
}

@Composable
private fun SharedTransitionScope.InlineTab(
    inlineTab: FloatingTabBarTab,
    onInlineTabClick: () -> Unit,
    shapes: FloatingTabBarShapes,
    sizes: FloatingTabBarSizes,
    colors: FloatingTabBarColors,
    elevations: FloatingTabBarElevations,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier,
    tabBarContentModifier: @Composable () -> Modifier
) {
    Box(
        modifier = modifier
            .sharedElement(
                sharedContentState = rememberSharedContentState("tabGroup"),
                animatedVisibilityScope = animatedVisibilityScope,
                zIndexInOverlay = 1f
            )
            .shadow(
                shape = shapes.tabBarShape,
                elevation = elevations.inlineElevation
            )
            .background(
                color = colors.backgroundColor,
                shape = shapes.tabBarShape
            )
            .clip(shapes.tabBarShape)
            .then(tabBarContentModifier())
            .clickable(
                onClick = {
                    onInlineTabClick()
                    inlineTab.onClick()
                },
                indication = inlineTab.indication?.invoke(),
                interactionSource = remember { MutableInteractionSource() }
            )
            .padding(sizes.tabInlineContentPadding)
    ) {
        Tab(
            icon = {
                Box(
                    Modifier.sharedElement(
                        sharedContentState = rememberSharedContentState("tab#${inlineTab.key}-icon"),
                        animatedVisibilityScope = animatedVisibilityScope,
                        zIndexInOverlay = 1f
                    )
                ) {
                    inlineTab.icon()
                }
            },
            title = { inlineTab.title() },
            isInline = true
        )
    }
}

@Composable
private fun SharedTransitionScope.InlineStandaloneTab(
    standaloneTab: FloatingTabBarTab,
    shapes: FloatingTabBarShapes,
    colors: FloatingTabBarColors,
    elevations: FloatingTabBarElevations,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier,
    tabBarContentModifier: @Composable () -> Modifier
) {
    Tab(
        icon = standaloneTab.icon,
        title = standaloneTab.title,
        isInline = true,
        isStandalone = true,
        modifier = modifier
            .sharedElement(
                sharedContentState = rememberSharedContentState("standaloneTab"),
                animatedVisibilityScope = animatedVisibilityScope,
                zIndexInOverlay = 1f
            )
            .shadow(
                shape = shapes.standaloneTabShape,
                elevation = elevations.inlineElevation
            )
            .background(
                color = colors.backgroundColor,
                shape = shapes.standaloneTabShape
            )
            .clip(shapes.standaloneTabShape)
            .then(tabBarContentModifier())
            .clickable(
                onClick = standaloneTab.onClick,
                indication = standaloneTab.indication?.invoke(),
                interactionSource = remember { MutableInteractionSource() }
            )
    )
}

@Composable
private fun SharedTransitionScope.InlineAccessory(
    accessory: (@Composable SharedTransitionScope.(Modifier, AnimatedVisibilityScope) -> Unit)?,
    isAccessoryShared: Boolean,
    colors: FloatingTabBarColors,
    shapes: FloatingTabBarShapes,
    elevations: FloatingTabBarElevations,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier
) {
    accessory?.let { acc ->
        Box(
            modifier = modifier.then(
                if (isAccessoryShared) {
                    Modifier.sharedElement(
                        sharedContentState = rememberSharedContentState("accessory"),
                        animatedVisibilityScope = animatedVisibilityScope
                    )
                } else {
                    Modifier.animateEnterExitAccessory(
                        sharedTransitionScope = this,
                        animatedVisibilityScope = animatedVisibilityScope
                    )
                }
            )
        ) {
            acc(
                Modifier
                    .fillMaxSize()
                    .shadow(shape = shapes.accessoryShape, elevation = elevations.inlineElevation)
                    .background(color = colors.accessoryBackgroundColor, shape = shapes.accessoryShape)
                    .clip(shapes.accessoryShape),
                animatedVisibilityScope
            )
        }
    }
}

@Composable
private fun SharedTransitionScope.ExpandedBar(
    scope: FloatingTabBarScopeImpl,
    selectedTabKey: Any?,
    accessory: (@Composable SharedTransitionScope.(Modifier, AnimatedVisibilityScope) -> Unit)?,
    isAccessoryShared: Boolean,
    colors: FloatingTabBarColors,
    shapes: FloatingTabBarShapes,
    sizes: FloatingTabBarSizes,
    elevations: FloatingTabBarElevations,
    tabBarContentModifier: @Composable () -> Modifier,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    val standaloneTab = scope.standaloneTab
    val hasTabGroup = scope.tabs.isNotEmpty()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(sizes.componentSpacing),
        modifier = Modifier.fillMaxWidth()
    ) {
        if (accessory != null) {
            ExpandedAccessory(
                accessory = accessory,
                isAccessoryShared = isAccessoryShared,
                shapes = shapes,
                colors = colors,
                elevations = elevations,
                animatedVisibilityScope = animatedVisibilityScope,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(sizes.componentSpacing),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max)
        ) {
            if (hasTabGroup) {
                ExpandedTabs(
                    scope = scope,
                    selectedTabKey = selectedTabKey,
                    shapes = shapes,
                    sizes = sizes,
                    colors = colors,
                    elevations = elevations,
                    animatedVisibilityScope = animatedVisibilityScope,
                    tabBarContentModifier = tabBarContentModifier,
                    modifier = Modifier.weight(1f)
                )
            }

            if (standaloneTab != null) {
                ExpandedStandaloneTab(
                    standaloneTab = standaloneTab,
                    shapes = shapes,
                    colors = colors,
                    elevations = elevations,
                    animatedVisibilityScope = animatedVisibilityScope,
                    tabBarContentModifier = tabBarContentModifier,
                    modifier = Modifier
                        .fillMaxHeight()
                        .aspectRatio(1f)
                )
            }
        }
    }
}

@Composable
private fun SharedTransitionScope.ExpandedAccessory(
    accessory: @Composable SharedTransitionScope.(Modifier, AnimatedVisibilityScope) -> Unit,
    isAccessoryShared: Boolean,
    colors: FloatingTabBarColors,
    shapes: FloatingTabBarShapes,
    elevations: FloatingTabBarElevations,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier
) {
    Box(
        modifier = modifier.then(
            if (isAccessoryShared) {
                Modifier.sharedElement(
                    sharedContentState = rememberSharedContentState("accessory"),
                    animatedVisibilityScope = animatedVisibilityScope
                )
            } else {
                Modifier.animateEnterExitAccessory(
                    sharedTransitionScope = this,
                    animatedVisibilityScope = animatedVisibilityScope
                )
            }
        )
    ) {
        accessory(
            Modifier
                .shadow(shape = shapes.accessoryShape, elevation = elevations.expandedElevation)
                .background(color = colors.accessoryBackgroundColor, shape = shapes.accessoryShape)
                .clip(shapes.accessoryShape),
            animatedVisibilityScope
        )
    }
}

@Composable
private fun SharedTransitionScope.ExpandedTabs(
    scope: FloatingTabBarScopeImpl,
    selectedTabKey: Any?,
    shapes: FloatingTabBarShapes,
    sizes: FloatingTabBarSizes,
    colors: FloatingTabBarColors,
    elevations: FloatingTabBarElevations,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier,
    tabBarContentModifier: @Composable () -> Modifier
) {
    val inlineTab = scope.getInlineTab(selectedTabKey)
    val selectedTabIndex = scope.tabs.indexOfFirst { it.key == selectedTabKey }
    val currentSelectedTabIndex by rememberUpdatedState(selectedTabIndex)
    val haptics = LocalHapticFeedback.current

    var dragOffset by remember { mutableFloatStateOf(0f) }
    var rowSize by remember { mutableStateOf(IntSize.Zero) }
    var lastHapticTab by remember { mutableIntStateOf(selectedTabIndex) }
    val density = LocalDensity.current
    val tabCount = scope.tabs.size
    val tabSpacingPx = with(density) { sizes.tabSpacing.toPx() }
    val tabWidthPx = if (rowSize.width > 0 && tabCount > 0) {
        (rowSize.width - tabSpacingPx * (tabCount - 1)) / tabCount
    } else 0f
    val tabStepPx = if (rowSize.width > 0 && tabCount > 0) {
        (rowSize.width + tabSpacingPx) / tabCount
    } else 0f
    val indicatorTargetPx = if (selectedTabIndex >= 0 && tabStepPx > 0f) {
        selectedTabIndex * tabStepPx + dragOffset
    } else 0f

    val animatedIndicatorOffset by animateFloatAsState(
        targetValue = indicatorTargetPx,
        animationSpec = GlassSpring,
        label = "glassPillOffset"
    )
    val lag = if (tabStepPx > 0f) {
        (abs(indicatorTargetPx - animatedIndicatorOffset) / tabStepPx).coerceIn(0f, 1f)
    } else 0f

    LaunchedEffect(selectedTabKey) {
        dragOffset = 0f
        lastHapticTab = selectedTabIndex
    }

    Box(
        modifier = modifier
            .sharedElement(
                sharedContentState = rememberSharedContentState("tabGroup"),
                animatedVisibilityScope = animatedVisibilityScope,
                zIndexInOverlay = 1f
            )
            .shadow(shape = shapes.tabBarShape, elevation = elevations.expandedElevation)
            .background(color = colors.backgroundColor, shape = shapes.tabBarShape)
            .clip(shapes.tabBarShape)
            .then(tabBarContentModifier())
            .padding(sizes.tabBarContentPadding)
            .animateContentSize()
    ) {
        if (selectedTabIndex >= 0 && tabWidthPx > 0f) {
            Box(
                modifier = Modifier
                    .width(with(density) { tabWidthPx.toDp() })
                    .height(with(density) { rowSize.height.toDp() })
                    .graphicsLayer {
                        translationX = animatedIndicatorOffset
                        scaleX = 1f + lag * STRETCH
                        scaleY = 1f - lag * STRETCH * SQUASH
                    }
                    .clip(shapes.tabShape)
                    .background(colors.indicatorColor, shapes.tabShape)
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(sizes.tabSpacing),
            modifier = Modifier
                .fillMaxWidth()
                .onSizeChanged { rowSize = it }
                .pointerInput(tabCount, tabStepPx, currentSelectedTabIndex) {
                    if (currentSelectedTabIndex < 0 || tabStepPx <= 0f) return@pointerInput

                    var totalDrag = 0f
                    detectHorizontalDragGestures(
                        onDragStart = { totalDrag = 0f },
                        onDragCancel = { dragOffset = 0f },
                        onDragEnd = {
                            val ratio = totalDrag / tabStepPx
                            val shift = when {
                                ratio > 0.35f -> maxOf(1, ratio.roundToInt())
                                ratio < -0.35f -> minOf(-1, ratio.roundToInt())
                                else -> 0
                            }
                            val newIndex = (currentSelectedTabIndex + shift).coerceIn(0, scope.tabs.lastIndex)
                            if (newIndex != currentSelectedTabIndex) {
                                scope.tabs[newIndex].onClick()
                            }
                            dragOffset = 0f
                        },
                        onHorizontalDrag = { _, delta ->
                            totalDrag += delta
                            dragOffset = when {
                                totalDrag > 0 && currentSelectedTabIndex == scope.tabs.lastIndex -> totalDrag * 0.25f
                                totalDrag < 0 && currentSelectedTabIndex == 0 -> totalDrag * 0.25f
                                else -> totalDrag
                            }

                            val approximateTab = (currentSelectedTabIndex + dragOffset / tabStepPx)
                                .coerceIn(0f, scope.tabs.lastIndex.toFloat())
                                .roundToInt()
                            if (approximateTab != lastHapticTab) {
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                lastHapticTab = approximateTab
                            }
                        }
                    )
                }
        ) {
            scope.tabs.forEach { tab ->
                val isSelected = tab.key == selectedTabKey
                val iconScale by animateFloatAsState(
                    targetValue = if (isSelected) 1.08f else 1f,
                    animationSpec = GlassSpring,
                    label = "glassTabScale"
                )
                Tab(
                    icon = {
                        Box(
                            modifier = (if (tab.key == inlineTab?.key) {
                                Modifier.sharedElement(
                                    sharedContentState = rememberSharedContentState("tab#${tab.key}-icon"),
                                    animatedVisibilityScope = animatedVisibilityScope,
                                    zIndexInOverlay = 1f
                                )
                            } else {
                                Modifier.animateEnterExitTab(
                                    sharedTransitionScope = this@ExpandedTabs,
                                    animatedVisibilityScope = animatedVisibilityScope
                                )
                            }).graphicsLayer {
                                scaleX = iconScale
                                scaleY = iconScale
                            }
                        ) {
                            tab.icon()
                        }
                    },
                    title = {
                        Box(
                            Modifier.animateEnterExitTab(
                                sharedTransitionScope = this@ExpandedTabs,
                                animatedVisibilityScope = animatedVisibilityScope
                            )
                        ) {
                            tab.title()
                        }
                    },
                    isInline = false,
                    modifier = Modifier
                        .weight(1f)
                        .skipToLookaheadSize()
                        .clip(shapes.tabShape)
                        .clickable(
                            onClick = tab.onClick,
                            indication = tab.indication?.invoke(),
                            interactionSource = remember { MutableInteractionSource() }
                        )
                        .padding(sizes.tabExpandedContentPadding)
                )
            }
        }
    }
}

@Composable
private fun SharedTransitionScope.ExpandedStandaloneTab(
    standaloneTab: FloatingTabBarTab,
    shapes: FloatingTabBarShapes,
    colors: FloatingTabBarColors,
    elevations: FloatingTabBarElevations,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier,
    tabBarContentModifier: @Composable () -> Modifier
) {
    Tab(
        icon = standaloneTab.icon,
        title = standaloneTab.title,
        isInline = false,
        isStandalone = true,
        modifier = modifier
            .sharedElement(
                sharedContentState = rememberSharedContentState("standaloneTab"),
                animatedVisibilityScope = animatedVisibilityScope,
                zIndexInOverlay = 1f
            )
            .shadow(shape = shapes.standaloneTabShape, elevation = elevations.expandedElevation)
            .background(color = colors.backgroundColor, shape = shapes.standaloneTabShape)
            .clip(shapes.standaloneTabShape)
            .then(tabBarContentModifier())
            .clickable(
                onClick = standaloneTab.onClick,
                indication = standaloneTab.indication?.invoke(),
                interactionSource = remember { MutableInteractionSource() }
            )
    )
}

@Composable
private fun Tab(
    icon: @Composable () -> Unit,
    title: @Composable () -> Unit,
    isInline: Boolean,
    modifier: Modifier = Modifier,
    isStandalone: Boolean = false
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        icon()
        if (!isStandalone && !isInline) {
            title()
        }
    }
}

@Composable
private fun Modifier.animateEnterExitAccessory(
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
): Modifier = with(sharedTransitionScope) {
    with(animatedVisibilityScope) {
        val animatedAlpha by transition.animateFloat { targetState ->
            when (targetState) {
                EnterExitState.Visible -> 1f
                else -> 0f
            }
        }

        this@animateEnterExitAccessory
            .renderInSharedTransitionScopeOverlay()
            .graphicsLayer(
                compositingStrategy = CompositingStrategy.ModulateAlpha,
                alpha = animatedAlpha
            )
    }
}

@Composable
private fun Modifier.animateEnterExitTab(
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
): Modifier = with(sharedTransitionScope) {
    with(animatedVisibilityScope) {
        val enterStartFraction = 0.5f
        val enterEndFraction = 0.8f
        val durationMs = 150

        val animatedAlpha by transition.animateFloat(
            transitionSpec = {
                keyframes {
                    durationMillis = durationMs
                    if (targetState == EnterExitState.Visible) {
                        0f atFraction enterStartFraction using FastOutSlowInEasing
                        1f atFraction enterEndFraction
                    }
                }
            }
        ) { targetState ->
            when (targetState) {
                EnterExitState.Visible -> 1f
                else -> 0f
            }
        }

        val blurRadius = with(LocalDensity.current) { 12.dp.toPx() }
        val animatedBlur by transition.animateFloat(
            transitionSpec = {
                keyframes {
                    durationMillis = durationMs
                    if (targetState == EnterExitState.Visible) {
                        blurRadius atFraction enterStartFraction using FastOutSlowInEasing
                        0f atFraction enterEndFraction
                    }
                }
            }
        ) { targetState ->
            when (targetState) {
                EnterExitState.Visible -> 0f
                else -> blurRadius
            }
        }

        graphicsLayer {
            alpha = animatedAlpha
            renderEffect = if (animatedBlur > 0f) BlurEffect(animatedBlur, animatedBlur) else null
        }
    }
}

private class FloatingTabBarScopeImpl : FloatingTabBarScope {
    val tabs = mutableStateListOf<FloatingTabBarTab>()
    var standaloneTab: FloatingTabBarTab? by mutableStateOf(null)
        private set
    private var inlineTab: FloatingTabBarTab? = null

    fun getInlineTab(selectedTabKey: Any?): FloatingTabBarTab? {
        return if (selectedTabKey != standaloneTab?.key) {
            val selectedTab = tabs.find { it.key == selectedTabKey }
            if (selectedTab != null) {
                inlineTab = selectedTab
                selectedTab
            } else {
                inlineTab ?: tabs.firstOrNull()
            }
        } else {
            inlineTab ?: tabs.firstOrNull()
        }
    }

    override fun tab(
        key: Any,
        title: @Composable () -> Unit,
        icon: @Composable () -> Unit,
        onClick: () -> Unit,
        indication: (@Composable () -> Indication)?
    ) {
        tabs.add(
            FloatingTabBarTab(
                key = key,
                title = title,
                icon = icon,
                onClick = onClick,
                indication = indication
            )
        )
    }

    override fun standaloneTab(
        key: Any,
        icon: @Composable () -> Unit,
        onClick: () -> Unit,
        indication: (@Composable () -> Indication)?
    ) {
        standaloneTab = FloatingTabBarTab(
            key = key,
            title = {},
            icon = icon,
            onClick = onClick,
            indication = indication
        )
    }
}

private data class FloatingTabBarTab(
    val key: Any,
    val title: @Composable () -> Unit,
    val icon: @Composable () -> Unit,
    val onClick: () -> Unit,
    val indication: (@Composable () -> Indication)?
)

@Immutable
data class FloatingTabBarColors(
    val backgroundColor: Color,
    val accessoryBackgroundColor: Color,
    val indicatorColor: Color,
)

@Immutable
data class FloatingTabBarShapes(
    val tabBarShape: Shape,
    val tabShape: Shape,
    val standaloneTabShape: Shape,
    val accessoryShape: Shape,
)

@Immutable
data class FloatingTabBarElevations(
    val inlineElevation: Dp,
    val expandedElevation: Dp,
)

@Immutable
data class FloatingTabBarSizes(
    val tabBarContentPadding: PaddingValues,
    val tabInlineContentPadding: PaddingValues,
    val tabExpandedContentPadding: PaddingValues,
    val componentSpacing: Dp,
    val tabSpacing: Dp,
)

object FloatingTabBarDefaults {
    @Composable
    fun colors(
        backgroundColor: Color = Color.Transparent,
        accessoryBackgroundColor: Color = Color.Transparent,
        indicatorColor: Color = Color.White.copy(alpha = 0.20f),
    ): FloatingTabBarColors = FloatingTabBarColors(
        backgroundColor = backgroundColor,
        accessoryBackgroundColor = accessoryBackgroundColor,
        indicatorColor = indicatorColor,
    )

    @Composable
    fun shapes(
        tabBarShape: Shape = RoundedCornerShape(100),
        tabShape: Shape = RoundedCornerShape(100),
        standaloneTabShape: Shape = CircleShape,
        accessoryShape: Shape = RoundedCornerShape(100),
    ): FloatingTabBarShapes = FloatingTabBarShapes(
        tabBarShape = tabBarShape,
        tabShape = tabShape,
        standaloneTabShape = standaloneTabShape,
        accessoryShape = accessoryShape,
    )

    @Composable
    fun sizes(
        tabBarContentPadding: PaddingValues = PaddingValues(vertical = 4.dp, horizontal = 4.dp),
        tabInlineContentPadding: PaddingValues = PaddingValues(10.dp),
        tabExpandedContentPadding: PaddingValues = PaddingValues(vertical = 8.dp, horizontal = 16.dp),
        componentSpacing: Dp = 8.dp,
        tabSpacing: Dp = 0.dp,
    ): FloatingTabBarSizes = FloatingTabBarSizes(
        tabBarContentPadding = tabBarContentPadding,
        tabInlineContentPadding = tabInlineContentPadding,
        tabExpandedContentPadding = tabExpandedContentPadding,
        componentSpacing = componentSpacing,
        tabSpacing = tabSpacing,
    )

    @Composable
    fun elevations(
        inlineElevation: Dp = 0.dp,
        expandedElevation: Dp = 0.dp,
    ): FloatingTabBarElevations = FloatingTabBarElevations(
        inlineElevation = inlineElevation,
        expandedElevation = expandedElevation,
    )
}
