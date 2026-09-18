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
    onOpenStreetView: () -> Unit,
    onGoToSuspectZone: () -> Unit,
    onDismissSuspectZoneModal: () -> Unit,
    onDismissProximityAlert: () -> Unit,
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

    // Is within 30m of a hidden ghost?
    val isNearGhost = nearestPlayer != null && nearestPlayer.distanceMeters <= 30.0

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepNavy)
    ) {
        // 1. Fullscreen Natural OpenStreetMap with Camera Centered on Pacman
        OsmMapView(
            seekerLocation = state.seekerLocation,
            seekerHeading = state.seekerHeading,
            hiddenPlayers = state.hiddenPlayers,
            suspectZone = state.suspectZone,
            pacDotsRoute = if (state.suspectZone?.isTargeted == true || state.suspectZone?.isModalVisible == true) state.pacDotsRoute else emptyList(),
            isRadarScanning = isNearGhost,
            onMapTap = onMapTap,
            onPlayerTap = onPlayerTap
        )

        // 2. Top Header & Dual Countdown Cards
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 6.dp)
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
                        .background(NavyIntermediate.copy(alpha = 0.95f))
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

                Spacer(modifier = Modifier.size(42.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Dual Horizontal Timer Counters (Écran 2)
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

            Spacer(modifier = Modifier.height(10.dp))

            // Écran 3 : Bannière Flottante "Proche d'un fantôme !" (moins de 30 mètres)
            AnimatedVisibility(
                visible = isNearGhost && state.isGhostProximityAlertVisible,
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.2.dp, BorderSubtle, RoundedCornerShape(18.dp)),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = NavyIntermediate.copy(alpha = 0.95f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Blue Ghost Icon in Capsule
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(DarkContrast),
                            contentAlignment = Alignment.Center
                        ) {
                            GhostComposable(size = 22.dp, baseColor = ScaredBlue, isScared = true)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Proche d'un fantôme !",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Il est à moins de 30 mètres...",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }

                        IconButton(
                            onClick = onDismissProximityAlert,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Fermer",
                                tint = TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Écran 7 : Bannière "Zone suspecte"
            AnimatedVisibility(
                visible = !isNearGhost && state.suspectZone?.isModalVisible == true,
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
                                Text(
                                    text = zone.title,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )

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

                            Spacer(modifier = Modifier.height(4.dp))

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

        // 3. Floating Bottom Controls: Mini Street View Inset Card & Recenter GPS
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.End
        ) {
            // Row with Proximity badge + Mini Street View Inset / Recenter button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                // Left: Contact / Proximity indicator
                nearestPlayer?.let { nearest ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (nearest.distanceMeters <= 25.0) GhostRed.copy(alpha = 0.9f) else NavyIntermediate.copy(alpha = 0.9f)
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
                                GhostComposable(size = 16.dp, baseColor = nearest.color, isScared = nearest.distanceMeters <= 25.0)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${nearest.name} : ${"%.1f".format(nearest.distanceMeters)}m",
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } ?: Spacer(modifier = Modifier.weight(1f))

                // Right: Mini Street View Inset Card (Écran 3) OR Recenter GPS
                if (isNearGhost && state.isStreetViewMiniVisible) {
                    // Floating Mini Street View Card
                    StreetViewComposable(
                        modifier = Modifier
                            .size(width = 175.dp, height = 135.dp)
                            .shadow(12.dp, RoundedCornerShape(16.dp)),
                        isMini = true,
                        timerText = "02:18",
                        onExpand = onOpenStreetView
                    )
                } else {
                    // Recenter GPS Button
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
                            contentDescription = "Recentrer",
                            tint = PacmanYellow,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Fixed Bottom Navigation Bar
            BottomNavigationBar(
                currentScreen = Screen.GAME,
                onNavigate = onNavigate
            )
        }

        // Screen 5: Capture Dialog Overlay (triggered at <= 0.30m)
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
        modifier = modifier.border(1.dp, BorderSubtle, RoundedCornerShape(18.dp)),
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
