package com.minitycoon.game.ui.visuals

import android.graphics.Paint
import android.graphics.Typeface
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import com.minitycoon.game.ui.theme.BarrelBlue
import com.minitycoon.game.ui.theme.BarrelBlueDark
import com.minitycoon.game.ui.theme.CrateWood
import com.minitycoon.game.ui.theme.CrateWoodDark
import com.minitycoon.game.ui.theme.FactoryAccentTeal
import com.minitycoon.game.ui.theme.FactoryDoorDark
import com.minitycoon.game.ui.theme.FactoryDoorLight
import com.minitycoon.game.ui.theme.FactoryRoofDark
import com.minitycoon.game.ui.theme.FactoryRoofLight
import com.minitycoon.game.ui.theme.FactoryRoofModernDark
import com.minitycoon.game.ui.theme.FactoryRoofModernLight
import com.minitycoon.game.ui.theme.FactoryWallDark
import com.minitycoon.game.ui.theme.FactoryWallLight
import com.minitycoon.game.ui.theme.FactoryWallModernDark
import com.minitycoon.game.ui.theme.FactoryWallModernLight
import com.minitycoon.game.ui.theme.FactoryWallModernOutline
import com.minitycoon.game.ui.theme.FactoryWallOutline
import com.minitycoon.game.ui.theme.FactoryWindowGlass
import com.minitycoon.game.ui.theme.FactoryWindowGlow
import com.minitycoon.game.ui.theme.FactoryWindowModernGlass
import com.minitycoon.game.ui.theme.FactoryWindowSky
import com.minitycoon.game.ui.theme.GlowYellow
import com.minitycoon.game.ui.theme.MetalDark
import com.minitycoon.game.ui.theme.MetalHighlight
import com.minitycoon.game.ui.theme.MetalLight
import com.minitycoon.game.ui.theme.MetalMid
import com.minitycoon.game.ui.theme.PalletTan
import com.minitycoon.game.ui.theme.SignBoardBrown
import com.minitycoon.game.ui.theme.SignBoardText
import com.minitycoon.game.ui.theme.SmokeGray
import com.minitycoon.game.ui.theme.SoftShadow
import com.minitycoon.game.ui.theme.TankOrange
import kotlin.math.sin

private data class FactoryStageSpec(
    val widthFraction: Float,
    val floors: Int,
    val chimneyCount: Int,
    val hasSideTank: Boolean,
    val hasVentFan: Boolean,
    val hasAntenna: Boolean,
    val propCount: Int,
    val modern: Boolean
)

private fun specForStage(stage: Int): FactoryStageSpec = when (stage) {
    1 -> FactoryStageSpec(0.48f, 1, 1, hasSideTank = false, hasVentFan = false, hasAntenna = false, propCount = 1, modern = false)
    2 -> FactoryStageSpec(0.62f, 1, 2, hasSideTank = false, hasVentFan = true, hasAntenna = false, propCount = 2, modern = false)
    3 -> FactoryStageSpec(0.74f, 2, 2, hasSideTank = true, hasVentFan = true, hasAntenna = false, propCount = 3, modern = false)
    4 -> FactoryStageSpec(0.86f, 2, 2, hasSideTank = true, hasVentFan = true, hasAntenna = true, propCount = 3, modern = true)
    else -> FactoryStageSpec(0.96f, 3, 3, hasSideTank = true, hasVentFan = true, hasAntenna = true, propCount = 4, modern = true)
}

private data class Materials(
    val wallLight: Color,
    val wallDark: Color,
    val wallOutline: Color,
    val roofLight: Color,
    val roofDark: Color,
    val windowTop: Color,
    val windowBottom: Color
)

private fun materialsFor(modern: Boolean): Materials = if (modern) {
    Materials(
        FactoryWallModernLight, FactoryWallModernDark, FactoryWallModernOutline,
        FactoryRoofModernLight, FactoryRoofModernDark,
        FactoryWindowModernGlass, FactoryAccentTeal
    )
} else {
    Materials(
        FactoryWallLight, FactoryWallDark, FactoryWallOutline,
        FactoryRoofLight, FactoryRoofDark,
        FactoryWindowGlass, FactoryWindowSky
    )
}

