package com.cachecache.game.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cachecache.game.R
import com.cachecache.game.model.GameState
import com.cachecache.game.ui.theme.*

@Composable
fun SplashScreen(
    state: GameState,
    onStartGame: () -> Unit,
    onOpenMenu: () -> Unit,
    onOpenGuide: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onCloseSettings: () -> Unit = {},
    onSaveSettings: (Int, Int) -> Unit = { _, _ -> }
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepNavy)
    ) {
        // 1. Official High-Resolution Illustration without buttons
        Image(
            painter = painterResource(id = R.drawable.splash_bg),
            contentDescription = "Pac & Seek",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // 2. Foreground UI Overlay (Buttons at bottom)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom
        ) {
            // Action Buttons matching the official mockup
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Button "Jouer" (Pill Yellow)
                Button(
                    onClick = onStartGame,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .shadow(16.dp, RoundedCornerShape(32.dp), spotColor = PacmanYellow),
                    shape = RoundedCornerShape(32.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PacmanYellow,
                        contentColor = DarkContrast
                    )
                ) {
                    Text(
                        text = "Jouer",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                // Button "Menu"
                Button(
                    onClick = onOpenMenu,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .border(1.5.dp, BorderSubtle, RoundedCornerShape(29.dp)),
                    shape = RoundedCornerShape(29.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0D1C38).copy(alpha = 0.95f),
                        contentColor = TextPrimary
                    )
                ) {
                    Text(
                        text = "Menu",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            }
        }
    }
}
