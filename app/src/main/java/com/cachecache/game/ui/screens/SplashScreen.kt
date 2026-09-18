package com.cachecache.game.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cachecache.game.model.GameState
import com.cachecache.game.ui.components.GhostComposable
import com.cachecache.game.ui.components.PacmanComposable
import com.cachecache.game.ui.components.SettingsDialog
import com.cachecache.game.ui.theme.*

@Composable
fun SplashScreen(
    state: GameState,
    onStartGame: () -> Unit,
    onOpenLeaderboard: () -> Unit,
    onOpenGuide: () -> Unit,
    onOpenSettings: () -> Unit,
    onCloseSettings: () -> Unit,
    onSaveSettings: (Int, Int) -> Unit
) {
    // Star twinkle animation
    val infiniteTransition = rememberInfiniteTransition(label = "starTwinkle")
    val starAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "starAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepNavy)
    ) {
        // Procedural Starry Night Background
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawStarryNight(starAlpha)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar: Guide & Settings
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onOpenGuide,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(CardSurface)
                        .border(1.dp, BorderSubtle, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Règles du jeu",
                        tint = TextSecondary
                    )
                }

                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(CardSurface)
                        .border(1.dp, BorderSubtle, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Réglages",
                        tint = PacmanYellow
                    )
                }
            }

            // 3D "Cache Cache" Title with Pacman Icon on the Right (100% Vector)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        // 3D Blue Extrusion Layer
                        Column {
                            Text(
                                text = "Cache",
                                fontSize = 48.sp,
                                lineHeight = 46.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.SansSerif,
                                color = Color(0xFF0F264A),
                                modifier = Modifier.offset(x = 3.dp, y = 4.dp)
                            )
                            Text(
                                text = "Cache",
                                fontSize = 48.sp,
                                lineHeight = 46.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.SansSerif,
                                color = Color(0xFF0F264A),
                                modifier = Modifier.offset(x = 3.dp, y = 4.dp)
                            )
                        }
                        // Yellow Foreground Layer
                        Column {
                            Text(
                                text = "Cache",
                                fontSize = 48.sp,
                                lineHeight = 46.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.SansSerif,
                                color = PacmanYellow
                            )
                            Text(
                                text = "Cache",
                                fontSize = 48.sp,
                                lineHeight = 46.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.SansSerif,
                                color = PacmanYellow
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // Pacman biting logo icon on the right
                    PacmanComposable(
                        size = 54.dp,
                        color = PacmanYellow,
                        isAnimated = true,
                        rotationAngle = 180f
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Explore la carte, trouve les cachés !",
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }

            // Central 3D Perspective Isometric Map Card (100% Native Vector Canvas)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .height(290.dp)
                    .border(1.5.dp, BorderSubtle, RoundedCornerShape(26.dp))
                    .shadow(16.dp, RoundedCornerShape(26.dp), spotColor = ScaredBlue),
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = NavyIntermediate)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawIsometricPerspectiveMap()
                    }

                    // Vector Characters placed on perspective card
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Pink Ghost with pin (top left)
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(start = 32.dp, top = 48.dp)
                        ) {
                            GhostComposable(size = 36.dp, baseColor = GhostPink, isScared = false)
                        }

                        // Blue Scared Ghost (middle left)
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .padding(start = 64.dp)
                        ) {
                            GhostComposable(size = 44.dp, baseColor = ScaredBlue, isScared = true)
                        }

                        // Yellow Pacman (bottom right chomping dots)
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(end = 52.dp, bottom = 38.dp)
                        ) {
                            PacmanComposable(size = 52.dp, color = PacmanYellow, isAnimated = true)
                        }
                    }
                }
            }

            // Action Buttons: "Jouer" and "Classement"
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Button(
                    onClick = onStartGame,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .shadow(14.dp, RoundedCornerShape(29.dp), spotColor = PacmanYellow),
                    shape = RoundedCornerShape(29.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PacmanYellow,
                        contentColor = DarkContrast
                    )
                ) {
                    Text(
                        text = "Jouer",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Button(
                    onClick = onOpenLeaderboard,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(27.dp)),
                    shape = RoundedCornerShape(27.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CardSurface,
                        contentColor = TextPrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "Classement",
                        tint = GoldTrophy,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Classement",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Settings Dialog
        if (state.showSettingsDialog) {
            SettingsDialog(
                currentSettings = state.settings,
                onDismiss = onCloseSettings,
                onSave = onSaveSettings
            )
        }
    }
}

private fun DrawScope.drawStarryNight(starAlpha: Float) {
    val stars = listOf(
        Offset(size.width * 0.15f, size.height * 0.08f),
        Offset(size.width * 0.85f, size.height * 0.12f),
        Offset(size.width * 0.25f, size.height * 0.22f),
        Offset(size.width * 0.72f, size.height * 0.05f),
        Offset(size.width * 0.90f, size.height * 0.28f),
        Offset(size.width * 0.08f, size.height * 0.35f)
    )

    stars.forEachIndexed { index, star ->
        val radius = if (index % 2 == 0) 2.5.dp.toPx() else 1.8.dp.toPx()
        val alpha = if (index % 2 == 0) starAlpha else (1.3f - starAlpha).coerceIn(0.2f, 1f)
        drawCircle(color = TextPrimary.copy(alpha = alpha), radius = radius, center = star)

        // Light cross sparkles
        if (index % 3 == 0) {
            drawLine(
                color = TextPrimary.copy(alpha = alpha * 0.6f),
                start = Offset(star.x - 5.dp.toPx(), star.y),
                end = Offset(star.x + 5.dp.toPx(), star.y),
                strokeWidth = 1.dp.toPx()
            )
            drawLine(
                color = TextPrimary.copy(alpha = alpha * 0.6f),
                start = Offset(star.x, star.y - 5.dp.toPx()),
                end = Offset(star.x, star.y + 5.dp.toPx()),
                strokeWidth = 1.dp.toPx()
            )
        }
    }
}

private fun DrawScope.drawIsometricPerspectiveMap() {
    val w = size.width
    val h = size.height

    // 1. Dark Gradient Plane
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF091424), Color(0xFF132A4C)),
            startY = 0f,
            endY = h
        )
    )

    // 2. Distant City Skyline Silhouettes
    val skylinePath = Path().apply {
        moveTo(0f, h * 0.38f)
        lineTo(w * 0.12f, h * 0.38f)
        lineTo(w * 0.12f, h * 0.28f)
        lineTo(w * 0.22f, h * 0.28f)
        lineTo(w * 0.22f, h * 0.38f)
        lineTo(w * 0.35f, h * 0.38f)
        lineTo(w * 0.38f, h * 0.20f)
        lineTo(w * 0.46f, h * 0.20f)
        lineTo(w * 0.49f, h * 0.38f)
        lineTo(w * 0.62f, h * 0.38f)
        lineTo(w * 0.65f, h * 0.25f)
        lineTo(w * 0.74f, h * 0.25f)
        lineTo(w * 0.76f, h * 0.38f)
        lineTo(w * 0.88f, h * 0.30f)
        lineTo(w * 0.94f, h * 0.30f)
        lineTo(w * 0.96f, h * 0.38f)
        lineTo(w, h * 0.38f)
        lineTo(w, h)
        lineTo(0f, h)
        close()
    }
    drawPath(path = skylinePath, color = Color(0xFF0C1B33))

    // 3. Isometric Glowing Grid Lines
    for (i in 0..8) {
        val yStart = h * 0.42f + (h * 0.55f) * (i / 8f)
        drawLine(
            color = BorderSubtle.copy(alpha = 0.4f),
            start = Offset(0f, yStart),
            end = Offset(w, yStart),
            strokeWidth = 1.dp.toPx()
        )
    }

    val gridCols = 8
    for (i in 0..gridCols) {
        val xTop = w * (i / gridCols.toFloat())
        val xBot = (xTop - w * 0.5f) * 1.6f + w * 0.5f
        drawLine(
            color = BorderSubtle.copy(alpha = 0.4f),
            start = Offset(xTop, h * 0.42f),
            end = Offset(xBot, h),
            strokeWidth = 1.dp.toPx()
        )
    }

    // 4. Glowing Pac-dots Path on Ground
    val dotPoints = listOf(
        Offset(w * 0.32f, h * 0.75f),
        Offset(w * 0.42f, h * 0.78f),
        Offset(w * 0.54f, h * 0.82f),
        Offset(w * 0.68f, h * 0.85f)
    )
    dotPoints.forEach { pt ->
        drawCircle(color = PacmanYellow.copy(alpha = 0.35f), radius = 6.dp.toPx(), center = pt)
        drawCircle(color = PacmanYellow, radius = 3.5.dp.toPx(), center = pt)
    }

    // 5. Map Location Pins (Pins with dots)
    drawLocationPin(center = Offset(w * 0.22f, h * 0.62f), pinColor = GhostPink)
    drawLocationPin(center = Offset(w * 0.82f, h * 0.52f), pinColor = GhostOrange)
}

private fun DrawScope.drawLocationPin(center: Offset, pinColor: Color) {
    val pinRadius = 6.dp.toPx()
    drawCircle(color = pinColor, radius = pinRadius, center = center)
    drawCircle(color = DarkContrast, radius = pinRadius * 0.45f, center = center)
}
