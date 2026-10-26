package com.cachecache.game.ui.components

import android.content.Context
import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.cachecache.game.data.MockData
import com.cachecache.game.model.Direction
import com.cachecache.game.ui.theme.*

/** Actual upright front/three-quarter/profile/back artwork; never rotate the image plane. */
private object GhostSpriteCache {
    private val cache=object:LruCache<String,ImageBitmap>(24*1024) {
        override fun sizeOf(key:String,value:ImageBitmap)=value.width*value.height*4/1024
    }
    @Synchronized fun load(context:Context,name:String):ImageBitmap? {
        cache.get(name)?.let{return it}
        val res=context.resources.getIdentifier(name,"drawable",context.packageName)
        if(res==0)return null
        val bounds=BitmapFactory.Options().apply{inJustDecodeBounds=true}
        BitmapFactory.decodeResource(context.resources,res,bounds)
        val options=BitmapFactory.Options().apply {
            var sample=1
            while(bounds.outWidth/sample>192 || bounds.outHeight/sample>192) sample*=2
            inSampleSize=sample
        }
        return BitmapFactory.decodeResource(context.resources,res,options)?.asImageBitmap()?.also{cache.put(name,it)}
    }
}

@Composable
fun rememberGhostSkins(ids:Set<String>):Map<String,List<ImageBitmap?>> {
    val context=LocalContext.current
    return remember(ids) { ids.filterNot{it.startsWith("pacman_")}.associateWith {id->
        val color=MockData.AvatarItemsCatalog.firstOrNull{it.id==id}?.ghostColor
        val prefix=when {
            id.contains("gold")||id.contains("pharaoh")||id.contains("divine")->"gold"
            color==GhostPink->"pink"
            color==GhostCyan->"cyan"
            color==GhostOrange->"orange"
            color==GhostRed->"red"
            else->"purple"
        }
        // Compass headings 0/60/120/180/240/300: back, profile, three-quarter, front, ...
        listOf("back","right","front","true_front","slight_left","left").map {view->
            GhostSpriteCache.load(context,"ghost_${prefix}_${view}_0")
        }
    } }
}

fun DrawScope.drawDirectionalGhost(sprites:List<ImageBitmap?>,id:String,heading:Float,topLeft:Offset,diameter:Float) {
    val view=Direction.index(heading)
    val bitmap=sprites.getOrNull(view) ?: return
    withTransform({translate(topLeft.x,topLeft.y);scale(diameter/100f,diameter/100f,pivot=Offset.Zero)}) {
        drawImage(bitmap,dstOffset=IntOffset.Zero,dstSize=IntSize(100,100))
    }
}
