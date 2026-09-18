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
            id = "pacman_crown",
            name = "Pac-Man Roi",
            category = AvatarCategory.PACMANS,
            description = "Couronne d'or impériale, le souverain du labyrinthe.",
            price = 250,
            drawableResId = com.cachecache.game.R.drawable.avatar_pacman_crown
        ),
        AvatarItem(
            id = "pacman_cap_red",
            name = "Pac-Man Casquette Rouge",
            category = AvatarCategory.PACMANS,
            description = "Style sportif streetwear avec sa casquette rouge vive.",
            price = 250,
            drawableResId = com.cachecache.game.R.drawable.avatar_pacman_cap_red
        ),
        AvatarItem(
            id = "pacman_cap_blue",
            name = "Pac-Man Casquette Bleue",
            category = AvatarCategory.PACMANS,
            description = "Look casual branché avec sa casquette bleu cobalt.",
            price = 250,
            drawableResId = com.cachecache.game.R.drawable.avatar_pacman_cap_blue
        ),
        AvatarItem(
            id = "pacman_cap_white",
            name = "Pac-Man Casquette Rétro",
            category = AvatarCategory.PACMANS,
            description = "Casquette baseball vintage rouge et blanche des années 80.",
            price = 300,
            drawableResId = com.cachecache.game.R.drawable.avatar_pacman_cap_white
        ),
        AvatarItem(
            id = "pacman_cowboy",
            name = "Pac-Man Cowboy",
            category = AvatarCategory.PACMANS,
            description = "Chapeau de cuir de shérif prêt à traquer les fugitifs.",
            price = 350,
            drawableResId = com.cachecache.game.R.drawable.avatar_pacman_cowboy
        ),
        AvatarItem(
            id = "pacman_biker",
            name = "Pac-Man Motard",
            category = AvatarCategory.PACMANS,
            description = "Bandana biker et lunettes de soleil noires ultra cools.",
            price = 400,
            drawableResId = com.cachecache.game.R.drawable.avatar_pacman_biker
        ),
        AvatarItem(
            id = "pacman_love",
            name = "Pac-Man Amoureux",
            category = AvatarCategory.PACMANS,
            description = "Regard attendri avec des cœurs étincelants d'émotion.",
            price = 450,
            drawableResId = com.cachecache.game.R.drawable.avatar_pacman_love
        ),
        AvatarItem(
            id = "pacman_halo",
            name = "Pac-Man Ange",
            category = AvatarCategory.PACMANS,
            description = "Auréole sacrée flottante et pureté céleste.",
            price = 500,
            drawableResId = com.cachecache.game.R.drawable.avatar_pacman_halo
        ),
        AvatarItem(
            id = "pacman_flower",
            name = "Pac-Man Fleuri",
            category = AvatarCategory.PACMANS,
            description = "Orné d'une délicate fleur rose printanière.",
            price = 500,
            drawableResId = com.cachecache.game.R.drawable.avatar_pacman_flower
        ),
        AvatarItem(
            id = "pacman_gamer",
            name = "Pac-Man Gamer",
            category = AvatarCategory.PACMANS,
            description = "Casque gamer pro circum-aural pour entendre les fantômes.",
            price = 550,
            drawableResId = com.cachecache.game.R.drawable.avatar_pacman_gamer
        ),
        AvatarItem(
            id = "pacman_pirate",
            name = "Pac-Man Pirate",
            category = AvatarCategory.PACMANS,
            description = "Tricorne de flibustier avec tête de mort des mers.",
            price = 600,
            drawableResId = com.cachecache.game.R.drawable.avatar_pacman_pirate
        ),
        AvatarItem(
            id = "pacman_devil",
            name = "Pac-Man Diablotin",
            category = AvatarCategory.PACMANS,
            description = "Cornes démoniaques et moustache de fripouille.",
            price = 650,
            drawableResId = com.cachecache.game.R.drawable.avatar_pacman_devil
        ),
        AvatarItem(
            id = "pacman_glasses",
            name = "Pac-Man Intello",
            category = AvatarCategory.PACMANS,
            description = "Grosses lunettes d'érudit pour repérer tous les secrets.",
            price = 700,
            drawableResId = com.cachecache.game.R.drawable.avatar_pacman_glasses
        ),
        AvatarItem(
            id = "pacman_3d",
            name = "Pac-Man Cinéma 3D",
            category = AvatarCategory.PACMANS,
            description = "Lunettes anaglyphes rétro cyan et rouge stéréoscopiques.",
            price = 750,
            drawableResId = com.cachecache.game.R.drawable.avatar_pacman_3d
        ),
        AvatarItem(
            id = "pacman_wizard",
            name = "Pac-Man Magicien",
            category = AvatarCategory.PACMANS,
            description = "Chapeau pointu étoilé infusé d'enchantements mystiques.",
            price = 800,
            drawableResId = com.cachecache.game.R.drawable.avatar_pacman_wizard
        ),
        AvatarItem(
            id = "pacman_neon",
            name = "Pac-Man Néon Cyan",
            category = AvatarCategory.PACMANS,
            description = "Aura énergétique incandescente pulsant d'électricité pure.",
            price = 1200,
            drawableResId = com.cachecache.game.R.drawable.avatar_pacman_neon
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
            price = 200,
            ghostColor = GhostCyan
        ),
        AvatarItem(
            id = "ghost_clyde",
            name = "Clyde (Orange)",
            category = AvatarCategory.GHOSTS,
            description = "L'électron libre qui adore errer hors des sentiers battus.",
            price = 200,
            ghostColor = GhostOrange
        ),
        AvatarItem(
            id = "ghost_sunglasses",
            name = "Fantôme Lunettes Noires",
            category = AvatarCategory.GHOSTS,
            description = "Lunettes de soleil ultra stylées, impassible en toute situation.",
            price = 300,
            drawableResId = com.cachecache.game.R.drawable.avatar_ghost_sunglasses
        ),
        AvatarItem(
            id = "ghost_crown",
            name = "Fantôme Roi",
            category = AvatarCategory.GHOSTS,
            description = "Couronné roi des ectoplasmes et des spectres errants.",
            price = 350,
            drawableResId = com.cachecache.game.R.drawable.avatar_ghost_crown
        ),
        AvatarItem(
            id = "ghost_cap",
            name = "Fantôme Casquette",
            category = AvatarCategory.GHOSTS,
            description = "Casquette bleue sportive portée fièrement sur la tête.",
            price = 400,
            drawableResId = com.cachecache.game.R.drawable.avatar_ghost_cap
        ),
        AvatarItem(
            id = "ghost_halo",
            name = "Fantôme Ange",
            category = AvatarCategory.GHOSTS,
            description = "Une auréole bienveillante pour un esprit pacifique.",
            price = 450,
            drawableResId = com.cachecache.game.R.drawable.avatar_ghost_halo
        ),
        AvatarItem(
            id = "ghost_headphones",
            name = "Fantôme DJ",
            category = AvatarCategory.GHOSTS,
            description = "Casque vissé sur la tête pour mixer des beats électro.",
            price = 500,
            drawableResId = com.cachecache.game.R.drawable.avatar_ghost_headphones
        ),
        AvatarItem(
            id = "ghost_pirate",
            name = "Fantôme Pirate",
            category = AvatarCategory.GHOSTS,
            description = "Capitaine légendaire à l'abordage des power-pellets.",
            price = 550,
            drawableResId = com.cachecache.game.R.drawable.avatar_ghost_pirate
        ),
        AvatarItem(
            id = "ghost_devil",
            name = "Fantôme Diablotin",
            category = AvatarCategory.GHOSTS,
            description = "Cornes écarlates et malice infernale.",
            price = 600,
            drawableResId = com.cachecache.game.R.drawable.avatar_ghost_devil
        ),
        AvatarItem(
            id = "ghost_mustache",
            name = "Fantôme Gentleman",
            category = AvatarCategory.GHOSTS,
            description = "Élégance avec sa moustache impeccablement taillée.",
            price = 650,
            drawableResId = com.cachecache.game.R.drawable.avatar_ghost_mustache
        ),
        AvatarItem(
            id = "ghost_3d",
            name = "Fantôme 3D",
            category = AvatarCategory.GHOSTS,
            description = "Prêt pour la séance de cinéma en trois dimensions.",
            price = 700,
            drawableResId = com.cachecache.game.R.drawable.avatar_ghost_3d
        ),
        AvatarItem(
            id = "ghost_santa",
            name = "Fantôme Père Noël",
            category = AvatarCategory.GHOSTS,
            description = "Bonnet pompon rouge et barbe soyeuse de fin d'année.",
            price = 750,
            drawableResId = com.cachecache.game.R.drawable.avatar_ghost_santa
        ),
        AvatarItem(
            id = "ghost_wizard",
            name = "Fantôme Sorcier",
            category = AvatarCategory.GHOSTS,
            description = "Maître des incantations et des disparitions furtives.",
            price = 800,
            drawableResId = com.cachecache.game.R.drawable.avatar_ghost_wizard
        ),
        AvatarItem(
            id = "ghost_snorkel",
            name = "Fantôme Plongée",
            category = AvatarCategory.GHOSTS,
            description = "Masque sous-marin et tuba prêt pour les abysses.",
            price = 850,
            drawableResId = com.cachecache.game.R.drawable.avatar_ghost_snorkel
        ),
        AvatarItem(
            id = "ghost_cowboy",
            name = "Fantôme Cowboy",
            category = AvatarCategory.GHOSTS,
            description = "Chapeau western avec étoile dorée du shérif.",
            price = 900,
            drawableResId = com.cachecache.game.R.drawable.avatar_ghost_cowboy
        ),
        AvatarItem(
            id = "ghost_dark_blue",
            name = "Fantôme Sombre",
            category = AvatarCategory.GHOSTS,
            description = "Éclipse nocturne avec sa couronne royale d'outre-tombe.",
            price = 1000,
            drawableResId = com.cachecache.game.R.drawable.avatar_ghost_dark_blue
        )
    )
}
