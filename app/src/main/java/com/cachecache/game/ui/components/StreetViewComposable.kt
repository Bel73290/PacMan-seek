package com.cachecache.game.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cachecache.game.ui.theme.*
import kotlin.math.sin

@Composable
fun StreetViewComposable(
    modifier: Modifier = Modifier,
    isMini: Boolean = false,
    timerText: String = "02:18",
    onExpand: () -> Unit = {}
) {
    // Gentle jumping animation for the scared ghost in the street
    val infiniteTransition = rememberInfiniteTransition(label = "ghostJumpTransition")
    val ghostBounce by infiniteTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(550, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ghostBounce"
    )

    val ghostTremble by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(75, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ghostTremble"
    )

    // Animated dots phase
    val pacDotsAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1200, easing = LinearEasing)),
        label = "dotsAnim"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(if (isMini) 16.dp else 0.dp))
            .border(if (isMini) 1.5.dp else 0.dp, BorderSubtle, RoundedCornerShape(if (isMini) 16.dp else 0.dp))
            .clickable(enabled = isMini, onClick = onExpand)
    ) {
        // 1. Procedural Realistic Street Scene Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawPerspectiveStreetScene(
                pacDotsPhase = pacDotsAnim,
                ghostBounce = ghostBounce,
                ghostTremble = ghostTremble,
                isMini = isMini
            )
        }

        // 2. Overlays for Mini Card Mode
        if (isMini) {
            // Top Bar in Mini Card: Timer pill + Expand button
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
                        .background(DarkContrast.copy(alpha = 0.8f))
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
                        .background(DarkContrast.copy(alpha = 0.8f))
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

            // Bottom Left Watermark: OpenStreetView
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Street View",
                    tint = TextPrimary.copy(alpha = 0.8f),
                    modifier = Modifier.size(11.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "OpenStreetView",
                    color = TextPrimary.copy(alpha = 0.85f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * Draws a realistic perspective street view with road, sidewalks, stone walls,
 * trees, Pac-dots along the road, Pacman in background, and cute scared ghost in foreground.
 */
private fun DrawScope.drawPerspectiveStreetScene(
    pacDotsPhase: Float,
    ghostBounce: Float,
    ghostTremble: Float,
    isMini: Boolean
) {
    val w = size.width
    val h = size.height

    val vanishX = w * 0.58f
    val vanishY = h * 0.22f

    // 1. Sky Gradient
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF88A4BD), Color(0xFFC3D4E2)),
            startY = 0f,
            endY = vanishY
        ),
        topLeft = Offset.Zero,
        size = Size(w, vanishY)
    )

    // 2. City Buildings / Walls Left & Right
    // Left stone wall / building facade
    val leftWall = Path().apply {
        moveTo(0f, 0f)
        lineTo(w * 0.35f, vanishY)
        lineTo(w * 0.32f, h)
        lineTo(0f, h)
        close()
    }
    drawPath(
        path = leftWall,
        brush = Brush.horizontalGradient(
            colors = listOf(Color(0xFF3F3B36), Color(0xFF6E6962)),
            startX = 0f,
            endX = w * 0.35f
        )
    )

    // Left wall windows / pillars
    for (i in 1..4) {
        val y1 = vanishY + (h - vanishY) * (i * 0.2f)
        val x1 = (w * 0.35f) * (1f - i * 0.2f)
        drawLine(
            color = Color(0xFF282522),
            start = Offset(x1, y1),
            end = Offset(x1, y1 + 30f),
            strokeWidth = 3.dp.toPx()
        )
    }

    // Right stone wall / buildings / green trees
    val rightWall = Path().apply {
        moveTo(w, 0f)
        lineTo(w * 0.72f, vanishY)
        lineTo(w * 0.88f, h)
        lineTo(w, h)
        close()
    }
    drawPath(
        path = rightWall,
        brush = Brush.horizontalGradient(
            colors = listOf(Color(0xFF787268), Color(0xFF4A453D)),
            startX = w * 0.72f,
            endX = w
        )
    )

    // Right green trees foliage
    drawCircle(
        color = Color(0xFF3E582E),
        radius = w * 0.18f,
        center = Offset(w * 0.88f, vanishY + h * 0.15f)
    )

    // 3. Paved Road Asphalt in Perspective
    val roadPath = Path().apply {
        moveTo(w * 0.35f, vanishY)
        lineTo(w * 0.72f, vanishY)
        lineTo(w * 0.88f, h)
        lineTo(w * 0.32f, h)
        close()
    }
    drawPath(
        path = roadPath,
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF535150), Color(0xFF2D2C2B)),
            startY = vanishY,
            endY = h
        )
    )

    // Road side curbs (Granite curbstones)
    drawLine(
        color = Color(0xFF8A8884),
        start = Offset(w * 0.35f, vanishY),
        end = Offset(w * 0.32f, h),
        strokeWidth = 4.dp.toPx()
    )
    drawLine(
        color = Color(0xFF8A8884),
        start = Offset(w * 0.72f, vanishY),
        end = Offset(w * 0.88f, h),
        strokeWidth = 4.dp.toPx()
    )

    // 4. Yellow Pac-dots Along Road in Perspective
    val dotCount = 7
    for (i in 1..dotCount) {
        val t = ((i + pacDotsPhase) % (dotCount + 1)) / (dotCount + 1)
        val x = vanishX + (w * 0.54f - vanishX) * t
        val y = vanishY + (h * 0.95f - vanishY) * t
        val dotRadius = (2.dp.toPx() + (8.dp.toPx() * t))

        // Glowing yellow dot
        drawCircle(
            color = PacmanYellow.copy(alpha = 0.4f),
            radius = dotRadius * 1.5f,
            center = Offset(x, y)
        )
        drawCircle(
            color = PacmanYellow,
            radius = dotRadius,
            center = Offset(x, y)
        )
    }

    // 5. Small Pacman in the Background (Up the street)
    val pacX = vanishX - w * 0.08f
    val pacY = vanishY + h * 0.06f
    val pacSize = if (isMini) 14.dp.toPx() else 24.dp.toPx()

    drawArc(
        color = PacmanYellow,
        startAngle = 200f,
        sweepAngle = 300f,
        useCenter = true,
        topLeft = Offset(pacX - pacSize / 2f, pacY - pacSize / 2f),
        size = Size(pacSize, pacSize)
    )

    // 6. Large Animated Scared Blue Ghost in the Foreground
    val ghostBaseX = w * 0.60f + ghostTremble
    val ghostBaseY = h * 0.68f + ghostBounce
    val ghostW = if (isMini) w * 0.28f else w * 0.36f
    val ghostH = ghostW * 1.18f

    val ghostTopLeft = Offset(ghostBaseX - ghostW / 2f, ghostBaseY - ghostH / 2f)

    // Shadow on the asphalt
    drawOval(
        color = Color.Black.copy(alpha = 0.45f),
        topLeft = Offset(ghostBaseX - ghostW * 0.45f, ghostBaseY + ghostH * 0.45f),
        size = Size(ghostW * 0.9f, ghostH * 0.22f)
    )

    // Ghost body path (rounded dome + 3 wavy ruffle feet)
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

    // Ghost fill: electric blue with dark navy outline
    drawPath(path = ghostPath, color = DarkContrast, style = Stroke(width = 3.dp.toPx()))
    drawPath(path = ghostPath, color = ScaredBlue)

    // Ghost worried eyes (white ovals with black pupils looking up/worried)
    val eyeW = ghostW * 0.25f
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

    // Terrified pupils looking towards Pacman
    drawCircle(color = DarkContrast, radius = eyeW * 0.32f, center = Offset(leftEyeCenter.x - 2f, leftEyeCenter.y - 2f))
    drawCircle(color = DarkContrast, radius = eyeW * 0.32f, center = Offset(rightEyeCenter.x - 2f, rightEyeCenter.y - 2f))

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
        style = Stroke(width = ghostW * 0.06f, cap = StrokeCap.Round)
    )

    // Panic sweat drops flying from the ghost
    if (!isMini) {
        val sweat1 = Offset(ghostTopLeft.x + ghostW * 0.90f, ghostTopLeft.y + ghostH * 0.12f)
        val sweat2 = Offset(ghostTopLeft.x + ghostW * 0.98f, ghostTopLeft.y + ghostH * 0.24f)
        drawCircle(color = ScaredBlue.copy(alpha = 0.9f), radius = 4.dp.toPx(), center = sweat1)
        drawCircle(color = ScaredBlue.copy(alpha = 0.9f), radius = 3.dp.toPx(), center = sweat2)
    }
}
