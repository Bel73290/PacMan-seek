package com.cachecache.game.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.cachecache.game.R
import com.cachecache.game.model.GameState
import com.cachecache.game.model.formatCompactScore
import com.cachecache.game.ui.theme.*
import com.cachecache.game.viewmodel.GameViewModel

/**
 * Composant Pac-Man qui danse pour célébrer la VICTOIRE :
 * - Saut vertical rythmé (jump)
 * - Déformation squash & stretch élastique
 * - Balancement / dandinement de danse (gauche / droite)
 * - Ombre dynamique au sol synchronisée
 * - Particules festives et notes de musique
 */
@Composable
fun DancingPacman(
    modifier: Modifier = Modifier,
    size: Dp = 96.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "dancingPacmanAnim")

    // 1. Saut vertical
    val jumpY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -26f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 360, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pacJumpY"
    )

    // 2. Déformation élastique Squash & Stretch
    val squashX by infiniteTransition.animateFloat(
        initialValue = 1.15f,
        targetValue = 0.90f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 360, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pacSquashX"
    )
    val squashY by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.14f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 360, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pacSquashY"
    )

    // 3. Dandinement de danse (rotation gauche / droite)
    val swayAngle by infiniteTransition.animateFloat(
        initialValue = -18f,
        targetValue = 18f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 360, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pacSwayAngle"
    )

    // 4. Pulse des étincelles festives
    val starPulse by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 450, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "starPulse"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp),
        contentAlignment = Alignment.Center
    ) {
        // Ombre au sol dynamique
        val shadowFactor = (1f - (jumpY / -26f) * 0.45f).coerceIn(0.5f, 1.2f)
        val shadowAlpha = (0.5f - (jumpY / -26f) * 0.3f).coerceIn(0.15f, 0.5f)
        Canvas(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = (-10).dp)
                .size(width = 80.dp * shadowFactor, height = 14.dp * shadowFactor)
        ) {
            drawOval(
                color = Color.Black.copy(alpha = shadowAlpha),
                topLeft = Offset.Zero,
                size = this.size
            )
        }

        // Particules festives animées (notes et étoiles)
        Text(
            text = "🎵",
            fontSize = (18 * starPulse).sp,
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 24.dp, y = (14 + jumpY * 0.3f).dp)
        )
        Text(
            text = "✨",
            fontSize = (22 * starPulse).sp,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-24).dp, y = (10 - jumpY * 0.3f).dp)
        )
        Text(
            text = "🎶",
            fontSize = (18 * starPulse).sp,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = 14.dp, y = (16 - jumpY * 0.4f).dp)
        )
        Text(
            text = "★",
            fontSize = (20 * starPulse).sp,
            color = PacmanYellow,
            fontWeight = FontWeight.Black,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(x = (-16).dp, y = (16 + jumpY * 0.4f).dp)
        )

        // Pac-Man dansant
        Box(
            modifier = Modifier
                .graphicsLayer {
                    translationY = jumpY.dp.toPx()
                    rotationZ = swayAngle
                    scaleX = squashX
                    scaleY = squashY
                },
            contentAlignment = Alignment.Center
        ) {
            PacmanComposable(
                size = size,
                color = PacmanYellow,
                isAnimated = true,
                rotationAngle = 0f
            )
        }
    }
}

/**
 * Troupe de fantômes qui dansent pour célébrer la DÉFAITE du chercheur :
 * - Sauts alternés en contrepoint (Blinky/Inky vs Pinky/Clyde)
 * - Balancement gauche/droite festif
 * - Animations des froufrous/pieds de fantômes
 * - Ombres dynamiques sous chaque fantôme
 */
