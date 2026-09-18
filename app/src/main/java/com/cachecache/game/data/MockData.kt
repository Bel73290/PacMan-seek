package com.cachecache.game.data

import androidx.compose.ui.graphics.Color
import com.cachecache.game.model.*
import com.cachecache.game.ui.theme.*

object MockData {

    // Savoie Technolac (USMB Le Bourget-du-Lac) Coordinates
    val SeekerStartLocation = GeoPoint(45.6410, 5.8672)

    val HiddenPlayersList = listOf(
        HiddenPlayer(
            id = 1,
            name = "Inky",
            locationName = "Polytech Annecy-Chambéry",
            location = GeoPoint(45.6420, 5.8690),
            color = GhostRed,
            isFound = false,
            isScared = false
        ),
        HiddenPlayer(
            id = 2,
            name = "Pinky",
            locationName = "BU Bourget - Savoie Technolac",
            location = GeoPoint(45.6428, 5.8682),
            color = GhostPink,
            isFound = false,
            isScared = false
        ),
        HiddenPlayer(
            id = 3,
            name = "Clyde",
            locationName = "Bâtiment Éveillon",
            location = GeoPoint(45.6402, 5.8680),
            color = GhostOrange,
            isFound = false,
            isScared = false
        ),
        HiddenPlayer(
            id = 4,
            name = "Blinky",
            locationName = "RU La Boussole",
            location = GeoPoint(45.6434, 5.8698),
            color = GhostCyan,
            isFound = false,
            isScared = false
        ),
        HiddenPlayer(
            id = 5,
            name = "Sue",
            locationName = "Hall des Sports USMB",
            location = GeoPoint(45.6395, 5.8705),
            color = GhostPurple,
            isFound = false,
            isScared = false
        )
    )

    val DefaultSuspectZone = SuspectZone(
        id = "zone_technolac_nord",
        title = "Zone suspecte",
        description = "3 personnes se cachent probablement ici.",
        center = GeoPoint(45.6424, 5.8688),
        polygon = listOf(
            GeoPoint(45.6415, 5.8678),
            GeoPoint(45.6432, 5.8676),
            GeoPoint(45.6435, 5.8702),
            GeoPoint(45.6418, 5.8706)
        ),
        hiddenCount = 3,
        isTargeted = false,
        isModalVisible = true
    )

    val PacDotsRouteWaypoints = listOf(
        GeoPoint(45.6410, 5.8672),
        GeoPoint(45.6413, 5.8676),
        GeoPoint(45.6416, 5.8680),
        GeoPoint(45.6419, 5.8683),
        GeoPoint(45.6421, 5.8685),
        GeoPoint(45.6424, 5.8688)
    )

    val GeneralLeaderboard = listOf(
        LeaderboardEntry(1, "ChasseurPro", 2340, false, GhostPink, "gold"),
        LeaderboardEntry(2, "Toi", 1820, true, PacmanYellow, "silver"),
        LeaderboardEntry(3, "FastGhost", 1760, false, GhostCyan, "bronze"),
        LeaderboardEntry(4, "PacManette", 1520, false, GhostRed, null),
        LeaderboardEntry(5, "Cacheur23", 1340, false, GhostOrange, null),
        LeaderboardEntry(6, "Léo", 980, false, GhostPurple, null),
        LeaderboardEntry(7, "Shadow", 860, false, GhostPink, null),
        LeaderboardEntry(8, "Nina", 730, false, GhostCyan, null),
        LeaderboardEntry(9, "GhostRider", 650, false, GhostRed, null),
        LeaderboardEntry(10, "Momo", 540, false, GhostOrange, null)
    )

    val FriendsLeaderboard = listOf(
        LeaderboardEntry(1, "Toi", 1820, true, PacmanYellow, "gold"),
        LeaderboardEntry(2, "FastGhost", 1760, false, GhostCyan, "silver"),
        LeaderboardEntry(3, "PacManette", 1520, false, GhostRed, "bronze"),
        LeaderboardEntry(4, "Léo", 980, false, GhostPurple, null),
        LeaderboardEntry(5, "Nina", 730, false, GhostCyan, null)
    )

    val SeasonsLeaderboard = listOf(
        LeaderboardEntry(1, "ChasseurPro", 4120, false, GhostPink, "gold"),
        LeaderboardEntry(2, "GhostRider", 3450, false, GhostRed, "silver"),
        LeaderboardEntry(3, "Toi", 2890, true, PacmanYellow, "bronze"),
        LeaderboardEntry(4, "Cacheur23", 2400, false, GhostOrange, null),
        LeaderboardEntry(5, "Shadow", 1980, false, GhostPurple, null)
    )

