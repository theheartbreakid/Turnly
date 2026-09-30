package com.crescentapps.turnly.presentation.m3expressive.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.crescentapps.turnly.presentation.m3expressive.theme.ExpressiveTokens
import com.crescentapps.turnly.presentation.m3expressive.theme.ExpressiveShapes

@Composable
fun TurnlyExpressiveCard(
    modifier: Modifier = Modifier,
    shape: Shape = ExpressiveShapes.ExtraLarge, // L-increased or XL for large interactions
    colors: CardColors = CardDefaults.cardColors(),
    elevation: CardElevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier,
            shape = shape,
            colors = colors,
            elevation = elevation,
            content = content
        )
    } else {
        Card(
            modifier = modifier,
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
            modifier = modifier, 
            colors = colors,
            shape = ExpressiveShapes.Full // Pill shape by default
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
            modifier = modifier, 
            colors = colors,
            shape = ExpressiveShapes.Full // Pill shape by default
        ) {
            if (icon != null) {
                icon()
                Spacer(Modifier.width(ExpressiveTokens.spacing.small))
            }
            Text(text)
        }
    }
}
