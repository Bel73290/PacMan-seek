package com.cachecache.game.ui.components

import androidx.compose.animation.animateColor
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cachecache.game.ui.theme.DarkContrast
import com.cachecache.game.ui.theme.GhostRed
import com.cachecache.game.ui.theme.ScaredBlue
import com.cachecache.game.ui.theme.TextPrimary
import kotlin.math.sin

@Composable
fun GhostComposable(
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    baseColor: Color = GhostRed,
    isScared: Boolean = false,
    isVulnerableBlinking: Boolean = false,
    lookDirection: Offset = Offset(1f, 0f)
) {
    // 1. Hovering (vertical sinusoidal displacement +/- 6 dp)
    val infiniteTransition = rememberInfiniteTransition(label = "ghostHover")
    val hoverPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.283185f, // 2 * PI
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "hoverPhase"
    )

    // 2. Skirt Bezier Wave Oscillation
    val skirtPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.283185f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "skirtPhase"
    )

    // 3. Fast panic tremble for scared mode
    val tremblePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.283185f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "tremblePhase"
    )

    // 4. Vulnerability blinking between blue and off-white in the last seconds
    val blinkColor by infiniteTransition.animateColor(
        initialValue = ScaredBlue,
        targetValue = Color(0xFFE8EEF5),
        animationSpec = infiniteRepeatable(
            animation = tween(220, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blinkColor"
    )

    val targetBodyColor = when {
        isScared && isVulnerableBlinking -> blinkColor
        isScared -> ScaredBlue
        else -> baseColor
    }

    val animatedColor by animateColorAsState(
        targetValue = targetBodyColor,
        animationSpec = tween(300),
        label = "ghostColor"
    )

    Canvas(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                translationY = sin(hoverPhase) * 6.dp.toPx()
                translationX = if (isScared) sin(tremblePhase * 30f) * 3.dp.toPx() else 0f
            }
    ) {
        drawGhost(
            bodyColor = animatedColor,
            isScared = isScared,
            lookDirection = lookDirection,
            skirtWaveOffset = skirtPhase
        )
    }
}

fun DrawScope.drawGhost(
    bodyColor: Color,
    isScared: Boolean,
    lookDirection: Offset = Offset(1f, 0f),
    skirtWaveOffset: Float = 0f
) {
    val w = size.width
    val h = size.height
    val radius = w / 2f

    // 1. Ghost Body Path with 3 animated wavy Bezier skirt ruffles
    val path = Path().apply {
        // Top dome
        moveTo(0f, radius)
        cubicTo(
            0f, 0f,
            w, 0f,
            w, radius
        )
        lineTo(w, h * 0.82f)

        // 3 bottom ruffle waves with oscillating Bezier control points
        val waveWidth = w / 3f
        val bottomY = h
        val innerY = h * 0.78f

        // Wave 1
        quadraticTo(
            w - waveWidth * 0.5f + skirtWaveOffset,
            bottomY,
            w - waveWidth,
            innerY
        )
        // Wave 2
        quadraticTo(
            w - waveWidth * 1.5f - skirtWaveOffset,
            bottomY,
            w - waveWidth * 2f,
            innerY
        )
        // Wave 3
        quadraticTo(
            w - waveWidth * 2.5f + skirtWaveOffset,
            bottomY,
            0f,
            innerY
        )

        lineTo(0f, radius)
        close()
    }

    // Outer dark stroke for 3D outline
    drawPath(path = path, color = DarkContrast, style = Stroke(width = 2.5.dp.toPx()))
    // Body fill
    drawPath(path = path, color = bodyColor)

    // 2. Eyes and Mouth
    if (!isScared) {
        // Normal mode eyes with interactive gaze towards player
        val eyeWidth = w * 0.23f
        val eyeHeight = h * 0.28f
        val pupilRadius = eyeWidth * 0.36f

        val leftEyeCenter = Offset(w * 0.32f, h * 0.38f)
        val rightEyeCenter = Offset(w * 0.68f, h * 0.38f)

        // White oval eye whites
        drawOval(
            color = TextPrimary,
            topLeft = Offset(leftEyeCenter.x - eyeWidth / 2f, leftEyeCenter.y - eyeHeight / 2f),
            size = Size(eyeWidth, eyeHeight)
        )
        drawOval(
            color = TextPrimary,
            topLeft = Offset(rightEyeCenter.x - eyeWidth / 2f, rightEyeCenter.y - eyeHeight / 2f),
            size = Size(eyeWidth, eyeHeight)
        )

        // Pupil offset tracking player direction
        val dirX = lookDirection.x.coerceIn(-1f, 1f) * (eyeWidth * 0.2f)
        val dirY = lookDirection.y.coerceIn(-1f, 1f) * (eyeHeight * 0.2f)

        drawCircle(
            color = DarkContrast,
            radius = pupilRadius,
            center = Offset(leftEyeCenter.x + dirX, leftEyeCenter.y + dirY)
        )
        drawCircle(
            color = DarkContrast,
            radius = pupilRadius,
            center = Offset(rightEyeCenter.x + dirX, rightEyeCenter.y + dirY)
        )
    } else {
        // Scared mode: terrified round eyes with round white pupils + zigzag wavy mouth!
        val eyeRadius = w * 0.09f
        val leftEyeCenter = Offset(w * 0.32f, h * 0.36f)
        val rightEyeCenter = Offset(w * 0.68f, h * 0.36f)

        drawCircle(color = TextPrimary, radius = eyeRadius, center = leftEyeCenter)
        drawCircle(color = TextPrimary, radius = eyeRadius, center = rightEyeCenter)

        // Wavy zigzag mouth
        val mouthPath = Path().apply {
            val startX = w * 0.22f
            val endX = w * 0.78f
            val mouthY = h * 0.64f
            val segWidth = (endX - startX) / 6f
            val waveAmp = h * 0.06f

            moveTo(startX, mouthY)
            var currentX = startX
            var up = true
            for (i in 0 until 6) {
                val nextX = currentX + segWidth
                val targetY = if (up) mouthY - waveAmp else mouthY + waveAmp
                lineTo(nextX, targetY)
                currentX = nextX
                up = !up
            }
        }

        drawPath(
            path = mouthPath,
            color = TextPrimary,
            style = Stroke(width = w * 0.065f, cap = StrokeCap.Round)
        )
    }
}
