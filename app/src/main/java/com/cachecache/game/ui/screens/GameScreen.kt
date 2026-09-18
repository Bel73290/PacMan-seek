package com.cachecache.game.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cachecache.game.model.*
import com.cachecache.game.ui.components.*
import com.cachecache.game.ui.theme.*
import com.cachecache.game.viewmodel.GameViewModel

@Composable
fun GameScreen(
    state: GameState,
    onBackToHome: () -> Unit,
    onNavigate: (Screen) -> Unit,
    onGoToSuspectZone: () -> Unit,
    onDismissSuspectZoneModal: () -> Unit,
    onMapTap: (GeoPoint) -> Unit,
    onPlayerTap: (HiddenPlayer) -> Unit,
    onRecenter: () -> Unit,
    onDismissCaptureDialog: () -> Unit,
    onTestInstantCapture: () -> Unit
) {
    val nearestPlayer = remember(state.hiddenPlayers, state.seekerLocation) {
        state.hiddenPlayers
            .filter { !it.isFound }
            .minByOrNull { it.distanceMeters }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepNavy)
    ) {
        // 1. Fullscreen Interactive OSM Map
        OsmMapView(
            seekerLocation = state.seekerLocation,
            seekerHeading = state.seekerHeading,
            hiddenPlayers = state.hiddenPlayers,
            suspectZone = state.suspectZone,
            pacDotsRoute = if (state.suspectZone?.isTargeted == true || state.suspectZone?.isModalVisible == true) state.pacDotsRoute else emptyList(),
            isRadarScanning = false,
            onMapTap = onMapTap,
            onPlayerTap = onPlayerTap
        )

        // 2. Top Header & Dual Countdown Cards
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackToHome,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(NavyIntermediate.copy(alpha = 0.9f))
                        .border(1.dp, BorderSubtle, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Retour",
                        tint = TextPrimary
                    )
                }

                Text(
                    text = "Partie en cours",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )

                // Spacer to balance the back button
                Spacer(modifier = Modifier.size(42.dp))
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Dual Horizontal Timer Counters
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Seeker Timer (Left)
                TimerCard(
                    modifier = Modifier.weight(1f),
                    title = "Chercheur",
                    timeText = GameViewModel.formatSeconds(state.seekerRemainingSeconds),
                    subText = "(${state.settings.seekerDurationMinutes} min)",
                    iconComposable = {
                        PacmanComposable(size = 24.dp, color = PacmanYellow, isAnimated = false)
                    }
                )

                // Hiding Timer (Right)
                TimerCard(
                    modifier = Modifier.weight(1f),
                    title = "Cachés",
                    timeText = GameViewModel.formatSeconds(state.hidingRemainingSeconds),
                    subText = "(${state.settings.hidingDurationMinutes} min)",
                    iconComposable = {
                        GhostComposable(size = 24.dp, baseColor = GhostPurple, isScared = false)
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Screen 3: Suspect Zone Modal (if visible)
            AnimatedVisibility(
                visible = state.suspectZone?.isModalVisible == true,
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut()
            ) {
                state.suspectZone?.let { zone ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp)),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = NavyIntermediate.copy(alpha = 0.95f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = "Zone suspecte",
                                        tint = ScaredBlue,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = zone.title,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                }

                                IconButton(
                                    onClick = onDismissSuspectZoneModal,
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Fermer",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = zone.description,
                                color = TextSecondary,
                                fontSize = 13.sp
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = onGoToSuspectZone,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp),
                                shape = RoundedCornerShape(22.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ScaredBlue,
                                    contentColor = TextPrimary
                                )
                            ) {
                                Text(
                                    text = "Aller dans cette zone",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Bottom Overlay Controls & Floating Guidance Banner
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.End
        ) {
            // Interactive Proximity & Recenter Buttons Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Real-time Proximity Badge (<25m panic, <=0.30m capture)
                nearestPlayer?.let { nearest ->
                    val isPanic = nearest.distanceMeters <= 25.0
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isPanic) GhostRed.copy(alpha = 0.85f) else NavyIntermediate.copy(alpha = 0.9f)
                        ),
                        modifier = Modifier
                            .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
                            .clickable { onTestInstantCapture() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.size(16.dp), contentAlignment = Alignment.Center) {
                                GhostComposable(size = 16.dp, baseColor = nearest.color, isScared = isPanic)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${nearest.name} : ${"%.1f".format(nearest.distanceMeters)}m",
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (nearest.distanceMeters <= 0.30) "(Touche !)" else if (isPanic) "(Panique !)" else "",
                                color = PacmanYellow,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } ?: Spacer(modifier = Modifier.weight(1f))

                // Floating GPS Recenter Button
                IconButton(
                    onClick = onRecenter,
                    modifier = Modifier
                        .size(50.dp)
                        .shadow(8.dp, CircleShape)
                        .clip(CircleShape)
                        .background(NavyIntermediate)
                        .border(1.dp, BorderSubtle, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "Recentrer campus",
                        tint = PacmanYellow,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Bottom Floating Guidance Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = NavyIntermediate.copy(alpha = 0.95f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PacmanComposable(size = 32.dp, color = PacmanYellow, isAnimated = true)
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(
                        text = "Explore les zones indiquées pour trouver les cachés !",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 18.sp
                    )
                }
            }

            // Fixed Bottom Navigation Bar
            BottomNavigationBar(
                currentScreen = Screen.GAME,
                onNavigate = onNavigate
            )
        }

        // Screen 4: Capture Dialog Overlay (triggers at <= 0.30m)
        state.capturedDialogPlayer?.let { capturedPlayer ->
            CaptureSuccessDialog(
                player = capturedPlayer,
                seekerRemainingSeconds = state.seekerRemainingSeconds,
                onDismiss = onDismissCaptureDialog
            )
        }
    }
}

@Composable
private fun TimerCard(
    modifier: Modifier = Modifier,
    title: String,
    timeText: String,
    subText: String,
    iconComposable: @Composable () -> Unit
) {
    Card(
        modifier = modifier
            .border(1.dp, BorderSubtle, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = NavyIntermediate.copy(alpha = 0.95f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            iconComposable()

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(
                    text = title,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = timeText,
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = subText,
                        color = TextSecondary,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}
