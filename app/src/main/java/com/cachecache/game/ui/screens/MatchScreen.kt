package com.cachecache.game.ui.screens

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.cachecache.game.data.MockData
import com.cachecache.game.model.*
import com.cachecache.game.ui.components.GhostCaptureOverlay
import com.cachecache.game.ui.components.GhostComposable
import com.cachecache.game.ui.components.OsmMapView
import com.cachecache.game.ui.components.PacmanComposable
import com.cachecache.game.ui.theme.*
import com.cachecache.game.viewmodel.GameViewModel
import com.cachecache.game.viewmodel.MatchViewModel

/** Écran de la partie multijoueur en temps réel, côté Pacman ou côté fantôme. */
@Composable
fun MatchScreen(
    state: MatchState,
    onPermissionsResult: () -> Unit,
    onCatch: () -> Unit,
    onLeave: () -> Unit
) {
    val context = LocalContext.current
    val isPacman = state.role == MatchRole.PACMAN
    var showQuitDialog by remember { mutableStateOf(false) }
    var recenterKey by remember { mutableIntStateOf(0) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { onPermissionsResult() }
    val bluetoothLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { onPermissionsResult() }

    LaunchedEffect(Unit) { permissionLauncher.launch(MatchViewModel.requiredPermissions()) }

    // L'écran reste allumé pendant la partie : sans ça, le GPS et le radar s'arrêtent
    val view = LocalView.current
    DisposableEffect(Unit) {
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = false }
    }

    BackHandler {
        if (state.phase == MatchPhase.FINISHED) onLeave() else showQuitDialog = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepNavy)
    ) {
        // 1. Carte OpenStreetMap centrée sur le joueur
        val center = state.myLocation ?: MockData.SeekerStartLocation
        val myGhostMarker = state.me
            ?.takeIf { !isPacman && state.myLocation != null && !state.iAmFound }
            ?.let { ghost ->
                HiddenPlayer(
                    id = ghost.id.hashCode(),
                    name = ghost.character,
                    locationName = "",
                    location = center,
                    color = resolveGhostColor(ghost.character),
                    isScared = state.isPacmanNearby,
                    skinId = ghost.skinId,
                    heading = state.heading
                )
            }
        OsmMapView(
            seekerLocation = center,
            seekerHeading = state.heading,
            seekerSkinId = state.pacmanSkin,
            recenterKey = recenterKey,
            isFacingRight = state.isFacingRight,
            isFrozen = isPacman && state.phase == MatchPhase.HIDING,
            hiddenPlayers = listOfNotNull(myGhostMarker),
            suspectZone = null,
            pacDotsRoute = state.pacDotsRoute,
            isRadarScanning = state.isInZone,
            zonePolygons = state.zones.map { it.polygon },
            showSeeker = isPacman
        )

        // 2. En-tête : retour, chrono, compteur de fantômes
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MatchHeader(
                state = state,
                onBack = { if (state.phase == MatchPhase.FINISHED) onLeave() else showQuitDialog = true }
            )

            InstructionCard(state)

            if (state.isOffline && state.phase != MatchPhase.FINISHED) {
                AlertBanner(
                    icon = { Icon(Icons.Default.CloudOff, contentDescription = null, tint = GhostOrange) },
                    text = "Hors ligne : positions et captures en pause. Le chrono continue ; les résultats seront confirmés au retour du réseau."
                )
            }

            if (!state.hasLocationPermission) {
                AlertBanner(
                    icon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = GhostRed) },
                    text = "La localisation est indispensable pour jouer.",
                    actionLabel = "Autoriser",
                    onAction = {
                        // La demande a déjà été refusée à l'entrée : on ouvre les réglages de l'app
                        context.startActivity(
                            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
                        )
                    }
                )
            } else if (state.myLocation == null && state.phase != MatchPhase.FINISHED) {
                AlertBanner(
                    icon = {
                        CircularProgressIndicator(color = PacmanYellow, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                    },
                    text = "Recherche de ta position GPS…"
                )
            }

            state.error?.let { error -> AlertBanner(icon={}, text=error, actionLabel="Réessayer", onAction=onPermissionsResult) }
            if ((state.gpsAccuracy ?: 0f)>25) AlertBanner(icon={},text="GPS imprécis : place-toi dehors et attends une meilleure précision.")
            if (state.stalePlayers>0) AlertBanner(icon={},text="${state.stalePlayers} position(s) en attente de mise à jour.")
            if (!state.routeAvailable) AlertBanner(icon={},text="Itinéraire piéton indisponible. Réessaie lorsque le réseau revient.")
            state.bluetoothIssue?.let { issue ->
                val canEnable = issue.startsWith("Active")
                val canAuthorize = issue.startsWith("Autorise")
                AlertBanner(
                    icon = { Icon(Icons.Default.Bluetooth, contentDescription = null, tint = ScaredBlue) },
                    text = issue,
                    actionLabel = when {
                        canEnable -> "Activer"
                        canAuthorize -> "Autoriser"
                        else -> null
                    },
                    onAction = {
                        if (canEnable) {
                            try {
                                bluetoothLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
                            } catch (_: SecurityException) {
                                permissionLauncher.launch(MatchViewModel.requiredPermissions())
                            }
                        } else {
                            permissionLauncher.launch(MatchViewModel.requiredPermissions())
                        }
                    }
                )
            }
        }

        // 3. Bas de l'écran : radar de proximité + bouton "Trouvé !" (Pacman)
        if (isPacman && state.phase == MatchPhase.SEEKING) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AnimatedVisibility(visible = state.proximity > 0f, enter = fadeIn(), exit = fadeOut()) {
                    ProximityMeter(proximity = state.proximity, source = state.proximitySource)
                }
                CatchButton(
                    enabled = state.catchCandidateId != null && state.capturingGhost == null && !state.syncingCapture && !state.isOffline,
                    onClick = onCatch
                )
            }
        }

        TextButton(onClick={recenterKey++},modifier=Modifier.align(Alignment.CenterEnd).background(NavyIntermediate)) { Text("Recentrer GPS") }
        Text("© OpenStreetMap contributors",color=TextSecondary,fontSize=10.sp,modifier=Modifier.align(Alignment.BottomStart).navigationBarsPadding())
        // 4. Animation de capture, écran "trouvé", fin de partie
        GhostCaptureOverlay(capturingGhost = state.capturingGhost)

        if (!isPacman && state.iAmFound && state.phase != MatchPhase.FINISHED) {
            FoundOverlay(character = state.myCharacter)
        }

        if (state.phase == MatchPhase.FINISHED) {
            MatchEndDialog(state = state, onBackHome = onLeave)
        }

        if (showQuitDialog) {
            AlertDialog(
                onDismissRequest = { showQuitDialog = false },
                containerColor = NavyIntermediate,
                title = { Text("Quitter la partie ?", color = TextPrimary, fontWeight = FontWeight.Bold) },
                text = {
                    Text(
                        if (isPacman) "La partie sera annulée pour tous les fantômes."
                        else "Tu ne pourras plus être trouvé dans cette partie.",
                        color = TextSecondary
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        showQuitDialog = false
                        onLeave()
                    }) { Text("Quitter", color = GhostRed, fontWeight = FontWeight.Bold) }
                },
                dismissButton = {
                    TextButton(onClick = { showQuitDialog = false }) { Text("Rester", color = TextPrimary) }
                }
            )
        }
    }
}

