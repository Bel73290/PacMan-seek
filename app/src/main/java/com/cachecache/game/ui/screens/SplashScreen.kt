package com.cachecache.game.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.cachecache.game.model.Screen
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
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepNavy)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header with top right Settings Gear
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
                        contentDescription = "Réglages timers",
                        tint = PacmanYellow
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 3D Stylized "Cache Cache" Title
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(contentAlignment = Alignment.Center) {
                    // Shadow layer
                    Text(
                        text = "Cache\nCache",
                        fontSize = 52.sp,
                        lineHeight = 52.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.SansSerif,
                        color = Color(0xFF1E355B),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.offset(x = 3.dp, y = 4.dp)
                    )
                    // Foreground Yellow layer
                    Text(
                        text = "Cache\nCache",
                        fontSize = 52.sp,
                        lineHeight = 52.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.SansSerif,
                        color = PacmanYellow,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Explore la carte, trouve les cachés !",
                    color = TextSecondary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Central Perspective Map Illustration with Pacman and Ghost
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .height(280.dp)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = NavyIntermediate)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Perspective City & Roads Canvas
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawPerspectiveCityMap()
                    }

                    // Ghost and Pacman on illustration
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 32.dp, start = 24.dp, end = 24.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Scared Blue Ghost
                        GhostComposable(
                            size = 54.dp,
                            baseColor = ScaredBlue,
                            isScared = true
                        )

                        // Pac-dots in between
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            repeat(3) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(PacmanYellow)
                                )
                            }
                        }

                        // Pacman eating dots
                        PacmanComposable(
                            size = 64.dp,
                            color = PacmanYellow,
                            isAnimated = true,
                            rotationAngle = 0f
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons: "Jouer" and "Classement"
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Button(
                    onClick = onStartGame,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .shadow(12.dp, RoundedCornerShape(29.dp), spotColor = PacmanYellow),
                    shape = RoundedCornerShape(29.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PacmanYellow,
                        contentColor = DarkContrast
                    )
                ) {
                    Text(
                        text = "Jouer",
                        fontSize = 18.sp,
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

        // Settings Dialog for Timers
        if (state.showSettingsDialog) {
            SettingsDialog(
                currentSettings = state.settings,
                onDismiss = onCloseSettings,
                onSave = onSaveSettings
            )
        }
    }
}

private fun DrawScope.drawPerspectiveCityMap() {
    val w = size.width
    val h = size.height

    // Background Gradient (Dark Sky to River/City Grid)
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF0D1B30), Color(0xFF132847)),
            startY = 0f,
            endY = h
        )
    )

    // Mountain / Hills silhouettes
    val hillsPath = Path().apply {
        moveTo(0f, h * 0.4f)
        cubicTo(w * 0.25f, h * 0.32f, w * 0.45f, h * 0.48f, w * 0.7f, h * 0.35f)
        cubicTo(w * 0.85f, h * 0.28f, w * 0.95f, h * 0.38f, w, h * 0.36f)
        lineTo(w, h)
        lineTo(0f, h)
        close()
    }
    drawPath(path = hillsPath, color = Color(0xFF183359))

    // Stylized perspective roads
    val roadPath = Path().apply {
        moveTo(w * 0.45f, h * 0.4f)
        lineTo(w * 0.55f, h * 0.4f)
        lineTo(w * 0.85f, h)
        lineTo(w * 0.15f, h)
        close()
    }
    drawPath(path = roadPath, color = Color(0xFF0B1728))

    // Road dashed line
    val roadDashes = Path().apply {
        moveTo(w * 0.5f, h * 0.42f)
        lineTo(w * 0.5f, h * 0.95f)
    }
    drawPath(
        path = roadDashes,
        color = BorderSubtle,
        style = Stroke(width = 3.dp.toPx(), pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f))
    )

    // Map Location Pins in landscape
    drawCircle(color = GhostOrange, radius = 6.dp.toPx(), center = Offset(w * 0.24f, h * 0.48f))
    drawCircle(color = GhostCyan, radius = 7.dp.toPx(), center = Offset(w * 0.76f, h * 0.44f))
}
