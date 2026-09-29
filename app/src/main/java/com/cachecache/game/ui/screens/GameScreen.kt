package com.cachecache.game.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cachecache.game.R
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
    onTestInstantCapture: () -> Unit = {},
    onReplay: () -> Unit = {}
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
            isFacingRight = state.isFacingRight,
            isFrozen = state.gamePhase == GamePhase.FREEZE,
            capturingGhost = state.capturingGhost,
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
            val isFreeze = state.gamePhase == GamePhase.FREEZE
            val timerColor = if (isFreeze) GhostCyan else PacmanYellow
            val timerText = if (isFreeze) "${state.freezeRemainingSeconds}s" else GameViewModel.formatSeconds(state.searchRemainingSeconds)

            // Top Header Bar: Back Button, Timer (replaces "Partie en cours"), and Ghosts Counter
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
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

                // In place of "Partie en cours": Live Game Timer
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = NavyIntermediate.copy(alpha = 0.95f),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, timerColor),
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isFreeze) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Immobilisé",
                                tint = GhostCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ATTENTE : $timerText",
                                color = GhostCyan,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                        } else {
                            PacmanComposable(
                                size = 22.dp,
                                color = PacmanYellow,
                                isAnimated = true,
                                rotationAngle = 0f
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = timerText,
                                color = PacmanYellow,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.5.sp
                            )
                        }
                    }
                }

                // Ghosts captured counter badge
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = NavyIntermediate.copy(alpha = 0.95f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier.height(42.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        GhostComposable(size = 18.dp, baseColor = GhostCyan, isScared = false)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${state.hiddenPlayers.count { it.isFound }}/${state.hiddenPlayers.size}",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

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
                        // Scared Blue Ghost with horizontal trembling in Capsule
                        GhostComposable(
                            size = 34.dp,
                            isScared = true
                        )

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
                    .padding(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                // Left: Contact / Proximity indicator
                nearestPlayer?.let { nearest ->
                    val ghostColor = nearest.actualColor
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (nearest.distanceMeters <= 25.0) ghostColor.copy(alpha = 0.85f) else NavyIntermediate.copy(alpha = 0.9f)
                        ),
                        modifier = Modifier
                            .border(1.dp, if (nearest.distanceMeters <= 25.0) ghostColor else BorderSubtle, RoundedCornerShape(16.dp))
                            .clickable { onTestInstantCapture() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            GhostComposable(
                                size = 22.dp,
                                baseColor = ghostColor,
                                isScared = nearest.isScared
                            )
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

                // Right: Mini Street View Inset Card (Écran 3)
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
                }
            }
        }

        // Arcade Ghost Absorption Animation (Blue Lines Dissolve + Fleeing Eyes + "+ 50 points")
        GhostCaptureOverlay(capturingGhost = state.capturingGhost)

        // Victory Dialog (All ghosts captured)
        if (state.gamePhase == GamePhase.VICTORY) {
            VictoryDialog(
                state = state,
                onReplay = onReplay,
                onBackHome = onBackToHome
            )
        }

        // Defeat Dialog (10 minutes expired with ghosts remaining)
        if (state.gamePhase == GamePhase.DEFEAT) {
            DefeatDialog(
                state = state,
                onRetry = onReplay,
                onBackHome = onBackToHome
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
