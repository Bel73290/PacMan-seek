package com.cachecache.game.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cachecache.game.model.BadgeItem
import com.cachecache.game.model.BadgeShape
import com.cachecache.game.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun TrophyComposable(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    rank: Int = 1
) {
    val trophyColor = when (rank) {
        1 -> GoldTrophy
        2 -> SilverTrophy
        3 -> BronzeTrophy
        else -> GoldTrophy
    }

    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        // Cup Body
        val cupPath = Path().apply {
            moveTo(w * 0.25f, h * 0.2f)
            lineTo(w * 0.75f, h * 0.2f)
            cubicTo(
                w * 0.75f, h * 0.6f,
                w * 0.6f, h * 0.7f,
                w * 0.5f, h * 0.75f
            )
            cubicTo(
                w * 0.4f, h * 0.7f,
                w * 0.25f, h * 0.6f,
                w * 0.25f, h * 0.2f
            )
            close()
        }
        drawPath(path = cupPath, color = trophyColor)

        // Handles left & right
        val leftHandle = Path().apply {
            moveTo(w * 0.25f, h * 0.25f)
            cubicTo(w * 0.05f, h * 0.25f, w * 0.05f, h * 0.5f, w * 0.27f, h * 0.52f)
        }
        val rightHandle = Path().apply {
            moveTo(w * 0.75f, h * 0.25f)
            cubicTo(w * 0.95f, h * 0.25f, w * 0.95f, h * 0.5f, w * 0.73f, h * 0.52f)
        }
        drawPath(path = leftHandle, color = trophyColor, style = Stroke(width = w * 0.08f, cap = StrokeCap.Round))
        drawPath(path = rightHandle, color = trophyColor, style = Stroke(width = w * 0.08f, cap = StrokeCap.Round))

        // Stem & Base
        drawRect(
            color = trophyColor,
            topLeft = Offset(w * 0.44f, h * 0.75f),
            size = Size(w * 0.12f, h * 0.12f)
        )
        drawRoundRect(
            color = trophyColor,
            topLeft = Offset(w * 0.28f, h * 0.86f),
            size = Size(w * 0.44f, h * 0.1f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.04f, w * 0.04f)
        )
    }
}

@Composable
fun StarComposable(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    color: Color = PacmanYellow
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val center = Offset(w / 2f, h / 2f)
        val outerRadius = w / 2f
        val innerRadius = outerRadius * 0.45f

        val path = Path()
        for (i in 0 until 10) {
            val radius = if (i % 2 == 0) outerRadius else innerRadius
            val angle = Math.toRadians((i * 36 - 90).toDouble())
            val x = (center.x + radius * cos(angle)).toFloat()
            val y = (center.y + radius * sin(angle)).toFloat()
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        drawPath(path = path, color = color)
    }
}

@Composable
fun BadgeComposable(
    modifier: Modifier = Modifier,
    badge: BadgeItem,
    size: Dp = 68.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val center = Offset(w / 2f, h / 2f)

        when (badge.shape) {
            BadgeShape.SHIELD -> {
                // Shield Shape
                val shieldPath = Path().apply {
                    moveTo(w * 0.15f, h * 0.15f)
                    lineTo(w * 0.85f, h * 0.15f)
                    cubicTo(w * 0.85f, h * 0.6f, w * 0.65f, h * 0.85f, w * 0.5f, h * 0.95f)
                    cubicTo(w * 0.35f, h * 0.85f, w * 0.15f, h * 0.6f, w * 0.15f, h * 0.15f)
                    close()
                }
                drawPath(path = shieldPath, color = CardSurface)
                drawPath(path = shieldPath, color = PacmanYellow, style = Stroke(width = 4.dp.toPx()))

                // Center Icon: Heart / Star
                drawCircle(color = GhostPink, radius = w * 0.18f, center = center)
            }
            BadgeShape.STAR -> {
                // Outer circle frame
                drawCircle(color = CardSurface, radius = w * 0.46f, center = center)
                drawCircle(color = ScaredBlue, radius = w * 0.46f, center = center, style = Stroke(width = 4.dp.toPx()))

                // Center star
                val outerRadius = w * 0.28f
                val innerRadius = outerRadius * 0.45f
                val starPath = Path()
                for (i in 0 until 10) {
                    val radius = if (i % 2 == 0) outerRadius else innerRadius
                    val angle = Math.toRadians((i * 36 - 90).toDouble())
                    val x = (center.x + radius * cos(angle)).toFloat()
                    val y = (center.y + radius * sin(angle)).toFloat()
                    if (i == 0) starPath.moveTo(x, y) else starPath.lineTo(x, y)
                }
                starPath.close()
                drawPath(path = starPath, color = PacmanYellow)
            }
            BadgeShape.HEXAGON -> {
                // 6-sided Polygon (Hexagon)
                val hexPath = Path()
                val hexRadius = w * 0.45f
                for (i in 0 until 6) {
                    val angle = Math.toRadians((i * 60 - 30).toDouble())
                    val x = (center.x + hexRadius * cos(angle)).toFloat()
                    val y = (center.y + hexRadius * sin(angle)).toFloat()
                    if (i == 0) hexPath.moveTo(x, y) else hexPath.lineTo(x, y)
                }
                hexPath.close()

                drawPath(path = hexPath, color = CardSurface)
                drawPath(path = hexPath, color = GhostCyan, style = Stroke(width = 4.dp.toPx()))

                // Center compass / diamond
                val diamondPath = Path().apply {
                    moveTo(center.x, center.y - w * 0.22f)
                    lineTo(center.x + w * 0.16f, center.y)
                    lineTo(center.x, center.y + w * 0.22f)
                    lineTo(center.x - w * 0.16f, center.y)
                    close()
                }
                drawPath(path = diamondPath, color = ScaredBlue)
            }
        }
    }
}
