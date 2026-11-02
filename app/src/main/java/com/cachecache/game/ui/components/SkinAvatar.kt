package com.cachecache.game.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cachecache.game.data.MockData
import com.cachecache.game.model.Direction
import com.cachecache.game.ui.theme.GhostRed

@Composable
fun SkinAvatar(id: String, size: Dp = 64.dp, heading: Float? = null) {
    val item=MockData.AvatarItemsCatalog.firstOrNull{it.id==id}
    val pacman=id.startsWith("pacman_")
    val directional=!pacman && heading!=null && id.endsWith("_classic")
    val sprites=rememberGhostSkins(if(directional)setOf(id) else emptySet())
    if(pacman) {
        PacmanComposable(size=size,isFacingRight=Direction.facesRight(heading ?: 90f))
    } else if(directional) {
        Canvas(Modifier.size(size)) {
            sprites[id]?.let{drawDirectionalGhost(it,id,heading!!,Offset.Zero,this.size.minDimension)}
        }
    } else if(item?.drawableResId!=null) {
        Image(painterResource(item.drawableResId),contentDescription=item.name,
            modifier=Modifier.size(size))
    } else {
        GhostComposable(size=size,baseColor=item?.ghostColor ?: GhostRed)
    }
}
