package com.cachecache.game.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cachecache.game.R
import com.cachecache.game.ui.theme.*
import kotlin.math.sin

@Composable
fun StreetViewComposable(
    modifier: Modifier = Modifier,
    isMini: Boolean = false,
    timerText: String = "02:18",
    onExpand: () -> Unit = {}
) {
    // Hovering & jump animation for the scared ghost in the street
    val infiniteTransition = rememberInfiniteTransition(label = "streetGhostTransition")
    val ghostBounce by infiniteTransition.animateFloat(
        initialValue = -7f,
        targetValue = 7f,
        animationSpec = infiniteRepeatable(
            animation = tween(550, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ghostBounce"
    )

    val ghostTremble by infiniteTransition.animateFloat(
        initialValue = -3.5f,
        targetValue = 3.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(75, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ghostTremble"
    )

    // Animated dots conveyor phase
    val pacDotsAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1100, easing = LinearEasing)),
        label = "dotsAnim"
    )

    // Reticule pulse
    val reticuleScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "reticuleScale"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(if (isMini) 16.dp else 0.dp))
            .border(if (isMini) 1.5.dp else 0.dp, BorderSubtle, RoundedCornerShape(if (isMini) 16.dp else 0.dp))
            .clickable(enabled = isMini, onClick = onExpand)
    ) {
        // 1. Real Authentic Street Photo Background
        Image(
            painter = painterResource(id = R.drawable.street_view_bg),
            contentDescription = "Vue rue Street View",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // 2. AR Game Elements (Reticule, Pac-dots, Pacman, Scared Ghost)
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawStreetAROverlay(
                pacDotsPhase = pacDotsAnim,
                ghostBounce = ghostBounce,
                ghostTremble = ghostTremble,
                reticuleScale = reticuleScale,
                isMini = isMini
            )
        }

        // 3. Overlays for Mini Card Mode
        if (isMini) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Timer Pill
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkContrast.copy(alpha = 0.85f))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = "Timer",
                        tint = PacmanYellow,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = timerText,
                        color = TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Expand Fullscreen Button
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(DarkContrast.copy(alpha = 0.85f))
                        .clickable { onExpand() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.OpenInFull,
                        contentDescription = "Agrandir",
                        tint = TextPrimary,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }

            // Bottom Left Watermark
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Street View",
                    tint = TextPrimary.copy(alpha = 0.9f),
                    modifier = Modifier.size(11.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "OpenStreetView",
                    color = TextPrimary.copy(alpha = 0.95f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private fun DrawScope.drawStreetAROverlay(
    pacDotsPhase: Float,
    ghostBounce: Float,
    ghostTremble: Float,
    reticuleScale: Float,
    isMini: Boolean
) {
    val w = size.width
    val h = size.height

    val vanishX = w * 0.52f
    val vanishY = h * 0.44f

    // 1. Blue ground targeting reticule on the pavement
    val retCenter = Offset(w * 0.62f, h * 0.82f)
    val retRadius = (if (isMini) 16.dp.toPx() else 32.dp.toPx()) * reticuleScale
    drawCircle(
        color = ScaredBlue.copy(alpha = 0.25f),
        radius = retRadius,
        center = retCenter
    )
    drawCircle(
        color = ScaredBlue.copy(alpha = 0.85f),
        radius = retRadius,
        center = retCenter,
        style = Stroke(width = 2.dp.toPx())
    )

    // 2. Conveyor belt of glowing yellow Pac-dots along the street
    val dotCount = 8
    for (i in 1..dotCount) {
        val t = ((i + pacDotsPhase) % (dotCount + 1)) / (dotCount + 1)
        val x = vanishX + (w * 0.54f - vanishX) * t
        val y = vanishY + (h * 0.94f - vanishY) * t
        val dotRadius = (2.dp.toPx() + (9.dp.toPx() * t))

        drawCircle(
            color = PacmanYellow.copy(alpha = 0.35f),
            radius = dotRadius * 1.6f,
            center = Offset(x, y)
        )
        drawCircle(
            color = PacmanYellow,
            radius = dotRadius,
            center = Offset(x, y)
        )
    }

    // 3. Small Pacman in the background down the road
    val pacX = vanishX - w * 0.04f
    val pacY = vanishY + h * 0.08f
    val pacSize = if (isMini) 16.dp.toPx() else 26.dp.toPx()

    drawArc(
        color = PacmanYellow,
        startAngle = 200f,
        sweepAngle = 300f,
        useCenter = true,
        topLeft = Offset(pacX - pacSize / 2f, pacY - pacSize / 2f),
        size = Size(pacSize, pacSize)
    )

    // 4. Large Animated Scared Blue Ghost in the Foreground
    val ghostBaseX = w * 0.62f + ghostTremble
    val ghostBaseY = h * 0.66f + ghostBounce
    val ghostW = if (isMini) w * 0.30f else w * 0.40f
    val ghostH = ghostW * 1.18f
    val ghostTopLeft = Offset(ghostBaseX - ghostW / 2f, ghostBaseY - ghostH / 2f)

    // Ground shadow
    drawOval(
        color = Color.Black.copy(alpha = 0.55f),
        topLeft = Offset(ghostBaseX - ghostW * 0.45f, ghostBaseY + ghostH * 0.46f),
        size = Size(ghostW * 0.9f, ghostH * 0.22f)
    )

    // Ghost body path with wavy ruffles
    val ghostPath = Path().apply {
        val r = ghostW / 2f
        moveTo(ghostTopLeft.x, ghostTopLeft.y + r)
        cubicTo(
            ghostTopLeft.x, ghostTopLeft.y,
            ghostTopLeft.x + ghostW, ghostTopLeft.y,
            ghostTopLeft.x + ghostW, ghostTopLeft.y + r
        )
        lineTo(ghostTopLeft.x + ghostW, ghostTopLeft.y + ghostH * 0.84f)

        val waveW = ghostW / 3f
        val botY = ghostTopLeft.y + ghostH
        val inY = ghostTopLeft.y + ghostH * 0.80f

        quadraticTo(ghostTopLeft.x + ghostW - waveW * 0.5f, botY, ghostTopLeft.x + ghostW - waveW, inY)
        quadraticTo(ghostTopLeft.x + ghostW - waveW * 1.5f, botY, ghostTopLeft.x + ghostW - waveW * 2f, inY)
        quadraticTo(ghostTopLeft.x + ghostW - waveW * 2.5f, botY, ghostTopLeft.x, inY)
        lineTo(ghostTopLeft.x, ghostTopLeft.y + r)
        close()
    }

    drawPath(path = ghostPath, color = DarkContrast, style = Stroke(width = 3.5.dp.toPx()))
    drawPath(path = ghostPath, color = ScaredBlue)

    // Scared worried eyes
    val eyeW = ghostW * 0.24f
    val eyeH = ghostH * 0.28f
    val leftEyeCenter = Offset(ghostTopLeft.x + ghostW * 0.32f, ghostTopLeft.y + ghostH * 0.38f)
    val rightEyeCenter = Offset(ghostTopLeft.x + ghostW * 0.68f, ghostTopLeft.y + ghostH * 0.38f)

    drawOval(
        color = TextPrimary,
        topLeft = Offset(leftEyeCenter.x - eyeW / 2f, leftEyeCenter.y - eyeH / 2f),
        size = Size(eyeW, eyeH)
    )
    drawOval(
        color = TextPrimary,
        topLeft = Offset(rightEyeCenter.x - eyeW / 2f, rightEyeCenter.y - eyeH / 2f),
        size = Size(eyeW, eyeH)
    )

    // Pupils looking back at Pacman
    drawCircle(color = DarkContrast, radius = eyeW * 0.34f, center = Offset(leftEyeCenter.x - 2f, leftEyeCenter.y - 2f))
    drawCircle(color = DarkContrast, radius = eyeW * 0.34f, center = Offset(rightEyeCenter.x - 2f, rightEyeCenter.y - 2f))

    // Wavy mouth
    val mouthPath = Path().apply {
        val startX = ghostTopLeft.x + ghostW * 0.22f
        val endX = ghostTopLeft.x + ghostW * 0.78f
        val mouthY = ghostTopLeft.y + ghostH * 0.66f
        val segW = (endX - startX) / 6f
        val waveAmp = ghostH * 0.05f

        moveTo(startX, mouthY)
        var currX = startX
        var up = true
        for (i in 0 until 6) {
            val nextX = currX + segW
            val targetY = if (up) mouthY - waveAmp else mouthY + waveAmp
            lineTo(nextX, targetY)
            currX = nextX
            up = !up
        }
    }
    drawPath(
        path = mouthPath,
        color = TextPrimary,
        style = Stroke(width = ghostW * 0.065f, cap = StrokeCap.Round)
    )

    // Panic sweat drops
    val sweat1 = Offset(ghostTopLeft.x + ghostW * 0.88f, ghostTopLeft.y + ghostH * 0.12f)
    val sweat2 = Offset(ghostTopLeft.x + ghostW * 0.98f, ghostTopLeft.y + ghostH * 0.24f)
    drawCircle(color = ScaredBlue, radius = 4.5.dp.toPx(), center = sweat1)
    drawCircle(color = TextPrimary, radius = 2.dp.toPx(), center = Offset(sweat1.x - 1f, sweat1.y - 1f))
    drawCircle(color = ScaredBlue, radius = 3.5.dp.toPx(), center = sweat2)
}
