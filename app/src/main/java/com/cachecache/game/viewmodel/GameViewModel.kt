package com.cachecache.game.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cachecache.game.data.MockData
import com.cachecache.game.model.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.*

class GameViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(
        GameState(
            seekerLocation = MockData.SeekerStartLocation,
            hiddenPlayers = MockData.HiddenPlayersList,
            suspectZone = MockData.DefaultSuspectZone,
            pacDotsRoute = MockData.PacDotsRouteWaypoints,
            recentHistory = MockData.RecentHistoryList
        )
    )
    val uiState: StateFlow<GameState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var walkSimulationJob: Job? = null

    init {
        // Initial distance calculation
        recalculateDistances(_uiState.value.seekerLocation)
    }

    fun startGame() {
        _uiState.update {
            it.copy(
                isGameRunning = true,
                currentScreen = Screen.GAME,
                gamePhase = GamePhase.FREEZE,
                freezeRemainingSeconds = 30,
                searchRemainingSeconds = 10 * 60,
                seekerRemainingSeconds = 10 * 60
            )
        }
        startTimers()
    }

    fun pauseGame() {
        _uiState.update { it.copy(isGameRunning = false) }
        timerJob?.cancel()
        walkSimulationJob?.cancel()
    }

    fun resumeGame() {
        _uiState.update { it.copy(isGameRunning = true) }
        startTimers()
    }

    private fun startTimers() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _uiState.update { state ->
                    if (!state.isGameRunning) return@update state

                    when (state.gamePhase) {
                        GamePhase.FREEZE -> {
                            val newFreeze = state.freezeRemainingSeconds - 1
                            if (newFreeze <= 0) {
                                state.copy(
                                    gamePhase = GamePhase.SEARCHING,
                                    freezeRemainingSeconds = 0,
                                    searchRemainingSeconds = 10 * 60,
                                    seekerRemainingSeconds = 10 * 60
                                )
                            } else {
                                state.copy(freezeRemainingSeconds = newFreeze)
                            }
                        }
                        GamePhase.SEARCHING -> {
                            val newSearch = state.searchRemainingSeconds - 1
                            if (newSearch <= 0) {
                                state.copy(
                                    gamePhase = GamePhase.DEFEAT,
                                    searchRemainingSeconds = 0,
                                    seekerRemainingSeconds = 0
                                )
                            } else {
                                state.copy(
                                    searchRemainingSeconds = newSearch,
                                    seekerRemainingSeconds = newSearch
                                )
                            }
                        }
                        GamePhase.VICTORY, GamePhase.DEFEAT -> state
                    }
                }
            }
        }
    }

    fun updateSettings(seekerMin: Int, hidingMin: Int) {
        _uiState.update { state ->
            val newSettings = state.settings.copy(
                seekerDurationMinutes = seekerMin,
                hidingDurationMinutes = hidingMin
            )
            state.copy(
                settings = newSettings,
                seekerRemainingSeconds = seekerMin * 60,
                hidingRemainingSeconds = hidingMin * 60,
                showSettingsDialog = false
            )
        }
    }

    fun setScreen(screen: Screen) {
        _uiState.update { it.copy(currentScreen = screen) }
    }

    fun openSettingsDialog(show: Boolean) {
        _uiState.update { it.copy(showSettingsDialog = show) }
    }

    fun openFilterMenu(show: Boolean) {
        _uiState.update { it.copy(showFilterMenu = show) }
    }

    fun setFilter(filter: FilterType) {
        _uiState.update { it.copy(activeFilter = filter, showFilterMenu = false) }
    }

    fun setLeaderboardTab(tab: LeaderboardTab) {
        _uiState.update { it.copy(activeLeaderboardTab = tab) }
    }

    fun selectBadge(badge: BadgeItem?) {
        _uiState.update { it.copy(selectedBadgeDetail = badge) }
    }

    fun dismissProximityAlert() {
        _uiState.update { it.copy(isGhostProximityAlertVisible = false) }
    }

    fun openStreetView() {
        _uiState.update { it.copy(currentScreen = Screen.STREET_VIEW) }
    }

    fun closeStreetView() {
        _uiState.update { it.copy(currentScreen = Screen.GAME) }
    }

    fun dismissCaptureDialog() {
        _uiState.update { it.copy(capturedDialogPlayer = null) }
    }

    fun dismissSuspectZoneModal() {
        _uiState.update { state ->
            state.copy(suspectZone = state.suspectZone?.copy(isModalVisible = false))
        }
    }

    fun onMapTapped(target: GeoPoint) {
        if (_uiState.value.gamePhase == GamePhase.FREEZE) {
            // Player is immobilized during 30s preparation phase
            return
        }
        // Walk towards the tapped coordinate
        walkTowards(target)
    }

    fun onPlayerTapped(player: HiddenPlayer) {
        if (_uiState.value.gamePhase == GamePhase.FREEZE) {
            // Player is immobilized during 30s preparation phase
            return
        }
        // If within 0.30m, capture immediately, otherwise walk directly to him
        val dist = calculateDistanceMeters(_uiState.value.seekerLocation, player.location)
        if (dist <= _uiState.value.settings.captureThresholdMeters) {
            triggerCapture(player)
        } else {
            // Walk smoothly right to the player (stops at 0.15m < 0.30m)
            walkTowards(player.location)
        }
    }

    fun triggerSimulatedCaptureTest(player: HiddenPlayer? = null) {
        val target = player ?: _uiState.value.hiddenPlayers.firstOrNull { !it.isFound } ?: return
        // Teleport to 0.20m (within 30cm) and capture
        val closeLocation = GeoPoint(
            target.location.latitude + 0.000001,
            target.location.longitude + 0.000001
        )
        _uiState.update { it.copy(seekerLocation = closeLocation) }
        recalculateDistances(closeLocation)
        triggerCapture(target)
    }

    fun goToSuspectZone() {
        if (_uiState.value.gamePhase == GamePhase.FREEZE) return
        val zone = _uiState.value.suspectZone ?: return
        _uiState.update { state ->
            state.copy(
                suspectZone = zone.copy(isTargeted = true, isModalVisible = false)
            )
        }
        walkAlongRoute(_uiState.value.pacDotsRoute)
    }

    private fun walkAlongRoute(route: List<GeoPoint>) {
        if (route.isEmpty()) return
        if (_uiState.value.gamePhase == GamePhase.FREEZE) return
        walkSimulationJob?.cancel()
        walkSimulationJob = viewModelScope.launch {
            _uiState.update { it.copy(isSimulationWalking = true) }
            for (wp in route) {
                val start = _uiState.value.seekerLocation
                val dist = calculateDistanceMeters(start, wp)
                val durationMs = (dist * 300).toLong().coerceIn(400L, 2500L)
                val steps = (durationMs / 25L).toInt().coerceAtLeast(10)
                stepTowardsPoint(wp, stepCount = steps, delayMs = 25L)
            }
            _uiState.update { it.copy(isSimulationWalking = false) }
        }
    }

    fun walkTowards(target: GeoPoint) {
        if (_uiState.value.gamePhase == GamePhase.FREEZE) return
        walkSimulationJob?.cancel()
        walkSimulationJob = viewModelScope.launch {
            _uiState.update { it.copy(isSimulationWalking = true) }
            val start = _uiState.value.seekerLocation
            val dist = calculateDistanceMeters(start, target)
            val durationMs = (dist * 350).toLong().coerceIn(600L, 3500L)
            val steps = (durationMs / 25L).toInt().coerceAtLeast(15)
            stepTowardsPoint(target, stepCount = steps, delayMs = 25L)
            _uiState.update { it.copy(isSimulationWalking = false) }
        }
    }

    private suspend fun stepTowardsPoint(target: GeoPoint, stepCount: Int, delayMs: Long) {
        val start = _uiState.value.seekerLocation
        val deltaLat = (target.latitude - start.latitude) / stepCount
        val deltaLon = (target.longitude - start.longitude) / stepCount

        // Calculate heading in degrees
        val heading = (Math.toDegrees(atan2(deltaLon, deltaLat)) + 360) % 360

        // Strict Left / Right orientation based on horizontal displacement
        val newFacingRight = when {
            deltaLon > 0.0000001 -> true
            deltaLon < -0.0000001 -> false
            else -> _uiState.value.isFacingRight
        }

        for (i in 1..stepCount) {
            delay(delayMs)
            val nextLat = start.latitude + deltaLat * i
            val nextLon = start.longitude + deltaLon * i
            val nextLoc = GeoPoint(nextLat, nextLon)

            _uiState.update {
                it.copy(
                    seekerLocation = nextLoc,
                    seekerHeading = heading.toFloat(),
                    isFacingRight = newFacingRight
                )
            }
            recalculateDistances(nextLoc)
        }
    }

    private fun recalculateDistances(seeker: GeoPoint) {
        val threshold = _uiState.value.settings.captureThresholdMeters // 0.30m
        var toCapture: HiddenPlayer? = null

        val updatedPlayers = _uiState.value.hiddenPlayers.map { player ->
            if (player.isFound) return@map player

            val dist = calculateDistanceMeters(seeker, player.location)
            val isScared = dist <= 25.0 // Panic mode if < 25m

            if (dist <= threshold && toCapture == null) {
                toCapture = player.copy(distanceMeters = dist, isScared = true)
            }

            player.copy(
                distanceMeters = dist,
                isScared = isScared
            )
        }

        _uiState.update { it.copy(hiddenPlayers = updatedPlayers) }

        // Trigger capture if reached <= 30cm
        toCapture?.let { player ->
            triggerCapture(player)
        }
    }

    private fun triggerCapture(player: HiddenPlayer) {
        if (_uiState.value.capturingGhost != null) return
        viewModelScope.launch {
            // 1. Enter capture animation state (arcade blue lines dissolution + fleeing eyes)
            _uiState.update { it.copy(capturingGhost = player) }

            // 2. Play absorption sequence for ~1100ms
            delay(1100L)

            // 3. Complete capture, update score & history, and show score dialog
            val updatedPlayers = _uiState.value.hiddenPlayers.map {
                if (it.id == player.id) it.copy(isFound = true, isScared = false) else it
            }

            val addedScore = 50
            val newScore = _uiState.value.userScore + addedScore
            val newXp = _uiState.value.userXp + addedScore
            val newHistoryItem = HistoryItem(
                id = "hist_${System.currentTimeMillis()}",
                date = "Aujourd'hui",
                title = "Trouvé ${player.name} (${player.locationName})",
                deltaScore = addedScore,
                iconType = "ghost"
            )

            val allFound = updatedPlayers.all { it.isFound }
            val nextPhase = if (allFound) GamePhase.VICTORY else GamePhase.SEARCHING

            _uiState.update { state ->
                state.copy(
                    hiddenPlayers = updatedPlayers,
                    gamePhase = nextPhase,
                    userScore = newScore,
                    userXp = if (newXp >= state.maxXp) newXp - state.maxXp else newXp,
                    userLevel = if (newXp >= state.maxXp) state.userLevel + 1 else state.userLevel,
                    capturedDialogPlayer = if (allFound) null else player,
                    capturingGhost = null,
                    recentHistory = listOf(newHistoryItem) + state.recentHistory
                )
            }
        }
    }

    fun restartGame() {
        walkSimulationJob?.cancel()
        val resetPlayers = MockData.HiddenPlayersList.map { it.copy(isFound = false, isScared = false) }
        _uiState.update { state ->
            state.copy(
                isGameRunning = true,
                currentScreen = Screen.GAME,
                gamePhase = GamePhase.FREEZE,
                freezeRemainingSeconds = 30,
                searchRemainingSeconds = 10 * 60,
                seekerRemainingSeconds = 10 * 60,
                hiddenPlayers = resetPlayers,
                capturingGhost = null,
                capturedDialogPlayer = null,
                seekerLocation = GeoPoint(45.6415, 5.8680),
                isFacingRight = true
            )
        }
        recalculateDistances(GeoPoint(45.6415, 5.8680))
        startTimers()
    }

    fun recenterOnCampus() {
        val startLoc = MockData.SeekerStartLocation
        _uiState.update {
            it.copy(
                seekerLocation = startLoc,
                seekerHeading = 45f
            )
        }
        recalculateDistances(startLoc)
    }

    fun equipAvatar(avatarId: String) {
        _uiState.update { it.copy(equippedAvatarId = avatarId) }
    }

    fun unlockAndEquipAvatar(avatar: AvatarItem) {
        _uiState.update { state ->
            if (state.unlockedAvatarIds.contains(avatar.id)) {
                state.copy(equippedAvatarId = avatar.id)
            } else if (state.userScore >= avatar.price) {
                state.copy(
                    userScore = state.userScore - avatar.price,
                    unlockedAvatarIds = state.unlockedAvatarIds + avatar.id,
                    equippedAvatarId = avatar.id
                )
            } else {
                state
            }
        }
    }

    companion object {
        // Haversine formula returning distance in meters
        fun calculateDistanceMeters(p1: GeoPoint, p2: GeoPoint): Double {
            val r = 6371000.0 // Earth radius in meters
            val dLat = Math.toRadians(p2.latitude - p1.latitude)
            val dLon = Math.toRadians(p2.longitude - p1.longitude)
            val lat1 = Math.toRadians(p1.latitude)
            val lat2 = Math.toRadians(p2.latitude)

            val a = sin(dLat / 2).pow(2) + sin(dLon / 2).pow(2) * cos(lat1) * cos(lat2)
            val c = 2 * atan2(sqrt(a), sqrt(1 - a))
            return r * c
        }

        fun formatSeconds(totalSeconds: Int): String {
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return "%02d:%02d".format(minutes, seconds)
        }
    }
}
