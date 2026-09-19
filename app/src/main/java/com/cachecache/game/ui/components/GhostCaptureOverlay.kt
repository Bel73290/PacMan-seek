package com.cachecache.game.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cachecache.game.R
import com.cachecache.game.model.HiddenPlayer
import com.cachecache.game.ui.theme.*

@Composable
fun GhostCaptureOverlay(
    capturingGhost: HiddenPlayer?,
    modifier: Modifier = Modifier
) {
    if (capturingGhost == null) return

    val animProgress = remember(capturingGhost) { Animatable(0f) }

    LaunchedEffect(capturingGhost) {
        animProgress.snapTo(0f)
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1050, easing = LinearEasing)
        )
    }

    val progress = animProgress.value

    // 1. Blue dashed outline dissolution (0.0 to 0.40)
    val outlineAlpha = ((1f - progress / 0.40f)).coerceIn(0f, 1f)
    val outlineScale = 1f + (progress / 0.40f).coerceIn(0f, 1f) * 0.35f
    val outlineFrame = ((progress * 1050 / 80).toInt() % 3).coerceIn(0, 2)
    val outlineResId = when (outlineFrame) {
        0 -> R.drawable.ghost_outline_0
        1 -> R.drawable.ghost_outline_1
        else -> R.drawable.ghost_outline_2
    }

    // 2. Fleeing floating eyes (0.15 to 0.95)
    val eyesStart = 0.15f
    val eyesEnd = 0.95f
    val eyesFraction = ((progress - eyesStart) / (eyesEnd - eyesStart)).coerceIn(0f, 1f)
    // Accelerated flight curve (powers of fraction for rapid rocket ascent)
    val flightCurve = eyesFraction * eyesFraction * (3f - 2f * eyesFraction)
    val eyesTranslationY = -flightCurve * 850f // Zips 850 pixels upwards off the screen!
    val eyesAlpha = if (progress >= eyesStart) 1f else 0f

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = (0.35f * (1f - progress)).coerceIn(0f, 0.35f))),
        contentAlignment = Alignment.Center
    ) {
        // Shockwave aura circle when ghost bursts into lines
        if (progress < 0.45f) {
            val auraAlpha = (1f - progress / 0.45f) * 0.5f
            val auraSize = 70.dp + (progress / 0.45f * 80).dp
            Box(
                modifier = Modifier
                    .size(auraSize)
                    .scale(outlineScale)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                ScaredBlue.copy(alpha = auraAlpha),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )
        }

        // 1. Ghost Blue Dashed Outline Dissolving
        if (outlineAlpha > 0.01f) {
            Image(
                painter = painterResource(id = outlineResId),
                contentDescription = "Ghost Dissolving",
                modifier = Modifier
                    .size(80.dp)
                    .scale(outlineScale)
                    .alpha(outlineAlpha)
            )
        }

        // 2. Ghost Floating Eyes Escaping Off-Screen
        if (eyesAlpha > 0f) {
            Image(
                painter = painterResource(id = R.drawable.ghost_eyes_up),
                contentDescription = "Fleeing Ghost Eyes",
                modifier = Modifier
                    .size(54.dp)
                    .offset(y = eyesTranslationY.dp)
                    .alpha(eyesAlpha)
            )
        }

        // Retro Arcade "ABSORBÉ !" Toast during the first 500ms
        if (progress < 0.6f) {
            val textAlpha = (1f - progress / 0.6f).coerceIn(0f, 1f)
            Box(
                modifier = Modifier
                    .offset(y = 65.dp)
                    .alpha(textAlpha)
            ) {
                Text(
                    text = "FANTÔME CAPTURÉ !",
                    color = PacmanYellow,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
            }
        }
    }
}
