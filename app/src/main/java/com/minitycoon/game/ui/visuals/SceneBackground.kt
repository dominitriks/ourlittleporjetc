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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.minitycoon.game.ui.theme.BushGreen
import com.minitycoon.game.ui.theme.CloudWhite
import com.minitycoon.game.ui.theme.FenceWood
import com.minitycoon.game.ui.theme.FenceWoodDark
import com.minitycoon.game.ui.theme.GroundGreen
import com.minitycoon.game.ui.theme.GroundGreenDark
import com.minitycoon.game.ui.theme.HillFar
import com.minitycoon.game.ui.theme.HillNear
import com.minitycoon.game.ui.theme.RoadGray
import com.minitycoon.game.ui.theme.RoadGrayLight
import com.minitycoon.game.ui.theme.RoadLineWhite
import com.minitycoon.game.ui.theme.SkyBottom
import com.minitycoon.game.ui.theme.SkyMid
import com.minitycoon.game.ui.theme.SkyTop
import com.minitycoon.game.ui.theme.SunGlow
import com.minitycoon.game.ui.theme.TreeCanopyGreen
import com.minitycoon.game.ui.theme.TreeCanopyGreenLight
import com.minitycoon.game.ui.theme.TreeTrunkBrown
import com.minitycoon.game.ui.theme.TruckGlass
import com.minitycoon.game.ui.theme.TruckRed
import com.minitycoon.game.ui.theme.TruckRedDark
import kotlin.math.min

/**
 * Layered backdrop: sky + sun glow, two parallax hill silhouettes, ground,
 * a road with a slowly driving delivery truck, a fence line, bushes/trees,
 * and drifting clouds. Everything is drawn with gradients and soft shapes
 * (no bitmaps) — see the art bible in ui/theme/Color.kt for the shading
 * rules this follows.
 */
@Composable
fun SceneBackground(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "scene")
    val cloudDrift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 18000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cloudDrift"
    )
    val truckDrift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 9000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "truckDrift"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        drawSky()
        drawHills()
        drawClouds(cloudDrift)
        drawGround()
        drawFence()
        drawRoadAndTruck(truckDrift)
        drawTree(xFraction = 0.10f, scale = 1f)
        drawBush(xFraction = 0.20f)
        drawTree(xFraction = 0.92f, scale = 0.85f)
        drawBush(xFraction = 0.82f)
    }
}

private fun DrawScope.drawSky() {
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(SkyTop, SkyMid, SkyBottom),
            startY = 0f,
            endY = size.height * 0.78f
        ),
        size = size
    )
    // Soft sun glow, upper-left — matches the art bible's top-left light source.
    val sunCenter = Offset(size.width * 0.20f, size.height * 0.14f)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(SunGlow.copy(alpha = 0.55f), SunGlow.copy(alpha = 0f)),
            center = sunCenter,
            radius = size.width * 0.30f
        ),
        radius = size.width * 0.30f,
        center = sunCenter
    )
}

private fun DrawScope.drawHills() {
    drawHillLayer(color = HillFar, baseYFraction = 0.56f, amplitude = 0.05f, phase = 0f)
    drawHillLayer(color = HillNear, baseYFraction = 0.62f, amplitude = 0.045f, phase = 1.3f)
}

private fun DrawScope.drawHillLayer(color: androidx.compose.ui.graphics.Color, baseYFraction: Float, amplitude: Float, phase: Float) {
    val baseY = size.height * baseYFraction
    val amp = size.height * amplitude
    val path = Path().apply {
        moveTo(0f, size.height)
        lineTo(0f, baseY)
        cubicTo(
            size.width * 0.18f, baseY - amp + phase * 4f,
            size.width * 0.30f, baseY + amp,
            size.width * 0.50f, baseY - amp * 0.6f
        )
        cubicTo(
            size.width * 0.68f, baseY - amp * 1.6f,
            size.width * 0.82f, baseY + amp * 0.8f,
            size.width, baseY - amp * 0.3f
        )
        lineTo(size.width, size.height)
        close()
    }
    drawPath(path, color = color)
}

private fun DrawScope.drawClouds(drift: Float) {
    val driftPx = size.width * 0.05f * drift
    drawCloud(centerX = size.width * 0.24f + driftPx, centerY = size.height * 0.13f, scale = 1f)
    drawCloud(centerX = size.width * 0.70f - driftPx, centerY = size.height * 0.08f, scale = 0.7f)
}

private fun DrawScope.drawCloud(centerX: Float, centerY: Float, scale: Float) {
    val r = size.width * 0.055f * scale
    val shadeBrush = Brush.verticalGradient(
        colors = listOf(CloudWhite, CloudWhite.copy(alpha = 0.85f))
    )
    drawCircle(shadeBrush, radius = r, center = Offset(centerX - r * 0.8f, centerY))
    drawCircle(shadeBrush, radius = r * 1.15f, center = Offset(centerX, centerY - r * 0.3f))
    drawCircle(shadeBrush, radius = r * 0.9f, center = Offset(centerX + r * 0.9f, centerY))
}

private fun DrawScope.drawGround() {
    val groundTop = size.height * 0.74f
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(GroundGreen, GroundGreenDark),
            startY = groundTop,
            endY = size.height
        ),
        topLeft = Offset(0f, groundTop),
        size = Size(size.width, size.height - groundTop)
    )
}

