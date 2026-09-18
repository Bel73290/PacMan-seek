package com.cachecache.game.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
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

@Composable
fun GhostComposable(
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    baseColor: Color = GhostRed,
    isScared: Boolean = false,
    lookDirection: Offset = Offset(1f, 0f)
) {
    val animatedColor by animateColorAsState(
        targetValue = if (isScared) ScaredBlue else baseColor,
        animationSpec = tween(400),
        label = "ghostColor"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "ghostTremble")
    val trembleOffset by if (isScared) {
        infiniteTransition.animateFloat(
            initialValue = -3f,
            targetValue = 3f,
            animationSpec = infiniteRepeatable(
                animation = tween(70, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "trembleX"
        )
    } else {
        rememberInfiniteTransition(label = "idleTremble").animateFloat(
            initialValue = 0f,
            targetValue = 0f,
            animationSpec = infiniteRepeatable(tween(1000)),
            label = "idleTrembleX"
        )
    }

    Canvas(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                translationX = trembleOffset
            }
    ) {
        drawGhost(
            bodyColor = animatedColor,
            isScared = isScared,
            lookDirection = lookDirection
        )
    }
}

fun DrawScope.drawGhost(
    bodyColor: Color,
    isScared: Boolean,
    lookDirection: Offset = Offset(1f, 0f)
) {
    val w = size.width
    val h = size.height
    val radius = w / 2f

    // 1. Ghost Body Path
    val path = Path().apply {
        // Top semicircle dome
        moveTo(0f, radius)
        cubicTo(
            0f, 0f,
            w, 0f,
            w, radius
        )
        // Right side going down
        lineTo(w, h * 0.85f)

        // 3 bottom ruffle waves (tentacles)
        val waveWidth = w / 3f
        val bottomY = h
        val innerY = h * 0.82f

        // Wave 1
        quadraticTo(w - waveWidth * 0.5f, bottomY, w - waveWidth, innerY)
        // Wave 2
        quadraticTo(w - waveWidth * 1.5f, bottomY, w - waveWidth * 2f, innerY)
        // Wave 3
        quadraticTo(w - waveWidth * 2.5f, bottomY, 0f, innerY)

        // Left side up
        lineTo(0f, radius)
        close()
    }

    drawPath(path = path, color = bodyColor)

    // 2. Eyes and Mouth
    if (!isScared) {
        // Normal mode eyes
        val eyeWidth = w * 0.22f
        val eyeHeight = h * 0.26f
        val pupilRadius = eyeWidth * 0.35f

        val leftEyeCenter = Offset(w * 0.32f, h * 0.38f)
        val rightEyeCenter = Offset(w * 0.68f, h * 0.38f)

        // White ovals
        drawOval(
            color = TextPrimary,
            topLeft = Offset(leftEyeCenter.x - eyeWidth / 2f, leftEyeCenter.y - eyeHeight / 2f),
            size = androidx.compose.ui.geometry.Size(eyeWidth, eyeHeight)
        )
        drawOval(
            color = TextPrimary,
            topLeft = Offset(rightEyeCenter.x - eyeWidth / 2f, rightEyeCenter.y - eyeHeight / 2f),
            size = androidx.compose.ui.geometry.Size(eyeWidth, eyeHeight)
        )

        // Blue / dark pupils looking in direction
        val dirX = lookDirection.x.coerceIn(-1f, 1f) * (eyeWidth * 0.18f)
        val dirY = lookDirection.y.coerceIn(-1f, 1f) * (eyeHeight * 0.18f)

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
        // Scared mode: small round white eyes with tiny pupils + wavy zigzag mouth!
        val eyeRadius = w * 0.08f
        val leftEyeCenter = Offset(w * 0.32f, h * 0.36f)
        val rightEyeCenter = Offset(w * 0.68f, h * 0.36f)

        drawCircle(color = TextPrimary, radius = eyeRadius, center = leftEyeCenter)
        drawCircle(color = TextPrimary, radius = eyeRadius, center = rightEyeCenter)

        // Wavy white mouth
        val mouthPath = Path().apply {
            val startX = w * 0.22f
            val endX = w * 0.78f
            val mouthY = h * 0.62f
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
            style = Stroke(width = w * 0.06f, cap = StrokeCap.Round)
        )
    }
}
