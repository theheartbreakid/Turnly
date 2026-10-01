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

@Composable
fun TurnlyExpressiveSplitButton(
    onPrimaryClick: () -> Unit,
    onOptionSelected: (String) -> Unit = {},
    primaryText: String = "New Schedule",
    modifier: Modifier = Modifier,
    primaryIcon: (@Composable () -> Unit)? = null
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(
                onClick = onPrimaryClick,
                shape = RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp, topEnd = 4.dp, bottomEnd = 4.dp)
            ) {
                if (primaryIcon != null) {
                    primaryIcon()
                    Spacer(Modifier.width(8.dp))
                }
                Text(primaryText, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(2.dp))
            FilledTonalButton(
                onClick = { expanded = true },
                shape = RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp, topEnd = 24.dp, bottomEnd = 24.dp),
                contentPadding = PaddingValues(horizontal = 12.dp)
            ) {
                Text("▼", fontSize = 12.sp)
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            shape = RoundedCornerShape(16.dp)
        ) {
            DropdownMenuItem(
                text = { Text("From Template") },
                leadingIcon = { Icon(Icons.Default.FileCopy, contentDescription = null) },
                onClick = {
                    expanded = false
                    onOptionSelected("TEMPLATE")
                }
            )
            DropdownMenuItem(
                text = { Text("Import from File") },
                leadingIcon = { Icon(Icons.Default.FileDownload, contentDescription = null) },
                onClick = {
                    expanded = false
                    onOptionSelected("IMPORT")
                }
            )
            DropdownMenuItem(
                text = { Text("Duplicate Schedule") },
                leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
                onClick = {
                    expanded = false
                    onOptionSelected("DUPLICATE")
                }
            )
            DropdownMenuItem(
                text = { Text("Create from Calendar") },
                leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                onClick = {
                    expanded = false
                    onOptionSelected("CALENDAR")
                }
            )
        }
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
                Button(
                    onClick = {},
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.extraLarge
                ) {
                    Text("Primary")
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
