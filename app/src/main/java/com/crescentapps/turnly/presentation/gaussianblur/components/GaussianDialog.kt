package com.crescentapps.turnly.presentation.gaussianblur.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.crescentapps.turnly.presentation.components.Text
import com.crescentapps.turnly.presentation.gaussianblur.theme.GaussianBlurTokens
import com.crescentapps.turnly.presentation.gaussianblur.theme.LocalGaussianBlurColors

@Composable
fun GaussianDialog(
    onDismissRequest: () -> Unit,
    title: String,
    modifier: Modifier = Modifier,
    message: String? = null,
    positiveText: String = "OK",
    negativeText: String? = "Cancel",
    onPositive: () -> Unit,
    onNegative: (() -> Unit)? = null,
    properties: DialogProperties = DialogProperties(usePlatformDefaultWidth = false),
    content: (@Composable () -> Unit)? = null
) {
    val colors = LocalGaussianBlurColors.current

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = properties
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.40f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismissRequest
                ),
            contentAlignment = Alignment.Center
        ) {
            GaussianCard(
                shape = GaussianBlurTokens.ShapeDialog,
                surfaceOpacity = 0.55f,
                blurRadiusDp = 32f,
                modifier = modifier
                    .fillMaxWidth(0.90f)
                    .widthIn(max = 420.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {} // Prevent dismiss on card click
                    )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )

                    if (message != null) {
                        Text(
                            text = message,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            color = colors.textSecondary,
                            lineHeight = 20.sp
                        )
                    }

                    if (content != null) {
                        content()
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (negativeText != null) {
                            GaussianButton(
                                onClick = {
                                    onNegative?.invoke()
                                    onDismissRequest()
                                },
                                shape = GaussianBlurTokens.ShapeChip,
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(negativeText, fontSize = 13.sp, color = colors.textSecondary)
                            }
                            Spacer(Modifier.width(8.dp))
                        }

                        GaussianButton(
                            onClick = {
                                onPositive()
                            },
                            accentTint = colors.accent,
                            shape = GaussianBlurTokens.ShapeChip,
                            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp)
                        ) {
                            Text(positiveText, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = colors.textOnAccent)
                        }
                    }
                }
            }
        }
    }
}
