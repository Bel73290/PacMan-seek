package com.cachecache.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cachecache.game.data.MockData
import com.cachecache.game.model.*
import com.cachecache.game.ui.components.BottomNavigationBar
import com.cachecache.game.ui.components.SkinAvatar
import com.cachecache.game.ui.components.BattlePassSection
import com.cachecache.game.ui.theme.*
import com.cachecache.game.viewmodel.OnlineState
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
private fun OnlinePage(title: String, screen: Screen, state: OnlineState, onNavigate: (Screen)->Unit,
    onRetry: ()->Unit, content: androidx.compose.foundation.lazy.LazyListScope.()->Unit) {
    Scaffold(containerColor=DeepNavy, bottomBar={ BottomNavigationBar(screen,onNavigate) }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).padding(horizontal=20.dp),
            verticalArrangement=Arrangement.spacedBy(14.dp), contentPadding=PaddingValues(bottom=24.dp)) {
            item {
                Row(Modifier.fillMaxWidth(), verticalAlignment=Alignment.CenterVertically) {
                    Text(title,Modifier.weight(1f),fontSize=28.sp,fontWeight=FontWeight.Black,color=TextPrimary)
                    TextButton(onClick={onNavigate(Screen.SPLASH)}) {Text("Accueil")}
                }
            }
            if(state.loading || state.busy) item {LinearProgressIndicator(Modifier.fillMaxWidth())}
            state.error?.let { error -> item {
                Card(colors=CardDefaults.cardColors(containerColor=GhostRed.copy(alpha=.12f))) {
                    Column(Modifier.padding(16.dp)) { Text(error,color=TextPrimary); TextButton(onClick=onRetry){Text("Réessayer")} }
                }
            } }
            state.message?.let { item { Text(it,color=GhostCyan) } }
            content()
        }
    }
}

@Composable
private fun InfoCard(title:String, text:String, content:@Composable ColumnScope.()->Unit = {}) {
    Card(Modifier.fillMaxWidth(),shape=RoundedCornerShape(20.dp),colors=CardDefaults.cardColors(containerColor=CardSurface)) {
        Column(Modifier.padding(18.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
            Text(title,fontWeight=FontWeight.Bold,color=TextPrimary); Text(text,color=TextSecondary); content()
        }
    }
}

@Composable
fun OnlineProfileScreen(state:OnlineState,onNavigate:(Screen)->Unit,onRetry:()->Unit,onConnect:(Boolean)->Unit,
    onSave:(String)->Unit,onEquip:(String)->Unit,onResume:(String)->Unit,onClaimPass:()->Unit = {}) {
    var name by remember(state.name) { mutableStateOf(state.name) }
    var confirmExisting by remember { mutableStateOf(false) }
    OnlinePage("Mon profil",Screen.PROFILE,state,onNavigate,onRetry) {
        item {
            InfoCard(if(state.guest) "Compte invité" else "Compte Google connecté",
                if(state.guest) "Connecte ton compte pour retrouver ta progression sur un autre téléphone." else "Ta progression est sauvegardée et liée à ton compte.") {
                if(state.guest) Button(onClick={onConnect(false)},enabled=!state.busy && !state.loading) {Text("Se connecter avec Google")}
                if(state.existingAccount) OutlinedButton(onClick={confirmExisting=true},enabled=!state.busy) {Text("Ouvrir mon compte existant")}
                Text("Code ami : ${state.profile["friendCode"] ?: "…"}",color=PacmanYellow)
            }
        }
        item {
            Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(16.dp)) {
                SkinAvatar(state.ghostSkin,72.dp)
                Column {Text(state.name,color=TextPrimary,fontSize=22.sp,fontWeight=FontWeight.Bold);Text("Niveau ${state.level}",color=PacmanYellow)}
            }
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(name,{if(it.length<=24)name=it},label={Text("Pseudo")},singleLine=true,enabled=!state.loading && !state.busy,modifier=Modifier.fillMaxWidth())
            TextButton(onClick={onSave(name)},enabled=!state.loading && !state.busy && name.trim().length>=2) {Text("Enregistrer le pseudo")}
            LinearProgressIndicator(progress={(state.number("xp")%500)/500f},modifier=Modifier.fillMaxWidth())
            Text("${state.number("xp")%500} / 500 XP",color=TextSecondary)
        }
        item {InfoCard("${state.number("score")} points • ${state.coins} pièces","${state.number("games")} parties • ${state.number("victories")} victoires • ${state.number("captures")} captures")}
        item {
            BattlePassSection(state.level,state.number("xp"),state.owned.toSet(),state.ghostSkin,state.busy,
                state.profile.isNotEmpty() && !state.loading,onEquip,onClaimPass)
        }
        (state.profile["activeGame"] as? String)?.let {code -> item {
            Button(onClick={onResume(code)},modifier=Modifier.fillMaxWidth()) {Text("Reprendre la partie $code")}
        } }
        item {OutlinedButton(onClick={onNavigate(Screen.FRIENDS)},modifier=Modifier.fillMaxWidth()){Text("Mes amis et invitations")}}
        item {Text("Mes skins",color=TextPrimary,fontWeight=FontWeight.Bold)}
        items(state.owned.chunked(4)) { group ->
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceEvenly) {
                group.forEach { id ->
                    Column(horizontalAlignment=Alignment.CenterHorizontally) {
                        SkinAvatar(id,48.dp)
                        TextButton(onClick={onEquip(id)},enabled=!state.busy) {Text(if(id==state.ghostSkin || id==state.pacmanSkin) "Équipé" else "Équiper",fontSize=11.sp)}
                    }
                }
            }
        }
        item {
            val badges=buildList {
                if(state.number("games")>=1)add("Première partie")
                if(state.number("captures")>=1)add("Premier trouvé")
                if(state.number("victories")>=10)add("10 victoires")
                if(state.level>=10)add("Niveau 10")
            }
            InfoCard("Badges",badges.joinToString(" • ").ifEmpty{"Termine des parties pour débloquer tes premiers badges."})
        }
        item {Text("Dernières parties",color=TextPrimary,fontWeight=FontWeight.Bold)}
        if(state.history.isEmpty()) item {Text("Tes résultats apparaîtront ici après une partie validée.",color=TextSecondary)}
        items(state.history,key={it["id"].toString()}) {row ->
            val date=(row["at"] as? Timestamp)?.toDate()?.let{SimpleDateFormat("dd/MM/yyyy HH:mm",Locale.FRANCE).format(it)} ?: "Synchronisation…"
            InfoCard(if(row["won"]==true) "Victoire" else "Partie terminée","$date • +${row["score"]} points • +${row["coins"]} pièces • +${row["xp"]} XP")
        }
    }
    if(confirmExisting) AlertDialog(onDismissRequest={confirmExisting=false},title={Text("Ouvrir le compte existant ?")},
        text={Text("Tu retrouveras la progression de ton compte Google. La progression de cet invité ne sera pas fusionnée. Ne change pas de compte pendant une partie.")},
        confirmButton={TextButton(onClick={confirmExisting=false;onConnect(true)}){Text("Ouvrir ce compte")}},
        dismissButton={TextButton(onClick={confirmExisting=false}){Text("Annuler")}})
}

