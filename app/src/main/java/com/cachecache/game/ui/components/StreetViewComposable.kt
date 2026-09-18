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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cachecache.game.R
import com.cachecache.game.ui.theme.*

@Composable
fun StreetViewComposable(
    modifier: Modifier = Modifier,
    isMini: Boolean = false,
    timerText: String = "02:18",
    onExpand: () -> Unit = {}
) {
    // Panicked horizontal tremble & walking skirt animation for the scared ghost in the street (NO vertical bounce)
    val infiniteTransition = rememberInfiniteTransition(label = "streetGhostTransition")
    val ghostTremble by infiniteTransition.animateFloat(
        initialValue = -3.5f,
        targetValue = 3.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(70, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ghostTremble"
    )

    val walkFrame by infiniteTransition.animateValue(
        initialValue = 0,
        targetValue = 1,
        typeConverter = Int.VectorConverter,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 360, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "walkFrame"
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

        // 2. AR Game Elements (Reticule, Conveyor Pac-dots)
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawStreetAROverlay(
                pacDotsPhase = pacDotsAnim,
                reticuleScale = reticuleScale,
                isMini = isMini
            )
        }

        // 3. Simple Round Pacman with Chomp Animation in the background down the road
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(
                    start = if (isMini) 62.dp else 125.dp,
                    top = if (isMini) 52.dp else 240.dp
                )
        ) {
            PacmanComposable(
                size = if (isMini) 22.dp else 48.dp,
                isAnimated = true,
                rotationAngle = 15f
            )
        }

        // 4. Large Animated Official Scared Blue Ghost in the Foreground (Horizontal tremble only, no up/down)
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(
                    end = if (isMini) 16.dp else 42.dp,
                    bottom = if (isMini) 14.dp else 120.dp
                )
                .graphicsLayer {
                    translationX = ghostTremble * (if (isMini) 1.2f else 3.2f)
                }
        ) {
            Image(
                painter = painterResource(
                    id = if (walkFrame == 0) R.drawable.ghost_scared_blue_0 else R.drawable.ghost_scared_blue_1
                ),
                contentDescription = "Fantôme apeuré",
                modifier = Modifier.size(if (isMini) 48.dp else 135.dp),
                contentScale = ContentScale.Fit
            )
        }

        // 5. Overlays for Mini Card Mode
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
}
