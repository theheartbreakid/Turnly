package com.crescentapps.turnly.presentation.gaussianblur.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crescentapps.turnly.presentation.components.Icon
import com.crescentapps.turnly.presentation.components.Text
import com.crescentapps.turnly.presentation.gaussianblur.theme.LocalGaussianBlurColors

@Composable
fun GaussianSettingsNavRow(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    value: String? = null,
    onClick: () -> Unit
) {
    val colors = LocalGaussianBlurColors.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.textSecondary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.width(16.dp))
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.textPrimary
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (value != null) {
                Text(
                    text = value,
                    fontSize = 13.sp,
                    color = colors.textMuted
                )
                Spacer(Modifier.width(8.dp))
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = colors.textMuted.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun GaussianSettingsToggleRow(
    title: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null
) {
    val colors = LocalGaussianBlurColors.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.textSecondary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = colors.textMuted,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }

        Spacer(Modifier.width(12.dp))

        GaussianSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