/**
 * Draws the factory as a shaded 2D illustration, sized and detailed purely
 * from [stage] (1..[com.minitycoon.game.game.FactoryVisualConfig.STAGE_COUNT]).
 * Every surface uses a light-to-dark gradient plus a metal/glass highlight,
 * per the art bible in ui/theme/Color.kt. Bump [glowTrigger] to play a
 * one-shot highlight pulse (used on upgrade).
 */
@Composable
fun FactoryVisual(
    stage: Int,
    glowTrigger: Int,
    modifier: Modifier = Modifier
) {
    val spec = remember(stage) { specForStage(stage) }
    val mat = remember(spec.modern) { materialsFor(spec.modern) }

    val motion = rememberInfiniteTransition(label = "factoryMotion")
    val smokeProgress by motion.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2200, easing = LinearEasing), RepeatMode.Restart),
        label = "smokeProgress"
    )
    val fanAngle by motion.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(3600, easing = LinearEasing), RepeatMode.Restart),
        label = "fanAngle"
    )
    val beaconPulse by motion.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing), RepeatMode.Reverse),
        label = "beaconPulse"
    )

    val glowAlpha = remember { Animatable(0f) }
    LaunchedEffect(glowTrigger) {
        if (glowTrigger > 0) {
            glowAlpha.animateTo(0.55f, tween(180))
            glowAlpha.animateTo(0f, tween(480))
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val buildingWidth = size.width * spec.widthFraction
        val buildingHeight = size.height * (0.30f + spec.floors * 0.115f)
        val left = (size.width - buildingWidth) / 2f
        val groundY = size.height * 0.86f
        val top = groundY - buildingHeight

        if (glowAlpha.value > 0f) {
            val center = Offset(size.width / 2f, top + buildingHeight / 2f)
            val radius = buildingWidth
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(GlowYellow.copy(alpha = glowAlpha.value), GlowYellow.copy(alpha = 0f)),
                    center = center,
                    radius = radius
                ),
                radius = radius,
                center = center
            )
        }

        drawGroundShadow(left, groundY, buildingWidth)
        if (spec.hasSideTank) drawStorageTank(left - buildingWidth * 0.20f, groundY, buildingWidth * 0.16f)
        drawChimneysAndSmoke(spec, left, top, buildingWidth, smokeProgress)
        drawBuildingBody(mat, left, top, buildingWidth, buildingHeight, spec.floors)
        drawRoof(mat, left, top, buildingWidth)
        if (spec.hasVentFan) drawVentFan(left, top, buildingWidth, fanAngle)
        drawWindows(mat, left, top, buildingWidth, buildingHeight, spec.floors)
        drawSidePipe(left, top, buildingWidth, buildingHeight)
        drawDoor(left, groundY, buildingWidth)
        drawSignBoard(left, top, buildingWidth)
        if (spec.hasAntenna) drawAntenna(left, top, buildingWidth, beaconPulse)
        drawYardProps(left, groundY, buildingWidth, spec.propCount)
    }
}

private fun DrawScope.drawGroundShadow(left: Float, groundY: Float, width: Float) {
    val center = Offset(left + width / 2f, groundY + width * 0.02f)
    val radius = width * 0.62f
    drawOval(
        brush = Brush.radialGradient(
            colors = listOf(SoftShadow, SoftShadow.copy(alpha = 0f)),
            center = center,
            radius = radius
        ),
        topLeft = Offset(center.x - radius, center.y - radius * 0.32f),
        size = Size(radius * 2f, radius * 0.64f)
    )
}

private fun DrawScope.drawBuildingBody(mat: Materials, left: Float, top: Float, width: Float, height: Float, floors: Int) {
    val corner = CornerRadius(width * 0.025f, width * 0.025f)
    drawRoundRect(
        brush = Brush.verticalGradient(colors = listOf(mat.wallLight, mat.wallDark), startY = top, endY = top + height),
        topLeft = Offset(left, top),
        size = Size(width, height),
        cornerRadius = corner
    )
    drawRoundRect(
        color = mat.wallOutline,
        topLeft = Offset(left, top),
        size = Size(width, height),
        cornerRadius = corner,
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = width * 0.006f)
    )
    // Diagonal sheen — top-left light source highlight.
    drawLine(
        color = MetalHighlight.copy(alpha = 0.22f),
        start = Offset(left + width * 0.06f, top + height * 0.05f),
        end = Offset(left + width * 0.30f, top + height * 0.85f),
        strokeWidth = width * 0.05f
    )
    if (floors > 1) {
        val floorHeight = height / floors
        for (f in 1 until floors) {
            drawLine(
                color = mat.wallOutline.copy(alpha = 0.35f),
                start = Offset(left, top + floorHeight * f),
                end = Offset(left + width, top + floorHeight * f),
                strokeWidth = height * 0.008f
            )
        }
    }
}

