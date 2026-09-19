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
            color = GhostCyan,
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
            color = GhostRed,
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
        // === CATÉGORIE 1 : CLASSIQUES (IMAGE 1) ===
        AvatarItem(
            id = "ghost_red_classic",
            name = "Blinky Classique",
            category = AvatarCategory.CLASSIC,
            description = "Le style légendaire et intemporel.",
            price = 0,
            ghostColor = GhostRed,
            drawableResId = com.cachecache.game.R.drawable.avatar_ghost_red_classic,
            isPremium = false
        ),
        AvatarItem(
            id = "ghost_red_sunglasses",
            name = "Blinky Lunettes Noires",
            category = AvatarCategory.CLASSIC,
            description = "Impassible et ultra-stylé avec ses verres teintés.",
            price = 300,
            ghostColor = GhostRed,
            drawableResId = com.cachecache.game.R.drawable.avatar_ghost_red_sunglasses,
            isPremium = false
        ),
        AvatarItem(
            id = "ghost_red_crown",
            name = "Blinky Roi",
            category = AvatarCategory.CLASSIC,
            description = "Couronné roi des ectoplasmes et des spectres errants.",
            price = 350,
            ghostColor = GhostRed,
            drawableResId = com.cachecache.game.R.drawable.avatar_ghost_red_crown,
            isPremium = false
        ),
        AvatarItem(
            id = "ghost_red_cap",
            name = "Blinky Casquette",
            category = AvatarCategory.CLASSIC,
            description = "Casquette sportive portée fièrement sur la tête.",
            price = 400,
            ghostColor = GhostRed,
            drawableResId = com.cachecache.game.R.drawable.avatar_ghost_red_cap,
            isPremium = false
        ),
        AvatarItem(
            id = "ghost_red_flower",
            name = "Blinky Fleuri",
            category = AvatarCategory.CLASSIC,
            description = "Orné d'une délicate fleur printanière.",
            price = 450,
            ghostColor = GhostRed,
            drawableResId = com.cachecache.game.R.drawable.avatar_ghost_red_flower,
            isPremium = false
        ),
        AvatarItem(
            id = "ghost_red_headband",
            name = "Blinky Bandeau",
            category = AvatarCategory.CLASSIC,
            description = "Bandeau d'athlète prêt pour le sprint.",
            price = 450,
            ghostColor = GhostRed,
            drawableResId = com.cachecache.game.R.drawable.avatar_ghost_red_headband,
            isPremium = false
        ),
        AvatarItem(
            id = "ghost_red_wink",
            name = "Blinky Clin d'Œil",
            category = AvatarCategory.CLASSIC,
            description = "Un clin d'œil malicieux pour désorienter l'adversaire.",
            price = 500,
            ghostColor = GhostRed,
            drawableResId = com.cachecache.game.R.drawable.avatar_ghost_red_wink,
            isPremium = false
        ),
        AvatarItem(
            id = "ghost_red_halo",
            name = "Blinky Ange",
            category = AvatarCategory.CLASSIC,
            description = "Une auréole sacrée pour un esprit pur et bienveillant.",
            price = 550,
            ghostColor = GhostRed,
            drawableResId = com.cachecache.game.R.drawable.avatar_ghost_red_halo,
            isPremium = false
        ),
        AvatarItem(
            id = "ghost_red_headphones",
            name = "Blinky DJ / Gamer",
            category = AvatarCategory.CLASSIC,
            description = "Casque circum-aural pour mixer des beats électro.",
            price = 600,
            ghostColor = GhostRed,
            drawableResId = com.cachecache.game.R.drawable.avatar_ghost_red_headphones,
            isPremium = false
        ),
        AvatarItem(
            id = "ghost_red_pirate",
            name = "Blinky Pirate",
            category = AvatarCategory.CLASSIC,
            description = "Capitaine corsaire à l'abordage des power-pellets.",
            price = 650,
            ghostColor = GhostRed,
            drawableResId = com.cachecache.game.R.drawable.avatar_ghost_red_pirate,
            isPremium = false
        ),
        AvatarItem(
            id = "ghost_red_devil",
            name = "Blinky Diablotin",
            category = AvatarCategory.CLASSIC,
            description = "Cornes écarlates et malice infernale.",
            price = 700,
            ghostColor = GhostRed,
            drawableResId = com.cachecache.game.R.drawable.avatar_ghost_red_devil,
            isPremium = false
        ),
        AvatarItem(
            id = "ghost_red_mustache",
            name = "Blinky Gentleman",
            category = AvatarCategory.CLASSIC,
            description = "Moustache distinguée et classe britannique vintage.",
            price = 750,
            ghostColor = GhostRed,
            drawableResId = com.cachecache.game.R.drawable.avatar_ghost_red_mustache,
            isPremium = false
        ),
        AvatarItem(
            id = "ghost_red_glasses",
            name = "Blinky Intello",
            category = AvatarCategory.CLASSIC,
            description = "Grosses montures d'érudit pour anticiper tous les pièges.",
            price = 800,
            ghostColor = GhostRed,
            drawableResId = com.cachecache.game.R.drawable.avatar_ghost_red_glasses,
            isPremium = false
        ),
        AvatarItem(
            id = "ghost_red_3d",
            name = "Blinky Cinéma 3D",
            category = AvatarCategory.CLASSIC,
            description = "Lunettes anaglyphes stéréoscopiques rouge et cyan.",
            price = 850,
            ghostColor = GhostRed,
            drawableResId = com.cachecache.game.R.drawable.avatar_ghost_red_3d,
            isPremium = false
        ),
        AvatarItem(
            id = "ghost_red_special",
            name = "Blinky Spécial",
            category = AvatarCategory.CLASSIC,
            description = "Costume thématique exclusif de grande fête.",
            price = 900,
            ghostColor = GhostRed,
            drawableResId = com.cachecache.game.R.drawable.avatar_ghost_red_special,
            isPremium = false
        ),
        AvatarItem(
            id = "ghost_pink_classic",
            name = "Pinky Classique",
            category = AvatarCategory.CLASSIC,
            description = "Le style légendaire et intemporel.",
            price = 0,
            ghostColor = GhostPink,
            drawableResId = com.cachecache.game.R.drawable.avatar_ghost_pink_classic,
            isPremium = false
        ),
        AvatarItem(
            id = "ghost_pink_sunglasses",
            name = "Pinky Lunettes Noires",
            category = AvatarCategory.CLASSIC,
            description = "Impassible et ultra-stylé avec ses verres teintés.",
            price = 300,
            ghostColor = GhostPink,
            drawableResId = com.cachecache.game.R.drawable.avatar_ghost_pink_sunglasses,
            isPremium = false
        ),
        AvatarItem(
            id = "ghost_pink_crown",
            name = "Pinky Roi",
            category = AvatarCategory.CLASSIC,
            description = "Couronné roi des ectoplasmes et des spectres errants.",
            price = 350,
            ghostColor = GhostPink,
            drawableResId = com.cachecache.game.R.drawable.avatar_ghost_pink_crown,
            isPremium = false
        ),
        AvatarItem(
            id = "ghost_pink_cap",
            name = "Pinky Casquette",
            category = AvatarCategory.CLASSIC,
            description = "Casquette sportive portée fièrement sur la tête.",
            price = 400,
            ghostColor = GhostPink,
            drawableResId = com.cachecache.game.R.drawable.avatar_ghost_pink_cap,
            isPremium = false
        ),
        AvatarItem(
            id = "ghost_pink_flower",
            name = "Pinky Fleuri",
            category = AvatarCategory.CLASSIC,
            description = "Orné d'une délicate fleur printanière.",
            price = 450,
            ghostColor = GhostPink,
            drawableResId = com.cachecache.game.R.drawable.avatar_ghost_pink_flower,
            isPremium = false
        ),
        AvatarItem(
            id = "ghost_pink_headband",
            name = "Pinky Bandeau",
            category = AvatarCategory.CLASSIC,
            description = "Bandeau d'athlète prêt pour le sprint.",
            price = 450,
            ghostColor = GhostPink,
            drawableResId = com.cachecache.game.R.drawable.avatar_ghost_pink_headband,
            isPremium = false
        ),
        AvatarItem(
            id = "ghost_pink_wink",
            name = "Pinky Clin d'Œil",
            category = AvatarCategory.CLASSIC,
            description = "Un clin d'œil malicieux pour désorienter l'adversaire.",
            price = 500,
            ghostColor = GhostPink,
            drawableResId = com.cachecache.game.R.drawable.avatar_ghost_pink_wink,
            isPremium = false
        ),
        AvatarItem(
            id = "ghost_pink_halo",
            name = "Pinky Ange",
            category = AvatarCategory.CLASSIC,
            description = "Une auréole sacrée pour un esprit pur et bienveillant.",
            price = 550,
            ghostColor = GhostPink,
            drawableResId = com.cachecache.game.R.drawable.avatar_ghost_pink_halo,
            isPremium = false
        ),
        AvatarItem(
            id = "ghost_pink_headphones",
            name = "Pinky DJ / Gamer",
            category = AvatarCategory.CLASSIC,
            description = "Casque circum-aural pour mixer des beats électro.",
            price = 600,
            ghostColor = GhostPink,
            drawableResId = com.cachecache.game.R.drawable.avatar_ghost_pink_headphones,
            isPremium = false
        ),
        AvatarItem(
            id = "ghost_pink_pirate",
            name = "Pinky Pirate",
            category = AvatarCategory.CLASSIC,
            description = "Capitaine corsaire à l'abordage des power-pellets.",
            price = 650,
            ghostColor = GhostPink,
            drawableResId = com.cachecache.game.R.drawable.avatar_ghost_pink_pirate,
            isPremium = false
        ),
        AvatarItem(
            id = "ghost_pink_devil",
            name = "Pinky Diablotin",
            category = AvatarCategory.CLASSIC,
            description = "Cornes écarlates et malice infernale.",
            price = 700,
            ghostColor = GhostPink,
            drawableResId = com.cachecache.game.R.drawable.avatar_ghost_pink_devil,
            isPremium = false
        ),
        AvatarItem(
            id = "ghost_pink_mustache",
            name = "Pinky Gentleman",
            category = AvatarCategory.CLASSIC,
            description = "Moustache distinguée et classe britannique vintage.",
            price = 750,
            ghostColor = GhostPink,
            drawableResId = com.cachecache.game.R.drawable.avatar_ghost_pink_mustache,
            isPremium = false
        ),
        AvatarItem(
            id = "ghost_pink_glasses",
            name = "Pinky Intello",
            category = AvatarCategory.CLASSIC,
            description = "Grosses montures d'érudit pour anticiper tous les pièges.",
            price = 800,
            ghostColor = GhostPink,
            drawableResId = com.cachecache.game.R.drawable.avatar_ghost_pink_glasses,
            isPremium = false
        ),
        AvatarItem(
            id = "ghost_pink_3d",
            name = "Pinky Cinéma 3D",
            category = AvatarCategory.CLASSIC,
            description = "Lunettes anaglyphes stéréoscopiques rouge et cyan.",
            price = 850,
            ghostColor = GhostPink,
            drawableResId = com.cachecache.game.R.drawable.avatar_ghost_pink_3d,
            isPremium = false
        ),
        AvatarItem(
            id = "ghost_pink_special",
            name = "Pinky Spécial",
            category = AvatarCategory.CLASSIC,
            description = "Costume thématique exclusif de grande fête.",
            price = 900,
            ghostColor = GhostPink,
            drawableResId = com.cachecache.game.R.drawable.avatar_ghost_pink_special,
            isPremium = false
        ),
        AvatarItem(
            id = "ghost_cyan_classic",
            name = "Inky Classique",
            category = AvatarCategory.CLASSIC,
            description = "Le style légendaire et intemporel.",
            price = 0,
            ghostColor = GhostCyan,
            drawableResId = com.cachecache.game.R.drawable.avatar_ghost_cyan_classic,
            isPremium = false
        ),
        AvatarItem(
            id = "ghost_cyan_sunglasses",
            name = "Inky Lunettes Noires",
            category = AvatarCategory.CLASSIC,
            description = "Impassible et ultra-stylé avec ses verres teintés.",
            price = 300,
            ghostColor = GhostCyan,
            drawableResId = com.cachecache.game.R.drawable.avatar_ghost_cyan_sunglasses,
            isPremium = false
        ),
        AvatarItem(
            id = "ghost_cyan_crown",
            name = "Inky Roi",
            category = AvatarCategory.CLASSIC,
            description = "Couronné roi des ectoplasmes et des spectres errants.",
            price = 350,
            ghostColor = GhostCyan,
            drawableResId = com.cachecache.game.R.drawable.avatar_ghost_cyan_crown,
            isPremium = false
        ),
        AvatarItem(
            id = "ghost_cyan_cap",
            name = "Inky Casquette",
            category = AvatarCategory.CLASSIC,
            description = "Casquette sportive portée fièrement sur la tête.",
            price = 400,
            ghostColor = GhostCyan,
            drawableResId = com.cachecache.game.R.drawable.avatar_ghost_cyan_cap,
            isPremium = false
        ),
        AvatarItem(
            id = "ghost_cyan_flower",
            name = "Inky Fleuri",
            category = AvatarCategory.CLASSIC,
            description = "Orné d'une délicate fleur printanière.",
            price = 450,
            ghostColor = GhostCyan,
            drawableResId = com.cachecache.game.R.drawable.avatar_ghost_cyan_flower,
            isPremium = false
        ),
        AvatarItem(
            id = "ghost_cyan_headband",
            name = "Inky Bandeau",
            category = AvatarCategory.CLASSIC,
            description = "Bandeau d'athlète prêt pour le sprint.",
            price = 450,
            ghostColor = GhostCyan,
            drawableResId = com.cachecache.game.R.drawable.avatar_ghost_cyan_headband,
            isPremium = false
        ),
        AvatarItem(
            id = "ghost_cyan_wink",
            name = "Inky Clin d'Œil",
            category = AvatarCategory.CLASSIC,
            description = "Un clin d'œil malicieux pour désorienter l'adversaire.",
            price = 500,
            ghostColor = GhostCyan,
            drawableResId = com.cachecache.game.R.drawable.avatar_ghost_cyan_wink,
            isPremium = false
        ),
        AvatarItem(
            id = "ghost_cyan_halo",
            name = "Inky Ange",
            category = AvatarCategory.CLASSIC,
            description = "Une auréole sacrée pour un esprit pur et bienveillant.",
            price = 550,
            ghostColor = GhostCyan,
            drawableResId = com.cachecache.game.R.drawable.avatar_ghost_cyan_halo,
            isPremium = false
        ),
        AvatarItem(
            id = "ghost_cyan_headphones",
            name = "Inky DJ / Gamer",
            category = AvatarCategory.CLASSIC,
            description = "Casque circum-aural pour mixer des beats électro.",
            price = 600,
            ghostColor = GhostCyan,
            drawableResId = com.cachecache.game.R.drawable.avatar_ghost_cyan_headphones,
            isPremium = false
        ),
        AvatarItem(
            id = "ghost_cyan_pirate",
            name = "Inky Pirate",
            category = AvatarCategory.CLASSIC,
            description = "Capitaine corsaire à l'abordage des power-pellets.",
            price = 650,
            ghostColor = GhostCyan,
            drawableResId = com.cachecache.game.R.drawable.avatar_ghost_cyan_pirate,
            isPremium = false
        ),
        AvatarItem(
            id = "ghost_cyan_devil",
            name = "Inky Diablotin",
            category = AvatarCategory.CLASSIC,
            description = "Cornes écarlates et malice infernale.",
            price = 700,
            ghostColor = GhostCyan,
            drawableResId = com.cachecache.game.R.drawable.avatar_ghost_cyan_devil,
            isPremium = false
        ),
        AvatarItem(
            id = "ghost_cyan_mustache",
            name = "Inky Gentleman",
            category = AvatarCategory.CLASSIC,
            description = "Moustache distinguée et classe britannique vintage.",
            price = 750,
            ghostColor = GhostCyan,
            drawableResId = com.cachecache.game.R.drawable.avatar_ghost_cyan_mustache,
            isPremium = false
        ),
        AvatarItem(
            id = "ghost_cyan_glasses",
            name = "Inky Intello",
            category = AvatarCategory.CLASSIC,
            description = "Grosses montures d'érudit pour anticiper tous les pièges.",
            price = 800,
            ghostColor = GhostCyan,
            drawableResId = com.cachecache.game.R.drawable.avatar_ghost_cyan_glasses,
            isPremium = false
        ),
        AvatarItem(
            id = "ghost_cyan_3d",
            name = "Inky Cinéma 3D",
            category = AvatarCategory.CLASSIC,
            description = "Lunettes anaglyphes stéréoscopiques rouge et cyan.",
            price = 850,
            ghostColor = GhostCyan,
            drawableResId = com.cachecache.game.R.drawable.avatar_ghost_cyan_3d,
            isPremium = false
        ),
        AvatarItem(
            id = "ghost_cyan_special",
            name = "Inky Spécial",
            category = AvatarCategory.CLASSIC,
            description = "Costume thématique exclusif de grande fête.",
            price = 900,
            ghostColor = GhostCyan,
            drawableResId = com.cachecache.game.R.drawable.avatar_ghost_cyan_special,
            isPremium = false
        ),
        AvatarItem(
            id = "ghost_orange_classic",
            name = "Clyde Classique",
            category = AvatarCategory.CLASSIC,
            description = "Le style légendaire et intemporel.",
            price = 0,
            ghostColor = GhostOrange,
            drawableResId = com.cachecache.game.R.drawable.avatar_ghost_orange_classic,
            isPremium = false
        ),
        AvatarItem(
            id = "ghost_orange_sunglasses",
            name = "Clyde Lunettes Noires",
            category = AvatarCategory.CLASSIC,
            description = "Impassible et ultra-stylé avec ses verres teintés.",
            price = 300,
            ghostColor = GhostOrange,
            drawableResId = com.cachecache.game.R.drawable.avatar_ghost_orange_sunglasses,
            isPremium = false
        ),
        AvatarItem(
            id = "ghost_orange_crown",
            name = "Clyde Roi",
            category = AvatarCategory.CLASSIC,
            description = "Couronné roi des ectoplasmes et des spectres errants.",
            price = 350,
            ghostColor = GhostOrange,
            drawableResId = com.cachecache.game.R.drawable.avatar_ghost_orange_crown,
            isPremium = false
        ),
        AvatarItem(
            id = "ghost_orange_cap",
            name = "Clyde Casquette",
            category = AvatarCategory.CLASSIC,
            description = "Casquette sportive portée fièrement sur la tête.",
            price = 400,
            ghostColor = GhostOrange,
            drawableResId = com.cachecache.game.R.drawable.avatar_ghost_orange_cap,
            isPremium = false
        ),
        AvatarItem(
            id = "ghost_orange_flower",
            name = "Clyde Fleuri",
            category = AvatarCategory.CLASSIC,
            description = "Orné d'une délicate fleur printanière.",
            price = 450,
            ghostColor = GhostOrange,
            drawableResId = com.cachecache.game.R.drawable.avatar_ghost_orange_flower,
            isPremium = false
        ),
        AvatarItem(
            id = "ghost_orange_headband",
            name = "Clyde Bandeau",
            category = AvatarCategory.CLASSIC,
            description = "Bandeau d'athlète prêt pour le sprint.",
            price = 450,
            ghostColor = GhostOrange,
            drawableResId = com.cachecache.game.R.drawable.avatar_ghost_orange_headband,
            isPremium = false
        ),
        AvatarItem(
            id = "ghost_orange_wink",
            name = "Clyde Clin d'Œil",
            category = AvatarCategory.CLASSIC,
            description = "Un clin d'œil malicieux pour désorienter l'adversaire.",
            price = 500,
            ghostColor = GhostOrange,
            drawableResId = com.cachecache.game.R.drawable.avatar_ghost_orange_wink,
            isPremium = false
        ),
        AvatarItem(
            id = "ghost_orange_halo",
            name = "Clyde Ange",
            category = AvatarCategory.CLASSIC,
            description = "Une auréole sacrée pour un esprit pur et bienveillant.",
            price = 550,
            ghostColor = GhostOrange,
            drawableResId = com.cachecache.game.R.drawable.avatar_ghost_orange_halo,
            isPremium = false
        ),
        AvatarItem(
            id = "ghost_orange_headphones",
            name = "Clyde DJ / Gamer",
            category = AvatarCategory.CLASSIC,
            description = "Casque circum-aural pour mixer des beats électro.",
            price = 600,
            ghostColor = GhostOrange,
            drawableResId = com.cachecache.game.R.drawable.avatar_ghost_orange_headphones,
            isPremium = false
        ),
        AvatarItem(
            id = "ghost_orange_pirate",
            name = "Clyde Pirate",
            category = AvatarCategory.CLASSIC,
            description = "Capitaine corsaire à l'abordage des power-pellets.",
            price = 650,
            ghostColor = GhostOrange,
            drawableResId = com.cachecache.game.R.drawable.avatar_ghost_orange_pirate,
            isPremium = false
        ),
        AvatarItem(
            id = "ghost_orange_devil",
            name = "Clyde Diablotin",
            category = AvatarCategory.CLASSIC,
            description = "Cornes écarlates et malice infernale.",
            price = 700,
            ghostColor = GhostOrange,
            drawableResId = com.cachecache.game.R.drawable.avatar_ghost_orange_devil,
            isPremium = false
        ),
        AvatarItem(
            id = "ghost_orange_mustache",
            name = "Clyde Gentleman",
            category = AvatarCategory.CLASSIC,
            description = "Moustache distinguée et classe britannique vintage.",
            price = 750,
            ghostColor = GhostOrange,
            drawableResId = com.cachecache.game.R.drawable.avatar_ghost_orange_mustache,
            isPremium = false
        ),
        AvatarItem(
            id = "ghost_orange_glasses",
            name = "Clyde Intello",
            category = AvatarCategory.CLASSIC,
            description = "Grosses montures d'érudit pour anticiper tous les pièges.",
            price = 800,
            ghostColor = GhostOrange,
            drawableResId = com.cachecache.game.R.drawable.avatar_ghost_orange_glasses,
            isPremium = false
        ),
        AvatarItem(
            id = "ghost_orange_3d",
            name = "Clyde Cinéma 3D",
            category = AvatarCategory.CLASSIC,
            description = "Lunettes anaglyphes stéréoscopiques rouge et cyan.",
            price = 850,
            ghostColor = GhostOrange,
            drawableResId = com.cachecache.game.R.drawable.avatar_ghost_orange_3d,
            isPremium = false
        ),
        AvatarItem(
            id = "ghost_orange_special",
            name = "Clyde Spécial",
            category = AvatarCategory.CLASSIC,
            description = "Costume thématique exclusif de grande fête.",
            price = 900,
            ghostColor = GhostOrange,
            drawableResId = com.cachecache.game.R.drawable.avatar_ghost_orange_special,
            isPremium = false
        ),
        AvatarItem(
            id = "ghost_blue_classic",
            name = "Spectre Bleu Classique",
            category = AvatarCategory.CLASSIC,
            description = "Le style légendaire et intemporel.",
            price = 0,
            ghostColor = GhostPurple,
            drawableResId = com.cachecache.game.R.drawable.avatar_ghost_blue_classic,
            isPremium = false
        ),
        AvatarItem(
            id = "ghost_blue_sunglasses",
            name = "Spectre Bleu Lunettes Noires",
            category = AvatarCategory.CLASSIC,
            description = "Impassible et ultra-stylé avec ses verres teintés.",
            price = 300,
            ghostColor = GhostPurple,
            drawableResId = com.cachecache.game.R.drawable.avatar_ghost_blue_sunglasses,
            isPremium = false
        ),
        AvatarItem(
            id = "ghost_blue_crown",
            name = "Spectre Bleu Roi",
            category = AvatarCategory.CLASSIC,
            description = "Couronné roi des ectoplasmes et des spectres errants.",
            price = 350,
            ghostColor = GhostPurple,
            drawableResId = com.cachecache.game.R.drawable.avatar_ghost_blue_crown,
            isPremium = false
        ),
        AvatarItem(
            id = "ghost_blue_cap",
            name = "Spectre Bleu Casquette",
            category = AvatarCategory.CLASSIC,
            description = "Casquette sportive portée fièrement sur la tête.",
            price = 400,
            ghostColor = GhostPurple,
            drawableResId = com.cachecache.game.R.drawable.avatar_ghost_blue_cap,
            isPremium = false
        ),
        AvatarItem(
            id = "ghost_blue_flower",
            name = "Spectre Bleu Fleuri",
            category = AvatarCategory.CLASSIC,
            description = "Orné d'une délicate fleur printanière.",
            price = 450,
            ghostColor = GhostPurple,
            drawableResId = com.cachecache.game.R.drawable.avatar_ghost_blue_flower,
            isPremium = false
        ),
        AvatarItem(
            id = "ghost_blue_headband",
            name = "Spectre Bleu Bandeau",
            category = AvatarCategory.CLASSIC,
            description = "Bandeau d'athlète prêt pour le sprint.",
            price = 450,
            ghostColor = GhostPurple,
            drawableResId = com.cachecache.game.R.drawable.avatar_ghost_blue_headband,
            isPremium = false
        ),
        AvatarItem(
            id = "ghost_blue_wink",
            name = "Spectre Bleu Clin d'Œil",
            category = AvatarCategory.CLASSIC,
            description = "Un clin d'œil malicieux pour désorienter l'adversaire.",
            price = 500,
            ghostColor = GhostPurple,
            drawableResId = com.cachecache.game.R.drawable.avatar_ghost_blue_wink,
            isPremium = false
        ),
        AvatarItem(
            id = "ghost_blue_halo",
            name = "Spectre Bleu Ange",
            category = AvatarCategory.CLASSIC,
            description = "Une auréole sacrée pour un esprit pur et bienveillant.",
            price = 550,
            ghostColor = GhostPurple,
            drawableResId = com.cachecache.game.R.drawable.avatar_ghost_blue_halo,
            isPremium = false
        ),
        AvatarItem(
            id = "ghost_blue_headphones",
            name = "Spectre Bleu DJ / Gamer",
            category = AvatarCategory.CLASSIC,
            description = "Casque circum-aural pour mixer des beats électro.",
            price = 600,
            ghostColor = GhostPurple,
            drawableResId = com.cachecache.game.R.drawable.avatar_ghost_blue_headphones,
            isPremium = false
        ),
        AvatarItem(
            id = "ghost_blue_pirate",
            name = "Spectre Bleu Pirate",
            category = AvatarCategory.CLASSIC,
            description = "Capitaine corsaire à l'abordage des power-pellets.",
            price = 650,
            ghostColor = GhostPurple,
            drawableResId = com.cachecache.game.R.drawable.avatar_ghost_blue_pirate,
            isPremium = false
        ),
        AvatarItem(
            id = "ghost_blue_devil",
            name = "Spectre Bleu Diablotin",
            category = AvatarCategory.CLASSIC,
            description = "Cornes écarlates et malice infernale.",
            price = 700,
            ghostColor = GhostPurple,
            drawableResId = com.cachecache.game.R.drawable.avatar_ghost_blue_devil,
            isPremium = false
        ),
        AvatarItem(
            id = "ghost_blue_mustache",
            name = "Spectre Bleu Gentleman",
            category = AvatarCategory.CLASSIC,
            description = "Moustache distinguée et classe britannique vintage.",
            price = 750,
            ghostColor = GhostPurple,
            drawableResId = com.cachecache.game.R.drawable.avatar_ghost_blue_mustache,
            isPremium = false
        ),
        AvatarItem(
            id = "ghost_blue_glasses",
            name = "Spectre Bleu Intello",
            category = AvatarCategory.CLASSIC,
            description = "Grosses montures d'érudit pour anticiper tous les pièges.",
            price = 800,
            ghostColor = GhostPurple,
            drawableResId = com.cachecache.game.R.drawable.avatar_ghost_blue_glasses,
            isPremium = false
        ),
        AvatarItem(
            id = "ghost_blue_3d",
            name = "Spectre Bleu Cinéma 3D",
            category = AvatarCategory.CLASSIC,
            description = "Lunettes anaglyphes stéréoscopiques rouge et cyan.",
            price = 850,
            ghostColor = GhostPurple,
            drawableResId = com.cachecache.game.R.drawable.avatar_ghost_blue_3d,
            isPremium = false
        ),
        AvatarItem(
            id = "ghost_blue_special",
            name = "Spectre Bleu Spécial",
            category = AvatarCategory.CLASSIC,
            description = "Costume thématique exclusif de grande fête.",
            price = 900,
            ghostColor = GhostPurple,
            drawableResId = com.cachecache.game.R.drawable.avatar_ghost_blue_special,
            isPremium = false
        ),
        // === CATÉGORIE 2 : SKINS PREMIUM (IMAGE 2) ===
        AvatarItem(
            id = "premium_ghost_gold_king",
            name = "Roi Doré Suprême",
            category = AvatarCategory.PREMIUM,
            description = "Souverain mythique auréolé d'or pur et de lauriers impériaux.",
            price = 2500,
            ghostColor = GhostOrange,
            drawableResId = com.cachecache.game.R.drawable.premium_ghost_gold_king,
            isPremium = true
        ),
        AvatarItem(
            id = "premium_ghost_crystal_ice",
            name = "Cristal de Givre",
            category = AvatarCategory.PREMIUM,
            description = "Taillé dans la glace éternelle avec ses éclats scintillants.",
            price = 2000,
            ghostColor = GhostOrange,
            drawableResId = com.cachecache.game.R.drawable.premium_ghost_crystal_ice,
            isPremium = true
        ),
        AvatarItem(
            id = "premium_ghost_fire_flame",
            name = "Flamme Infernale",
            category = AvatarCategory.PREMIUM,
            description = "Brasier incandescent pulsant d'une énergie volcanique.",
            price = 2200,
            ghostColor = GhostOrange,
            drawableResId = com.cachecache.game.R.drawable.premium_ghost_fire_flame,
            isPremium = true
        ),
        AvatarItem(
            id = "premium_ghost_pirate_captain",
            name = "Vieux Loup de Mer",
            category = AvatarCategory.PREMIUM,
            description = "Capitaine pirate légendaire avec bicorne et cache-œil.",
            price = 1800,
            ghostColor = GhostOrange,
            drawableResId = com.cachecache.game.R.drawable.premium_ghost_pirate_captain,
            isPremium = true
        ),
        AvatarItem(
            id = "premium_ghost_angel_wings",
            name = "Séraphin Ailé",
            category = AvatarCategory.PREMIUM,
            description = "Ailes d'or divines et auréole céleste flottante.",
            price = 2400,
            ghostColor = GhostOrange,
            drawableResId = com.cachecache.game.R.drawable.premium_ghost_angel_wings,
            isPremium = true
        ),
        AvatarItem(
            id = "premium_ghost_dark_void",
            name = "Ombre Abyssale",
            category = AvatarCategory.PREMIUM,
            description = "Seigneur des ténèbres couronné d'améthyste et de flammes sombres.",
            price = 2600,
            ghostColor = GhostOrange,
            drawableResId = com.cachecache.game.R.drawable.premium_ghost_dark_void,
            isPremium = true
        ),
        AvatarItem(
            id = "premium_ghost_cyberpunk",
            name = "Cyberpunk 2099",
            category = AvatarCategory.PREMIUM,
            description = "Visière néon HUD et câblage cybernétique futuriste.",
            price = 2100,
            ghostColor = GhostOrange,
            drawableResId = com.cachecache.game.R.drawable.premium_ghost_cyberpunk,
            isPremium = true
        ),
        AvatarItem(
            id = "premium_ghost_flower_spring",
            name = "Printemps Fleuri",
            category = AvatarCategory.PREMIUM,
            description = "Couronne florale luxuriante aux parfums de victoire.",
            price = 1600,
            ghostColor = GhostOrange,
            drawableResId = com.cachecache.game.R.drawable.premium_ghost_flower_spring,
            isPremium = true
        ),
        AvatarItem(
            id = "premium_ghost_superhero",
            name = "Super-Fantôme",
            category = AvatarCategory.PREMIUM,
            description = "Cape volante rouge et blason héroïque invincible.",
            price = 1900,
            ghostColor = GhostOrange,
            drawableResId = com.cachecache.game.R.drawable.premium_ghost_superhero,
            isPremium = true
        ),
        AvatarItem(
            id = "premium_ghost_disco_synthwave",
            name = "Disco Synthwave",
            category = AvatarCategory.PREMIUM,
            description = "Ambiance néon rétro 80s et lunettes à persiennes dorées.",
            price = 2300,
            ghostColor = GhostOrange,
            drawableResId = com.cachecache.game.R.drawable.premium_ghost_disco_synthwave,
            isPremium = true
        ),
        AvatarItem(
            id = "premium_ghost_samurai",
            name = "Samouraï Kabuto",
            category = AvatarCategory.PREMIUM,
            description = "Casque orné traditionnel et honneur guerrier intouchable.",
            price = 2200,
            ghostColor = GhostOrange,
            drawableResId = com.cachecache.game.R.drawable.premium_ghost_samurai,
            isPremium = true
        ),
        AvatarItem(
            id = "premium_ghost_pharaoh",
            name = "Pharaon Solaire",
            category = AvatarCategory.PREMIUM,
            description = "Némès d'or et de lapis-lazuli des souverains du Nil.",
            price = 2400,
            ghostColor = GhostOrange,
            drawableResId = com.cachecache.game.R.drawable.premium_ghost_pharaoh,
            isPremium = true
        ),
        AvatarItem(
            id = "premium_ghost_vampire",
            name = "Comte Dracula",
            category = AvatarCategory.PREMIUM,
            description = "Cape de velours rouge, canines acérées et nœud papillon.",
            price = 2000,
            ghostColor = GhostOrange,
            drawableResId = com.cachecache.game.R.drawable.premium_ghost_vampire,
            isPremium = true
        ),
        AvatarItem(
            id = "premium_ghost_princess",
            name = "Princesse Impériale",
            category = AvatarCategory.PREMIUM,
            description = "Tiare incrustée de rubis et collier de perles précieuses.",
            price = 1800,
            ghostColor = GhostOrange,
            drawableResId = com.cachecache.game.R.drawable.premium_ghost_princess,
            isPremium = true
        ),
        AvatarItem(
            id = "premium_ghost_astral_mage",
            name = "Mage Astral",
            category = AvatarCategory.PREMIUM,
            description = "Chapeau mystique brodé d'étoiles et croissant de lune.",
            price = 2500,
            ghostColor = GhostOrange,
            drawableResId = com.cachecache.game.R.drawable.premium_ghost_astral_mage,
            isPremium = true
        ),
        AvatarItem(
            id = "premium_ghost_santa_deluxe",
            name = "Père Noël Deluxe",
            category = AvatarCategory.PREMIUM,
            description = "Bonnet en velours rouge, barbe blanche et houx festif.",
            price = 1900,
            ghostColor = GhostOrange,
            drawableResId = com.cachecache.game.R.drawable.premium_ghost_santa_deluxe,
            isPremium = true
        ),
        AvatarItem(
            id = "premium_ghost_steampunk",
            name = "Steampunk Aviateur",
            category = AvatarCategory.PREMIUM,
            description = "Lunettes de pilote en laiton et engrenages mécaniques.",
            price = 2100,
            ghostColor = GhostOrange,
            drawableResId = com.cachecache.game.R.drawable.premium_ghost_steampunk,
            isPremium = true
        ),
        AvatarItem(
            id = "premium_ghost_punk_rock",
            name = "Punk Rock Néon",
            category = AvatarCategory.PREMIUM,
            description = "Crête multicolore et lunettes shutter néon électriques.",
            price = 2000,
            ghostColor = GhostOrange,
            drawableResId = com.cachecache.game.R.drawable.premium_ghost_punk_rock,
            isPremium = true
        ),
        AvatarItem(
            id = "premium_ghost_mummy",
            name = "Momie Ancienne",
            category = AvatarCategory.PREMIUM,
            description = "Bandelettes sacrées et amulette Ânkh de vie éternelle.",
            price = 1900,
            ghostColor = GhostOrange,
            drawableResId = com.cachecache.game.R.drawable.premium_ghost_mummy,
            isPremium = true
        ),
        AvatarItem(
            id = "premium_ghost_divine_sun",
            name = "Divinité Solaire",
            category = AvatarCategory.PREMIUM,
            description = "Astres étincelants et couronne de triomphe d'or massif.",
            price = 3000,
            ghostColor = GhostOrange,
            drawableResId = com.cachecache.game.R.drawable.premium_ghost_divine_sun,
            isPremium = true
        ),
    )
}
