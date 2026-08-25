package com.minitycoon.game.ui.visuals

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.minitycoon.game.ui.theme.ChimneyGray
import com.minitycoon.game.ui.theme.FactoryDoorBrown
import com.minitycoon.game.ui.theme.FactoryRoofModern
import com.minitycoon.game.ui.theme.FactoryRoofRed
import com.minitycoon.game.ui.theme.FactoryWallCream
import com.minitycoon.game.ui.theme.FactoryWallModern
import com.minitycoon.game.ui.theme.FactoryWindowBlue
import com.minitycoon.game.ui.theme.FactoryWindowModern
import com.minitycoon.game.ui.theme.FactoryWindowShine
import com.minitycoon.game.ui.theme.GlowYellow
import com.minitycoon.game.ui.theme.GroundGreenDark
import com.minitycoon.game.ui.theme.SignBoardBrown
import com.minitycoon.game.ui.theme.SmokeGray
import kotlin.math.sin

private data class FactoryStageSpec(
    val chimneyCount: Int,
    val floors: Int,
    val widthFraction: Float,
    val wallColor: Color,
    val roofColor: Color,
    val windowColor: Color
)

private fun specForStage(stage: Int): FactoryStageSpec = when (stage) {
    1 -> FactoryStageSpec(1, 1, 0.55f, FactoryWallCream, FactoryRoofRed, FactoryWindowBlue)
    2 -> FactoryStageSpec(2, 1, 0.70f, FactoryWallCream, FactoryRoofRed, FactoryWindowBlue)
    3 -> FactoryStageSpec(2, 2, 0.80f, FactoryWallCream, FactoryRoofRed, FactoryWindowBlue)
    else -> FactoryStageSpec(3, 2, 0.92f, FactoryWallModern, FactoryRoofModern, FactoryWindowModern)
}

/**
 * Draws the factory as flat 2D cartoon shapes, sized purely from [stage]
 * (1..[com.minitycoon.game.game.FactoryVisualConfig.STAGE_COUNT]). Bump
 * [glowTrigger] to play a one-shot highlight pulse (used on upgrade).
 */
@Composable
fun FactoryVisual(
    stage: Int,
    glowTrigger: Int,
    modifier: Modifier = Modifier
) {
    val spec = remember(stage) { specForStage(stage) }

    // Smoke: a few particles looping upward with a gentle sideways wobble.
    val smokeTransition = rememberInfiniteTransition(label = "smoke")
    val smokeProgress by smokeTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "smokeProgress"
    )

    val glowAlpha = remember { Animatable(0f) }
    LaunchedEffect(glowTrigger) {
        if (glowTrigger > 0) {
            glowAlpha.animateTo(0.55f, tween(180))
            glowAlpha.animateTo(0f, tween(420))
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val buildingWidth = size.width * spec.widthFraction
        val buildingHeight = size.height * (0.34f + spec.floors * 0.13f)
        val left = (size.width - buildingWidth) / 2f
        val groundY = size.height * 0.90f
        val top = groundY - buildingHeight

        if (glowAlpha.value > 0f) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(GlowYellow.copy(alpha = glowAlpha.value), GlowYellow.copy(alpha = 0f)),
                    center = Offset(size.width / 2f, top + buildingHeight / 2f),
                    radius = buildingWidth * 0.9f
                ),
                radius = buildingWidth * 0.9f,
                center = Offset(size.width / 2f, top + buildingHeight / 2f)
            )
        }

        drawShadow(left, groundY, buildingWidth)
        drawChimneysAndSmoke(spec, left, top, buildingWidth, smokeProgress)
        drawBuildingBody(spec, left, top, buildingWidth, buildingHeight)
        drawRoof(spec, left, top, buildingWidth)
        drawWindows(spec, left, top, buildingWidth, buildingHeight)
        drawDoor(left, groundY, buildingWidth)
        drawSignBoard(left, top, buildingWidth)
        drawDecorations(left, groundY, buildingWidth)
    }
}

private fun DrawScope.drawShadow(left: Float, groundY: Float, width: Float) {
    drawOval(
        color = GroundGreenDark.copy(alpha = 0.35f),
        topLeft = Offset(left + width * 0.05f, groundY - width * 0.03f),
        size = Size(width * 0.9f, width * 0.10f)
    )
}

private fun DrawScope.drawBuildingBody(spec: FactoryStageSpec, left: Float, top: Float, width: Float, height: Float) {
    drawRoundRect(
        color = spec.wallColor,
        topLeft = Offset(left, top),
        size = Size(width, height),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(width * 0.03f, width * 0.03f)
    )
    // Floor divider line(s) for multi-floor stages.
    if (spec.floors > 1) {
        val floorHeight = height / spec.floors
        for (f in 1 until spec.floors) {
            drawLine(
                color = spec.roofColor.copy(alpha = 0.25f),
                start = Offset(left, top + floorHeight * f),
                end = Offset(left + width, top + floorHeight * f),
                strokeWidth = height * 0.01f
            )
        }
    }
}

