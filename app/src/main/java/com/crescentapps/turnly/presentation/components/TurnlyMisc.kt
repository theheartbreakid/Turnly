package com.crescentapps.turnly.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import com.crescentapps.turnly.presentation.components.HorizontalDivider
import com.crescentapps.turnly.presentation.components.CircularProgressIndicator
import com.crescentapps.turnly.presentation.components.LinearProgressIndicator
import com.crescentapps.turnly.presentation.components.RadioButton
import com.crescentapps.turnly.presentation.components.Checkbox

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.crescentapps.turnly.presentation.theme.LocalTurnlyColors

@Composable
fun HorizontalDivider(
    modifier: Modifier = Modifier,
    thickness: androidx.compose.ui.unit.Dp = 1.dp,
    color: Color = LocalTurnlyColors.current.divider
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(thickness)
            .background(color)
    )
}

@Composable
fun CircularProgressIndicator(
    modifier: Modifier = Modifier,
    color: Color = LocalTurnlyColors.current.accent,
    strokeWidth: androidx.compose.ui.unit.Dp = 4.dp
) {
    // A simple placeholder for CircularProgressIndicator using Foundation
    // An actual rotating animation could be added here
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.2f)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(color)
        )
    }
}

@Composable
fun LinearProgressIndicator(
    progress: () -> Float,
    modifier: Modifier = Modifier,
    color: Color = LocalTurnlyColors.current.accent,
    trackColor: Color = LocalTurnlyColors.current.surfaceVariant
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(4.dp)
            .background(trackColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(fraction = progress().coerceIn(0f, 1f))
                .background(color)
        )
    }
}

@Composable
fun RadioButton(
    selected: Boolean,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    selectedColor: Color = LocalTurnlyColors.current.accent,
    unselectedColor: Color = LocalTurnlyColors.current.surfaceVariant
) {
    Box(
        modifier = modifier
            .size(20.dp)
            .clip(CircleShape)
            .background(if (selected) selectedColor else unselectedColor)
            .clickable(enabled = onClick != null, onClick = { onClick?.invoke() }),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            )
        }
    }
}

@Composable
fun Checkbox(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    checkedColor: Color = LocalTurnlyColors.current.accent,
    uncheckedColor: Color = LocalTurnlyColors.current.surfaceVariant
) {
    Box(
        modifier = modifier
            .size(20.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(if (checked) checkedColor else uncheckedColor)
            .clickable(enabled = onCheckedChange != null, onClick = { onCheckedChange?.invoke(!checked) }),
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White)
            )
        }
    }
}
