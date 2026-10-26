package com.cachecache.game.model

/** Partie multijoueur en cours (code Firebase + rôle de ce téléphone). */
data class ActiveMatch(
    val code: String,
    val isHost: Boolean
)

/** L'hôte est Pacman (le chercheur), les autres joueurs sont les fantômes. */
enum class MatchRole { PACMAN, GHOST }

enum class MatchPhase {
    LOADING,  // En attente des infos Firebase
    HIDING,   // Les fantômes se cachent (3 min par défaut)
    SEEKING,  // Pacman cherche (9 min par défaut)
    FINISHED
}

/** D'où vient l'indication de proximité affichée / vibrée chez Pacman. */
enum class ProximitySource { NONE, GPS, BLUETOOTH }

data class MatchGhost(
    val id: String,
    val playerName: String,
    val character: String, // Blinky, Pinky, Inky, Clyde, Sue : choisit la couleur du sprite
    val location: GeoPoint?,
    val isFound: Boolean,
    val isMe: Boolean,
    val skinId: String = "ghost_red_classic",
    val heading: Float = 0f,
    val locationFresh: Boolean = false
)

/** Périmètre (taille d'un bâtiment) dans lequel un fantôme est caché. */
data class GhostZone(
    val ghostId: String,
    val center: GeoPoint,
    val radiusMeters: Double,
    val polygon: List<GeoPoint>
)

data class MatchState(
    val code: String = "",
    val role: MatchRole = MatchRole.GHOST,
    val phase: MatchPhase = MatchPhase.LOADING,
    val remainingSeconds: Int = 0,
    val myLocation: GeoPoint? = null,
    val heading: Float = 0f,
    val pacmanSkin: String = "pacman_classic",
    val gpsAccuracy: Float? = null,
    val error: String? = null,
    val routeAvailable: Boolean = true,
    val stalePlayers: Int = 0,
    val syncingCapture: Boolean = false,
    val isFacingRight: Boolean = true,
    val ghosts: List<MatchGhost> = emptyList(),
    val zones: List<GhostZone> = emptyList(),
    val pacDotsRoute: List<GeoPoint> = emptyList(),
    // --- Pacman ---
    val isInZone: Boolean = false,
    val proximity: Float = 0f, // 0 = loin, 1 = collé au fantôme
    val proximitySource: ProximitySource = ProximitySource.NONE,
    val catchCandidateId: String? = null, // Fantôme assez proche pour appuyer sur "Trouvé !"
    val capturingGhost: HiddenPlayer? = null, // Animation de capture en cours
    // --- Fantôme ---
    val myCharacter: String? = null,
    val isPacmanNearby: Boolean = false,
    val iAmFound: Boolean = false,
    // --- Fin de partie ---
    val winner: MatchRole? = null,
    val endMessage: String? = null,
    // --- Capteurs ---
    val isOffline: Boolean = false, // Pas de réseau : la partie continue, Firebase synchronise au retour
    val hasLocationPermission: Boolean = true,
    val isBluetoothActive: Boolean = false,
    val bluetoothIssue: String? = null
) {
    val me: MatchGhost? get() = ghosts.firstOrNull { it.isMe }
    val foundCount: Int get() = ghosts.count { it.isFound }
}
