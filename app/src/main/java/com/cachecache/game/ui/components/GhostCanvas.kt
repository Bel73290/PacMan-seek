package com.cachecache.game.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cachecache.game.R
import com.cachecache.game.ui.theme.*
import kotlin.math.sin

@Composable
fun GhostComposable(
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    baseColor: Color = GhostRed,
    isScared: Boolean = false,
    isVulnerableBlinking: Boolean = false,
    lookDirection: androidx.compose.ui.geometry.Offset = androidx.compose.ui.geometry.Offset(1f, 0f)
) {
    // 1. Hovering (vertical sinusoidal displacement +/- 5 dp)
    val infiniteTransition = rememberInfiniteTransition(label = "ghostHover")
    val hoverPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.283185f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "hoverPhase"
    )

    // 2. Fast panic tremble for scared mode
    val tremblePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.283185f,
        animationSpec = infiniteRepeatable(
            animation = tween(120, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "tremblePhase"
    )

    val ghostDrawable = when {
        isScared -> R.drawable.ghost_scared_blue
        baseColor == GhostPink -> R.drawable.ghost_pink
        baseColor == GhostCyan -> R.drawable.ghost_cyan
        baseColor == GhostOrange -> R.drawable.ghost_orange
        baseColor == GhostPurple -> R.drawable.ghost_purple
        else -> R.drawable.ghost_red
    }

    Image(
        painter = painterResource(id = ghostDrawable),
        contentDescription = "Fantôme",
        modifier = modifier
            .size(size)
            .graphicsLayer {
                translationY = sin(hoverPhase) * 5.dp.toPx()
                translationX = if (isScared) sin(tremblePhase * 30f) * 3.dp.toPx() else 0f
            },
        contentScale = ContentScale.Fit
    )
}
