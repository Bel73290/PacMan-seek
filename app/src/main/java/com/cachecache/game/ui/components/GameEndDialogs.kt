package com.cachecache.game.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.cachecache.game.R
import com.cachecache.game.model.GameState
import com.cachecache.game.ui.theme.*
import com.cachecache.game.viewmodel.GameViewModel
import java.text.NumberFormat
import java.util.Locale

@Composable
fun VictoryDialog(
    state: GameState,
    onReplay: () -> Unit,
    onBackHome: () -> Unit
) {
    val numberFormat = NumberFormat.getNumberInstance(Locale.FRANCE)
    val infiniteTransition = rememberInfiniteTransition(label = "victoryFloat")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "eyeFloat"
    )

    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .shadow(24.dp, RoundedCornerShape(28.dp), spotColor = PacmanYellow)
                .border(2.dp, PacmanYellow, RoundedCornerShape(28.dp)),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = CardSurface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Glow Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = PacmanYellow.copy(alpha = 0.20f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PacmanYellow)
                ) {
                    Text(
                        text = "★ MISSION ACCOMPLIE ★",
                        color = PacmanYellow,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "VICTOIRE !",
                    style = MaterialTheme.typography.headlineMedium,
                    color = PacmanYellow,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Tous les fantômes ont été capturés avant la fin du temps !",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Visual Showcase: Pac-Man with ghost eyes in front of him
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = NavyIntermediate)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp, horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // Pac-Man Chomp Chomp
                        PacmanComposable(
                            size = 56.dp,
                            color = PacmanYellow,
                            isAnimated = true,
                            rotationAngle = 0f
                        )

                        Spacer(modifier = Modifier.width(14.dp))

                        // Fleeing Eyes in front of Pac-Man
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            repeat(state.hiddenPlayers.size.coerceAtMost(5)) { index ->
                                val stagger = if (index % 2 == 0) floatOffset else -floatOffset
                                Image(
                                    painter = painterResource(id = R.drawable.ghost_eyes_left),
                                    contentDescription = "Yeux fantôme",
                                    modifier = Modifier
                                        .size(28.dp)
                                        .offset(y = stagger.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Stats Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = NavyIntermediate)
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "Temps restant", color = TextSecondary, fontSize = 11.sp)
                            Text(
                                text = GameViewModel.formatSeconds(state.searchRemainingSeconds),
                                color = PacmanYellow,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            )
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = NavyIntermediate)
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "Score Total", color = TextSecondary, fontSize = 11.sp)
                            Text(
                                text = "${numberFormat.format(state.userScore)} pts",
                                color = PacmanYellow,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Button(
                    onClick = onReplay,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PacmanYellow,
                        contentColor = DarkContrast
                    )
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Rejouer une partie", fontWeight = FontWeight.Black, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = onBackHome,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Menu principal", color = TextSecondary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
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
    val numberFormat = NumberFormat.getNumberInstance(Locale.FRANCE)
    val ghostsFound = state.hiddenPlayers.count { it.isFound }
    val totalGhosts = state.hiddenPlayers.size

    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .shadow(24.dp, RoundedCornerShape(28.dp), spotColor = GhostRed)
                .border(2.dp, GhostRed, RoundedCornerShape(28.dp)),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = CardSurface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Defeat Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = GhostRed.copy(alpha = 0.20f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GhostRed)
                ) {
                    Text(
                        text = "TEMPS ÉCOULÉ",
                        color = GhostRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "DÉFAITE !",
                    style = MaterialTheme.typography.headlineMedium,
                    color = GhostRed,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Les 10 minutes sont écoulées ! Les fantômes vous ont encerclé...",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Visual Showcase: Ghosts surrounding Pac-Man in circle
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = NavyIntermediate)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Center: Pacman trapped
                        PacmanComposable(
                            size = 46.dp,
                            color = PacmanYellow,
                            isAnimated = false,
                            rotationAngle = 0f
                        )

                        // Surrounding Ghosts looking towards Pac-Man
                        val ghostColors = listOf(GhostRed, GhostPink, GhostCyan, GhostOrange, GhostPurple)
                        val radius = 55.dp

                        ghostColors.forEachIndexed { index, color ->
                            val angleRad = Math.toRadians((index * (360.0 / ghostColors.size)) - 90.0)
                            val xOffset = (radius.value * kotlin.math.cos(angleRad)).dp
                            val yOffset = (radius.value * kotlin.math.sin(angleRad)).dp

                            Box(
                                modifier = Modifier.offset(x = xOffset, y = yOffset)
                            ) {
                                GhostComposable(
                                    size = 34.dp,
                                    baseColor = color,
                                    isScared = false
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Stats Row
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = NavyIntermediate)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Fantômes trouvés", color = TextSecondary, fontSize = 11.sp)
                            Text(
                                text = "$ghostsFound / $totalGhosts",
                                color = if (ghostsFound > 0) PacmanYellow else TextPrimary,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Score final", color = TextSecondary, fontSize = 11.sp)
                            Text(
                                text = "${numberFormat.format(state.userScore)} pts",
                                color = PacmanYellow,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Button(
                    onClick = onRetry,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GhostRed,
                        contentColor = TextPrimary
                    )
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Réessayer la partie", fontWeight = FontWeight.Black, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = onBackHome,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Menu principal", color = TextSecondary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
            }
        }
    }
}
