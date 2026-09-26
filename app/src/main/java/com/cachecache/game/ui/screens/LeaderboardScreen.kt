package com.cachecache.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cachecache.game.data.MockData
import com.cachecache.game.model.*
import com.cachecache.game.ui.components.BottomNavigationBar
import com.cachecache.game.ui.components.GhostComposable
import com.cachecache.game.ui.components.MedalComposable
import com.cachecache.game.ui.components.PacmanComposable
import com.cachecache.game.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

@Composable
fun LeaderboardScreen(
    state: GameState,
    onBack: () -> Unit,
    onNavigate: (Screen) -> Unit,
    onSelectTab: (LeaderboardTab) -> Unit
) {
    // Dynamic list based on selected tab and user score, sorted by score descending
    val entries = when (state.activeLeaderboardTab) {
        LeaderboardTab.GENERAL -> MockData.GeneralLeaderboard.map {
            if (it.isCurrentUser) it.copy(score = state.userScore) else it
        }
        LeaderboardTab.FRIENDS -> MockData.FriendsLeaderboard.map {
            if (it.isCurrentUser) it.copy(score = state.userScore) else it
        }
    }.sortedByDescending { it.score }.mapIndexed { index, entry ->
        entry.copy(rank = index + 1)
    }

    val top1 = entries.getOrNull(0)
    val top2 = entries.getOrNull(1)
    val top3 = entries.getOrNull(2)
    val remainingEntries = if (entries.size > 3) entries.subList(3, entries.size) else emptyList()

    Scaffold(
        containerColor = DeepNavy,
        bottomBar = {
            BottomNavigationBar(
                currentScreen = Screen.LEADERBOARD,
                onNavigate = onNavigate
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .statusBarsPadding()
        ) {
            // Header: Back Arrow & Title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 2.dp, bottom = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(NavyIntermediate)
                        .border(1.dp, BorderSubtle, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Retour",
                        tint = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Text(
                    text = "Classement",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Filter Tabs (Pills): Général vs Amis
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LeaderboardTab.entries.forEach { tab ->
                    val isSelected = state.activeLeaderboardTab == tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                            .clip(RoundedCornerShape(17.dp))
                            .background(if (isSelected) PacmanYellow else CardSurface)
                            .border(1.dp, if (isSelected) PacmanYellow else BorderSubtle, RoundedCornerShape(17.dp))
                            .clickable { onSelectTab(tab) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab.label,
                            color = if (isSelected) DarkContrast else TextSecondary,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // LazyColumn with Podium at top and remaining ranks below
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                // Podium Header (Top 3)
                item {
                    LeaderboardPodium(
                        top1 = top1,
                        top2 = top2,
                        top3 = top3,
                        equippedAvatarId = state.equippedAvatarId
                    )
                }

                // Remaining Ranks (4th place and below)
                items(remainingEntries) { entry ->
                    LeaderboardRow(
                        entry = entry,
                        equippedAvatarId = state.equippedAvatarId
                    )
                }
            }
        }
    }
}

@Composable
private fun LeaderboardPodium(
    top1: LeaderboardEntry?,
    top2: LeaderboardEntry?,
    top3: LeaderboardEntry?,
    equippedAvatarId: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, start = 8.dp, end = 8.dp, bottom = 0.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.Bottom
            ) {
                // #2 Silver (Left)
                top2?.let { entry ->
                    PodiumColumn(
                        entry = entry,
                        equippedAvatarId = equippedAvatarId,
                        rank = 2,
                        pedestalHeight = 62.dp,
                        accentColor = Color(0xFFC0C0C0),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // #1 Gold (Center, highest)
                top1?.let { entry ->
                    PodiumColumn(
                        entry = entry,
                        equippedAvatarId = equippedAvatarId,
                        rank = 1,
                        pedestalHeight = 88.dp,
                        accentColor = Color(0xFFFFD700),
                        modifier = Modifier.weight(1.15f)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // #3 Bronze (Right)
                top3?.let { entry ->
                    PodiumColumn(
                        entry = entry,
                        equippedAvatarId = equippedAvatarId,
                        rank = 3,
                        pedestalHeight = 48.dp,
                        accentColor = Color(0xFFCD7F32),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun PodiumColumn(
    entry: LeaderboardEntry,
    equippedAvatarId: String,
    rank: Int,
    pedestalHeight: androidx.compose.ui.unit.Dp,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val avatarSize = if (rank == 1) 46.dp else 38.dp

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Medal Badge (Olympic style medal instead of trophy cup)
        MedalComposable(size = if (rank == 1) 24.dp else 20.dp, rank = rank)

        Spacer(modifier = Modifier.height(2.dp))

        // Avatar
        Box(
            modifier = Modifier
                .size(avatarSize)
                .clip(CircleShape)
                .background(NavyIntermediate)
                .border(if (rank == 1) 2.5.dp else 2.dp, accentColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (entry.isCurrentUser) {
                val equippedAvatar = MockData.AvatarItemsCatalog.firstOrNull { it.id == equippedAvatarId }
                    ?: MockData.AvatarItemsCatalog.first()
                AvatarVisual(avatar = equippedAvatar, size = avatarSize * 0.72f)
            } else {
                GhostComposable(size = avatarSize * 0.68f, baseColor = entry.avatarColor, isScared = false)
            }
        }

        Spacer(modifier = Modifier.height(3.dp))

        // Player Name
        Text(
            text = entry.name,
            color = if (entry.isCurrentUser) PacmanYellow else TextPrimary,
            fontWeight = if (rank == 1 || entry.isCurrentUser) FontWeight.Black else FontWeight.Bold,
            fontSize = if (rank == 1) 12.sp else 11.sp,
            maxLines = 1,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        // Player Score
        Text(
            text = "${formatCompactScore(entry.score)} pts",
            color = accentColor,
            fontWeight = FontWeight.ExtraBold,
            fontSize = if (rank == 1) 10.sp else 9.sp,
            maxLines = 1,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Pedestal Block
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(pedestalHeight)
                .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                .background(
                    if (rank == 1) NavyIntermediate else DarkContrast.copy(alpha = 0.8f)
                )
                .border(
                    1.2.dp,
                    accentColor.copy(alpha = if (rank == 1) 0.8f else 0.5f),
                    RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$rank",
                    color = accentColor,
                    fontSize = if (rank == 1) 22.sp else 16.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
private fun LeaderboardRow(
    entry: LeaderboardEntry,
    equippedAvatarId: String = "pacman_classic"
) {
    val formattedScore = "${formatCompactScore(entry.score)} pts"

    val rowBackground = if (entry.isCurrentUser) SelectedSurface else CardSurface
    val rowBorder = if (entry.isCurrentUser) PacmanYellow.copy(alpha = 0.6f) else BorderSubtle

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, rowBorder, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = rowBackground)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank Badge or Number
            Box(
                modifier = Modifier.width(32.dp),
                contentAlignment = Alignment.Center
            ) {
                when (entry.rank) {
                    1, 2, 3 -> MedalComposable(size = 22.dp, rank = entry.rank)
                    else -> Text(
                        text = entry.rank.toString(),
                        color = TextSecondary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Round Avatar
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(NavyIntermediate)
                    .border(1.5.dp, if (entry.isCurrentUser) PacmanYellow else entry.avatarColor.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (entry.isCurrentUser) {
                    val equippedAvatar = MockData.AvatarItemsCatalog.firstOrNull { it.id == equippedAvatarId }
                        ?: MockData.AvatarItemsCatalog.first()
                    AvatarVisual(avatar = equippedAvatar, size = 26.dp)
                } else {
                    GhostComposable(size = 24.dp, baseColor = entry.avatarColor, isScared = false)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Player Name
            Text(
                text = entry.name,
                color = if (entry.isCurrentUser) PacmanYellow else TextPrimary,
                fontSize = 14.sp,
                fontWeight = if (entry.isCurrentUser) FontWeight.Black else FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )

            // Score with optional medal for 1st
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = formattedScore,
                    color = if (entry.isCurrentUser) PacmanYellow else TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                if (entry.rank == 1) {
                    Spacer(modifier = Modifier.width(6.dp))
                    MedalComposable(size = 16.dp, rank = 1)
                }
            }
        }
    }
}
