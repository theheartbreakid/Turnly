package com.crescentapps.turnly.presentation.m3expressive.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FileCopy
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crescentapps.turnly.presentation.m3expressive.theme.ExpressiveTokens
import com.crescentapps.turnly.presentation.m3expressive.theme.ExpressiveShapes

@Composable
fun TurnlyExpressiveCard(
    modifier: Modifier = Modifier,
    shape: Shape = ExpressiveShapes.ExtraLarge,
    colors: CardColors = CardDefaults.cardColors(),
    elevation: CardElevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier.expressiveLightRay(shape = shape),
            shape = shape,
            colors = colors,
            elevation = elevation,
            content = content
        )
    } else {
        Card(
            modifier = modifier.expressiveLightRay(shape = shape),
            shape = shape,
            colors = colors,
            elevation = elevation,
            content = content
        )
    }
}

@Composable
fun TurnlyExpressiveButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String,
    icon: (@Composable () -> Unit)? = null,
    isPrimary: Boolean = true,
    isDestructive: Boolean = false
) {
    val colors = if (isDestructive) {
        ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onError
        )
    } else if (isPrimary) {
        ButtonDefaults.buttonColors()
    } else {
        ButtonDefaults.filledTonalButtonColors()
    }

    if (isPrimary || isDestructive) {
        Button(
            onClick = onClick, 
            modifier = modifier.expressiveLightRay(shape = ExpressiveShapes.Full), 
            colors = colors,
            shape = ExpressiveShapes.Full
        ) {
            if (icon != null) {
                icon()
                Spacer(Modifier.width(ExpressiveTokens.spacing.small))
            }
            Text(text)
        }
    } else {
        FilledTonalButton(
            onClick = onClick, 
            modifier = modifier.expressiveLightRay(shape = ExpressiveShapes.Full), 
            colors = colors,
            shape = ExpressiveShapes.Full
        ) {
            if (icon != null) {
                icon()
                Spacer(Modifier.width(ExpressiveTokens.spacing.small))
            }
            Text(text)
        }
    }
}

@Composable
fun TurnlyExpressiveSplitButton(
    onPrimaryClick: () -> Unit,
    primaryText: String = "New Schedule",
    modifier: Modifier = Modifier,
    primaryIcon: (@Composable () -> Unit)? = null
) {
    Button(
        onClick = onPrimaryClick,
        shape = ExpressiveShapes.Full,
        modifier = modifier
    ) {
        if (primaryIcon != null) {
            primaryIcon()
            Spacer(Modifier.width(8.dp))
        }
        Text(primaryText, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun TurnlyExpressiveLivePreview(
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Live Appearance Preview",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Here we apply the expressiveLightRay modifier for live preview
                // It will only render if enabled, and read current local composition
                val primaryColor = MaterialTheme.colorScheme.primary
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .expressiveLightRay(
                            shape = MaterialTheme.shapes.extraLarge
                        ),
                    shape = MaterialTheme.shapes.extraLarge,
                    color = primaryColor,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("Primary", fontWeight = FontWeight.Bold)
                    }
                }
                
                FilledTonalButton(
                    onClick = {},
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.extraLarge
                ) {
                    Text("Tonal")
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = true,
                    onClick = {},
                    label = { Text("Active Chip") }
                )
                AssistChip(
                    onClick = {},
                    label = { Text("Next Turn • 2:00 PM") }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TurnlyExpressiveLoadingIndicator(
    modifier: Modifier = Modifier
) {
    LoadingIndicator(modifier = modifier)
}
