package com.cachecache.game.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cachecache.game.R
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
    val chompFrames = remember {
        listOf(
            R.drawable.pacman_chomp_0,
            R.drawable.pacman_chomp_1,
            R.drawable.pacman_chomp_2,
            R.drawable.pacman_chomp_3,
            R.drawable.pacman_chomp_4,
            R.drawable.pacman_chomp_5,
            R.drawable.pacman_chomp_6,
            R.drawable.pacman_chomp_7,
            R.drawable.pacman_chomp_8,
            R.drawable.pacman_chomp_7,
            R.drawable.pacman_chomp_6,
            R.drawable.pacman_chomp_5,
            R.drawable.pacman_chomp_4,
            R.drawable.pacman_chomp_3,
            R.drawable.pacman_chomp_2,
            R.drawable.pacman_chomp_1
        )
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pacmanChompAnim")
    val frameIndex by if (isAnimated) {
        infiniteTransition.animateValue(
            initialValue = 0,
            targetValue = chompFrames.size - 1,
            typeConverter = Int.VectorConverter,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 400, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "frameIndex"
        )
    } else {
        remember { androidx.compose.runtime.mutableIntStateOf(4) }
    }

    Image(
        painter = painterResource(id = chompFrames[frameIndex.coerceIn(0, chompFrames.size - 1)]),
        contentDescription = "Pacman",
        modifier = modifier
            .size(size)
            .graphicsLayer { rotationZ = rotationAngle },
        contentScale = ContentScale.Fit
    )
}

/**
 * Researcher Marker on Map with:
 * - 0° to 60° chomp mouth animation
 * - Fluid bearing rotation via spring interpolation
 * - 3 concentric expanding radar rings (radii 10dp to 70dp, alpha 0.4f to 0f)
 */
@Composable
fun ResearcherMarker(
    modifier: Modifier = Modifier,
    targetBearing: Float = 0f,
    size: Dp = 46.dp
) {
    // Bearing rotation smoothly interpolated with spring
    val animatedBearing by animateFloatAsState(
        targetValue = targetBearing,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "bearingAnim"
    )

    // Multi-ring radar wave animation
    val infiniteTransition = rememberInfiniteTransition(label = "radarRings")
    val waveProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "waveProgress"
    )

    Box(
        modifier = modifier.size(size * 3.2f),
        contentAlignment = Alignment.Center
    ) {
        // 3 concentric expanding radar circles
        Canvas(modifier = Modifier.matchParentSize()) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val minRadius = 10.dp.toPx()
            val maxRadius = 70.dp.toPx()

            for (ring in 0 until 3) {
                val ringProgress = (waveProgress + ring * 0.33f) % 1f
                val ringRadius = minRadius + (maxRadius - minRadius) * ringProgress
                val ringAlpha = (1f - ringProgress).coerceIn(0f, 1f) * 0.4f

                drawCircle(
                    color = ScaredBlue.copy(alpha = ringAlpha * 0.4f),
                    radius = ringRadius,
                    center = center
                )
                drawCircle(
                    color = ScaredBlue.copy(alpha = ringAlpha),
                    radius = ringRadius,
                    center = center,
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }

        // Animated Pacman in center
        PacmanComposable(
            size = size,
            color = PacmanYellow,
            isAnimated = true,
            rotationAngle = animatedBearing
        )
    }
}

fun DrawScope.drawPacman(
    color: Color = PacmanYellow,
    mouthSweep: Float = 40f
) {
    val radius = size.minDimension / 2f
    val center = Offset(size.width / 2f, size.height / 2f)

    // Mouth slice cut out from 0° to 60°
    val startAngle = mouthSweep / 2f
    val sweepAngle = 360f - mouthSweep

    // Outer dark stroke for 3D outline
    drawArc(
        color = DarkContrast,
        startAngle = startAngle,
        sweepAngle = sweepAngle,
        useCenter = true,
        topLeft = Offset(center.x - radius, center.y - radius),
        size = Size(radius * 2f, radius * 2f),
        style = Stroke(width = 3.dp.toPx())
    )

    // Body fill
    drawArc(
        color = color,
        startAngle = startAngle,
        sweepAngle = sweepAngle,
        useCenter = true,
        topLeft = Offset(center.x - radius, center.y - radius),
        size = Size(radius * 2f, radius * 2f)
    )

    // Eye
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

        // Radial confetti explosion
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
                    drawRect(
                        color = color,
                        topLeft = Offset(cx - 3.5.dp.toPx(), cy - 7.dp.toPx()),
                        size = Size(7.dp.toPx(), 14.dp.toPx())
                    )
                } else if (i % 3 == 1) {
                    drawCircle(
                        color = color,
                        radius = 4.dp.toPx(),
                        center = Offset(cx, cy)
                    )
                } else {
                    drawLine(
                        color = color,
                        start = Offset(cx - 5.dp.toPx(), cy - 5.dp.toPx()),
                        end = Offset(cx + 5.dp.toPx(), cy + 5.dp.toPx()),
                        strokeWidth = 3.dp.toPx()
                    )
                }
            }
        }

        // Glowing yellow Pacman in Center
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
                style = Stroke(width = 4.dp.toPx())
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
