package com.cachecache.game.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cachecache.game.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun PacmanComposable(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    color: Color = PacmanYellow,
    isAnimated: Boolean = true,
    rotationAngle: Float = 0f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pacmanMouth")
    val mouthAngle by if (isAnimated) {
        infiniteTransition.animateFloat(
            initialValue = 10f,
            targetValue = 60f,
            animationSpec = infiniteRepeatable(
                animation = tween(280, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "mouthAngle"
        )
    } else {
        rememberInfiniteTransition(label = "staticMouth").animateFloat(
            initialValue = 35f,
            targetValue = 35f,
            animationSpec = infiniteRepeatable(tween(1000)),
            label = "staticAngle"
        )
    }

    Canvas(modifier = modifier.size(size)) {
        rotate(rotationAngle) {
            drawPacman(
                color = color,
                mouthSweep = mouthAngle
            )
        }
    }
}

fun DrawScope.drawPacman(
    color: Color = PacmanYellow,
    mouthSweep: Float = 40f
) {
    val radius = size.minDimension / 2f
    val center = Offset(size.width / 2f, size.height / 2f)

    // Draw Pacman body with mouth slice cut out
    val startAngle = mouthSweep / 2f
    val sweepAngle = 360f - mouthSweep

    // Outline for crisp contrast
    drawArc(
        color = DarkContrast,
        startAngle = startAngle,
        sweepAngle = sweepAngle,
        useCenter = true,
        topLeft = Offset(center.x - radius, center.y - radius),
        size = Size(radius * 2f, radius * 2f),
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx())
    )

    drawArc(
        color = color,
        startAngle = startAngle,
        sweepAngle = sweepAngle,
        useCenter = true,
        topLeft = Offset(center.x - radius, center.y - radius),
        size = Size(radius * 2f, radius * 2f)
    )

    // Eye position (above mouth opening)
    val eyeRadius = radius * 0.13f
    val eyeX = center.x + radius * 0.16f
    val eyeY = center.y - radius * 0.46f

    drawCircle(
        color = DarkContrast,
        radius = eyeRadius,
        center = Offset(eyeX, eyeY)
    )
}

/**
 * Festive Pacman surrounded by a radial burst of colorful confetti particles
 * as seen on Screen 5 of the mockup ("Tu as trouvé un caché !")
 */
@Composable
fun FestivePacmanComposable(
    modifier: Modifier = Modifier,
    size: Dp = 150.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "festiveTransition")
    val bounceOffset by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "festiveBounce"
    )

    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "festiveRot"
    )

    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val center = Offset(w / 2f, h / 2f)

        // 1. Draw explosive radial confetti
        val confettiColors = listOf(GhostRed, GhostPink, GhostOrange, GhostCyan, PacmanYellow, ScaredBlue, GhostPurple)
        val confettiCount = 26

        for (i in 0 until confettiCount) {
            val angle = Math.toRadians((i * (360.0 / confettiCount) + (i * 7)).toDouble())
            val dist = (w * 0.36f) + (i % 5) * 8.dp.toPx()
            val cx = (center.x + dist * cos(angle)).toFloat()
            val cy = (center.y + dist * sin(angle)).toFloat()
            val color = confettiColors[i % confettiColors.size]

            rotate(degrees = (i * 35).toFloat(), pivot = Offset(cx, cy)) {
                if (i % 3 == 0) {
                    // Confetti rectangle
                    drawRect(
                        color = color,
                        topLeft = Offset(cx - 3.5.dp.toPx(), cy - 7.dp.toPx()),
                        size = Size(7.dp.toPx(), 14.dp.toPx())
                    )
                } else if (i % 3 == 1) {
                    // Confetti dot
                    drawCircle(
                        color = color,
                        radius = 4.dp.toPx(),
                        center = Offset(cx, cy)
                    )
                } else {
                    // Confetti ribbon
                    drawLine(
                        color = color,
                        start = Offset(cx - 5.dp.toPx(), cy - 5.dp.toPx()),
                        end = Offset(cx + 5.dp.toPx(), cy + 5.dp.toPx()),
                        strokeWidth = 3.dp.toPx()
                    )
                }
            }
        }

        // 2. Glowing yellow Pacman in Center
        val pacmanRadius = w * 0.24f
        val pacCenter = Offset(center.x, center.y + bounceOffset)

        rotate(degrees = -18f + rotationAngle, pivot = pacCenter) {
            // Soft glow
            drawCircle(
                color = PacmanYellow.copy(alpha = 0.35f),
                radius = pacmanRadius * 1.25f,
                center = pacCenter
            )

            // Outer dark stroke
            drawArc(
                color = DarkContrast,
                startAngle = 30f,
                sweepAngle = 300f,
                useCenter = true,
                topLeft = Offset(pacCenter.x - pacmanRadius, pacCenter.y - pacmanRadius),
                size = Size(pacmanRadius * 2f, pacmanRadius * 2f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4.dp.toPx())
            )

            // Body
            drawArc(
                color = PacmanYellow,
                startAngle = 30f,
                sweepAngle = 300f,
                useCenter = true,
                topLeft = Offset(pacCenter.x - pacmanRadius, pacCenter.y - pacmanRadius),
                size = Size(pacmanRadius * 2f, pacmanRadius * 2f)
            )

            // Eye
            val eyeRadius = pacmanRadius * 0.15f
            val eyeX = pacCenter.x + pacmanRadius * 0.16f
            val eyeY = pacCenter.y - pacmanRadius * 0.48f
            drawCircle(
                color = DarkContrast,
                radius = eyeRadius,
                center = Offset(eyeX, eyeY)
            )
        }
    }
}