private fun DrawScope.drawRoof(spec: FactoryStageSpec, left: Float, top: Float, width: Float) {
    val roofHeight = width * 0.16f
    val path = Path().apply {
        moveTo(left - width * 0.04f, top)
        lineTo(left + width / 2f, top - roofHeight)
        lineTo(left + width * 1.04f, top)
        close()
    }
    drawPath(path, color = spec.roofColor)
}

private fun DrawScope.drawWindows(spec: FactoryStageSpec, left: Float, top: Float, width: Float, height: Float) {
    val floorHeight = height / spec.floors
    val windowSize = width * 0.11f
    val margin = width * 0.10f
    val windowsPerFloor = if (width > 0f) ((width - margin * 2f) / (windowSize * 1.8f)).toInt().coerceIn(2, 4) else 2

    for (f in 0 until spec.floors) {
        val floorTop = top + floorHeight * f + floorHeight * 0.28f
        val gap = (width - margin * 2f - windowsPerFloor * windowSize) / (windowsPerFloor - 1).coerceAtLeast(1)
        for (i in 0 until windowsPerFloor) {
            val x = left + margin + i * (windowSize + gap)
            drawRoundRect(
                color = spec.windowColor,
                topLeft = Offset(x, floorTop),
                size = Size(windowSize, windowSize),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(windowSize * 0.15f)
            )
            drawRoundRect(
                color = FactoryWindowShine.copy(alpha = 0.7f),
                topLeft = Offset(x + windowSize * 0.12f, floorTop + windowSize * 0.12f),
                size = Size(windowSize * 0.35f, windowSize * 0.35f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(windowSize * 0.08f)
            )
        }
    }
}

private fun DrawScope.drawDoor(left: Float, groundY: Float, width: Float) {
    val doorWidth = width * 0.16f
    val doorHeight = width * 0.22f
    drawRoundRect(
        color = FactoryDoorBrown,
        topLeft = Offset(left + width / 2f - doorWidth / 2f, groundY - doorHeight),
        size = Size(doorWidth, doorHeight),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(doorWidth * 0.15f, doorWidth * 0.15f)
    )
}

private fun DrawScope.drawSignBoard(left: Float, top: Float, width: Float) {
    val boardWidth = width * 0.30f
    val boardHeight = width * 0.09f
    val x = left + width * 0.06f
    val y = top + width * 0.08f
    drawRoundRect(
        color = SignBoardBrown,
        topLeft = Offset(x, y),
        size = Size(boardWidth, boardHeight),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(boardHeight * 0.2f)
    )
}

private fun DrawScope.drawDecorations(left: Float, groundY: Float, width: Float) {
    // A couple of small crates beside the entrance — cheap decorative detail.
    val crateSize = width * 0.07f
    drawRoundRect(
        color = com.minitycoon.game.ui.theme.SignBoardBrown.copy(alpha = 0.8f),
        topLeft = Offset(left - crateSize * 1.3f, groundY - crateSize),
        size = Size(crateSize, crateSize),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(crateSize * 0.12f)
    )
    drawRoundRect(
        color = com.minitycoon.game.ui.theme.SignBoardBrown.copy(alpha = 0.6f),
        topLeft = Offset(left + width + crateSize * 0.3f, groundY - crateSize * 0.8f),
        size = Size(crateSize * 0.8f, crateSize * 0.8f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(crateSize * 0.1f)
    )
}

private fun DrawScope.drawChimneysAndSmoke(
    spec: FactoryStageSpec,
    left: Float,
    top: Float,
    width: Float,
    smokeProgress: Float
) {
    val chimneyWidth = width * 0.08f
    val chimneyHeight = width * 0.18f
    val spacing = width * 0.14f
    val startX = left + width * 0.62f

    for (i in 0 until spec.chimneyCount) {
        val cx = startX + i * spacing
        val chimneyTop = top - chimneyHeight * 0.7f
        drawRect(
            color = ChimneyGray,
            topLeft = Offset(cx, chimneyTop),
            size = Size(chimneyWidth, chimneyHeight)
        )

        // 3 smoke puffs per chimney, phase-offset so they don't move in lockstep.
        for (p in 0 until 3) {
            val phase = (smokeProgress + p / 3f) % 1f
            val puffRadius = chimneyWidth * (0.35f + phase * 0.45f)
            val riseDistance = chimneyHeight * 1.8f
            val puffY = chimneyTop - phase * riseDistance
            val wobble = sin(phase * 6.28f + i) * chimneyWidth * 0.4f
            val puffAlpha = (1f - phase) * 0.55f
            if (puffAlpha > 0.02f) {
                drawCircle(
                    color = SmokeGray.copy(alpha = puffAlpha),
                    radius = puffRadius,
                    center = Offset(cx + chimneyWidth / 2f + wobble, puffY)
                )
            }
        }
    }
}
