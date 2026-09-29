package com.crescentapps.turnly.presentation.gaussianblur.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.crescentapps.turnly.presentation.gaussianblur.theme.LocalGaussianBlurColors

@Composable
fun GaussianSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = LocalGaussianBlurColors.current

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
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
            contentAlignment = Alignment.BottomCenter
        ) {
            GaussianCard(
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp, bottomStart = 0.dp, bottomEnd = 0.dp),
                surfaceOpacity = 0.60f,
                blurRadiusDp = 32f,
                modifier = modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {} // Prevent dismiss on sheet body tap
                    )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Sheet Drag Handle
                    Box(
                        modifier = Modifier
                            .width(36.dp)
                            .height(5.dp)
                            .clip(CircleShape)
                            .background(colors.textMuted.copy(alpha = 0.5f))
                    )
                    Spacer(Modifier.height(16.dp))

                    content()
                }
            }
        }
    }
}