private fun DrawScope.drawRoof(mat: Materials, left: Float, top: Float, width: Float) {
    val roofHeight = width * 0.15f
    val path = Path().apply {
        moveTo(left - width * 0.05f, top)
        lineTo(left + width / 2f, top - roofHeight)
        lineTo(left + width * 1.05f, top)
        close()
    }
    drawPath(
        path,
        brush = Brush.linearGradient(
            colors = listOf(mat.roofLight, mat.roofDark),
            start = Offset(left, top - roofHeight),
            end = Offset(left + width, top)
        )
    )
    drawPath(path, color = mat.wallOutline.copy(alpha = 0.5f), style = androidx.compose.ui.graphics.drawscope.Stroke(width = width * 0.006f))
    // Corrugation texture lines.
    var lx = left + width * 0.08f
    while (lx < left + width * 0.92f) {
        drawLine(
            color = mat.roofDark.copy(alpha = 0.4f),
            start = Offset(lx, top - roofHeight * 0.1f),
            end = Offset(lx, top - roofHeight * 0.5f),
            strokeWidth = width * 0.004f
        )
        lx += width * 0.07f
    }
}

private fun DrawScope.drawWindows(mat: Materials, left: Float, top: Float, width: Float, height: Float, floors: Int) {
    val floorHeight = height / floors
    val windowSize = width * 0.10f
    val margin = width * 0.10f
    val windowsPerFloor = ((width - margin * 2f) / (windowSize * 1.8f)).toInt().coerceIn(2, 5)
    val gap = (width - margin * 2f - windowsPerFloor * windowSize) / (windowsPerFloor - 1).coerceAtLeast(1)

    for (f in 0 until floors) {
        val floorTop = top + floorHeight * f + floorHeight * 0.30f
        for (i in 0 until windowsPerFloor) {
            val x = left + margin + i * (windowSize + gap)
            val lit = (i + f) % 2 == 0
            if (lit) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(FactoryWindowGlow.copy(alpha = 0.45f), FactoryWindowGlow.copy(alpha = 0f)),
                        center = Offset(x + windowSize / 2f, floorTop + windowSize / 2f),
                        radius = windowSize * 1.4f
                    ),
                    radius = windowSize * 1.4f,
                    center = Offset(x + windowSize / 2f, floorTop + windowSize / 2f)
                )
            }
            drawRoundRect(
                brush = Brush.verticalGradient(colors = listOf(mat.windowTop, mat.windowBottom), startY = floorTop, endY = floorTop + windowSize),
                topLeft = Offset(x, floorTop),
                size = Size(windowSize, windowSize),
                cornerRadius = CornerRadius(windowSize * 0.18f)
            )
            drawRoundRect(
                color = mat.wallOutline.copy(alpha = 0.4f),
                topLeft = Offset(x, floorTop),
                size = Size(windowSize, windowSize),
                cornerRadius = CornerRadius(windowSize * 0.18f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = windowSize * 0.05f)
            )
            drawRoundRect(
                color = Color.White.copy(alpha = 0.55f),
                topLeft = Offset(x + windowSize * 0.12f, floorTop + windowSize * 0.10f),
                size = Size(windowSize * 0.30f, windowSize * 0.30f),
                cornerRadius = CornerRadius(windowSize * 0.08f)
            )
        }
    }
}

private fun DrawScope.drawDoor(left: Float, groundY: Float, width: Float) {
    val doorWidth = width * 0.15f
    val doorHeight = width * 0.22f
    val topLeft = Offset(left + width / 2f - doorWidth / 2f, groundY - doorHeight)
    drawRoundRect(
        brush = Brush.verticalGradient(colors = listOf(FactoryDoorLight, FactoryDoorDark), startY = topLeft.y, endY = topLeft.y + doorHeight),
        topLeft = topLeft,
        size = Size(doorWidth, doorHeight),
        cornerRadius = CornerRadius(doorWidth * 0.12f, doorWidth * 0.12f)
    )
    drawCircle(MetalLight, radius = doorWidth * 0.06f, center = Offset(topLeft.x + doorWidth * 0.82f, topLeft.y + doorHeight * 0.55f))
}

