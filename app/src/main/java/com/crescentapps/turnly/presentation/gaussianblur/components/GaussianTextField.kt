package com.crescentapps.turnly.presentation.gaussianblur.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crescentapps.turnly.presentation.components.Icon
import com.crescentapps.turnly.presentation.components.Text
import com.crescentapps.turnly.presentation.gaussianblur.backdrop.LocalAppBackdrop
import com.crescentapps.turnly.presentation.gaussianblur.backdrop.gaussianGlass
import com.crescentapps.turnly.presentation.gaussianblur.theme.GaussianBlurTokens
import com.crescentapps.turnly.presentation.gaussianblur.theme.LocalGaussianBlurColors

@Composable
fun GaussianTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    singleLine: Boolean = true,
    shape: CornerBasedShape = GaussianBlurTokens.ShapeInput,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    leadingIcon: (@Composable () -> Unit)? = null
) {
    val backdrop = LocalAppBackdrop.current
    val colors = LocalGaussianBlurColors.current

    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Column(modifier = modifier) {
        if (label != null) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isFocused) colors.accent else colors.textMuted,
                modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = GaussianBlurTokens.MinimumTouchTarget)
                .gaussianGlass(
                    shape = shape,
                    backdrop = backdrop,
                    blurRadiusDp = 18f,
                    surfaceOpacity = if (isFocused) 0.50f else 0.30f,
                    isDark = colors.isDark
                )
                .padding(horizontal = 16.dp, vertical = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (leadingIcon != null) {
                    leadingIcon()
                    Spacer(Modifier.width(10.dp))
                }

                Box(modifier = Modifier.weight(1f)) {
                    if (value.isEmpty() && placeholder != null) {
                        Text(
                            text = placeholder,
                            style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Normal),
                            color = colors.textMuted
                        )
                    }

                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = singleLine,
                        textStyle = TextStyle(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.textPrimary
                        ),
                        cursorBrush = SolidColor(colors.accent),
                        interactionSource = interactionSource,
                        keyboardOptions = keyboardOptions,
                        keyboardActions = keyboardActions,
                        visualTransformation = visualTransformation
                    )
                }

                if (value.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { onValueChange("") },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Clear text",
                            tint = colors.textMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
