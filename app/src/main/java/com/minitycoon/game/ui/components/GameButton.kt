package com.minitycoon.game.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.minitycoon.game.ui.theme.PanelShadow

/**
 * A hand-drawn "premium mobile game" button: vertical gradient fill, a
 * glossy top highlight strip, a soft drop shadow, and a press-scale
 * animation. Used instead of the stock Material [androidx.compose.material3.Button]
 * so the primary actions read as game controls, not a generic Android form.
 */
@Composable
fun GameButton(
    onClick: () -> Unit,
    enabled: Boolean,
    topColor: Color,
    bottomColor: Color,
    disabledTopColor: Color,
    disabledBottomColor: Color,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed && enabled) 0.95f else 1f, label = "gameButtonScale")

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val corner = CornerRadius(size.height * 0.22f)
            val shadowOffset = 4.dp.toPx()

            drawRoundRect(
                color = PanelShadow,
                topLeft = Offset(0f, shadowOffset),
                size = size,
                cornerRadius = corner
            )

            val top = if (enabled) topColor else disabledTopColor
            val bottom = if (enabled) bottomColor else disabledBottomColor
            drawRoundRect(
                brush = Brush.verticalGradient(listOf(top, bottom)),
                size = Size(size.width, size.height - shadowOffset * 0.6f),
                cornerRadius = corner
            )

            drawRoundRect(
                brush = Brush.verticalGradient(
                    listOf(Color.White.copy(alpha = 0.38f), Color.White.copy(alpha = 0f))
                ),
                topLeft = Offset(size.width * 0.06f, size.height * 0.07f),
                size = Size(size.width * 0.88f, size.height * 0.34f),
                cornerRadius = CornerRadius(corner.x * 0.6f)
            )
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
        ) {
            content()
        }
    }
}