private fun DrawScope.drawSignBoard(left: Float, top: Float, width: Float) {
    val boardWidth = width * 0.34f
    val boardHeight = width * 0.095f
    val x = left + width * 0.05f
    val y = top + width * 0.07f
    drawRoundRect(
        brush = Brush.verticalGradient(colors = listOf(SignBoardBrown.copy(alpha = 1f), SignBoardBrown.copy(alpha = 0.85f)), startY = y, endY = y + boardHeight),
        topLeft = Offset(x, y),
        size = Size(boardWidth, boardHeight),
        cornerRadius = CornerRadius(boardHeight * 0.22f)
    )
    val paint = Paint().apply {
        color = SignBoardText.toArgb()
        textSize = boardHeight * 0.5f
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
    }
    drawContext.canvas.nativeCanvas.drawText(
        "FACTORY",
        x + boardWidth / 2f,
        y + boardHeight * 0.66f,
        paint
    )
}

private fun DrawScope.drawSidePipe(left: Float, top: Float, width: Float, height: Float) {
    val pipeWidth = width * 0.035f
    val pipeX = left + width * 0.94f
    drawRoundRect(
        brush = Brush.horizontalGradient(colors = listOf(MetalLight, MetalDark)),
        topLeft = Offset(pipeX, top + height * 0.1f),
        size = Size(pipeWidth, height * 0.85f),
        cornerRadius = CornerRadius(pipeWidth * 0.4f)
    )
    var jy = top + height * 0.25f
    while (jy < top + height * 0.85f) {
        drawRoundRect(
            color = MetalDark,
            topLeft = Offset(pipeX - pipeWidth * 0.25f, jy),
            size = Size(pipeWidth * 1.5f, pipeWidth * 0.5f),
            cornerRadius = CornerRadius(pipeWidth * 0.2f)
        )
        jy += height * 0.22f
    }
}

private fun DrawScope.drawVentFan(left: Float, top: Float, width: Float, angle: Float) {
    val radius = width * 0.06f
    val center = Offset(left + width * 0.30f, top - radius * 0.2f)
    drawCircle(Brush.radialGradient(colors = listOf(MetalLight, MetalMid), center = center, radius = radius), radius = radius, center = center)
    rotate(degrees = angle, pivot = center) {
        for (b in 0 until 3) {
            rotate(degrees = b * 120f, pivot = center) {
                drawRoundRect(
                    color = MetalDark,
                    topLeft = Offset(center.x - radius * 0.08f, center.y - radius * 0.85f),
                    size = Size(radius * 0.16f, radius * 0.85f),
                    cornerRadius = CornerRadius(radius * 0.08f)
                )
            }
        }
    }
    drawCircle(MetalDark, radius = radius * 0.18f, center = center)
}

private fun DrawScope.drawAntenna(left: Float, top: Float, width: Float, beaconAlpha: Float) {
    val x = left + width * 0.82f
    val poleTop = top - width * 0.22f
    drawLine(MetalMid, start = Offset(x, top - width * 0.02f), end = Offset(x, poleTop), strokeWidth = width * 0.01f)
    drawCircle(Color(0xFFFF5252).copy(alpha = beaconAlpha), radius = width * 0.018f, center = Offset(x, poleTop))
}

private fun DrawScope.drawStorageTank(centerX: Float, groundY: Float, radius: Float) {
    val tankHeight = radius * 2.6f
    val top = groundY - tankHeight
    drawOval(
        brush = Brush.radialGradient(colors = listOf(SoftShadow, SoftShadow.copy(alpha = 0f)), center = Offset(centerX, groundY), radius = radius * 1.4f),
        topLeft = Offset(centerX - radius * 1.3f, groundY - radius * 0.2f),
        size = Size(radius * 2.6f, radius * 0.5f)
    )
    drawRoundRect(
        brush = Brush.horizontalGradient(colors = listOf(TankOrange, MetalDark.copy(alpha = 0.85f), TankOrange)),
        topLeft = Offset(centerX - radius, top),
        size = Size(radius * 2f, tankHeight),
        cornerRadius = CornerRadius(radius * 0.4f)
    )
    drawOval(
        brush = Brush.verticalGradient(colors = listOf(MetalLight, MetalMid)),
        topLeft = Offset(centerX - radius, top - radius * 0.22f),
        size = Size(radius * 2f, radius * 0.44f)
    )
    drawLine(MetalHighlight.copy(alpha = 0.35f), start = Offset(centerX - radius * 0.6f, top + tankHeight * 0.1f), end = Offset(centerX - radius * 0.6f, top + tankHeight * 0.75f), strokeWidth = radius * 0.12f)
    drawRect(MetalDark.copy(alpha = 0.6f), topLeft = Offset(centerX - radius, top + tankHeight * 0.35f), size = Size(radius * 2f, tankHeight * 0.04f))
}

