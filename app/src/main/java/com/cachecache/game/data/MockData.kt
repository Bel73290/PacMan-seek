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
        ),
        HiddenPlayer(
            id = 6,
            name = "Goldy",
            locationName = "Place Centrale Technolac",
            location = GeoPoint(45.6412, 5.8675),
            color = Color(0xFFFFD700),
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
        LeaderboardEntry(1, "ChasseurPro", 2340, level = 48, isCurrentUser = false, avatarColor = GhostPink, badgeType = "gold"),
        LeaderboardEntry(2, "FastGhost", 1760, level = 35, isCurrentUser = false, avatarColor = GhostCyan, badgeType = "silver"),
        LeaderboardEntry(3, "PacManette", 1520, level = 28, isCurrentUser = false, avatarColor = GhostRed, badgeType = "bronze"),
        LeaderboardEntry(4, "Cacheur23", 1340, level = 22, isCurrentUser = false, avatarColor = GhostOrange, badgeType = null),
        LeaderboardEntry(5, "Léo", 980, level = 18, isCurrentUser = false, avatarColor = GhostPurple, badgeType = null),
        LeaderboardEntry(6, "Shadow", 860, level = 15, isCurrentUser = false, avatarColor = GhostPink, badgeType = null),
        LeaderboardEntry(7, "Toi", 1820, level = 12, isCurrentUser = true, avatarColor = PacmanYellow, badgeType = null),
        LeaderboardEntry(8, "Nina", 730, level = 9, isCurrentUser = false, avatarColor = GhostCyan, badgeType = null),
        LeaderboardEntry(9, "GhostRider", 650, level = 5, isCurrentUser = false, avatarColor = GhostRed, badgeType = null),
        LeaderboardEntry(10, "Momo", 540, level = 2, isCurrentUser = false, avatarColor = GhostOrange, badgeType = null)
    )

    val FriendsLeaderboard = listOf(
        LeaderboardEntry(1, "Toi", 1820, level = 12, isCurrentUser = true, avatarColor = PacmanYellow, badgeType = "gold"),
        LeaderboardEntry(2, "FastGhost", 1760, level = 35, isCurrentUser = false, avatarColor = GhostCyan, badgeType = "silver"),
        LeaderboardEntry(3, "PacManette", 1520, level = 28, isCurrentUser = false, avatarColor = GhostRed, badgeType = "bronze"),
        LeaderboardEntry(4, "Léo", 980, level = 18, isCurrentUser = false, avatarColor = GhostPurple, badgeType = null),
        LeaderboardEntry(5, "Nina", 730, level = 9, isCurrentUser = false, avatarColor = GhostCyan, badgeType = null)
    )

    val SeasonsLeaderboard = listOf(
        LeaderboardEntry(1, "ChasseurPro", 4120, false, GhostPink, "gold", level = 52),
        LeaderboardEntry(2, "GhostRider", 3450, false, GhostRed, "silver", level = 41),
        LeaderboardEntry(3, "Toi", 2890, true, PacmanYellow, "bronze", level = 12),
        LeaderboardEntry(4, "Cacheur23", 2400, false, GhostOrange, null, level = 30),
        LeaderboardEntry(5, "Shadow", 1980, false, GhostPurple, null, level = 25)
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

    val AchievementSkinsList = listOf(
        AchievementSkinItem(
            id = "achieve_gold_king",
            avatarId = "premium_ghost_gold_king",
            title = "Roi Doré Suprême",
            requirementDescription = "Atteindre 1Mill de points",
            progressCurrent = 1_000_000,
            progressTarget = 1_000_000,
            isUnlocked = true,
            unlockDate = "25/09"
        ),
        AchievementSkinItem(
            id = "achieve_devil",
            avatarId = "ghost_blue_devil",
            title = "Diablotin Farceur",
            requirementDescription = "Attraper Sue dans le Bâtiment Éveillon",
            progressCurrent = 1,
            progressTarget = 1,
            isUnlocked = true,
            unlockDate = "12/04"
        ),
        AchievementSkinItem(
            id = "achieve_cyberpunk",
            avatarId = "premium_ghost_cyberpunk",
            title = "Cyberpunk 2099",
            requirementDescription = "Gagner 10 parties en tant que chercheur",
            progressCurrent = 7,
            progressTarget = 10,
            isUnlocked = false
        ),
        AchievementSkinItem(
            id = "achieve_pirate",
            avatarId = "premium_ghost_pirate_captain",
            title = "Capitaine Corsaire",
            requirementDescription = "Capturer 5 fantômes en moins de 3 minutes",
            progressCurrent = 3,
            progressTarget = 5,
            isUnlocked = false
        ),
        AchievementSkinItem(
            id = "achieve_angel",
            avatarId = "premium_ghost_angel_wings",
            title = "Séraphin Ailé",
            requirementDescription = "Terminer 3 parties consécutives sans radar",
            progressCurrent = 1,
            progressTarget = 3,
            isUnlocked = false
        )
    )

    // === BATTLE PASS : PALIERS TOUS LES 10 NIVEAUX (STYLE OVERWATCH) ===
    val BattlePassTiers = listOf(
        BattlePassTier(
            level = 10,
            avatarId = "ghost_red_headphones",
            title = "Blinky Gamer DJ",
            description = "Casque gaming immersif circum-aural pour mixer et traquer.",
            isPremium = false,
            rarity = "RARE",
            xpRequired = 500
        ),
        BattlePassTier(
            level = 20,
            avatarId = "avatar_ghost_cowboy",
            title = "Shérif de l'Ouest",
            description = "Stetson texan paré pour le duel au coucher du soleil.",
            isPremium = false,
            rarity = "RARE",
            xpRequired = 500
        ),
        BattlePassTier(
            level = 30,
            avatarId = "ghost_cyan_cap",
            title = "Inky Rider Urbain",
            description = "Casquette street portée à l'envers pour le parkour urbain.",
            isPremium = false,
            rarity = "ÉPIQUE",
            xpRequired = 500
        ),
        BattlePassTier(
            level = 40,
            avatarId = "avatar_ghost_pirate",
            title = "Fantôme Pirate",
            description = "Tricorne, cache-œil et esprit d'aventure sur les allées.",
            isPremium = false,
            rarity = "ÉPIQUE",
            xpRequired = 500
        ),
        BattlePassTier(
            level = 50,
            avatarId = "avatar_ghost_wizard",
            title = "Archimage Spectral",
            description = "Chapeau pointu mystique et arcanes de téléportation.",
            isPremium = false,
            rarity = "ÉPIQUE",
            xpRequired = 500
        ),
        BattlePassTier(
            level = 60,
            avatarId = "premium_ghost_crystal_ice",
            title = "Cristal de Givre",
            description = "Taillé dans la glace éternelle boréale scintillante.",
            isPremium = true,
            rarity = "LÉGENDAIRE",
            xpRequired = 500
        ),
        BattlePassTier(
            level = 70,
            avatarId = "premium_ghost_cyberpunk",
            title = "Cyberpunk 2099",
            description = "Visière HUD cybernétique et circuits imprimés néon.",
            isPremium = true,
            rarity = "LÉGENDAIRE",
            xpRequired = 500
        ),
        BattlePassTier(
            level = 80,
            avatarId = "premium_ghost_fire_flame",
            title = "Flamme Infernale",
            description = "Brasier incandescent issu des entrailles volcaniques.",
            isPremium = true,
            rarity = "LÉGENDAIRE",
            xpRequired = 500
        ),
        BattlePassTier(
            level = 90,
            avatarId = "premium_ghost_angel_wings",
            title = "Séraphin Ailé",
            description = "Ailes d'or célestes et rayonnement divin majestueux.",
            isPremium = true,
            rarity = "LÉGENDAIRE",
            xpRequired = 500
        ),
        BattlePassTier(
            level = 100,
            avatarId = "premium_ghost_gold_king",
            title = "Roi Doré Suprême",
            description = "Le skin Ultime : couronne en or massif 24 carats sertie de joyaux !",
            isPremium = true,
            rarity = "MYTHIQUE",
            xpRequired = 500
        )
    )

    val AvatarItemsCatalog = listOf(
        AvatarItem("pacman_classic", "Pacman Classique", AvatarCategory.CLASSIC, "Le chercheur original.", isAnimatedPacman = true),
        AvatarItem("pacman_neon", "Pacman Néon", AvatarCategory.PREMIUM, "Une lueur électrique pour la chasse.", price = 650, drawableResId = com.cachecache.game.R.drawable.avatar_pacman_neon, isPremium = true),
        AvatarItem("pacman_pirate", "Pacman Pirate", AvatarCategory.PREMIUM, "À l'abordage des cachettes.", price = 800, drawableResId = com.cachecache.game.R.drawable.avatar_pacman_pirate, isPremium = true),
        AvatarItem("pacman_wizard", "Pacman Mage", AvatarCategory.PREMIUM, "Un chercheur aux pouvoirs mystérieux.", price = 900, drawableResId = com.cachecache.game.R.drawable.avatar_pacman_wizard, isPremium = true),
        // =========================================================================
        // === 1. TOUS LES FANTÔMES CLASSIQUES & THÉMATIQUES (22 SKINS) ===
        // =========================================================================
        AvatarItem(
            id = "ghost_red_classic",
            name = "Blinky L'Ombre",
            category = AvatarCategory.CLASSIC,
            description = "Le fantôme rouge légendaire, rapide et redoutable.",
            price = 0,
            ghostColor = GhostRed,
            isAnimatedPacman = false,
            drawableResId = com.cachecache.game.R.drawable.avatar_ghost_red_classic,
            isPremium = false
        ),
        AvatarItem(
            id = "ghost_pink_classic",
            name = "Pinky La Furtive",
            category = AvatarCategory.CLASSIC,
            description = "Embusquée dans les ruelles, elle anticipe chaque virage.",
            price = 150,
            ghostColor = GhostPink,
            isAnimatedPacman = false,
            drawableResId = com.cachecache.game.R.drawable.avatar_ghost_pink_classic,
            isPremium = false
        ),
        AvatarItem(
            id = "ghost_cyan_classic",
            name = "Inky Le Farceur",
            category = AvatarCategory.CLASSIC,
            description = "Imprévisible et facétieux avec sa robe turquoise.",
            price = 200,
            ghostColor = GhostCyan,
            isAnimatedPacman = false,
            drawableResId = com.cachecache.game.R.drawable.avatar_ghost_cyan_classic,
            isPremium = false
        ),
        AvatarItem(
            id = "ghost_orange_classic",
            name = "Clyde Le Rêveur",
            category = AvatarCategory.CLASSIC,
            description = "Un esprit solitaire et contemplatif à la robe orangée.",
            price = 200,
            ghostColor = GhostOrange,
            isAnimatedPacman = false,
            drawableResId = com.cachecache.game.R.drawable.avatar_ghost_orange_classic,
            isPremium = false
        ),
        AvatarItem(
            id = "ghost_purple_classic",
            name = "Sue La Mystique",
            category = AvatarCategory.CLASSIC,
            description = "Fantôme nocturne drapée de violet sombre et énigmatique.",
            price = 250,
            ghostColor = GhostPurple,
            isAnimatedPacman = false,
            drawableResId = com.cachecache.game.R.drawable.avatar_ghost_dark_blue,
            isPremium = false
        ),
        AvatarItem(
            id = "ghost_blue_sunglasses",
            name = "Spectre VIP Lunettes",
            category = AvatarCategory.CLASSIC,
            description = "Impassible et ultra-stylé avec ses verres teintés sombres.",
            price = 350,
            ghostColor = GhostPurple,
            isAnimatedPacman = false,
            drawableResId = com.cachecache.game.R.drawable.avatar_ghost_blue_sunglasses,
            isPremium = false
        ),
        AvatarItem(
            id = "ghost_red_headphones",
            name = "Blinky Gamer DJ",
            category = AvatarCategory.CLASSIC,
            description = "Casque circum-aural pour mixer et traquer sur le meilleur son.",
            price = 450,
            ghostColor = GhostRed,
            isAnimatedPacman = false,
            drawableResId = com.cachecache.game.R.drawable.avatar_ghost_red_headphones,
            isPremium = false
        ),
        AvatarItem(
            id = "ghost_pink_flower",
            name = "Pinky Bohème",
            category = AvatarCategory.CLASSIC,
            description = "Ornée d'une fleur printanière cueillie au bord du lac.",
            price = 400,
            ghostColor = GhostPink,
            isAnimatedPacman = false,
            drawableResId = com.cachecache.game.R.drawable.avatar_ghost_pink_flower,
            isPremium = false
        ),
        AvatarItem(
            id = "ghost_cyan_cap",
            name = "Inky Rider Urbain",
            category = AvatarCategory.CLASSIC,
            description = "Casquette street portée à l'envers pour le parkour du campus.",
            price = 450,
            ghostColor = GhostCyan,
            isAnimatedPacman = false,
            drawableResId = com.cachecache.game.R.drawable.avatar_ghost_cyan_cap,
            isPremium = false
        ),
        AvatarItem(
            id = "ghost_red_crown",
            name = "Blinky Monarque",
            category = AvatarCategory.CLASSIC,
            description = "Couronné de rubis pour régner sur les couloirs universitaires.",
            price = 600,
            ghostColor = GhostRed,
            isAnimatedPacman = false,
            drawableResId = com.cachecache.game.R.drawable.avatar_ghost_red_crown,
            isPremium = false
        ),
        AvatarItem(
            id = "avatar_ghost_cowboy",
            name = "Shérif de l'Ouest",
            category = AvatarCategory.CLASSIC,
            description = "Stetson texan paré pour le duel au coucher du soleil.",
            price = 500,
            ghostColor = GhostOrange,
            isAnimatedPacman = false,
            drawableResId = com.cachecache.game.R.drawable.avatar_ghost_cowboy,
            isPremium = false
        ),
        AvatarItem(
            id = "avatar_ghost_wizard",
            name = "Archimage Spectral",
            category = AvatarCategory.CLASSIC,
            description = "Chapeau pointu infusé d'arcanes de téléportation furtive.",
            price = 600,
            ghostColor = GhostPurple,
            isAnimatedPacman = false,
            drawableResId = com.cachecache.game.R.drawable.avatar_ghost_wizard,
            isPremium = false
        ),
        AvatarItem(
            id = "avatar_ghost_pirate",
            name = "Fantôme Pirate",
            category = AvatarCategory.CLASSIC,
            description = "Tricorne, cache-œil et esprit d'aventure sur les mers.",
            price = 600,
            ghostColor = GhostCyan,
            isAnimatedPacman = false,
            drawableResId = com.cachecache.game.R.drawable.avatar_ghost_pirate,
            isPremium = false
        ),
        AvatarItem(
            id = "avatar_ghost_santa",
            name = "Père Noël Spectral",
            category = AvatarCategory.CLASSIC,
            description = "Bonnet à pompon rouge et cadeaux cachés dans la hotte.",
            price = 550,
            ghostColor = GhostRed,
            isAnimatedPacman = false,
            drawableResId = com.cachecache.game.R.drawable.avatar_ghost_santa,
            isPremium = false
        ),
        AvatarItem(
            id = "avatar_ghost_snorkel",
            name = "Plongeur Sous-Marin",
            category = AvatarCategory.CLASSIC,
            description = "Masque et tuba pour explorer le fond du lac du Bourget.",
            price = 450,
            ghostColor = GhostCyan,
            isAnimatedPacman = false,
            drawableResId = com.cachecache.game.R.drawable.avatar_ghost_snorkel,
            isPremium = false
        ),
        AvatarItem(
            id = "avatar_ghost_mustache",
            name = "Gentleman Moustache",
            category = AvatarCategory.CLASSIC,
            description = "Moustache soignée avec distinction et flegme britannique.",
            price = 400,
            ghostColor = GhostOrange,
            isAnimatedPacman = false,
            drawableResId = com.cachecache.game.R.drawable.avatar_ghost_mustache,
            isPremium = false
        ),
        AvatarItem(
            id = "ghost_blue_devil",
            name = "Diablotin Farceur",
            category = AvatarCategory.CLASSIC,
            description = "Cornes écarlates et malice infernale légendaire.",
            price = 500,
            ghostColor = GhostPurple,
            isAnimatedPacman = false,
            drawableResId = com.cachecache.game.R.drawable.avatar_ghost_blue_devil,
            isPremium = false
        ),
        AvatarItem(
            id = "ghost_blue_halo",
            name = "Ange Pur",
            category = AvatarCategory.CLASSIC,
            description = "Aura immaculée et anneau céleste brillant.",
            price = 500,
            ghostColor = GhostCyan,
            isAnimatedPacman = false,
            drawableResId = com.cachecache.game.R.drawable.avatar_ghost_blue_halo,
            isPremium = false
        ),
        AvatarItem(
            id = "ghost_scared",
            name = "Fantôme Apeuré Bleu",
            category = AvatarCategory.CLASSIC,
            description = "Le spectre bleu tremblant lorsqu'une super pac-gomme est avalée !",
            price = 300,
            ghostColor = ScaredBlue,
            isAnimatedPacman = false,
            drawableResId = com.cachecache.game.R.drawable.ghost_scared_blue,
            isPremium = false
        ),

        // =========================================================================
        // === 3. TOUS LES SKINS PREMIUM (20 SKINS ÉPIQUES) ===
        // =========================================================================
        AvatarItem(
            id = "premium_ghost_crystal_ice",
            name = "Cristal de Givre",
            category = AvatarCategory.PREMIUM,
            description = "Taillé dans la glace éternelle avec des reflets prismatiques scintillants.",
            price = 2000,
            ghostColor = GhostCyan,
            isAnimatedPacman = false,
            drawableResId = com.cachecache.game.R.drawable.premium_ghost_crystal_ice,
            isPremium = true
        ),
        AvatarItem(
            id = "premium_ghost_fire_flame",
            name = "Flamme Infernale",
            category = AvatarCategory.PREMIUM,
            description = "Brasier incandescent pulsant d'une énergie volcanique bouillonnante.",
            price = 2200,
            ghostColor = GhostRed,
            isAnimatedPacman = false,
            drawableResId = com.cachecache.game.R.drawable.premium_ghost_fire_flame,
            isPremium = true
        ),
        AvatarItem(
            id = "premium_ghost_gold_king",
            name = "Roi Doré Suprême",
            category = AvatarCategory.PREMIUM,
            description = "Souverain auréolé d'or pur. Le prestige absolu du Palier 100 !",
            price = 3000,
            ghostColor = GhostOrange,
            isAnimatedPacman = false,
            drawableResId = com.cachecache.game.R.drawable.premium_ghost_gold_king,
            isPremium = true
        ),
        AvatarItem(
            id = "premium_ghost_cyberpunk",
            name = "Cyberpunk 2099",
            category = AvatarCategory.PREMIUM,
            description = "Visière HUD cybernétique et circuits imprimés néon haute technologie.",
            price = 2100,
            ghostColor = GhostCyan,
            isAnimatedPacman = false,
            drawableResId = com.cachecache.game.R.drawable.premium_ghost_cyberpunk,
            isPremium = true
        ),
        AvatarItem(
            id = "premium_ghost_angel_wings",
            name = "Séraphin Ailé",
            category = AvatarCategory.PREMIUM,
            description = "Ailes d'or divines et auréole céleste majestueuse.",
            price = 2400,
            ghostColor = GhostPink,
            isAnimatedPacman = false,
            drawableResId = com.cachecache.game.R.drawable.premium_ghost_angel_wings,
            isPremium = true
        ),
        AvatarItem(
            id = "premium_ghost_disco_synthwave",
            name = "Disco Synthwave",
            category = AvatarCategory.PREMIUM,
            description = "Ambiance néon rétro 80s et lunettes à persiennes dorées.",
            price = 2300,
            ghostColor = GhostPink,
            isAnimatedPacman = false,
            drawableResId = com.cachecache.game.R.drawable.premium_ghost_disco_synthwave,
            isPremium = true
        ),
        AvatarItem(
            id = "premium_ghost_vampire",
            name = "Comte Dracula",
            category = AvatarCategory.PREMIUM,
            description = "Cape de velours rouge, canines acérées et nœud papillon.",
            price = 2000,
            ghostColor = GhostPurple,
            isAnimatedPacman = false,
            drawableResId = com.cachecache.game.R.drawable.premium_ghost_vampire,
            isPremium = true
        ),
        AvatarItem(
            id = "premium_ghost_steampunk",
            name = "Steampunk Aviateur",
            category = AvatarCategory.PREMIUM,
            description = "Lunettes de pilote en laiton et engrenages mécaniques ouvragés.",
            price = 2100,
            ghostColor = GhostOrange,
            isAnimatedPacman = false,
            drawableResId = com.cachecache.game.R.drawable.premium_ghost_steampunk,
            isPremium = true
        ),
        AvatarItem(
            id = "premium_ghost_astral_mage",
            name = "Mage Astral",
            category = AvatarCategory.PREMIUM,
            description = "Chapeau mystique brodé d'étoiles filantes et croissant lunaire.",
            price = 2500,
            ghostColor = GhostCyan,
            isAnimatedPacman = false,
            drawableResId = com.cachecache.game.R.drawable.premium_ghost_astral_mage,
            isPremium = true
        ),
        AvatarItem(
            id = "premium_ghost_divine_sun",
            name = "Divinité Solaire",
            category = AvatarCategory.PREMIUM,
            description = "Astres étincelants et couronne de triomphe d'or massif.",
            price = 3000,
            ghostColor = PacmanYellow,
            isAnimatedPacman = false,
            drawableResId = com.cachecache.game.R.drawable.premium_ghost_divine_sun,
            isPremium = true
        ),
        AvatarItem(
            id = "premium_ghost_pirate_captain",
            name = "Vieux Loup de Mer",
            category = AvatarCategory.PREMIUM,
            description = "Capitaine corsaire légendaire avec barbe blanche et bicorne d'or.",
            price = 1800,
            ghostColor = GhostOrange,
            isAnimatedPacman = false,
            drawableResId = com.cachecache.game.R.drawable.premium_ghost_pirate_captain,
            isPremium = true
        ),
        AvatarItem(
            id = "premium_ghost_flower_spring",
            name = "Fleur Printanière",
            category = AvatarCategory.PREMIUM,
            description = "Pétales fleuris et couronne florale parfumée des Alpes.",
            price = 1900,
            ghostColor = GhostPink,
            isAnimatedPacman = false,
            drawableResId = com.cachecache.game.R.drawable.premium_ghost_flower_spring,
            isPremium = true
        ),
        AvatarItem(
            id = "premium_ghost_mummy",
            name = "Momie Millénaire",
            category = AvatarCategory.PREMIUM,
            description = "Bandelettes antiques enveloppant un esprit venu du fond des âges.",
            price = 2000,
            ghostColor = GhostOrange,
            isAnimatedPacman = false,
            drawableResId = com.cachecache.game.R.drawable.premium_ghost_mummy,
            isPremium = true
        ),
        AvatarItem(
            id = "premium_ghost_pharaoh",
            name = "Pharaon d'Égypte",
            category = AvatarCategory.PREMIUM,
            description = "Coiffe royale némès d'or et de lapis-lazuli des souverains du Nil.",
            price = 2500,
            ghostColor = GhostOrange,
            isAnimatedPacman = false,
            drawableResId = com.cachecache.game.R.drawable.premium_ghost_pharaoh,
            isPremium = true
        ),
        AvatarItem(
            id = "premium_ghost_princess",
            name = "Princesse Royale",
            category = AvatarCategory.PREMIUM,
            description = "Diadème de princesse étincelant de mille feux et ruban rose.",
            price = 2100,
            ghostColor = GhostPink,
            isAnimatedPacman = false,
            drawableResId = com.cachecache.game.R.drawable.premium_ghost_princess,
            isPremium = true
        ),
        AvatarItem(
            id = "premium_ghost_santa_deluxe",
            name = "Père Noël Deluxe",
            category = AvatarCategory.PREMIUM,
            description = "Tenue de fêtes en velours brodé et grande barbe blanche d'hiver.",
            price = 2200,
            ghostColor = GhostRed,
            isAnimatedPacman = false,
            drawableResId = com.cachecache.game.R.drawable.premium_ghost_santa_deluxe,
            isPremium = true
        )
    )
}