    val BadgesList = listOf(
        BadgeItem(
            id = "first_found",
            title = "Premier trouvé",
            description = "Tu as trouvé ton tout premier caché sur le campus Savoie Technolac !",
            shape = BadgeShape.SHIELD,
            unlocked = true,
            unlockDate = "12/04"
        ),
        BadgeItem(
            id = "win_streak",
            title = "Série de victoires",
            description = "Remporté 3 parties consécutives sans dépasser le temps imparti.",
            shape = BadgeShape.STAR,
            unlocked = true,
            unlockDate = "08/04"
        ),
        BadgeItem(
            id = "urban_explorer",
            title = "Explorateur urbain",
            description = "Découvert plus de 20 cachettes distinctes sur l'ensemble du campus USMB.",
            shape = BadgeShape.HEXAGON,
            unlocked = true,
            unlockDate = "02/04"
        )
    )

    val RecentHistoryList = listOf(
        HistoryItem("hist_1", "12/04", "Trouvé 3 cachés", 150, "ghost"),
        HistoryItem("hist_2", "10/04", "2e place", 80, "ranking"),
        HistoryItem("hist_3", "08/04", "Victoire", 200, "trophy")
    )

    val AvatarItemsCatalog = listOf(
        // Category 1: Pac-Mans
        AvatarItem(
            id = "pacman_classic",
            name = "Pac-Man Classique",
            category = AvatarCategory.PACMANS,
            description = "Le héros légendaire avec sa célèbre mastication d'arcade.",
            price = 0,
            isAnimatedPacman = true
        ),
        AvatarItem(
            id = "pacman_hero",
            name = "Pac-Man Vitesse",
            category = AvatarCategory.PACMANS,
            description = "En pleine course avec ses traînées supersoniques néon.",
            price = 300,
            drawableResId = com.cachecache.game.R.drawable.pacman_hero
        ),
        AvatarItem(
            id = "pacman_giant",
            name = "Pac-Man Géant",
            category = AvatarCategory.PACMANS,
            description = "Tête géante en relief 3D, le style maître du jeu.",
            price = 600,
            drawableResId = com.cachecache.game.R.drawable.pacman_giant
        ),
        AvatarItem(
            id = "pacman_starburst",
            name = "Pac-Man Starburst",
            category = AvatarCategory.PACMANS,
            description = "Étincelles dorées et lueurs stellaires de champion.",
            price = 1000,
            drawableResId = com.cachecache.game.R.drawable.pacman_starburst
        ),

        // Category 2: Fantômes
        AvatarItem(
            id = "ghost_blinky",
            name = "Blinky (Rouge)",
            category = AvatarCategory.GHOSTS,
            description = "L'ombre rouge tenace qui ne lâche jamais sa proie.",
            price = 0,
            ghostColor = GhostRed
        ),
        AvatarItem(
            id = "ghost_pinky",
            name = "Pinky (Rose)",
            category = AvatarCategory.GHOSTS,
            description = "La complice rapide experte en embuscades surprises.",
            price = 0,
            ghostColor = GhostPink
        ),
        AvatarItem(
            id = "ghost_inky",
            name = "Inky (Cyan)",
            category = AvatarCategory.GHOSTS,
            description = "Le fantôme bleu turquoise aux trajectoires imprévisibles.",
            price = 400,
            ghostColor = GhostCyan
        ),
        AvatarItem(
            id = "ghost_clyde",
            name = "Clyde (Orange)",
            category = AvatarCategory.GHOSTS,
            description = "L'électron libre qui adore errer hors des sentiers battus.",
            price = 400,
            ghostColor = GhostOrange
        ),
        AvatarItem(
            id = "ghost_purple",
            name = "Sue (Violette)",
            category = AvatarCategory.GHOSTS,
            description = "La maîtresse du mystère aux reflets améthyste.",
            price = 600,
            ghostColor = GhostPurple
        ),
        AvatarItem(
            id = "ghost_scared",
            name = "Fantôme Apeuré (Bleu)",
            category = AvatarCategory.GHOSTS,
            description = "Frissonnant de panique avec son zigzag emblématique.",
            price = 800,
            ghostColor = ScaredBlue
        )
    )
}
