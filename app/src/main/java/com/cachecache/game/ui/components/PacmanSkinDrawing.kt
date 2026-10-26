package com.cachecache.game.ui.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import com.cachecache.game.ui.theme.PacmanYellow

/** All artwork is drawn upright facing right. The caller only mirrors it horizontally. */
fun DrawScope.drawPacmanSkin(id:String,origin:Offset,diameter:Float,mouthAngle:Float=48f) {
    withTransform({translate(origin.x,origin.y);scale(diameter/100f,diameter/100f,pivot=Offset.Zero)}) {
        if(id=="pacman_retro") {
            for(y in 0..17)for(x in 0..17) {
                val dx=x-8.5f;val dy=y-8.5f
                if(dx*dx+dy*dy<60 && !(dx>0 && kotlin.math.abs(dy)<dx*.65f))
                    drawRect(PacmanYellow,Offset(7+x*4.8f,7+y*4.8f),Size(5f,5f))
            }
            drawRect(Color(0xFF111D32),Offset(49f,25f),Size(5f,10f))
            return@withTransform
        }
        if(id=="pacman_super") {
            drawPath(Path().apply{moveTo(28f,35f);lineTo(5f,81f);lineTo(27f,74f);lineTo(18f,91f);lineTo(51f,76f);close()},Color(0xFFE94459))
        }
        val body=if(id=="pacman_neon")Color(0xFF4EF5E2) else PacmanYellow
        if(id=="pacman_neon") drawCircle(body.copy(alpha=.18f),48f,Offset(50f,50f))
        val shape=Path().apply {
            moveTo(50f,50f)
            arcTo(Rect(14f,14f,86f,86f),mouthAngle/2,360-mouthAngle,false)
            close()
        }
        drawPath(shape,Brush.radialGradient(listOf(Color(0xFFFFF4A3),body,body),center=Offset(35f,30f),radius=68f))
        drawPath(shape,Color(0xFF19243D),style=Stroke(1.5f))
        drawOval(Color(0xFF142039),Offset(48f,27f),Size(5f,10f))
        if(id=="pacman_miss") {
            val red=Color(0xFFEF4563)
            drawPath(Path().apply{moveTo(40f,15f);lineTo(25f,2f);lineTo(23f,22f);close()},red)
            drawPath(Path().apply{moveTo(42f,15f);lineTo(58f,3f);lineTo(60f,23f);close()},red)
            drawCircle(red,5f,Offset(41f,15f))
            drawCircle(Color.White.copy(alpha=.8f),2f,Offset(30f,13f))
            drawCircle(Color.White.copy(alpha=.8f),2f,Offset(53f,13f))
            drawLine(Color(0xFF142039),Offset(50f,29f),Offset(47f,23f),2f)
            drawLine(Color(0xFF142039),Offset(52f,29f),Offset(54f,23f),2f)
            val radians=Math.toRadians((mouthAngle/2).toDouble())
            val lipX=50+36*kotlin.math.cos(radians).toFloat()
            val lipY=36*kotlin.math.sin(radians).toFloat()
            drawLine(red,Offset(lipX-5,50-lipY+2),Offset(lipX+1,50-lipY),4f,StrokeCap.Round)
            drawLine(red,Offset(lipX-5,50+lipY-2),Offset(lipX+1,50+lipY),4f,StrokeCap.Round)
        }
        if(id=="pacman_super") {
            drawPath(Path().apply{moveTo(19f,30f);lineTo(51f,23f);lineTo(64f,32f);lineTo(58f,42f);lineTo(21f,39f);close()},Color(0xFF397CDD))
            drawOval(Color.White,Offset(47f,29f),Size(10f,5f))
        }
    }
}
