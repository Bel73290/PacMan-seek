package com.cachecache.game.model

import androidx.compose.ui.graphics.Color
import com.cachecache.game.ui.theme.*

data class GeoPoint(
    val latitude: Double,
    val longitude: Double
)

data class HiddenPlayer(
    val id: Int,
    val name: String,
    val locationName: String,
    val location: GeoPoint,
    val color: Color,
    val isFound: Boolean = false,
    val isScared: Boolean = false,
    val distanceMeters: Double = 999.0
)

data class SuspectZone(
    val id: String,
    val title: String,
    val description: String,
    val center: GeoPoint,
    val polygon: List<GeoPoint>,
    val hiddenCount: Int = 3,
    val isTargeted: Boolean = false,
    val isModalVisible: Boolean = true
)

data class LeaderboardEntry(
    val rank: Int,
    val name: String,
    val score: Int,
    val isCurrentUser: Boolean = false,
    val avatarColor: Color,
    val badgeType: String? = null // "gold", "silver", "bronze"
)

enum class BadgeShape {
    SHIELD,
    STAR,
    HEXAGON
}

data class BadgeItem(
    val id: String,
    val title: String,
    val description: String,
    val shape: BadgeShape,
    val unlocked: Boolean = true,
    val unlockDate: String
)

data class HistoryItem(
    val id: String,
    val date: String,
    val title: String,
    val deltaScore: Int,
    val iconType: String // "ghost", "ranking", "trophy"
)

enum class FilterType(val label: String) {
    ALL("Tous"),
    NEARBY("Proches (<50m)"),
    SUSPECT_ZONE("Zone suspecte"),
    SCARED("Apeurés")
}

enum class LeaderboardTab(val label: String) {
    GENERAL("Général"),
    FRIENDS("Amis"),
    SEASONS("Saisons")
}

enum class Screen {
    SPLASH,
    GAME,
    STREET_VIEW,
    LEADERBOARD,
    RADAR_FILTERS,
    GUIDE,
    PROFILE
}

data class GameSettings(
    val seekerDurationMinutes: Int = 3,
    val hidingDurationMinutes: Int = 10,
    val captureThresholdMeters: Double = 0.30
)

data class GameState(
    val currentScreen: Screen = Screen.SPLASH,
    val settings: GameSettings = GameSettings(),
    val seekerRemainingSeconds: Int = 2 * 60 + 45, // 02:45
    val hidingRemainingSeconds: Int = 9 * 60 + 32, // 09:32
    val streetViewRemainingSeconds: Int = 2 * 60 + 18, // 02:18
    val isGameRunning: Boolean = false,
    val isSimulationWalking: Boolean = false,
    val seekerLocation: GeoPoint = GeoPoint(45.6415, 5.8680),
    val seekerHeading: Float = 45f,
    val hiddenPlayers: List<HiddenPlayer> = emptyList(),
    val suspectZone: SuspectZone? = null,
    val pacDotsRoute: List<GeoPoint> = emptyList(),
    val activeFilter: FilterType = FilterType.ALL,
    val activeLeaderboardTab: LeaderboardTab = LeaderboardTab.GENERAL,
    val userScore: Int = 1820,
    val userLevel: Int = 5,
    val userXp: Int = 320,
    val maxXp: Int = 500,
    val totalGamesPlayed: Int = 12,
    val totalVictories: Int = 7,
    val isGhostProximityAlertVisible: Boolean = true,
    val isStreetViewMiniVisible: Boolean = true,
    val isStreetViewRadarOpen: Boolean = true,
    val capturedDialogPlayer: HiddenPlayer? = null,
    val selectedBadgeDetail: BadgeItem? = null,
    val showSettingsDialog: Boolean = false,
    val showFilterMenu: Boolean = false,
    val recentHistory: List<HistoryItem> = emptyList()
)
