package com.cachecache.game.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cachecache.game.data.MockData
import com.cachecache.game.model.Direction
import com.cachecache.game.ui.theme.PacmanYellow

@Composable
fun SkinAvatar(id: String, size: Dp = 64.dp, heading: Float? = null) {
    val item = MockData.AvatarItemsCatalog.firstOrNull { it.id == id }
    val rotation = heading?.let { Direction.angle(it) - if(id.startsWith("pacman_")) 90f else 180f } ?: 0f
    if (item?.drawableResId != null && id != "pacman_neon") {
        Image(painterResource(item.drawableResId), contentDescription = item.name,
            modifier = Modifier.size(size).graphicsLayer { rotationZ = rotation })
    } else {
        Canvas(Modifier.size(size).background(Color(0xFF15243D), CircleShape)) {
            rotate(rotation) {
                val diameter = this.size.minDimension * .8f
                val start = Offset((this.size.width-diameter)/2, (this.size.height-diameter)/2)
                val color = if (id == "pacman_neon") Color(0xFF4EF5E2) else PacmanYellow
                drawCircle(color.copy(alpha=.12f), radius=this.size.minDimension/2)
                drawArc(color, 32f, 296f, true, start, Size(diameter, diameter))
                drawCircle(Color(0xFF101C32), this.size.minDimension*.045f, Offset(this.size.width*.53f,this.size.height*.27f))
            }
        }
    }
}
