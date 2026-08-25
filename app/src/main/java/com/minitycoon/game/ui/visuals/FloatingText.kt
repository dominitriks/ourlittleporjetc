package com.minitycoon.game.ui.visuals

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/** A single small piece of transient UI feedback, e.g. "+$10" or "+LEVEL". */
data class FloatingTextItem(val id: Long, val text: String, val color: Color)

/**
 * Renders [item], rising and fading over ~900ms, then calls [onFinished] so
 * the caller can drop it from whatever list is holding active items.
 */
@Composable
fun FloatingText(item: FloatingTextItem, onFinished: (Long) -> Unit, modifier: Modifier = Modifier) {
    val offsetY = remember { Animatable(0f) }
    val alpha = remember { Animatable(1f) }

    LaunchedEffect(item.id) {
        launch { offsetY.animateTo(-70f, tween(900)) }
        alpha.animateTo(0f, tween(900))
        onFinished(item.id)
    }

    Text(
        text = item.text,
        color = item.color,
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.titleLarge,
        modifier = modifier
            .offset(y = offsetY.value.dp)
            .alpha(alpha.value)
    )
}
