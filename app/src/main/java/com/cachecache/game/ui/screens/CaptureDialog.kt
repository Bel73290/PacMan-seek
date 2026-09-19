package com.cachecache.game.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.cachecache.game.R
import com.cachecache.game.model.HiddenPlayer
import com.cachecache.game.ui.components.GhostComposable
import com.cachecache.game.ui.theme.*
import com.cachecache.game.viewmodel.GameViewModel

@Composable
fun CaptureSuccessDialog(
    player: HiddenPlayer,
    seekerRemainingSeconds: Int,
    onDismiss: () -> Unit
) {
    var animStarted by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { animStarted = true }

    // Bouncy scale animation
    val scale by animateFloatAsState(
        targetValue = if (animStarted) 1f else 0.4f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "dialogScale"
    )

    // Animated score count
    val scoreAnim by animateIntAsState(
        targetValue = if (animStarted) 50 else 0,
        animationSpec = tween(700, easing = FastOutSlowInEasing),
        label = "scoreCount"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .scale(scale)
                .border(1.5.dp, BorderSubtle, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = NavyIntermediate)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Titre festif
                Text(
                    text = "Tu as trouvé un caché !",
                    color = TextPrimary,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Visuel officiel de célébration : Grand Pacman sautant dans les confettis
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.capture_celebration),
                        contentDescription = "Célébration capture",
                        modifier = Modifier
                            .fillMaxHeight()
                            .wrapContentWidth(),
                        contentScale = ContentScale.Fit
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Score géant : "+ 50 points"
                Text(
                    text = "+ $scoreAnim points",
                    color = PacmanYellow,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Fiche récapitulative élégante (Écran 5)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = CardSurface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // 1. Icône chrono + "Temps restant (chercheur) : 01:42"
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = "Chrono",
                                tint = PacmanYellow,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Temps restant : ${GameViewModel.formatSeconds(seekerRemainingSeconds)}",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // 2. Icône fantôme animé + "Caché trouvé : #3"
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            GhostComposable(
                                size = 22.dp,
                                baseColor = player.color,
                                isScared = false
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Caché trouvé : #${player.id}",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // 3. Icône épingle de localisation officielle + "Position : ..."
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = R.drawable.map_pin_red),
                                contentDescription = "Position",
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Position : ${player.locationName}",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Bouton pilule jaune "Continuer"
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PacmanYellow,
                        contentColor = DarkContrast
                    )
                ) {
                    Text(
                        text = "Continuer",
                        fontWeight = FontWeight.Black,
                        fontSize = 17.sp
                    )
                }
            }
        }
    }
}
