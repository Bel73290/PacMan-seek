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
        drawGhostAccessories(id,view)
    }
}

/** Accessories keep their vertical axis and change visibility/placement with the body view. */
private fun DrawScope.drawGhostAccessories(id:String,view:Int) {
    val back=view==0
    val profile=view==1||view==5
    val faceX=when(view){1->73f;2->60f;4->40f;5->27f;else->50f}
    val gold=Color(0xFFFFCD45)
    fun oval(color:Color,x:Float,y:Float,w:Float,h:Float)=drawOval(color,Offset(x,y),Size(w,h))
    fun hat(color:Color) {
        oval(color,16f,27f,68f,13f)
        drawRoundRect(color,Offset(29f,9f),Size(43f,25f),cornerRadius=CornerRadius(9f,9f))
        drawLine(gold,Offset(30f,29f),Offset(70f,29f),4f)
    }
    when {
        id.contains("headphones")->{
            drawArc(Color(0xFF27314C),180f,180f,false,Offset(16f,8f),Size(68f,61f),style=Stroke(8f))
            oval(Color(0xFF51DCEC),12f,37f,13f,24f);oval(Color(0xFF51DCEC),75f,37f,13f,24f)
        }
        id.contains("crown")||id.contains("gold_king")||id.contains("princess")||id.contains("pharaoh")->{
            val crown=Path().apply{moveTo(23f,34f);lineTo(19f,10f);lineTo(36f,21f);lineTo(50f,4f);lineTo(64f,21f);lineTo(81f,10f);lineTo(77f,34f);close()}
            drawPath(crown,gold);drawCircle(Color(0xFFF15E91),4f,Offset(if(back)50f else faceX,26f))
        }
        id.contains("wizard")||id.contains("mage")->{
            oval(Color(0xFF7754D7),14f,27f,72f,13f)
            drawPath(Path().apply{moveTo(29f,31f);lineTo(55f,0f);lineTo(73f,31f);close()},Color(0xFF7754D7))
            drawCircle(gold,4f,Offset(53f,18f))
        }
        id.contains("pirate")->{
            drawPath(Path().apply{moveTo(14f,33f);quadraticBezierTo(23f,5f,50f,18f);quadraticBezierTo(77f,5f,86f,33f);close()},Color(0xFF222332))
            drawLine(gold,Offset(18f,32f),Offset(82f,32f),3f)
            if(!back) {drawCircle(Color.White,5f,Offset(faceX,24f));drawCircle(Color(0xFF171A29),1.5f,Offset(faceX-2f,24f));drawCircle(Color(0xFF171A29),1.5f,Offset(faceX+2f,24f))}
        }
        id.contains("cowboy")->{hat(Color(0xFF9B602C))}
        id.contains("santa")->{
            drawPath(Path().apply{moveTo(24f,30f);quadraticBezierTo(55f,-5f,80f,12f);lineTo(70f,32f);close()},Color(0xFFE84B57))
            oval(Color.White,20f,26f,60f,12f);drawCircle(Color.White,7f,Offset(81f,14f))
        }
        id.endsWith("_cap")->{oval(Color(0xFF2477BA),23f,11f,54f,28f);oval(Color(0xFF2477BA),if(back)22f else faceX-15f,29f,42f,9f)}
        id.contains("halo")||id.contains("angel")->{
            drawOval(gold,Offset(27f,5f),Size(46f,11f),style=Stroke(4f))
            if(id.contains("wings")){oval(Color.White,1f,38f,18f,38f);oval(Color.White,81f,38f,18f,38f)}
        }
        id.contains("devil")->{
            listOf(24f,67f).forEach{x->drawPath(Path().apply{moveTo(x,24f);lineTo(x+3f,4f);lineTo(x+15f,25f);close()},Color(0xFFED345C))}
        }
        id.contains("flower")->{
            val c=Offset(if(back)40f else 68f,21f)
            repeat(5){n->val a=Math.toRadians(n*72.0);drawCircle(Color(0xFFFF79BC),6f,c+Offset(kotlin.math.cos(a).toFloat()*7,kotlin.math.sin(a).toFloat()*7))};drawCircle(gold,4f,c)
        }
        id.contains("fire")->{
            drawPath(Path().apply{moveTo(22f,30f);lineTo(30f,2f);lineTo(45f,19f);lineTo(58f,0f);lineTo(77f,31f);close()},Color(0xFFFF793F))
        }
        id.contains("crystal")->{
            drawPath(Path().apply{moveTo(20f,29f);lineTo(29f,6f);lineTo(48f,23f);lineTo(65f,2f);lineTo(80f,31f);close()},Color(0xFFB2F5FF))
        }
        id.contains("vampire")->{drawPath(Path().apply{moveTo(9f,73f);lineTo(22f,41f);lineTo(31f,81f);lineTo(74f,81f);lineTo(80f,41f);lineTo(93f,73f);close()},Color(0xFF843255))}
        id.contains("mummy")->{repeat(4){n->drawLine(Color(0xFFE1D6B3).copy(alpha=.7f),Offset(19f,27f+n*16f),Offset(81f,33f+n*16f),6f)}}
        id.contains("disco")->{drawLine(Color(0xFFEB66F2),Offset(21f,22f),Offset(79f,22f),7f)}
    }
    if(!back && (id.contains("sunglasses")||id.contains("cyberpunk")||id.contains("steampunk")||id.contains("snorkel"))) {
        val lens=if(id.contains("cyberpunk"))Color(0xFF52F5EC) else Color(0xFF152337)
        val width=if(profile)18f else 42f
        drawRoundRect(lens,Offset(faceX-width/2,38f),Size(width,15f),cornerRadius=CornerRadius(5f,5f))
        drawLine(Color.White.copy(alpha=.65f),Offset(faceX-width/2+4,41f),Offset(faceX-1,41f),2f)
    }
    if(!back && id.contains("mustache")) {
        oval(Color(0xFF33202D),faceX-14f,62f,16f,7f);oval(Color(0xFF33202D),faceX,62f,14f,7f)
    }
}
