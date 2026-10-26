package com.cachecache.game.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cachecache.game.data.MockData
import com.cachecache.game.ui.theme.*

/** The original ten progression rewards, presented as a selectable pass and hero preview. */
@Composable
fun BattlePassSection(level:Int,xp:Int,owned:Set<String>,equipped:String,busy:Boolean,connected:Boolean,
    onEquip:(String)->Unit,onClaim:()->Unit) {
    val tiers=MockData.BattlePassTiers
    val initial=tiers.indexOfFirst{it.level>level}.let{if(it<0)tiers.lastIndex else it}
    var selected by remember(level){mutableIntStateOf(initial)}
    val track=rememberLazyListState(initialFirstVisibleItemIndex=(initial-1).coerceAtLeast(0))
    val tier=tiers[selected]
    val unlocked=tier.avatarId in owned
    val eligible=level>=tier.level
    val color=when(tier.rarity){"MYTHIQUE"->Color(0xFFFF4081);"LÉGENDAIRE"->Color(0xFFFA9C1E);"ÉPIQUE"->Color(0xFFAB47BC);else->ScaredBlue}
    Column(verticalArrangement=Arrangement.spacedBy(12.dp)) {
        Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("PASS DE COMBAT",color=TextPrimary,fontSize=21.sp,fontWeight=FontWeight.Black)
                Text("Progresse et débloque des skins exclusifs",color=TextSecondary,fontSize=12.sp)
            }
            Text("NIV. $level",color=PacmanYellow,fontWeight=FontWeight.Black)
        }
        Card(shape=RoundedCornerShape(20.dp),border=BorderStroke(1.dp,color),colors=CardDefaults.cardColors(containerColor=CardSurface)) {
            Column(Modifier.fillMaxWidth().padding(18.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) {
                Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
                    Text("PALIER ${tier.level}",Modifier.weight(1f),color=color,fontWeight=FontWeight.Bold)
                    Text(tier.rarity,color=color,fontSize=11.sp,fontWeight=FontWeight.Bold)
                }
                Text(tier.title,color=TextPrimary,fontSize=20.sp,fontWeight=FontWeight.Black)
                Box(Modifier.fillMaxWidth().height(175.dp).background(Brush.radialGradient(listOf(color.copy(alpha=.24f),Color.Transparent))),contentAlignment=Alignment.Center) {
                    SkinAvatar(tier.avatarId,136.dp)
                    if(!unlocked) Icon(Icons.Default.Lock,contentDescription="Skin verrouillé",tint=TextPrimary,
                        modifier=Modifier.align(Alignment.BottomEnd).size(30.dp))
                }
                Text(tier.description,color=TextSecondary,fontSize=13.sp)
                val targetXp=(tier.level-1)*500
                LinearProgressIndicator(progress={if(targetXp==0)1f else (xp.toFloat()/targetXp).coerceIn(0f,1f)},modifier=Modifier.fillMaxWidth(),color=color)
                Text(if(unlocked) "Récompense débloquée" else "${xp.coerceAtMost(targetXp)} / $targetXp XP · Niveau ${tier.level}",color=TextSecondary,fontSize=12.sp)
                Button(onClick={if(unlocked)onEquip(tier.avatarId) else onClaim()},
                    enabled=connected && !busy && (unlocked || eligible) && tier.avatarId!=equipped,modifier=Modifier.fillMaxWidth()) {
                    Text(when {tier.avatarId==equipped->"Équipé";unlocked->"Équiper";eligible->"Récupérer la récompense";else->"Niveau ${tier.level} requis"})
                }
                if(!connected) Text("Aperçu du pass. Ta progression sera disponible avec les services en ligne.",color=TextSecondary,fontSize=12.sp)
            }
        }
        LazyRow(state=track,horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            itemsIndexed(tiers,key={_,t->t.level}) {index,reward->
                val hasReward=reward.avatarId in owned
                Card(onClick={selected=index},modifier=Modifier.width(104.dp),
                    border=BorderStroke(if(selected==index)2.dp else 1.dp,if(selected==index)color else BorderSubtle),
                    colors=CardDefaults.cardColors(containerColor=if(hasReward)SelectedSurface else CardSurface)) {
                    Column(Modifier.fillMaxWidth().padding(10.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(6.dp)) {
                        Text("NIV. ${reward.level}",color=TextPrimary,fontWeight=FontWeight.Bold,fontSize=12.sp)
                        SkinAvatar(reward.avatarId,56.dp)
                        Text(if(hasReward) "Débloqué" else "Verrouillé",color=if(hasReward)GhostCyan else TextSecondary,fontSize=11.sp)
                    }
                }
            }
        }
    }
}
