package com.crescentapps.turnly.presentation.m3expressive.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
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
            modifier = modifier, 
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TurnlyExpressiveSplitButton(
    onPrimaryClick: () -> Unit,
    onSecondaryClick: () -> Unit,
    primaryText: String,
    modifier: Modifier = Modifier,
    primaryIcon: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = onPrimaryClick,
            shape = RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp, topEnd = 4.dp, bottomEnd = 4.dp)
        ) {
            if (primaryIcon != null) {
                primaryIcon()
                Spacer(Modifier.width(ExpressiveTokens.spacing.small))
            }
            Text(primaryText)
        }
        Spacer(Modifier.width(2.dp))
        FilledTonalButton(
            onClick = onSecondaryClick,
            shape = RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp, topEnd = 24.dp, bottomEnd = 24.dp),
            contentPadding = PaddingValues(horizontal = 12.dp)
        ) {
            Text("▼")
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