@Composable
fun OnlineLeaderboardScreen(state:OnlineState,onNavigate:(Screen)->Unit,onLoad:(String,Boolean)->Unit) {
    OnlinePage("Classement",Screen.LEADERBOARD,state,onNavigate,{onLoad(state.scope,false)}) {
        item {Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            listOf("general" to "Mondial","season" to "Ce mois","friends" to "Amis").forEach{(scope,label)->
                FilterChip(selected=state.scope==scope,onClick={onLoad(scope,false)},label={Text(label)})
            }
        }}
        item {InfoCard("Ta position : ${state.rank?.let{"#$it"} ?: "Non classé"}","Les points gagnés restent au classement lorsque tu dépenses tes pièces.")}
        if(state.boardLoading) item {LinearProgressIndicator(Modifier.fillMaxWidth())}
        if(state.entries.isEmpty() && !state.boardLoading) item {Text("Aucun résultat pour le moment. Termine une partie ou ajoute des amis.",color=TextSecondary)}
        items(state.entries.size) {index->
            val row=state.entries[index]; val me=row["uid"]==state.uid
            Card(colors=CardDefaults.cardColors(containerColor=if(me) SelectedSurface else CardSurface)) {
                Row(Modifier.fillMaxWidth().padding(14.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                    Text("${index+1}",color=PacmanYellow,fontWeight=FontWeight.Bold)
                    SkinAvatar(row["ghostSkin"] as? String ?: "ghost_red_classic",40.dp)
                    Column(Modifier.weight(1f)) {Text("${row["name"]}${if(me) " (toi)" else ""}",color=TextPrimary);Text("Niveau ${row["level"] ?: 1}",color=TextSecondary)}
                    Text("${row["score"]} pts",color=TextPrimary)
                }
            }
        }
        if(state.hasMore) item {OutlinedButton(onClick={onLoad(state.scope,true)},enabled=!state.boardLoading){Text("Voir la suite")}}
        item {TextButton(onClick={onLoad(state.scope,false)}){Text("Actualiser")}}
    }
}

@Composable
fun OnlineShopScreen(state:OnlineState,onNavigate:(Screen)->Unit,onRetry:()->Unit,onBuy:(String)->Unit,onEquip:(String)->Unit) {
    var role by remember {mutableStateOf("ghost")}
    var selected by remember {mutableStateOf("ghost_red_classic")}
    var confirmation by remember {mutableStateOf<AvatarItem?>(null)}
    val passSkins=MockData.BattlePassTiers.map{it.avatarId}.toSet()
    val catalog=MockData.AvatarItemsCatalog.filter{(it.id.startsWith("pacman_"))==(role=="pacman") && it.id !in passSkins}
    val preview=MockData.AvatarItemsCatalog.firstOrNull{it.id==selected} ?: catalog.first()
    OnlinePage("Boutique",Screen.SHOP,state,onNavigate,onRetry) {
        item {InfoCard("${state.coins} pièces","Les skins sont cosmétiques. Tes achats et ta collection sont sauvegardés en ligne.")}
        item {Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            FilterChip(role=="ghost",onClick={role="ghost";selected=state.ghostSkin},label={Text("Fantômes")})
            FilterChip(role=="pacman",onClick={role="pacman";selected=state.pacmanSkin},label={Text("Pacman")})
        }}
        item {
            InfoCard(preview.name,preview.description) {
                Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceEvenly) {
                    if(preview.id.startsWith("pacman_")) {
                        SkinAvatar(preview.id,56.dp,90f);SkinAvatar(preview.id,56.dp,270f)
                    } else if(preview.id.endsWith("_classic")) repeat(6) {direction->SkinAvatar(preview.id,38.dp,direction*60f)}
                    else SkinAvatar(preview.id,96.dp)
                }
                Text(if(preview.id.startsWith("pacman_")) "Gauche et droite, sans inclinaison" else if(preview.id.endsWith("_classic")) "6 vues : face, trois-quarts, profils et dos" else "Visuel original du skin",color=TextSecondary,fontSize=12.sp)
            }
        }
        items(catalog,key={it.id}) {skin ->
            val owned=skin.id in state.owned
            val equipped=skin.id==state.ghostSkin || skin.id==state.pacmanSkin
            Card(onClick={selected=skin.id},colors=CardDefaults.cardColors(containerColor=if(selected==skin.id)SelectedSurface else CardSurface)) {
                Row(Modifier.fillMaxWidth().padding(14.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                    SkinAvatar(skin.id,52.dp)
                    Column(Modifier.weight(1f)) {Text(skin.name,color=TextPrimary,fontWeight=FontWeight.Bold);Text(if(owned) "Dans ta collection" else "${skin.price} pièces",color=TextSecondary)}
                    Button(onClick={if(owned)onEquip(skin.id) else confirmation=skin},enabled=!state.busy && !state.loading && !equipped && (owned || state.coins>=skin.price)) {
                        Text(if(equipped)"Équipé" else if(owned)"Équiper" else "Acheter",fontSize=12.sp)
                    }
                }
            }
        }
    }
    confirmation?.let {skin->AlertDialog(onDismissRequest={confirmation=null},title={Text("Acheter ${skin.name} ?")},text={Text("${skin.price} pièces seront retirées de ton solde. Ton score de classement restera identique.")},
        confirmButton={TextButton(onClick={confirmation=null;onBuy(skin.id)}){Text("Acheter")}},dismissButton={TextButton(onClick={confirmation=null}){Text("Annuler")}})}
}

@Composable
fun FriendsScreen(state:OnlineState,onNavigate:(Screen)->Unit,onRetry:()->Unit,onAction:(String,Map<String,Any?>)->Unit,onJoin:(String)->Unit) {
    var code by remember {mutableStateOf("")}
    OnlinePage("Mes amis",Screen.FRIENDS,state,onNavigate,onRetry) {
        item {
            InfoCard("Ton code : ${state.profile["friendCode"] ?: "…"}","Partage ton code pour recevoir une demande d’ami.") {
                OutlinedTextField(code,{code=it.uppercase().take(12)},label={Text("Code ami")},singleLine=true,modifier=Modifier.fillMaxWidth())
                Button(onClick={onAction("sendFriendRequest",mapOf("friendCode" to code))},enabled=!state.busy && code.length==12){Text("Ajouter un ami")}
            }
        }
        items(state.requests,key={"request-${it["id"]}"}) {request ->
            InfoCard("Demande de ${request["name"]}","Souhaites-tu ajouter ce joueur ?") {
                Row {TextButton(onClick={onAction("respondFriendRequest",mapOf("uid" to request["id"],"accept" to true))},enabled=!state.busy){Text("Accepter")}
                    TextButton(onClick={onAction("respondFriendRequest",mapOf("uid" to request["id"],"accept" to false))},enabled=!state.busy){Text("Refuser")}}
            }
        }
        items(state.invites,key={"invite-${it["id"]}"}) {invite ->
            InfoCard("Invitation de ${invite["from"]}","Salon ${invite["code"]}") {Button(onClick={onJoin(invite["code"].toString())}){Text("Rejoindre")}}
        }
        if(state.friends.isEmpty()) item {Text("Aucun ami pour le moment.",color=TextSecondary)}
        items(state.friends,key={"friend-${it["id"]}"}) {friend->
            InfoCard(friend["name"].toString(),"Ami") {
                (state.profile["activeGame"] as? String)?.let{game->
                    TextButton(onClick={onAction("inviteFriend",mapOf("uid" to friend["id"],"code" to game))},enabled=!state.busy){Text("Inviter dans mon salon")}
                }
                TextButton(onClick={onAction("removeFriend",mapOf("uid" to friend["id"]))},enabled=!state.busy){Text("Retirer des amis")}
            }
        }
    }
}
