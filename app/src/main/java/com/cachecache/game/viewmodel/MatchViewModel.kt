package com.cachecache.game.viewmodel

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.os.Build
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cachecache.game.data.BleProximity
import com.cachecache.game.data.FootRouteService
import com.cachecache.game.data.GameRepository
import com.cachecache.game.data.GeoUtils
import com.cachecache.game.data.LocationTracker
import com.cachecache.game.model.*
import com.cachecache.game.viewmodel.GameViewModel.Companion.calculateDistanceMeters
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * Partie de cache-cache en temps réel.
 *  - HIDING : les fantômes ont quelques minutes pour se cacher, Pacman attend.
 *  - SEEKING : Pacman voit une zone (taille d'un bâtiment) par fantôme, des pac-gommes le guident
 *    jusqu'à la plus proche ; dans la zone, son téléphone vibre de plus en plus vite en approchant
 *    (GPS de loin, Bluetooth de près). Assez proche, il peut appuyer sur "Trouvé !".
 *
 * Le chrono continue hors ligne. Les captures et résultats attendent une confirmation serveur.
 * Le Bluetooth est uniquement un indice de proximité ; les positions anciennes sont signalées.
 */
class MatchViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "MatchViewModel"
        private val GHOST_CHARACTERS = listOf("Blinky", "Pinky", "Inky", "Clyde", "Sue")

        // Proximité par GPS (précision de quelques mètres en extérieur)
        private const val GPS_PROXIMITY_RANGE_METERS = 40.0
        private const val GPS_CATCH_DISTANCE_METERS = 10.0
        private const val ZONE_ENTRY_MARGIN_METERS = 5.0

        // Proximité par Bluetooth (RSSI en dBm, plus c'est haut plus c'est proche)
        private const val BLE_RSSI_FAR = -95.0
        private const val BLE_RSSI_NEAR = -55.0
        private const val BLE_CATCH_RSSI = -70.0 // ≈ 2 à 4 m selon les téléphones
        private const val BLE_FRESH_MS = 4_000L
        private const val BLE_SCAN_RETRY_MS = 10_000L
        private const val BLE_PACMAN_NEARBY_RSSI = -80.0 // ≈ une dizaine de mètres

        // Envoi de la position à Firebase (limite le nombre d'écritures)
        private const val UPLOAD_MIN_MOVE_METERS = 3.0
        private const val UPLOAD_MAX_INTERVAL_MS = 15_000L

        private const val PACMAN_NEARBY_METERS = 30.0
        private const val ROUTE_REFRESH_MOVE_METERS = 40.0
        private const val ROUTE_REFRESH_MS = 30_000L

        /** Permissions demandées à l'entrée dans la partie. */
        fun requiredPermissions(): Array<String> {
            val permissions = mutableListOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                permissions += listOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_ADVERTISE,
                    Manifest.permission.BLUETOOTH_CONNECT
                )
            }
            return permissions.toTypedArray()
        }
    }

    private data class BleReading(val rssi: Double, val seenAt: Long)
    private data class RouteRequest(val targetGhostId: String, val from: GeoPoint, val at: Long)
    private data class Candidate(val ghost: MatchGhost, val gpsDistance: Double?, val bleRssi: Double?)

    private val repository = GameRepository()
    private val locationTracker = LocationTracker(application)
    private val ble = BleProximity(application)
    private val vibrator: Vibrator =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            application.getSystemService(VibratorManager::class.java).defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            application.getSystemService(Vibrator::class.java)
        }

    private val connectivity = application.getSystemService(ConnectivityManager::class.java)
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            _state.update { it.copy(isOffline = false) }
        }

        override fun onLost(network: Network) {
            _state.update { it.copy(isOffline = true) }
        }
    }
    private var networkCallbackRegistered = false

    private val _state = MutableStateFlow(MatchState())
    val state: StateFlow<MatchState> = _state.asStateFlow()

    private var sessionJob: Job? = null
    private var locationJob: Job? = null
    private var scanJob: Job? = null
    private var routeJob: Job? = null

    private var myUid: String? = null
    private var game: GameRepository.GameSnapshot? = null
    private var players: List<GameRepository.PlayerSnapshot> = emptyList()
    private val zonesByGhost = mutableMapOf<String, GhostZone>()
    private val bleReadings = mutableMapOf<String, BleReading>()
    private var lastUpload: Pair<GeoPoint, Long>? = null
    private var serverOffsetMs = 0L
    private var lastGpsTime = 0L
    private var uploadJob: Job? = null
    private var lastScanStart = 0L
    private var advertiseFailed = false
    private var routeRequest: RouteRequest? = null
    private var currentRoute: List<GeoPoint> = emptyList()
    private var finishRequested = false
    private var foundFeedbackDone = false
    private var foundByBluetooth = false // Le fantôme a capté la balise de Pacman annonçant sa capture
    private var pacmanReading: BleReading? = null

    // ---- Cycle de vie ----

    fun start(match: ActiveMatch) {
        if (_state.value.code == match.code && sessionJob?.isActive == true) return
        stopSession()
        _state.value = MatchState(
            code = match.code,
            role = if (match.isHost) MatchRole.PACMAN else MatchRole.GHOST,
            hasLocationPermission = hasLocationPermission(),
            isOffline = connectivity?.activeNetwork == null
        )
        registerNetworkCallback()
        sessionJob = viewModelScope.launch {
            myUid = try {
                repository.myUserId()
            } catch (e: Exception) {
                Log.w(TAG, "Identifiant joueur indisponible", e)
                null
            }
            try {
                val before = System.currentTimeMillis()
                val server = repository.serverTime()
                serverOffsetMs = server - (before + System.currentTimeMillis()) / 2
            } catch (e: Exception) { report(e) }
            launch {
                repository.observeGame(match.code)
                    .catch { report(it) }
                    .collect {
                        if (it == null) _state.update { state -> state.copy(phase=MatchPhase.FINISHED, endMessage="Cette partie n’existe plus.") }
                        game = it; refresh()
                    }
            }
            launch {
                repository.observeMatchPlayers(match.code)
                    .catch { report(it) }
                    .collect { players = it; refresh() }
            }
            launch {
                while (isActive) {
                    refresh()
                    ensureBluetooth()
                    delay(1_000L)
                }
            }
            launch { vibrationLoop() }
        }
        startLocationIfAllowed()
    }

    /** Quitte la partie ; si Pacman part avant la fin, la partie est annulée pour tous. */
    fun leave() {
        val current = _state.value
        stopSession()
        _state.value = MatchState()
        if (current.code.isNotEmpty() && current.phase != MatchPhase.FINISHED) {
            viewModelScope.launch {
                try {
                    repository.leaveGame(current.code, isHost = current.role == MatchRole.PACMAN)
                } catch (e: Exception) {
                    Log.w(TAG, "Départ non enregistré", e)
                }
            }
        }
    }

    fun onPermissionsResult() {
        val current = _state.value
        if (current.error != null && current.code.isNotEmpty()) {
            val match = ActiveMatch(current.code,current.role==MatchRole.PACMAN)
            stopSession()
            start(match)
            return
        }
        _state.update { it.copy(hasLocationPermission = hasLocationPermission()) }
        startLocationIfAllowed()
        ensureBluetooth()
    }

    override fun onCleared() {
        stopSession()
        super.onCleared()
    }

    private fun stopSession() {
        sessionJob?.cancel()
        locationJob?.cancel()
        scanJob?.cancel()
        routeJob?.cancel()
        uploadJob?.cancel()
        sessionJob = null
        unregisterNetworkCallback()
        ble.stopAdvertising()
        vibrator.cancel()
        game = null
        players = emptyList()
        zonesByGhost.clear()
        bleReadings.clear()
        lastUpload = null
        advertiseFailed = false
        routeRequest = null
        currentRoute = emptyList()
        finishRequested = false
        foundFeedbackDone = false
        foundByBluetooth = false
        pacmanReading = null
    }

    private fun registerNetworkCallback() {
        if (networkCallbackRegistered || connectivity == null) return
        try {
            connectivity.registerDefaultNetworkCallback(networkCallback)
            networkCallbackRegistered = true
        } catch (e: Exception) {
            Log.w(TAG, "Suivi du réseau indisponible", e)
        }
    }

    private fun unregisterNetworkCallback() {
        if (!networkCallbackRegistered) return
        networkCallbackRegistered = false
        try {
            connectivity?.unregisterNetworkCallback(networkCallback)
        } catch (_: Exception) {
            // Déjà désinscrit
        }
    }

    // ---- Actions de Pacman ----

    fun catchGhost() {
        val current = _state.value
        if (current.role != MatchRole.PACMAN || current.phase != MatchPhase.SEEKING) return
        if (current.capturingGhost != null) return
        val ghost = current.ghosts.firstOrNull { it.id == current.catchCandidateId } ?: return

        if (current.isOffline || current.syncingCapture) return
        _state.update { it.copy(syncingCapture = true, error = null) }
        viewModelScope.launch {
            try {
                repository.markGhostFound(current.code, ghost.id)
                _state.update { it.copy(capturingGhost = HiddenPlayer(
                    id=ghost.id.hashCode(), name=ghost.character, locationName="",
                    location=ghost.location ?: current.myLocation ?: GeoPoint(0.0,0.0),
                    color=resolveGhostColor(ghost.character), isFound=true, skinId=ghost.skinId
                )) }
                vibrate(350L,255)
                delay(1100L)
                _state.update { it.copy(capturingGhost=null) }
            } catch (e: Exception) { report(e) }
            finally { _state.update { it.copy(syncingCapture=false) } }
        }
    }

    private fun report(error: Throwable) {
        if (error is CancellationException) throw error
        Log.w(TAG,"Synchronisation",error)
        _state.update { it.copy(error=error.message ?: "Synchronisation impossible. Réessaie avec du réseau.") }
    }

    // ---- Position GPS ----

    private fun startLocationIfAllowed() {
        if (sessionJob == null || locationJob?.isActive == true || !hasLocationPermission()) return
        locationJob = viewModelScope.launch {
            locationTracker.updates()
                .catch { report(it) }
                .collect { location ->
                    if (android.os.SystemClock.elapsedRealtimeNanos() - location.elapsedRealtimeNanos <= 15_000_000_000L) onLocation(location)
                }
        }
    }

    private fun onLocation(location: android.location.Location) {
        val point=GeoPoint(location.latitude,location.longitude)
        val previous=_state.value.myLocation
        val moved=previous != null && calculateDistanceMeters(previous,point)>=3
        val heading=when {
            location.hasBearing() && location.hasSpeed() && location.speed>=0.5f -> ((location.bearing%360)+360)%360
            moved -> Direction.bearing(previous!!,point)
            else -> _state.value.heading
        }
        lastGpsTime=SystemClock.elapsedRealtime()
        _state.update { it.copy(myLocation=point, heading=heading, gpsAccuracy=location.accuracy,
            isFacingRight=Direction.facesRight(heading,it.isFacingRight)) }
        maybeUploadLocation(point,location.accuracy,heading,location.time+serverOffsetMs)
        refresh()
    }

    private fun maybeUploadLocation(point: GeoPoint, accuracy: Float, heading: Float, measuredAt: Long) {
        val current=_state.value
        if (current.code.isEmpty() || current.phase==MatchPhase.FINISHED || current.iAmFound || current.isOffline || game?.status!=GameRepository.STATUS_STARTED) return
        if (uploadJob?.isActive==true) return
        val now=SystemClock.elapsedRealtime(); val last=lastUpload
        if (last!=null && now-last.second<3_000L) return
        if (last!=null && calculateDistanceMeters(last.first,point)<UPLOAD_MIN_MOVE_METERS && now-last.second<UPLOAD_MAX_INTERVAL_MS && kotlin.math.abs(current.heading-heading)<30) return
        uploadJob=viewModelScope.launch {
            try { repository.updateMyLocation(current.code,point,accuracy,heading,measuredAt); lastUpload=point to now }
            catch(e:Exception) { report(e) }
        }
    }

    // ---- Calcul de l'état de la partie ----

    private fun refresh() {
        val current = _state.value
        val code = current.code
        val game = game ?: return
        if (code.isEmpty()) return
        val uid = myUid

        val ghosts = players
            .filter { !it.isHost && it.id != game.hostId && it.active }
            .mapIndexed { index, player ->
                MatchGhost(
                    id = player.id,
                    playerName = player.name,
                    character = GHOST_CHARACTERS[index % GHOST_CHARACTERS.size],
                    location = player.location,
                    isFound = player.id in game.foundGhostIds,
                    isMe = player.id == uid,
                    skinId = player.skinId,
                    heading = player.heading,
                    locationFresh = System.currentTimeMillis()+serverOffsetMs-player.locationUpdatedAt in 0..30_000 && player.accuracy<=25
                )
            }
        val pacmanLocation = players.firstOrNull { it.id == game.hostId }?.location

        // Chrono commun : tous les téléphones partent de l'heure de lancement enregistrée par Firebase
        _state.update { it.copy(pacmanSkin=players.firstOrNull { p -> p.id==game.hostId }?.pacmanSkin ?: "pacman_classic",
            stalePlayers=ghosts.count { g -> !g.isFound && !g.locationFresh }) }
        val now = System.currentTimeMillis()+serverOffsetMs
        val elapsed = (((now - (game.startedAtMillis ?: now)) / 1000L).toInt()).coerceAtLeast(0)
        val hidingEnd = game.hidingSeconds
        val seekingEnd = game.hidingSeconds + game.seekingSeconds
        val allFound = ghosts.isNotEmpty() && ghosts.all { it.isFound }

        var winner: MatchRole? = null
        var endMessage: String? = null
        val phase: MatchPhase
        var remaining: Int
        when {
            game.status == GameRepository.STATUS_CANCELLED -> {
                phase = MatchPhase.FINISHED
                remaining = 0
                endMessage = "Pacman a quitté la partie."
            }
            game.status == GameRepository.STATUS_FINISHED -> {
                phase = MatchPhase.FINISHED
                remaining = (seekingEnd - elapsed).coerceAtLeast(0)
                winner = if (game.winner == GameRepository.WINNER_PACMAN) MatchRole.PACMAN else MatchRole.GHOST
            }
            allFound && elapsed >= hidingEnd -> {
                phase = MatchPhase.FINISHED
                remaining = seekingEnd - elapsed
                winner = MatchRole.PACMAN
            }
            elapsed < hidingEnd -> {
                phase = MatchPhase.HIDING
                remaining = hidingEnd - elapsed
            }
            elapsed < seekingEnd -> {
                phase = MatchPhase.SEEKING
                remaining = seekingEnd - elapsed
            }
            else -> {
                phase = MatchPhase.FINISHED
                remaining = 0
                winner = MatchRole.GHOST
            }
        }
        // Le temps restant affiché en fin de partie ne doit plus bouger
        if (current.phase == MatchPhase.FINISHED && phase == MatchPhase.FINISHED) remaining = current.remainingSeconds

        // Pacman (hôte) enregistre la fin de partie pour tout le monde
        if (phase == MatchPhase.FINISHED && winner != null &&
            game.status == GameRepository.STATUS_STARTED && !finishRequested && !current.isOffline
        ) {
            finishRequested = true
            viewModelScope.launch {
                try { repository.finishGame(code, if (winner == MatchRole.PACMAN) GameRepository.WINNER_PACMAN else GameRepository.WINNER_GHOSTS) }
                catch (e: Exception) { report(e); delay(5_000L); finishRequested=false }
            }
        }

        val confirmed = game.status == GameRepository.STATUS_FINISHED || game.status == GameRepository.STATUS_CANCELLED
        val displayedPhase = if (phase == MatchPhase.FINISHED && !confirmed) MatchPhase.SEEKING else phase
        val displayedWinner = if (confirmed) winner else null
        if (current.role == MatchRole.PACMAN) {
            refreshPacman(current, ghosts, displayedPhase, remaining.coerceAtLeast(0), elapsed >= hidingEnd, displayedWinner, endMessage)
        } else {
            refreshGhost(current, ghosts, pacmanLocation, displayedPhase, remaining.coerceAtLeast(0), displayedWinner, endMessage)
        }
    }

    private fun refreshPacman(
        current: MatchState,
        ghosts: List<MatchGhost>,
        phase: MatchPhase,
        remaining: Int,
        hidingOver: Boolean,
        winner: MatchRole?,
        endMessage: String?
    ) {
        // Les zones sont figées à la fin du temps de cache, à partir de la position des fantômes
        if (hidingOver) {
            ghosts.forEach { ghost ->
                if (ghost.id !in zonesByGhost && ghost.location != null) {
                    zonesByGhost[ghost.id] = GeoUtils.buildZone(current.code, ghost.id, ghost.location)
                }
            }
        }

        val myLocation = current.myLocation
        val unfound = ghosts.filter { !it.isFound }
        val activeZones = if (phase == MatchPhase.SEEKING) unfound.mapNotNull { zonesByGhost[it.id] } else emptyList()

        var isInZone = false
        var proximity = 0f
        var source = ProximitySource.NONE
        var catchCandidate: String? = null
        var route = emptyList<GeoPoint>()

        if (phase == MatchPhase.SEEKING && myLocation != null) {
            isInZone = activeZones.any {
                calculateDistanceMeters(myLocation, it.center) <= it.radiusMeters + ZONE_ENTRY_MARGIN_METERS
            }

            val nowMs = SystemClock.elapsedRealtime()
            val candidates = unfound.filter { it.locationFresh }.map { ghost ->
                Candidate(
                    ghost = ghost,
                    gpsDistance = ghost.location?.let { calculateDistanceMeters(myLocation, it) },
                    bleRssi = bleReadings[BleProximity.shortId(ghost.id)]
                        ?.takeIf { nowMs - it.seenAt <= BLE_FRESH_MS }?.rssi
                )
            }
            val best = candidates.maxByOrNull { proximityOf(it) }
            val bestProximity = best?.let { proximityOf(it) } ?: 0f
            val heardByBluetooth = best?.bleRssi != null

            // Le radar (et la vibration) ne s'active que dans une zone, ou si le Bluetooth capte un fantôme
            if (best != null && bestProximity > 0f && (isInZone || heardByBluetooth)) {
                proximity = bestProximity
                source = if (heardByBluetooth) ProximitySource.BLUETOOTH else ProximitySource.GPS
            }
            if (best != null) {
                val closeByBluetooth = (best.bleRssi ?: Double.NEGATIVE_INFINITY) >= BLE_CATCH_RSSI
                val closeByGps = (best.gpsDistance ?: Double.MAX_VALUE) <= GPS_CATCH_DISTANCE_METERS
                if (closeByGps && !current.isOffline && SystemClock.elapsedRealtime()-lastGpsTime<15_000L && (current.gpsAccuracy ?: 1000f)<=25f) catchCandidate = best.ghost.id
            }

            // Pac-gommes vers la zone la plus proche, tant qu'on n'est pas dedans
            if (!isInZone) {
                activeZones.minByOrNull { calculateDistanceMeters(myLocation, it.center) }?.let { target ->
                    route = routeTowards(myLocation, target)
                }
            }
        }

        _state.update {
            it.copy(
                phase = phase,
                remainingSeconds = remaining,
                ghosts = ghosts,
                zones = activeZones,
                pacDotsRoute = route,
                isInZone = isInZone,
                proximity = proximity,
                proximitySource = source,
                catchCandidateId = catchCandidate,
                winner = winner,
                endMessage = endMessage
            )
        }
    }

    private fun refreshGhost(
        current: MatchState,
        ghosts: List<MatchGhost>,
        pacmanLocation: GeoPoint?,
        phase: MatchPhase,
        remaining: Int,
        winner: MatchRole?,
        endMessage: String?
    ) {
        val me = ghosts.firstOrNull { it.isMe }
        val iAmFound = me?.isFound == true
        val myLocation = current.myLocation
        val nearbyByGps = myLocation != null && pacmanLocation != null &&
            calculateDistanceMeters(myLocation, pacmanLocation) <= PACMAN_NEARBY_METERS
        val nearbyByBluetooth = pacmanReading?.let {
            SystemClock.elapsedRealtime() - it.seenAt <= BLE_FRESH_MS && it.rssi >= BLE_PACMAN_NEARBY_RSSI
        } == true
        val pacmanNearby = phase == MatchPhase.SEEKING && !iAmFound && (nearbyByGps || nearbyByBluetooth)

        if (iAmFound && !foundFeedbackDone) {
            foundFeedbackDone = true
            vibrate(600L, 255)
        }

        _state.update {
            it.copy(
                phase = phase,
                remainingSeconds = remaining,
                // Capture apprise par Bluetooth avant que Firebase ne la synchronise
                ghosts = if (foundByBluetooth) ghosts.map { g -> if (g.isMe) g.copy(isFound = true) else g } else ghosts,
                myCharacter = me?.character,
                isPacmanNearby = pacmanNearby,
                iAmFound = iAmFound,
                winner = winner,
                endMessage = endMessage
            )
        }
    }

    private fun proximityOf(candidate: Candidate): Float {
        candidate.bleRssi?.let { rssi ->
            return ((rssi - BLE_RSSI_FAR) / (BLE_RSSI_NEAR - BLE_RSSI_FAR)).toFloat().coerceIn(0.05f, 1f)
        }
        val distance = candidate.gpsDistance ?: return 0f
        return (1.0 - distance / GPS_PROXIMITY_RANGE_METERS).toFloat().coerceIn(0f, 1f)
    }

    /** Chemin à pied (OpenStreetMap) vers la zone ; ligne droite en attendant ou si le service ne répond pas. */
    private fun routeTowards(from: GeoPoint, zone: GhostZone): List<GeoPoint> {
        val request = routeRequest
        val now = SystemClock.elapsedRealtime()
        val targetChanged = request == null || request.targetGhostId != zone.ghostId
        if (targetChanged) currentRoute = emptyList()

        val needsRefresh = targetChanged ||
            calculateDistanceMeters(request!!.from, from) > ROUTE_REFRESH_MOVE_METERS ||
            now - request.at > ROUTE_REFRESH_MS
        if (needsRefresh && routeJob?.isActive != true) {
            routeRequest = RouteRequest(zone.ghostId, from, now)
            routeJob = viewModelScope.launch {
                val path = FootRouteService.route(from, zone.center)
                currentRoute = path.orEmpty()
                _state.update { it.copy(routeAvailable=path!=null) }
                refresh()
            }
        }
        if(currentRoute.size<2) return emptyList()
        val nearestIndex = currentRoute.indices.minByOrNull { calculateDistanceMeters(from, currentRoute[it]) } ?: 0
        return currentRoute.drop(nearestIndex)
    }

    // ---- Bluetooth ----

    private fun ensureBluetooth() {
        val current = _state.value
        val isGhost = current.role == MatchRole.GHOST
        // Pacman émet la liste des fantômes trouvés et écoute les fantômes ;
        // un fantôme émet sa balise et écoute Pacman, jusqu'à ce qu'il soit trouvé
        val wantsBluetooth = current.phase == MatchPhase.SEEKING && !(isGhost && current.iAmFound)

        if (!wantsBluetooth) {
            stopBluetooth()
            _state.update { it.copy(isBluetoothActive = false, bluetoothIssue = null) }
            return
        }

        val issue = when {
            !ble.isSupported -> "Pas de Bluetooth : radar par GPS uniquement."
            !hasBluetoothPermission() -> "Autorise le Bluetooth pour un radar plus précis."
            !ble.isEnabled -> "Active le Bluetooth pour un radar plus précis."
            isGhost && advertiseFailed -> "Ce téléphone ne peut pas émettre en Bluetooth : Pacman te trouvera par GPS."
            else -> null
        }
        if (issue != null) {
            stopBluetooth()
            _state.update { it.copy(isBluetoothActive = false, bluetoothIssue = issue) }
            return
        }

        val uid = myUid ?: return
        val beacon = if (isGhost) {
            BleProximity.ghostBeacon(uid)
        } else {
            BleProximity.pacmanBeacon(current.ghosts.filter { it.isFound }.map { it.id })
        }
        val advertising = ble.advertise(beacon) { if (isGhost) advertiseFailed = true }
        if (!advertising && isGhost) advertiseFailed = true

        if (scanJob?.isActive != true) {
            val now = SystemClock.elapsedRealtime()
            // Android bloque les applis qui relancent le scan trop souvent
            if (now - lastScanStart >= BLE_SCAN_RETRY_MS) {
                lastScanStart = now
                val magic = if (isGhost) BleProximity.MAGIC_PACMAN else BleProximity.MAGIC_GHOST
                scanJob = viewModelScope.launch {
                    ble.scan(magic)
                        .catch { Log.w(TAG, "Scan Bluetooth interrompu", it) }
                        .collect { sighting -> if (isGhost) onPacmanBeacon(sighting) else onGhostBeacon(sighting) }
                }
            }
        }

        val active = if (isGhost) ble.isAdvertising else scanJob?.isActive == true
        _state.update { it.copy(isBluetoothActive = active, bluetoothIssue = null) }
    }

    /** Pacman capte un fantôme : on lisse le RSSI, qui varie beaucoup d'une mesure à l'autre. */
    private fun onGhostBeacon(sighting: BleProximity.Sighting) {
        val now = SystemClock.elapsedRealtime()
        val previous = bleReadings[sighting.payloadHex]?.takeIf { now - it.seenAt <= BLE_FRESH_MS }
        bleReadings[sighting.payloadHex] = BleReading(smoothRssi(previous, sighting.rssi), now)
    }

    /** Un fantôme capte Pacman : proximité, et capture annoncée même sans réseau. */
    private fun onPacmanBeacon(sighting: BleProximity.Sighting) {
        val now = SystemClock.elapsedRealtime()
        val previous = pacmanReading?.takeIf { now - it.seenAt <= BLE_FRESH_MS }
        pacmanReading = BleReading(smoothRssi(previous, sighting.rssi), now)

        val uid = myUid ?: return
        if (!foundByBluetooth && BleProximity.shortId(uid) in BleProximity.shortIdsIn(sighting.payloadHex)) {
            foundByBluetooth = true
            refresh()
        }
    }

    private fun smoothRssi(previous: BleReading?, rssi: Int): Double =
        previous?.let { it.rssi * 0.7 + rssi * 0.3 } ?: rssi.toDouble()

    private fun stopBluetooth() {
        scanJob?.cancel()
        scanJob = null
        ble.stopAdvertising()
    }

    // ---- Vibrations ----

    /** Plus Pacman est proche d'un fantôme, plus les vibrations sont rapprochées et fortes. */
    private suspend fun vibrationLoop() {
        while (true) {
            val current = _state.value
            if (current.role == MatchRole.PACMAN && current.phase == MatchPhase.SEEKING &&
                current.proximity > 0f && current.capturingGhost == null
            ) {
                val p = current.proximity.coerceIn(0f, 1f)
                vibrate(durationMs = (40 + 90 * p).toLong(), amplitude = (70 + 185 * p).toInt())
                delay((1_600 - 1_400 * p).toLong()) // 1,6 s loin → 0,2 s collé
            } else {
                delay(300L)
            }
        }
    }

    private fun vibrate(durationMs: Long, amplitude: Int) {
        if (!vibrator.hasVibrator()) return
        val effectiveAmplitude =
            if (vibrator.hasAmplitudeControl()) amplitude.coerceIn(1, 255) else VibrationEffect.DEFAULT_AMPLITUDE
        vibrator.vibrate(VibrationEffect.createOneShot(durationMs, effectiveAmplitude))
    }

    // ---- Permissions ----

    private fun isGranted(permission: String) =
        ContextCompat.checkSelfPermission(getApplication(), permission) == PackageManager.PERMISSION_GRANTED

    private fun hasLocationPermission() =
        isGranted(Manifest.permission.ACCESS_FINE_LOCATION) || isGranted(Manifest.permission.ACCESS_COARSE_LOCATION)

    private fun hasBluetoothPermission(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            isGranted(Manifest.permission.BLUETOOTH_SCAN) &&
                isGranted(Manifest.permission.BLUETOOTH_ADVERTISE) &&
                isGranted(Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            isGranted(Manifest.permission.ACCESS_FINE_LOCATION)
        }
}
