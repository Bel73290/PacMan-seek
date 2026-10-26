package com.cachecache.game.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cachecache.game.model.Direction

@Composable
fun SkinAvatar(id: String, size: Dp = 64.dp, heading: Float? = null) {
    val pacman=id.startsWith("pacman_")
    val sprites=rememberGhostSkins(if(pacman) emptySet() else setOf(id))
    if(!pacman) {
        Canvas(Modifier.size(size)) {
            sprites[id]?.let { drawDirectionalGhost(it,id,heading ?: 180f,Offset.Zero,this.size.minDimension) }
        }
        return
    }
    val right=Direction.facesRight(heading ?: 90f)
    Canvas(Modifier.size(size)) {
        scale(scaleX=if(right) 1f else -1f,scaleY=1f) {
            drawPacmanSkin(id,Offset.Zero,this.size.minDimension)
        }
    }
}