@Composable
fun DancingGhostsTroupe(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "dancingGhostsAnim")

    // Sauts alternés en rythme
    val jumpA by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -22f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 340, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ghostJumpA"
    )
    val jumpB by infiniteTransition.animateFloat(
        initialValue = -22f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 340, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ghostJumpB"
    )

    // Balancement gauche/droite
    val tiltA by infiniteTransition.animateFloat(
        initialValue = -15f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 340, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ghostTiltA"
    )
    val tiltB by infiniteTransition.animateFloat(
        initialValue = 15f,
        targetValue = -15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 340, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ghostTiltB"
    )

    // Pulse festive
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val ghostData = listOf(
        Pair(GhostRed, false),
        Pair(GhostPink, true),
        Pair(GhostCyan, false),
        Pair(GhostOrange, true),
        Pair(GhostPurple, false)
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(155.dp),
        contentAlignment = Alignment.Center
    ) {
        // Émojis festifs des fantômes vainqueurs
        Text(
            text = "😈",
            fontSize = (18 * pulse).sp,
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 10.dp, y = (10 + jumpA * 0.3f).dp)
        )
        Text(
            text = "🔥",
            fontSize = (18 * pulse).sp,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-10).dp, y = (10 - jumpA * 0.3f).dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            ghostData.forEach { (color, isGroupB) ->
                val currentJump = if (isGroupB) jumpB else jumpA
                val currentTilt = if (isGroupB) tiltB else tiltA

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .graphicsLayer {
                                translationY = currentJump.dp.toPx()
                                rotationZ = currentTilt
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        GhostComposable(
                            size = 46.dp,
                            baseColor = color,
                            isScared = false
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Ombre au sol individuelle
                    val shadowFactor = (1f - (currentJump / -22f) * 0.4f).coerceIn(0.5f, 1.2f)
                    val shadowAlpha = (0.5f - (currentJump / -22f) * 0.3f).coerceIn(0.15f, 0.5f)
                    Canvas(
                        modifier = Modifier.size(width = 34.dp * shadowFactor, height = 7.dp * shadowFactor)
                    ) {
                        drawOval(
                            color = Color.Black.copy(alpha = shadowAlpha),
                            topLeft = Offset.Zero,
                            size = this.size
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VictoryDialog(
    state: GameState,
    onReplay: () -> Unit,
    onBackHome: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "victoryGlow")
    val titleScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "titleScale"
    )

    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .shadow(28.dp, RoundedCornerShape(32.dp), spotColor = PacmanYellow)
                .border(2.5.dp, PacmanYellow, RoundedCornerShape(32.dp)),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = CardSurface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Badge Haut
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = PacmanYellow.copy(alpha = 0.18f),
                    border = androidx.compose.foundation.BorderStroke(1.2.dp, PacmanYellow)
                ) {
                    Text(
                        text = "★ TOUS LES CACHÉS CAPTURÉS ★",
                        color = PacmanYellow,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // GROSSE LETTRE JAUNE VICTOIRE
                Text(
                    text = "VICTOIRE",
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Black,
                    color = PacmanYellow,
                    letterSpacing = 4.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.graphicsLayer {
                        scaleX = titleScale
                        scaleY = titleScale
                    }
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Bravo ! Tous les fantômes ont été trouvés à temps !",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Scène : Pac-Man qui danse !
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.2.dp, BorderSubtle, RoundedCornerShape(22.dp)),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = NavyIntermediate)
                ) {
                    DancingPacman(
                        size = 92.dp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Ligne de statistiques
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = NavyIntermediate),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "Temps restant", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = GameViewModel.formatSeconds(state.searchRemainingSeconds),
                                color = PacmanYellow,
                                fontWeight = FontWeight.Black,
                                fontSize = 17.sp
                            )
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = NavyIntermediate),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "Points gagnés", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(2.dp))
                            val victoryPoints = state.hiddenPlayers.size.coerceAtLeast(1) * 50
                            Text(
                                text = "+${formatCompactScore(victoryPoints)} pts",
                                color = PacmanYellow,
                                fontWeight = FontWeight.Black,
                                fontSize = 17.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Boutons d'action
                Button(
                    onClick = onReplay,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .shadow(12.dp, RoundedCornerShape(27.dp), spotColor = PacmanYellow),
                    shape = RoundedCornerShape(27.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PacmanYellow,
                        contentColor = DarkContrast
                    )
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Rejouer une partie", fontWeight = FontWeight.Black, fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onBackHome,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .border(1.2.dp, BorderSubtle, RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NavyIntermediate,
                        contentColor = TextPrimary
                    )
                ) {
                    Icon(imageVector = Icons.Default.Home, contentDescription = null, modifier = Modifier.size(18.dp), tint = TextSecondary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Menu principal", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun DefeatDialog(
    state: GameState,
    onRetry: () -> Unit,
    onBackHome: () -> Unit
) {
    val ghostsFound = state.hiddenPlayers.count { it.isFound }
    val totalGhosts = state.hiddenPlayers.size

    val infiniteTransition = rememberInfiniteTransition(label = "defeatGlow")
    val titleScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "defeatTitleScale"
    )

    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .shadow(28.dp, RoundedCornerShape(32.dp), spotColor = GhostRed)
                .border(2.5.dp, GhostRed, RoundedCornerShape(32.dp)),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = CardSurface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Badge Haut
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = GhostRed.copy(alpha = 0.18f),
                    border = androidx.compose.foundation.BorderStroke(1.2.dp, GhostRed)
                ) {
                    Text(
                        text = "TEMPS ÉCOULÉ",
                        color = GhostRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // GROSSE LETTRE ROUGE DEFAITE
                Text(
                    text = "DÉFAITE",
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Black,
                    color = GhostRed,
                    letterSpacing = 4.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.graphicsLayer {
                        scaleX = titleScale
                        scaleY = titleScale
                    }
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Le temps est écoulé ! Les fantômes ont gagné...",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Scène : Les fantômes qui dansent !
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.2.dp, BorderSubtle, RoundedCornerShape(22.dp)),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = NavyIntermediate)
                ) {
                    DancingGhostsTroupe(
                        modifier = Modifier.padding(vertical = 10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Ligne de statistiques
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = NavyIntermediate),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "Fantômes trouvés", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "$ghostsFound / $totalGhosts",
                                color = if (ghostsFound > 0) PacmanYellow else TextPrimary,
                                fontWeight = FontWeight.Black,
                                fontSize = 17.sp
                            )
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = NavyIntermediate),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "Points gagnés", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(2.dp))
                            val earnedPoints = ghostsFound * 50
                            Text(
                                text = if (earnedPoints > 0) "+${formatCompactScore(earnedPoints)} pts" else "0 pt",
                                color = if (earnedPoints > 0) PacmanYellow else TextSecondary,
                                fontWeight = FontWeight.Black,
                                fontSize = 17.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Boutons d'action
                Button(
                    onClick = onRetry,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .shadow(12.dp, RoundedCornerShape(27.dp), spotColor = GhostRed),
                    shape = RoundedCornerShape(27.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GhostRed,
                        contentColor = TextPrimary
                    )
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Réessayer la partie", fontWeight = FontWeight.Black, fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onBackHome,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .border(1.2.dp, BorderSubtle, RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NavyIntermediate,
                        contentColor = TextPrimary
                    )
                ) {
                    Icon(imageVector = Icons.Default.Home, contentDescription = null, modifier = Modifier.size(18.dp), tint = TextSecondary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Menu principal", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}
