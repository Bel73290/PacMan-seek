package com.cachecache.game.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import kotlinx.coroutines.launch
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.cachecache.game.R
import com.cachecache.game.data.MockData
import com.cachecache.game.model.*
import com.cachecache.game.ui.components.*
import com.cachecache.game.ui.theme.*

@Composable
fun ProfileScreen(
    state: GameState,
    onNavigate: (Screen) -> Unit,
    onBack: () -> Unit = { onNavigate(Screen.SPLASH) },
    onOpenSettings: () -> Unit = {},
    onEquipAvatar: (String) -> Unit = {},
    onSelectAchievementSkin: (AchievementSkinItem?) -> Unit = {},
    onSelectBadge: (BadgeItem?) -> Unit = {},
    onAddXp: (Int) -> Unit = {},
    onAddLevel: (Int) -> Unit = {}
) {
    Scaffold(
        containerColor = DeepNavy,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            BottomNavigationBar(
                currentScreen = Screen.PROFILE,
                onNavigate = onNavigate
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(bottom = paddingValues.calculateBottomPadding())
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 2.dp)
        ) {
            val equippedAvatar = MockData.AvatarItemsCatalog.firstOrNull { it.id == state.equippedAvatarId }
                ?: MockData.AvatarItemsCatalog.first()

            val currentTiers = MockData.BattlePassTiers
            val nextMilestoneIndex = currentTiers.indexOfFirst { it.level > state.userLevel }
                .let { if (it >= 0) it else currentTiers.size - 1 }
            var selectedTierIndex by remember { mutableStateOf(nextMilestoneIndex) }
            val listState = rememberLazyListState(initialFirstVisibleItemIndex = (nextMilestoneIndex - 1).coerceAtLeast(0))
            val coroutineScope = rememberCoroutineScope()
            val selectedTier = currentTiers.getOrElse(selectedTierIndex) { currentTiers.first() }
            val selectedAvatar = MockData.AvatarItemsCatalog.firstOrNull { it.id == selectedTier.avatarId }
            val isSelectedTierUnlocked = state.userLevel >= selectedTier.level
            val isSelectedTierEquipped = state.equippedAvatarId == selectedTier.avatarId

            // Header: Back Arrow + Skin Avatar + Level & Responsive XP Bar (fills width)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(NavyIntermediate)
                        .border(1.dp, BorderSubtle, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Retour",
                        tint = TextPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Equipped Skin Avatar
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(NavyIntermediate)
                        .border(2.dp, PacmanYellow, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    AvatarVisual(avatar = equippedAvatar, size = 38.dp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Responsive Player Info & Progress
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Toi",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black
                        )

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = NavyIntermediate,
                            border = androidx.compose.foundation.BorderStroke(1.dp, PacmanYellow.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "Niveau ${state.userLevel}",
                                color = PacmanYellow,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(5.dp))

                    // Responsive XP Progress Bar expanding across the remaining space
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LinearProgressIndicator(
                            progress = { (state.userXp.toFloat() / state.maxXp.toFloat()).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .weight(1f)
                                .height(7.dp)
                                .clip(RoundedCornerShape(3.5.dp)),
                            color = PacmanYellow,
                            trackColor = CardSurface
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "${formatCompactScore(state.userXp)} / ${formatCompactScore(state.maxXp)} pts",
                            color = TextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Three Key Metrics Grid (Points with Pac-gomme, Parties with Gamepad, Victoires with Medal)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Points (Pac-gomme round yellow circle) formatted with k / Mill
                StatCard(
                    modifier = Modifier.weight(1f),
                    value = formatCompactScore(state.userScore),
                    label = "Points",
                    iconContent = {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .shadow(6.dp, CircleShape, spotColor = PacmanYellow)
                                .clip(CircleShape)
                                .background(PacmanYellow)
                        )
                    }
                )

                // Parties (Gamepad Icon in Purple)
                StatCard(
                    modifier = Modifier.weight(1f),
                    value = state.totalGamesPlayed.toString(),
                    label = "Parties",
                    iconContent = {
                        Icon(
                            imageVector = Icons.Default.SportsEsports,
                            contentDescription = "Parties",
                            tint = GhostPurple,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )

                // Victoires (Medal)
                StatCard(
                    modifier = Modifier.weight(1f),
                    value = state.totalVictories.toString(),
                    label = "Victoires",
                    iconContent = { MedalComposable(size = 18.dp, rank = 1) }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // =========================================================================
            // 3. PASS DE COMBAT / OVERWATCH STYLE BATTLE PASS
            // =========================================================================
            // 3.1 Header (Season, Countdown, Pass Status)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "PASS DE COMBAT",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                    }
                    Text(
                        text = "SAISON 1 • 84 JOURS RESTANTS",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFFA9C1E).copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFA9C1E))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.WorkspacePremium,
                            contentDescription = null,
                            tint = Color(0xFFFA9C1E),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "NIV. ${state.userLevel}",
                            color = Color(0xFFFA9C1E),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3.2 Hero Showcase Stage (Overwatch Centerpiece)
            val rarityColor = when (selectedTier.rarity.uppercase()) {
                "MYTHIQUE" -> Color(0xFFFF4081)
                "LÉGENDAIRE" -> Color(0xFFFA9C1E)
                "ÉPIQUE" -> Color(0xFFAB47BC)
                else -> Color(0xFF29B6F6)
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.5.dp,
                        Brush.verticalGradient(
                            listOf(
                                rarityColor.copy(alpha = 0.8f),
                                Color(0xFF1E293B)
                            )
                        ),
                        RoundedCornerShape(20.dp)
                    ),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Top Info Row: Tier Level Title & Rarity Tag
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "PALIER ${selectedTier.level}",
                                color = rarityColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = selectedTier.title,
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = rarityColor.copy(alpha = 0.2f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, rarityColor)
                            ) {
                                Text(
                                    text = selectedTier.rarity,
                                    color = rarityColor,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (selectedTier.isPremium) Color(0xFFFA9C1E) else Color.White,
                            ) {
                                Text(
                                    text = if (selectedTier.isPremium) "PREMIUM" else "GRATUIT",
                                    color = if (selectedTier.isPremium) Color.White else Color(0xFF0F172A),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Character Podium / Spotlight Stage
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(175.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Floor Pedestal Glow (Radial spotlight)
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth(0.75f)
                                .height(50.dp)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            rarityColor.copy(alpha = 0.55f),
                                            rarityColor.copy(alpha = 0.15f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )

                        // Avatar Model
                        if (selectedAvatar != null) {
                            AvatarVisual(
                                avatar = selectedAvatar,
                                size = 135.dp
                            )
                        } else {
                            GhostComposable(
                                size = 130.dp,
                                baseColor = GhostCyan,
                                isScared = false
                            )
                        }

                        // Lock Badge if not unlocked
                        if (!isSelectedTierUnlocked) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xCC000000),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                                modifier = Modifier.align(Alignment.TopEnd)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Niv. ${selectedTier.level}",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Description & XP Requirements
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedAvatar?.description ?: "Récompense de palier",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            maxLines = 1,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = if (isSelectedTierUnlocked) "✓ Débloqué" else "XP : ${state.userXp % 100}/100",
                            color = if (isSelectedTierUnlocked) Color(0xFF10B981) else TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Action Button (Equip / Equipped / Locked)
                    when {
                        isSelectedTierEquipped -> {
                            Button(
                                onClick = { },
                                enabled = false,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    disabledContainerColor = Color(0xFF10B981).copy(alpha = 0.2f),
                                    disabledContentColor = Color(0xFF10B981)
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "SKIN ACTUELLEMENT ÉQUIPÉ",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                        isSelectedTierUnlocked -> {
                            Button(
                                onClick = { onEquipAvatar(selectedTier.avatarId) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFFA9C1E),
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "ÉQUIPER CE SKIN",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                        else -> {
                            Button(
                                onClick = { },
                                enabled = false,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    disabledContainerColor = Color(0xFF1E293B).copy(alpha = 0.6f),
                                    disabledContentColor = TextSecondary
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "DÉVERROUILLÉ AU NIVEAU ${selectedTier.level}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3.3 Horizontal Carousel Ribbon Track (Overwatch Bottom Track)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Arrow
                IconButton(
                    onClick = {
                        if (selectedTierIndex > 0) {
                            selectedTierIndex--
                            coroutineScope.launch {
                                listState.animateScrollToItem((selectedTierIndex - 1).coerceAtLeast(0))
                            }
                        }
                    },
                    enabled = selectedTierIndex > 0,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(if (selectedTierIndex > 0) Color(0xFF1E293B) else Color.Transparent)
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = "Précédent",
                        tint = if (selectedTierIndex > 0) Color.White else Color.Gray.copy(alpha = 0.4f),
                        modifier = Modifier.size(24.dp)
                    )
                }

                // LazyRow Track
                LazyRow(
                    state = listState,
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    itemsIndexed(currentTiers) { idx, tier ->
                        val avatar = MockData.AvatarItemsCatalog.firstOrNull { it.id == tier.avatarId }
                        val isUnlocked = state.userLevel >= tier.level
                        OverwatchTierCard(
                            tier = tier,
                            avatar = avatar,
                            isUnlocked = isUnlocked,
                            isSelected = selectedTierIndex == idx,
                            userLevel = state.userLevel,
                            onClick = {
                                selectedTierIndex = idx
                                coroutineScope.launch {
                                    listState.animateScrollToItem((idx - 1).coerceAtLeast(0))
                                }
                            }
                        )
                    }
                }

                // Right Arrow
                IconButton(
                    onClick = {
                        if (selectedTierIndex < currentTiers.size - 1) {
                            selectedTierIndex++
                            coroutineScope.launch {
                                listState.animateScrollToItem((selectedTierIndex - 1).coerceAtLeast(0))
                            }
                        }
                    },
                    enabled = selectedTierIndex < currentTiers.size - 1,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(if (selectedTierIndex < currentTiers.size - 1) Color(0xFF1E293B) else Color.Transparent)
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Suivant",
                        tint = if (selectedTierIndex < currentTiers.size - 1) Color.White else Color.Gray.copy(alpha = 0.4f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 3.4 Page Indicators (1 2 3 ... 10)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                currentTiers.forEachIndexed { idx, _ ->
                    val isCurrent = selectedTierIndex == idx
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .clickable {
                                selectedTierIndex = idx
                                coroutineScope.launch {
                                    listState.animateScrollToItem((idx - 1).coerceAtLeast(0))
                                }
                            }
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${idx + 1}",
                                color = if (isCurrent) Color.White else TextSecondary.copy(alpha = 0.6f),
                                fontWeight = if (isCurrent) FontWeight.Black else FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            if (isCurrent) {
                                Box(
                                    modifier = Modifier
                                        .width(14.dp)
                                        .height(2.dp)
                                        .background(Color(0xFFFA9C1E))
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 4. Section "Skins de Défis & Exploits"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Skins de Défis",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Accomplis des exploits spéciaux pour débloquer ces skins",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                MockData.AchievementSkinsList.forEach { achievement ->
                    val isEquipped = state.equippedAvatarId == achievement.avatarId
                    AchievementSkinCard(
                        achievement = achievement,
                        isEquipped = isEquipped,
                        onEquip = { onEquipAvatar(achievement.avatarId) },
                        onClick = { onSelectAchievementSkin(achievement) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }

        // Achievement Skin Detail Dialog
        state.selectedAchievementSkin?.let { achievement ->
            val isEquipped = state.equippedAvatarId == achievement.avatarId
            AchievementSkinDetailDialog(
                achievement = achievement,
                isEquipped = isEquipped,
                onEquip = { onEquipAvatar(achievement.avatarId) },
                onDismiss = { onSelectAchievementSkin(null) }
            )
        }

        // Fallback Badge Detail Dialog
        state.selectedBadgeDetail?.let { badge ->
            BadgeDetailDialog(
                badge = badge,
                onDismiss = { onSelectBadge(null) }
            )
        }
    }
}

@Composable
private fun OverwatchTierCard(
    tier: BattlePassTier,
    avatar: AvatarItem?,
    isUnlocked: Boolean,
    isSelected: Boolean,
    userLevel: Int,
    onClick: () -> Unit
) {
    val cardBorderColor = when {
        isSelected -> Color.White
        isUnlocked -> Color(0xFFFFD700).copy(alpha = 0.5f)
        tier.isPremium -> Color(0xFFFA9C1E).copy(alpha = 0.35f)
        else -> BorderSubtle
    }

    Box(
        modifier = Modifier
            .width(82.dp)
            .height(126.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF131B2C))
            .border(
                width = if (isSelected) 2.5.dp else 1.dp,
                color = cardBorderColor,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Top Ribbon Badge: GRATUIT or PREMIUM
            val bannerBg = if (tier.isPremium) Color(0xFFFA9C1E) else Color.White
            val bannerTextColor = if (tier.isPremium) Color.White else Color(0xFF111827)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp)
                    .background(bannerBg),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (tier.isPremium) "PREMIUM" else "GRATUIT",
                    color = bannerTextColor,
                    fontWeight = FontWeight.Black,
                    fontSize = 9.sp,
                    letterSpacing = 0.5.sp
                )
            }

            // 2. Middle Body: Ghost Thumbnail + Lock Watermark
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                if (avatar != null) {
                    AvatarVisual(avatar = avatar, size = 44.dp)
                } else {
                    GhostComposable(size = 40.dp, baseColor = GhostCyan, isScared = false)
                }

                if (!isUnlocked) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0x73000000)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Verrouillé",
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }

            // 3. Bottom Footer: Tier Number Pill
            val footerBg = when {
                isSelected -> Color.White.copy(alpha = 0.15f)
                isUnlocked -> Color(0x33FFD700)
                else -> Color(0xFF0B101D)
            }
            val footerTextColor = when {
                isSelected -> Color.White
                isUnlocked -> Color(0xFFFFE066)
                else -> TextSecondary
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .background(footerBg),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${tier.level}",
                    color = footerTextColor,
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun AchievementSkinCard(
    achievement: AchievementSkinItem,
    isEquipped: Boolean,
    onEquip: () -> Unit,
    onClick: () -> Unit
) {
    val avatar = MockData.AvatarItemsCatalog.firstOrNull { it.id == achievement.avatarId }
    val borderColor = when {
        isEquipped -> PacmanYellow
        achievement.isUnlocked -> Color(0xFFFFD700).copy(alpha = 0.6f)
        else -> BorderSubtle
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.2.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar with unlock badge/lock icon
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(NavyIntermediate)
                    .border(
                        1.5.dp,
                        if (achievement.isUnlocked) Color(0xFFFFD700) else BorderSubtle,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (avatar != null) {
                    AvatarVisual(avatar = avatar, size = 34.dp)
                } else {
                    GhostComposable(size = 30.dp, baseColor = GhostCyan, isScared = false)
                }

                if (!achievement.isUnlocked) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.45f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Verrouillé",
                            tint = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Details & Progress
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = achievement.title,
                    color = if (achievement.isUnlocked) Color(0xFFFFE066) else TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = achievement.requirementDescription,
                    color = TextSecondary,
                    fontSize = 10.sp,
                    lineHeight = 13.sp,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Progress Indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    LinearProgressIndicator(
                        progress = { achievement.progressRatio },
                        modifier = Modifier
                            .weight(1f)
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (achievement.isUnlocked) Color(0xFFFFD700) else PacmanYellow,
                        trackColor = NavyIntermediate
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = "${formatCompactScore(achievement.progressCurrent)}/${formatCompactScore(achievement.progressTarget)}",
                        color = if (achievement.isUnlocked) Color(0xFFFFD700) else TextSecondary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Action / Status Button
            when {
                isEquipped -> {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = PacmanYellow,
                        modifier = Modifier.height(34.dp)
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "ÉQUIPÉ",
                                color = DarkContrast,
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
                achievement.isUnlocked -> {
                    Button(
                        onClick = onEquip,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFD700),
                            contentColor = DarkContrast
                        ),
                        modifier = Modifier.height(34.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                    ) {
                        Text(
                            text = "Équiper",
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp
                        )
                    }
                }
                else -> {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = NavyIntermediate,
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Défi",
                                color = TextSecondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AchievementSkinDetailDialog(
    achievement: AchievementSkinItem,
    isEquipped: Boolean,
    onEquip: () -> Unit,
    onDismiss: () -> Unit
) {
    val avatar = MockData.AvatarItemsCatalog.firstOrNull { it.id == achievement.avatarId }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .shadow(16.dp, RoundedCornerShape(26.dp), spotColor = if (achievement.isUnlocked) Color(0xFFFFD700) else Color.Black)
                .border(
                    1.5.dp,
                    if (achievement.isUnlocked) Color(0xFFFFD700) else BorderSubtle,
                    RoundedCornerShape(26.dp)
                ),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = NavyIntermediate)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (achievement.isUnlocked) Color(0xFFFFD700).copy(alpha = 0.2f) else CardSurface,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (achievement.isUnlocked) Color(0xFFFFD700) else BorderSubtle
                    )
                ) {
                    Text(
                        text = if (achievement.isUnlocked) "★ SKIN DÉBLOQUÉ ★" else "DÉFI EN COURS",
                        color = if (achievement.isUnlocked) Color(0xFFFFD700) else TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Avatar showcase
                Box(
                    modifier = Modifier
                        .size(86.dp)
                        .clip(CircleShape)
                        .background(CardSurface)
                        .border(
                            3.dp,
                            if (achievement.isUnlocked) Color(0xFFFFD700) else BorderSubtle,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (avatar != null) {
                        AvatarVisual(avatar = avatar, size = 70.dp)
                    } else {
                        GhostComposable(size = 60.dp, baseColor = GhostCyan, isScared = false)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = achievement.title,
                    color = if (achievement.isUnlocked) Color(0xFFFFE066) else TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = achievement.requirementDescription,
                    color = TextSecondary,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Progress bar
                LinearProgressIndicator(
                    progress = { achievement.progressRatio },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (achievement.isUnlocked) Color(0xFFFFD700) else PacmanYellow,
                    trackColor = CardSurface
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Progression : ${formatCompactScore(achievement.progressCurrent)} / ${formatCompactScore(achievement.progressTarget)}",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Action button
                if (achievement.isUnlocked) {
                    if (isEquipped) {
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            shape = RoundedCornerShape(23.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CardSurface,
                                contentColor = PacmanYellow
                            )
                        ) {
                            Text(text = "Actuellement équipé", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    } else {
                        Button(
                            onClick = {
                                onEquip()
                                onDismiss()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            shape = RoundedCornerShape(23.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFFD700),
                                contentColor = DarkContrast
                            )
                        ) {
                            Text(text = "Équiper ce skin", fontWeight = FontWeight.Black, fontSize = 14.sp)
                        }
                    }
                } else {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = RoundedCornerShape(23.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CardSurface,
                            contentColor = TextPrimary
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                    ) {
                        Text(text = "Fermer", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    iconContent: @Composable () -> Unit
) {
    Card(
        modifier = modifier.border(1.dp, BorderSubtle, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NavyIntermediate)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.height(24.dp),
                contentAlignment = Alignment.Center
            ) {
                iconContent()
            }

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = value,
                color = TextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Black
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = label,
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