private fun DrawScope.drawYardProps(left: Float, groundY: Float, width: Float, count: Int) {
    val crateSize = width * 0.065f
    val slots = listOf(
        Offset(left - crateSize * 1.6f, groundY - crateSize),
        Offset(left + width + crateSize * 0.5f, groundY - crateSize * 0.9f),
        Offset(left + width + crateSize * 1.7f, groundY - crateSize * 0.6f),
        Offset(left - crateSize * 0.2f, groundY - crateSize * 0.55f)
    )
    val kinds = listOf(PropKind.CRATE, PropKind.PALLET, PropKind.BARREL, PropKind.CRATE)
    for (i in 0 until count.coerceAtMost(slots.size)) {
        drawYardProp(kinds[i], slots[i], crateSize)
    }
}

private enum class PropKind { CRATE, PALLET, BARREL }

private fun DrawScope.drawYardProp(kind: PropKind, position: Offset, size: Float) {
    when (kind) {
        PropKind.CRATE -> {
            drawRoundRect(
                brush = Brush.verticalGradient(colors = listOf(CrateWood, CrateWoodDark)),
                topLeft = position,
                size = Size(size, size),
                cornerRadius = CornerRadius(size * 0.1f)
            )
            drawLine(CrateWoodDark, position + Offset(0f, size * 0.5f), position + Offset(size, size * 0.5f), strokeWidth = size * 0.05f)
        }
        PropKind.PALLET -> {
            var y = position.y
            repeat(3) {
                drawRect(PalletTan, topLeft = Offset(position.x, y), size = Size(size * 0.9f, size * 0.14f))
                y += size * 0.22f
            }
        }
        PropKind.BARREL -> {
            drawRoundRect(
                brush = Brush.horizontalGradient(colors = listOf(BarrelBlueDark, BarrelBlue, BarrelBlueDark)),
                topLeft = position,
                size = Size(size * 0.75f, size),
                cornerRadius = CornerRadius(size * 0.25f)
            )
            drawRect(BarrelBlueDark, topLeft = Offset(position.x, position.y + size * 0.4f), size = Size(size * 0.75f, size * 0.08f))
        }
    }
}

private fun DrawScope.drawChimneysAndSmoke(spec: FactoryStageSpec, left: Float, top: Float, width: Float, smokeProgress: Float) {
    val chimneyWidth = width * 0.075f
    val chimneyHeight = width * 0.20f
    val spacing = width * 0.12f
    val startX = left + width * 0.58f

    for (i in 0 until spec.chimneyCount) {
        val cx = startX + i * spacing
        val chimneyTop = top - chimneyHeight * 0.72f
        drawRoundRect(
            brush = Brush.horizontalGradient(colors = listOf(MetalLight, MetalMid, MetalDark)),
            topLeft = Offset(cx, chimneyTop),
            size = Size(chimneyWidth, chimneyHeight),
            cornerRadius = CornerRadius(chimneyWidth * 0.15f)
        )
        drawRect(MetalDark, topLeft = Offset(cx, chimneyTop), size = Size(chimneyWidth, chimneyHeight * 0.08f))

        for (p in 0 until 3) {
            val phase = (smokeProgress + p / 3f + i * 0.15f) % 1f
            val puffRadius = chimneyWidth * (0.35f + phase * 0.5f)
            val riseDistance = chimneyHeight * 2f
            val puffY = chimneyTop - phase * riseDistance
            val wobble = sin(phase * 6.28f + i) * chimneyWidth * 0.5f
            val puffAlpha = (1f - phase) * 0.5f
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