@Composable
private fun MatchHeader(state: MatchState, onBack: () -> Unit) {
    val isHiding = state.phase == MatchPhase.HIDING
    val timerColor = if (isHiding) GhostCyan else PacmanYellow

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(NavyIntermediate.copy(alpha = 0.95f))
                .border(1.dp, BorderSubtle, CircleShape)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quitter", tint = TextPrimary)
        }

        Surface(
            shape = RoundedCornerShape(22.dp),
            color = NavyIntermediate.copy(alpha = 0.95f),
            border = BorderStroke(1.5.dp, timerColor)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                when (state.phase) {
                    MatchPhase.HIDING -> {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = GhostCyan, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "CACHE : ${GameViewModel.formatSeconds(state.remainingSeconds)}",
                            color = GhostCyan,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }
                    MatchPhase.LOADING -> {
                        CircularProgressIndicator(color = PacmanYellow, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                    }
                    else -> {
                        PacmanComposable(size = 22.dp, color = PacmanYellow, isAnimated = true)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = GameViewModel.formatSeconds(state.remainingSeconds),
                            color = PacmanYellow,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.5.sp
                        )
                    }
                }
            }
        }

        Surface(
            shape = RoundedCornerShape(20.dp),
            color = NavyIntermediate.copy(alpha = 0.95f),
            border = BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier.height(42.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GhostComposable(size = 18.dp, baseColor = GhostCyan, isScared = false)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${state.foundCount}/${state.totalGhosts}",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun InstructionCard(state: MatchState) {
    if (state.phase == MatchPhase.FINISHED || state.phase == MatchPhase.LOADING) return
    val isPacman = state.role == MatchRole.PACMAN
    val character = state.myCharacter ?: "un fantôme"

    val (title, subtitle) = when {
        isPacman && state.phase == MatchPhase.HIDING ->
            "Tu es Pacman !" to "Ferme les yeux, les fantômes se cachent. La chasse commence à la fin du chrono."
        isPacman && state.isInZone ->
            "Tu es dans la zone !" to "Ton téléphone vibre de plus en plus vite quand tu te rapproches d'un fantôme."
        isPacman && state.zones.isEmpty() ->
            "La chasse est ouverte !" to "En attente de la position des fantômes…"
        isPacman ->
            "Suis les pac-gommes" to "Elles te mènent à la zone où un fantôme se cache."
        state.phase == MatchPhase.HIDING ->
            "Tu es $character !" to "Cache-toi avant la fin du chrono. Ensuite, ne bouge plus !"
        state.iAmFound ->
            "Trouvé !" to "Pacman t'a attrapé. Attends la fin de la partie."
        state.isPacmanNearby ->
            "Pacman approche !" to "Il est tout près… Reste immobile et silencieux."
        else ->
            "Ne bouge plus !" to "Pacman te cherche. Il voit seulement la zone autour de ta cachette."
    }
    val accent = when {
        !isPacman && state.isPacmanNearby -> GhostRed
        isPacman && state.isInZone -> PacmanYellow
        else -> BorderSubtle
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.2.dp, accent, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = NavyIntermediate.copy(alpha = 0.95f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isPacman) {
                PacmanComposable(size = 34.dp, color = PacmanYellow, isAnimated = true)
            } else {
                GhostComposable(
                    size = 34.dp,
                    baseColor = resolveGhostColor(character),
                    isScared = state.isPacmanNearby || state.iAmFound
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(text = subtitle, color = TextSecondary, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun AlertBanner(
    icon: @Composable () -> Unit,
    text: String,
    actionLabel: String? = null,
    onAction: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = NavyIntermediate.copy(alpha = 0.95f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon()
            Spacer(modifier = Modifier.width(10.dp))
            Text(text = text, color = TextPrimary, fontSize = 12.sp, modifier = Modifier.weight(1f))
            if (actionLabel != null) {
                TextButton(onClick = onAction) {
                    Text(actionLabel, color = PacmanYellow, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

/** Jauge "chaud / froid" : se remplit et rougit quand Pacman s'approche d'un fantôme. */
@Composable
private fun ProximityMeter(proximity: Float, source: ProximitySource) {
    val label = when {
        proximity >= 0.8f -> "Brûlant !"
        proximity >= 0.55f -> "Très chaud"
        proximity >= 0.3f -> "Chaud"
        else -> "Tiède"
    }
    val color by animateColorAsState(
        targetValue = if (proximity < 0.5f) lerp(ScaredBlue, PacmanYellow, proximity * 2f)
        else lerp(PacmanYellow, GhostRed, (proximity - 0.5f) * 2f),
        label = "proximityColor"
    )
    val segments = 12
    val lit = (proximity * segments).toInt().coerceIn(1, segments)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.2.dp, color, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = NavyIntermediate.copy(alpha = 0.95f))
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = label, color = color, fontWeight = FontWeight.Black, fontSize = 16.sp, modifier = Modifier.weight(1f))
                if (source == ProximitySource.BLUETOOTH) {
                    Icon(Icons.Default.Bluetooth, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                }
                Text(
                    text = if (source == ProximitySource.BLUETOOTH) "Bluetooth" else "GPS",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(segments) { index ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(10.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (index < lit) color else CardSurface)
                    )
                }
            }
        }
    }
}

@Composable
private fun CatchButton(enabled: Boolean, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Button(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .then(if (enabled) Modifier.shadow(14.dp, RoundedCornerShape(30.dp), spotColor = PacmanYellow) else Modifier),
            shape = RoundedCornerShape(30.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PacmanYellow,
                contentColor = DarkContrast,
                disabledContainerColor = NavyIntermediate.copy(alpha = 0.95f),
                disabledContentColor = TextMuted
            ),
            border = if (enabled) null else BorderStroke(1.dp, BorderSubtle)
        ) {
            Text(text = "TROUVÉ !", fontSize = 20.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
        }
        if (!enabled) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Approche-toi d'un fantôme pour pouvoir l'attraper",
                color = TextSecondary,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun FoundOverlay(character: String?) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepNavy.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            GhostComposable(size = 110.dp, baseColor = resolveGhostColor(character ?: ""), isScared = true)
            Spacer(modifier = Modifier.height(18.dp))
            Text("TROUVÉ !", color = PacmanYellow, fontSize = 34.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "Pacman t'a attrapé.\nAttends la fin de la partie.",
                color = TextSecondary,
                fontSize = 15.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun MatchEndDialog(state: MatchState, onBackHome: () -> Unit) {
    val isPacman = state.role == MatchRole.PACMAN
    val iWon = state.winner != null && (state.winner == MatchRole.PACMAN) == isPacman
    val title = when {
        state.endMessage != null -> "PARTIE ANNULÉE"
        iWon -> "VICTOIRE !"
        else -> "DÉFAITE…"
    }
    val subtitle = state.endMessage ?: when (state.winner) {
        MatchRole.PACMAN -> "Pacman a trouvé tous les fantômes" +
            " avec ${GameViewModel.formatSeconds(state.remainingSeconds)} d'avance !"
        else -> "Le temps est écoulé : ${state.totalGhosts - state.foundCount} fantôme(s) jamais trouvé(s) !"
    }
    val accent = if (iWon) PacmanYellow else if (state.endMessage != null) TextSecondary else GhostRed

    Dialog(onDismissRequest = {}) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = NavyIntermediate),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, accent, RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isPacman) {
                    PacmanComposable(size = 72.dp, color = PacmanYellow, isAnimated = true)
                } else {
                    GhostComposable(
                        size = 72.dp,
                        baseColor = resolveGhostColor(state.myCharacter ?: ""),
                        isScared = !iWon
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
                Text(title, color = accent, fontSize = 28.sp, fontWeight = FontWeight.Black)
                Spacer(modifier = Modifier.height(6.dp))
                Text(subtitle, color = TextSecondary, fontSize = 14.sp, textAlign = TextAlign.Center)

                if (state.ghosts.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(18.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                        state.ghosts.forEach { ghost ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(if (ghost.isMe) SelectedSurface else CardSurface, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                GhostComposable(size = 22.dp, baseColor = resolveGhostColor(ghost.character), isScared = ghost.isFound)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "${ghost.character} · ${ghost.playerName}" + if (ghost.isMe) " (toi)" else "",
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = if (ghost.isFound) "Trouvé" else "Caché",
                                    color = if (ghost.isFound) GhostRed else GhostCyan,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = onBackHome,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PacmanYellow, contentColor = DarkContrast)
                ) {
                    Text("Retour à l'accueil", fontWeight = FontWeight.Black, fontSize = 16.sp)
                }
            }
        }
    }
}