private fun DrawScope.drawFence() {
    val fenceBaseY = size.height * 0.78f
    val fenceHeight = size.height * 0.045f
    val postWidth = size.width * 0.008f
    val spacing = size.width * 0.045f
    var x = size.width * 0.04f

    drawLine(
        color = FenceWood,
        start = Offset(size.width * 0.03f, fenceBaseY - fenceHeight * 0.35f),
        end = Offset(size.width * 0.30f, fenceBaseY - fenceHeight * 0.35f),
        strokeWidth = fenceHeight * 0.12f
    )
    while (x < size.width * 0.30f) {
        drawRect(
            color = FenceWoodDark,
            topLeft = Offset(x, fenceBaseY - fenceHeight),
            size = Size(postWidth, fenceHeight)
        )
        x += spacing
    }
}

private fun DrawScope.drawRoadAndTruck(drift: Float) {
    val roadTop = size.height * 0.90f
    val roadHeight = size.height - roadTop
    drawRect(
        brush = Brush.verticalGradient(colors = listOf(RoadGrayLight, RoadGray), startY = roadTop, endY = size.height),
        topLeft = Offset(size.width * 0.30f, roadTop),
        size = Size(size.width * 0.42f, roadHeight)
    )
    val dashWidth = size.width * 0.028f
    val dashY = roadTop + roadHeight / 2f
    var dashX = size.width * 0.33f
    while (dashX < size.width * 0.68f) {
        drawLine(
            color = RoadLineWhite,
            start = Offset(dashX, dashY),
            end = Offset(dashX + dashWidth, dashY),
            strokeWidth = roadHeight * 0.10f
        )
        dashX += dashWidth * 2.2f
    }

    // Small delivery truck driving slowly left-to-right along the road, then looping.
    val truckWidth = size.width * 0.09f
    val travel = size.width * 0.62f
    val truckX = size.width * 0.30f - truckWidth + drift * travel
    val truckY = roadTop - truckWidth * 0.30f
    drawTruck(truckX, truckY, truckWidth)
}

private fun DrawScope.drawTruck(x: Float, y: Float, w: Float) {
    val h = w * 0.55f
    // Trailer body
    drawRoundRect(
        brush = Brush.verticalGradient(colors = listOf(TruckRed, TruckRedDark), startY = y, endY = y + h),
        topLeft = Offset(x, y),
        size = Size(w * 0.62f, h),
        cornerRadius = CornerRadius(w * 0.05f)
    )
    // Cab
    drawRoundRect(
        brush = Brush.verticalGradient(colors = listOf(TruckRed, TruckRedDark), startY = y + h * 0.15f, endY = y + h),
        topLeft = Offset(x + w * 0.60f, y + h * 0.15f),
        size = Size(w * 0.32f, h * 0.85f),
        cornerRadius = CornerRadius(w * 0.05f)
    )
    // Windshield
    drawRoundRect(
        color = TruckGlass,
        topLeft = Offset(x + w * 0.66f, y + h * 0.22f),
        size = Size(w * 0.18f, h * 0.32f),
        cornerRadius = CornerRadius(w * 0.02f)
    )
    // Wheels
    val wheelRadius = h * 0.22f
    drawCircle(androidx.compose.ui.graphics.Color(0xFF2B2B2B), radius = wheelRadius, center = Offset(x + w * 0.18f, y + h))
    drawCircle(androidx.compose.ui.graphics.Color(0xFF2B2B2B), radius = wheelRadius, center = Offset(x + w * 0.80f, y + h))
}

private fun DrawScope.drawTree(xFraction: Float, scale: Float) {
    val trunkTop = size.height * 0.68f
    val trunkWidth = size.width * 0.018f * scale
    val trunkHeight = size.height * 0.09f * scale
    val x = size.width * xFraction

    drawRect(
        color = TreeTrunkBrown,
        topLeft = Offset(x - trunkWidth / 2f, trunkTop),
        size = Size(trunkWidth, trunkHeight)
    )

    val canopyRadius = size.width * 0.05f * scale
    val canopyCenterY = trunkTop - canopyRadius * 0.4f
    val canopyBrush = Brush.radialGradient(
        colors = listOf(TreeCanopyGreenLight, TreeCanopyGreen),
        center = Offset(x - canopyRadius * 0.3f, canopyCenterY - canopyRadius * 0.3f),
        radius = canopyRadius * 2.2f
    )
    drawCircle(canopyBrush, radius = canopyRadius, center = Offset(x, canopyCenterY))
    drawCircle(canopyBrush, radius = canopyRadius * 0.8f, center = Offset(x - canopyRadius * 0.7f, canopyCenterY + canopyRadius * 0.3f))
    drawCircle(canopyBrush, radius = canopyRadius * 0.8f, center = Offset(x + canopyRadius * 0.7f, canopyCenterY + canopyRadius * 0.3f))
}

private fun DrawScope.drawBush(xFraction: Float) {
    val baseY = size.height * 0.78f
    val x = size.width * xFraction
    val r = min(size.width, size.height) * 0.028f
    val brush = Brush.radialGradient(
        colors = listOf(TreeCanopyGreenLight, BushGreen),
        center = Offset(x - r * 0.3f, baseY - r * 0.6f),
        radius = r * 2.4f
    )
    drawCircle(brush, radius = r, center = Offset(x - r * 0.6f, baseY - r * 0.5f))
    drawCircle(brush, radius = r * 1.1f, center = Offset(x, baseY - r * 0.7f))
    drawCircle(brush, radius = r * 0.9f, center = Offset(x + r * 0.6f, baseY - r * 0.4f))
}
