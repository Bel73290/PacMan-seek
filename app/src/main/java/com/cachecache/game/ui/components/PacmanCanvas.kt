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
import com.cachecache.game.ui.theme.DarkContrast
import com.cachecache.game.ui.theme.PacmanYellow
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

    drawArc(
        color = color,
        startAngle = startAngle,
        sweepAngle = sweepAngle,
        useCenter = true,
        topLeft = Offset(center.x - radius, center.y - radius),
        size = Size(radius * 2f, radius * 2f)
    )

    // Eye position (above mouth opening)
    val eyeRadius = radius * 0.12f
    val eyeX = center.x + radius * 0.15f
    val eyeY = center.y - radius * 0.45f

    drawCircle(
        color = DarkContrast,
        radius = eyeRadius,
        center = Offset(eyeX, eyeY)
    )
}
