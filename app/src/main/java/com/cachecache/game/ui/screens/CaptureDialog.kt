package com.cachecache.game.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.cachecache.game.model.HiddenPlayer
import com.cachecache.game.ui.components.GhostComposable
import com.cachecache.game.ui.theme.*
import com.cachecache.game.viewmodel.GameViewModel
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CaptureSuccessDialog(
    player: HiddenPlayer,
    seekerRemainingSeconds: Int,
    onDismiss: () -> Unit
) {
    var animStarted by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { animStarted = true }

    // Bouncy scale animation (spring damping ratio medium bouncy)
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
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Titre festif
                Text(
                    text = "Tu as trouvé un caché !",
                    color = TextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Grand Fantôme Bleu Apeuré au centre avec onde de particules dorées
                Box(
                    modifier = Modifier.size(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawRadiatingGoldenParticles()
                    }

                    GhostComposable(
                        size = 88.dp,
                        baseColor = ScaredBlue,
                        isScared = true
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Score géant rebondissant : "+ 50 points"
                Text(
                    text = "+ $scoreAnim points",
                    color = PacmanYellow,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Fiche récapitulative élégante (fond plus sombre)
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
                        // 1. Icône horloge + "Temps restant (chercheur) : 01:20"
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = "Chrono",
                                tint = PacmanYellow,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Temps restant (chercheur) : ${GameViewModel.formatSeconds(seekerRemainingSeconds)}",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // 2. Icône fantôme + "Caché trouvé : #2"
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                GhostComposable(size = 18.dp, baseColor = player.color, isScared = false)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Caché trouvé : #${player.id}",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // 3. Icône épingle de carte + "Position : Parc des Lilas" (ou campus)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Position",
                                tint = ScaredBlue,
                                modifier = Modifier.size(20.dp)
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

                Spacer(modifier = Modifier.height(24.dp))

                // Large bouton pilule jaune "Continuer"
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(30.dp),
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

private fun DrawScope.drawRadiatingGoldenParticles() {
    val center = Offset(size.width / 2f, size.height / 2f)
    val particleCount = 18
    val innerR = size.width * 0.35f
    val outerR = size.width * 0.48f

    val colors = listOf(PacmanYellow, GoldTrophy, ScaredBlue, GhostOrange, TextPrimary)

    for (i in 0 until particleCount) {
        val angle = Math.toRadians((i * (360.0 / particleCount) + 10).toDouble())
        val x1 = (center.x + innerR * cos(angle)).toFloat()
        val y1 = (center.y + innerR * sin(angle)).toFloat()
        val x2 = (center.x + outerR * cos(angle)).toFloat()
        val y2 = (center.y + outerR * sin(angle)).toFloat()

        val color = colors[i % colors.size]
        drawLine(
            color = color,
            start = Offset(x1, y1),
            end = Offset(x2, y2),
            strokeWidth = 3.dp.toPx(),
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )

        // Floating sparkle dots
        val dotR = outerR + 6.dp.toPx()
        val dotX = (center.x + dotR * cos(angle)).toFloat()
        val dotY = (center.y + dotR * sin(angle)).toFloat()
        drawCircle(color = color, radius = 2.5.dp.toPx(), center = Offset(dotX, dotY))
    }
}
