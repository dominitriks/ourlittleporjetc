package com.minitycoon.game.ui.components

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.minitycoon.game.ui.theme.ButtonTextLight
import com.minitycoon.game.ui.theme.CoinGold
import com.minitycoon.game.ui.theme.CoinGoldDeep
import com.minitycoon.game.ui.theme.CoinGoldOutline
import com.minitycoon.game.ui.theme.MoneyGreen
import com.minitycoon.game.ui.theme.PanelBorderHighlight
import com.minitycoon.game.ui.theme.PanelNavyBottom
import com.minitycoon.game.ui.theme.PanelNavyTop
import java.util.Locale

/** A small hand-drawn gold coin — stands in for a "💰" emoji as a real game asset. */
@Composable
fun CoinIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val r = size.minDimension / 2f
        val center = Offset(size.width / 2f, size.height / 2f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(CoinGold, CoinGoldDeep),
                center = Offset(center.x - r * 0.3f, center.y - r * 0.3f),
                radius = r * 1.7f
            ),
            radius = r,
            center = center
        )
        drawCircle(CoinGoldOutline, radius = r * 0.94f, center = center, style = Stroke(width = r * 0.14f))
        drawArc(
            color = Color.White.copy(alpha = 0.55f),
            startAngle = 200f,
            sweepAngle = 65f,
            useCenter = false,
            style = Stroke(width = r * 0.14f),
            topLeft = Offset(center.x - r * 0.62f, center.y - r * 0.62f),
            size = Size(r * 1.24f, r * 1.24f)
        )
        val paint = Paint().apply {
            color = CoinGoldOutline.toArgb()
            textSize = r * 1.15f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        }
        drawContext.canvas.nativeCanvas.drawText("$", center.x, center.y + r * 0.40f, paint)
    }
}

/**
 * The top HUD: a dark glossy panel showing current money (with a coin icon
 * and a brief pulse when it changes) and the passive income rate.
 */
@Composable
fun MoneyHud(
    money: Double,
    incomePerSecond: Double,
    pulseTrigger: Int,
    modifier: Modifier = Modifier
) {
    val scale = remember { Animatable(1f) }
    LaunchedEffect(pulseTrigger) {
        if (pulseTrigger > 0) {
            scale.animateTo(1.10f, tween(120))
            scale.animateTo(1f, tween(200))
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .drawBehind {
                drawRect(
                    brush = Brush.verticalGradient(listOf(PanelNavyTop, PanelNavyBottom))
                )
                drawRect(
                    color = PanelBorderHighlight,
                    topLeft = Offset.Zero,
                    size = Size(size.width, size.height * 0.06f)
                )
            }
            .padding(horizontal = 18.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CoinIcon(modifier = Modifier.size(30.dp))
            Spacer(Modifier.width(10.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.graphicsLayer {
                    scaleX = scale.value
                    scaleY = scale.value
                }
            ) {
                Text(
                    text = formatMoney(money),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = ButtonTextLight
                )
            }
            Spacer(Modifier.width(14.dp))
            Text(
                text = "+${formatMoney(incomePerSecond)}/s",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MoneyGreen
            )
        }
    }
}

internal fun formatMoney(value: Double): String = "$" + String.format(Locale.US, "%,.0f", value)
