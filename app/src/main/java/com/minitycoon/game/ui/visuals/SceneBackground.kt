package com.minitycoon.game.ui.visuals

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.minitycoon.game.ui.theme.CloudWhite
import com.minitycoon.game.ui.theme.GroundGreen
import com.minitycoon.game.ui.theme.GroundGreenDark
import com.minitycoon.game.ui.theme.RoadGray
import com.minitycoon.game.ui.theme.RoadLineWhite
import com.minitycoon.game.ui.theme.SkyBottom
import com.minitycoon.game.ui.theme.SkyTop
import com.minitycoon.game.ui.theme.TreeCanopyGreen
import com.minitycoon.game.ui.theme.TreeTrunkBrown

/**
 * Lightweight, static-ish backdrop: sky gradient, ground, a road strip, two
 * decorative trees, and a couple of clouds that drift very slowly. Everything
 * is drawn with plain shapes — no bitmaps — so it's cheap and scales to any
 * screen size for free.
 */
@Composable
fun SceneBackground(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "clouds")
    val cloudDrift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 16000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cloudDrift"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        drawSky()
        drawClouds(cloudDrift)
        drawGround()
        drawTree(xFraction = 0.12f)
        drawTree(xFraction = 0.90f)
    }
}

private fun DrawScope.drawSky() {
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(SkyTop, SkyBottom),
            startY = 0f,
            endY = size.height * 0.75f
        ),
        size = size
    )
}

private fun DrawScope.drawClouds(drift: Float) {
    val driftPx = size.width * 0.06f * drift
    drawCloud(centerX = size.width * 0.22f + driftPx, centerY = size.height * 0.16f, scale = 1f)
    drawCloud(centerX = size.width * 0.68f - driftPx, centerY = size.height * 0.10f, scale = 0.75f)
}

private fun DrawScope.drawCloud(centerX: Float, centerY: Float, scale: Float) {
    val r = size.width * 0.06f * scale
    drawCircle(CloudWhite, radius = r, center = Offset(centerX - r * 0.8f, centerY))
    drawCircle(CloudWhite, radius = r * 1.15f, center = Offset(centerX, centerY - r * 0.3f))
    drawCircle(CloudWhite, radius = r * 0.9f, center = Offset(centerX + r * 0.9f, centerY))
}

private fun DrawScope.drawGround() {
    val groundTop = size.height * 0.74f
    drawRect(
        color = GroundGreen,
        topLeft = Offset(0f, groundTop),
        size = androidx.compose.ui.geometry.Size(size.width, size.height - groundTop)
    )
    drawRect(
        color = GroundGreenDark,
        topLeft = Offset(0f, groundTop),
        size = androidx.compose.ui.geometry.Size(size.width, size.height * 0.02f)
    )

    // Small road strip in front of the factory.
    val roadTop = size.height * 0.90f
    val roadHeight = size.height - roadTop
    drawRect(
        color = RoadGray,
        topLeft = Offset(size.width * 0.30f, roadTop),
        size = androidx.compose.ui.geometry.Size(size.width * 0.40f, roadHeight)
    )
    val dashWidth = size.width * 0.03f
    val dashY = roadTop + roadHeight / 2f
    var dashX = size.width * 0.33f
    while (dashX < size.width * 0.66f) {
        drawLine(
            color = RoadLineWhite,
            start = Offset(dashX, dashY),
            end = Offset(dashX + dashWidth, dashY),
            strokeWidth = roadHeight * 0.12f
        )
        dashX += dashWidth * 2f
    }
}

private fun DrawScope.drawTree(xFraction: Float) {
    val trunkTop = size.height * 0.68f
    val trunkWidth = size.width * 0.02f
    val trunkHeight = size.height * 0.10f
    val x = size.width * xFraction

    drawRect(
        color = TreeTrunkBrown,
        topLeft = Offset(x - trunkWidth / 2f, trunkTop),
        size = androidx.compose.ui.geometry.Size(trunkWidth, trunkHeight)
    )

    val canopyRadius = size.width * 0.055f
    val canopyCenterY = trunkTop - canopyRadius * 0.4f
    drawCircle(TreeCanopyGreen, radius = canopyRadius, center = Offset(x, canopyCenterY))
    drawCircle(TreeCanopyGreen, radius = canopyRadius * 0.8f, center = Offset(x - canopyRadius * 0.7f, canopyCenterY + canopyRadius * 0.3f))
    drawCircle(TreeCanopyGreen, radius = canopyRadius * 0.8f, center = Offset(x + canopyRadius * 0.7f, canopyCenterY + canopyRadius * 0.3f))
}
