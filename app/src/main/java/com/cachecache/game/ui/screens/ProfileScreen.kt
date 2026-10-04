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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
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
    onSelectBadge: (BadgeItem?) -> Unit = {}
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

            Spacer(modifier = Modifier.height(12.dp))

            // 3. Section "Skins à débloquer" (replaces static Badges)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Skins à débloquer",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Accomplis des exploits pour débloquer ces skins exclusifs",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

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

            Spacer(modifier = Modifier.height(20.dp))

            // 4. Section "Historique récent"
            Text(
                text = "Historique récent",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                state.recentHistory.forEach { historyItem ->
                    HistoryRow(item = historyItem)
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

@Composable
private fun HistoryRow(item: HistoryItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(NavyIntermediate),
                contentAlignment = Alignment.Center
            ) {
                when (item.iconType) {
                    "ghost" -> GhostComposable(size = 20.dp, baseColor = GhostPurple, isScared = false)
                    "ranking" -> TrophyComposable(size = 18.dp, rank = 2)
                    "trophy" -> StarComposable(size = 18.dp, color = GhostOrange)
                    else -> GhostComposable(size = 20.dp, baseColor = GhostCyan, isScared = false)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${item.date} ${item.title}",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Text(
                text = "+${formatCompactScore(item.deltaScore)} pts",
                color = PacmanYellow,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}
