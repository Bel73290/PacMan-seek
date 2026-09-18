package com.cachecache.game.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
    // Spring bounce animation on appearance
    var animStarted by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { animStarted = true }

    val scale by animateFloatAsState(
        targetValue = if (animStarted) 1f else 0.4f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "dialogScale"
    )

    // Animated score increment
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
                .border(1.dp, BorderSubtle, RoundedCornerShape(28.dp)),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = NavyIntermediate)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Festive Header Title
                Text(
                    text = "Tu as trouvé un caché !",
                    color = TextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Ghost with radiating festive sparks
                Box(
                    modifier = Modifier.size(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawRadiatingSparks()
                    }

                    // Large Animated Scared Ghost
                    GhostComposable(
                        size = 86.dp,
                        baseColor = player.color,
                        isScared = true
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Big Score Label: "+ 50 points"
                Text(
                    text = "+ $scoreAnim points",
                    color = PacmanYellow,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(22.dp))

                // Summary Card with 3 info rows
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
                        // Row 1: Remaining Time
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

                        // Row 2: Hidden Player
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                GhostComposable(size = 18.dp, baseColor = player.color, isScared = false)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Caché trouvé : #${player.id} (${player.name})",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // Row 3: Campus Position
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Localisation",
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

                // Yellow Continue Button
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
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawRadiatingSparks() {
    val center = Offset(size.width / 2f, size.height / 2f)
    val sparkCount = 14
    val innerR = size.width * 0.38f
    val outerR = size.width * 0.48f

    val sparkColors = listOf(GhostRed, GhostPink, GhostOrange, GhostCyan, PacmanYellow, ScaredBlue)

    for (i in 0 until sparkCount) {
        val angle = Math.toRadians((i * (360.0 / sparkCount)).toDouble())
        val x1 = (center.x + innerR * cos(angle)).toFloat()
        val y1 = (center.y + innerR * sin(angle)).toFloat()
        val x2 = (center.x + outerR * cos(angle)).toFloat()
        val y2 = (center.y + outerR * sin(angle)).toFloat()

        val color = sparkColors[i % sparkColors.size]
        drawLine(
            color = color,
            start = Offset(x1, y1),
            end = Offset(x2, y2),
            strokeWidth = 3.dp.toPx(),
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )
    }
}
