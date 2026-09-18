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

@Composable
fun GhostComposable(
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    baseColor: Color = GhostRed,
    isScared: Boolean = false,
    isVulnerableBlinking: Boolean = false,
    lookDirection: androidx.compose.ui.geometry.Offset = androidx.compose.ui.geometry.Offset(0f, 0f)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ghostAnim")

    // 1. Authentic skirt walking animation (cycles between frame 0 and 1 every 180ms)
    val walkFrame by infiniteTransition.animateValue(
        initialValue = 0,
        targetValue = 1,
        typeConverter = Int.VectorConverter,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 360, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "walkFrame"
    )

    // 2. Panicked horizontal left-right trembling on the spot when scared
    val trembleX by infiniteTransition.animateFloat(
        initialValue = -3.5f,
        targetValue = 3.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(75, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "trembleX"
    )

    val drawableId = getGhostDrawableId(
        baseColor = baseColor,
        isScared = isScared,
        lookDirX = lookDirection.x,
        lookDirY = lookDirection.y,
        walkFrame = walkFrame
    )

    Image(
        painter = painterResource(id = drawableId),
        contentDescription = "Fantôme",
        modifier = modifier
            .size(size)
            .graphicsLayer {
                // Pure horizontal tremble when scared, NO vertical up/down bobbing
                translationX = if (isScared) trembleX.dp.toPx() else 0f
            },
        contentScale = ContentScale.Fit
    )
}

fun getGhostDrawableId(
    baseColor: Color,
    isScared: Boolean,
    lookDirX: Float,
    lookDirY: Float = 0f,
    walkFrame: Int
): Int {
    val frameSuffix = if (walkFrame == 0) "0" else "1"

    if (isScared) {
        return if (walkFrame == 0) R.drawable.ghost_scared_blue_0 else R.drawable.ghost_scared_blue_1
    }

    val colorPrefix = when (baseColor) {
        GhostPink -> "pink"
        GhostCyan -> "cyan"
        GhostOrange -> "orange"
        GhostPurple -> "purple"
        else -> "red"
    }

    val absX = kotlin.math.abs(lookDirX)
    val absY = kotlin.math.abs(lookDirY)

    val dir = when {
        absY > absX && lookDirY < -0.15f -> "back"
        absY > absX && lookDirY > 0.15f  -> "front"
        lookDirX < -0.15f                -> "left"
        lookDirX > 0.15f                 -> "right"
        lookDirY < -0.15f                -> "back"
        else                             -> "front"
    }

    return when ("${colorPrefix}_${dir}_$frameSuffix") {
        "red_front_0" -> R.drawable.ghost_red_front_0
        "red_front_1" -> R.drawable.ghost_red_front_1
        "red_left_0" -> R.drawable.ghost_red_left_0
        "red_left_1" -> R.drawable.ghost_red_left_1
        "red_right_0" -> R.drawable.ghost_red_right_0
        "red_right_1" -> R.drawable.ghost_red_right_1
        "red_back_0" -> R.drawable.ghost_red_back_0
        "red_back_1" -> R.drawable.ghost_red_back_1

        "pink_front_0" -> R.drawable.ghost_pink_front_0
        "pink_front_1" -> R.drawable.ghost_pink_front_1
        "pink_left_0" -> R.drawable.ghost_pink_left_0
        "pink_left_1" -> R.drawable.ghost_pink_left_1
        "pink_right_0" -> R.drawable.ghost_pink_right_0
        "pink_right_1" -> R.drawable.ghost_pink_right_1
        "pink_back_0" -> R.drawable.ghost_pink_back_0
        "pink_back_1" -> R.drawable.ghost_pink_back_1

        "cyan_front_0" -> R.drawable.ghost_cyan_front_0
        "cyan_front_1" -> R.drawable.ghost_cyan_front_1
        "cyan_left_0" -> R.drawable.ghost_cyan_left_0
        "cyan_left_1" -> R.drawable.ghost_cyan_left_1
        "cyan_right_0" -> R.drawable.ghost_cyan_right_0
        "cyan_right_1" -> R.drawable.ghost_cyan_right_1
        "cyan_back_0" -> R.drawable.ghost_cyan_back_0
        "cyan_back_1" -> R.drawable.ghost_cyan_back_1

        "orange_front_0" -> R.drawable.ghost_orange_front_0
        "orange_front_1" -> R.drawable.ghost_orange_front_1
        "orange_left_0" -> R.drawable.ghost_orange_left_0
        "orange_left_1" -> R.drawable.ghost_orange_left_1
        "orange_right_0" -> R.drawable.ghost_orange_right_0
        "orange_right_1" -> R.drawable.ghost_orange_right_1
        "orange_back_0" -> R.drawable.ghost_orange_back_0
        "orange_back_1" -> R.drawable.ghost_orange_back_1

        "purple_front_0" -> R.drawable.ghost_purple_front_0
        "purple_front_1" -> R.drawable.ghost_purple_front_1
        "purple_left_0" -> R.drawable.ghost_purple_left_0
        "purple_left_1" -> R.drawable.ghost_purple_left_1
        "purple_right_0" -> R.drawable.ghost_purple_right_0
        "purple_right_1" -> R.drawable.ghost_purple_right_1
        "purple_back_0" -> R.drawable.ghost_purple_back_0
        "purple_back_1" -> R.drawable.ghost_purple_back_1

        else -> R.drawable.ghost_red_front_0
    }
}
