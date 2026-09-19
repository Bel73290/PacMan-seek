package com.cachecache.game.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cachecache.game.model.GameState
import com.cachecache.game.ui.components.StreetViewComposable
import com.cachecache.game.ui.theme.*
import com.cachecache.game.viewmodel.GameViewModel

@Composable
fun StreetViewScreen(
    state: GameState,
    onBack: () -> Unit,
    onCaptureTrigger: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepNavy)
    ) {
        // 1. Fullscreen Perspective Street Scene
        StreetViewComposable(
            modifier = Modifier.fillMaxSize(),
            isMini = false,
            timerText = GameViewModel.formatSeconds(state.searchRemainingSeconds),
            onExpand = {}
        )

        // 2. Top Header HUD
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back button
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(DarkContrast.copy(alpha = 0.85f))
                    .border(1.dp, BorderSubtle, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Retour",
                    tint = TextPrimary
                )
            }

            // Center Stopwatch Pill Badge: "02:18"
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(DarkContrast.copy(alpha = 0.9f))
                    .border(2.dp, PacmanYellow, RoundedCornerShape(24.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = "Timer",
                    tint = PacmanYellow,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = GameViewModel.formatSeconds(state.searchRemainingSeconds),
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black
                )
            }

            // Fullscreen toggle button
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(DarkContrast.copy(alpha = 0.85f))
                    .border(1.dp, BorderSubtle, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.FullscreenExit,
                    contentDescription = "Quitter plein écran",
                    tint = TextPrimary
                )
            }
        }

        // 3. Bottom HUD Elements
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(16.dp)
        ) {
            // Watermark: OpenStreetView
            Row(
                modifier = Modifier.align(Alignment.BottomStart),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Street View",
                    tint = TextPrimary.copy(alpha = 0.85f),
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "OpenStreetView",
                    color = TextPrimary.copy(alpha = 0.9f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Bottom Right: Mini Radar Map Circle Overlay (Picture-in-Picture)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(DarkContrast.copy(alpha = 0.92f))
                    .border(2.dp, Color.White.copy(alpha = 0.8f), CircleShape)
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                // Mini Radar Canvas
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val center = Offset(w / 2f, h / 2f)

                    // Soft map grid circles
                    drawCircle(color = ScaredBlue.copy(alpha = 0.2f), radius = w * 0.42f, center = center)
                    drawCircle(color = ScaredBlue.copy(alpha = 0.4f), radius = w * 0.25f, center = center)

                    // Field of view radar cone
                    val conePath = Path().apply {
                        moveTo(center.x, center.y)
                        lineTo(center.x - w * 0.35f, center.y - h * 0.42f)
                        lineTo(center.x + w * 0.35f, center.y - h * 0.42f)
                        close()
                    }
                    drawPath(path = conePath, color = PacmanYellow.copy(alpha = 0.25f))

                    // Player center dot
                    drawCircle(color = PacmanYellow, radius = 5.dp.toPx(), center = center)

                    // Target ghost pin in distance
                    drawCircle(color = ScaredBlue, radius = 4.dp.toPx(), center = Offset(center.x + 12.dp.toPx(), center.y - 18.dp.toPx()))
                }

                // Mini Close X button on the radar
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(DarkContrast)
                        .border(1.dp, BorderSubtle, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Fermer mini map",
                        tint = TextPrimary,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}
