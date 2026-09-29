package com.crescentapps.turnly.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit

@Composable
fun Text(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontWeight: FontWeight? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textAlign: TextAlign = TextAlign.Unspecified,
    lineHeight: TextUnit = TextUnit.Unspecified,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    style: TextStyle = TextStyle.Default
) {
    val finalColor = if (color == Color.Unspecified) com.crescentapps.turnly.presentation.theme.LocalTurnlyColors.current.textPrimary else color
    
    BasicText(
        text = text,
        modifier = modifier,
        style = style.merge(
            TextStyle(
                color = finalColor,
                fontSize = fontSize,
                fontWeight = fontWeight,
                letterSpacing = letterSpacing,
                textAlign = textAlign,
                lineHeight = lineHeight
            )
        ),
        maxLines = maxLines,
        overflow = overflow
    )
}

@Composable
fun Icon(
    imageVector: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified
) {
    val finalTint = if (tint == Color.Unspecified) com.crescentapps.turnly.presentation.theme.LocalTurnlyColors.current.iconTint else tint
    Image(
        imageVector = imageVector,
        contentDescription = contentDescription,
        modifier = modifier,
        colorFilter = ColorFilter.tint(finalTint)
    )
}
