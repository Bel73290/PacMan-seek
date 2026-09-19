package com.cachecache.game.model

import androidx.compose.ui.graphics.Color
import com.cachecache.game.ui.theme.*

data class GeoPoint(
    val latitude: Double,
    val longitude: Double
)

fun resolveGhostColor(name: String, defaultColor: Color = GhostRed): Color {
    return when (name.trim().lowercase()) {
        "blinky" -> GhostRed
        "pinky" -> GhostPink
        "inky" -> GhostCyan
        "clyde" -> GhostOrange
        "sue" -> GhostPurple
        else -> defaultColor
    }
}

data class HiddenPlayer(
    val id: Int,
    val name: String,
    val locationName: String,
    val location: GeoPoint,
    val color: Color,
    val isFound: Boolean = false,
    val isScared: Boolean = false,
    val distanceMeters: Double = 999.0
) {
    val actualColor: Color get() = resolveGhostColor(name, color)
}

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
    FRIENDS("Amis")
}

enum class Screen {
    SPLASH,
    GAME,
    STREET_VIEW,
    LEADERBOARD,
    RADAR_FILTERS,
    GUIDE,
    PROFILE,
    SHOP
}

enum class AvatarCategory(val label: String) {
    CLASSIC("Classiques"),
    PREMIUM("✨ Premium")
}

data class AvatarItem(
    val id: String,
    val name: String,
    val category: AvatarCategory,
    val description: String,
    val price: Int = 0, // 0 = gratuit
    val ghostColor: Color? = null,
    val isAnimatedPacman: Boolean = false,
    val drawableResId: Int? = null,
    val isPremium: Boolean = false
)

enum class GamePhase {
    FREEZE,      // 30s preparation, player immobilized
    SEARCHING,   // 10min hunt timer, player seeks ghosts
    VICTORY,     // All ghosts caught
    DEFEAT       // 10min expired with ghosts remaining
}

data class GameSettings(
    val freezeDurationSeconds: Int = 30,
    val huntDurationMinutes: Int = 10,
    val captureThresholdMeters: Double = 0.30,
    val seekerDurationMinutes: Int = 10,
    val hidingDurationMinutes: Int = 10
)

data class GameState(
    val currentScreen: Screen = Screen.SPLASH,
    val settings: GameSettings = GameSettings(),
    val gamePhase: GamePhase = GamePhase.FREEZE,
    val freezeRemainingSeconds: Int = 30, // 30s freeze phase
    val searchRemainingSeconds: Int = 10 * 60, // 10 min search phase
    val seekerRemainingSeconds: Int = 10 * 60, // Kept for compatibility
    val hidingRemainingSeconds: Int = 10 * 60,
    val streetViewRemainingSeconds: Int = 2 * 60 + 18, // 02:18
    val isGameRunning: Boolean = false,
    val isSimulationWalking: Boolean = false,
    val seekerLocation: GeoPoint = GeoPoint(45.6415, 5.8680),
    val seekerHeading: Float = 45f,
    val isFacingRight: Boolean = true,
    val hiddenPlayers: List<HiddenPlayer> = emptyList(),
    val suspectZone: SuspectZone? = null,
    val pacDotsRoute: List<GeoPoint> = emptyList(),
    val activeFilter: FilterType = FilterType.ALL,
    val activeLeaderboardTab: LeaderboardTab = LeaderboardTab.GENERAL,
    val userScore: Int = 1_000_000,
    val userLevel: Int = 5,
    val userXp: Int = 320,
    val maxXp: Int = 500,
    val totalGamesPlayed: Int = 12,
    val totalVictories: Int = 7,
    val equippedAvatarId: String = "ghost_red_classic",
    val unlockedAvatarIds: Set<String> = setOf("ghost_red_classic", "ghost_pink_classic"),
    val isGhostProximityAlertVisible: Boolean = true,
    val isStreetViewMiniVisible: Boolean = true,
    val isStreetViewRadarOpen: Boolean = true,
    val capturingGhost: HiddenPlayer? = null,
    val capturedDialogPlayer: HiddenPlayer? = null,
    val selectedBadgeDetail: BadgeItem? = null,
    val showSettingsDialog: Boolean = false,
    val showFilterMenu: Boolean = false,
    val recentHistory: List<HistoryItem> = emptyList()
)
