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
import com.cachecache.game.model.GameState
import com.cachecache.game.model.LeaderboardEntry
import com.cachecache.game.model.LeaderboardTab
import com.cachecache.game.model.Screen
import com.cachecache.game.ui.components.BottomNavigationBar
import com.cachecache.game.ui.components.GhostComposable
import com.cachecache.game.ui.components.PacmanComposable
import com.cachecache.game.ui.components.TrophyComposable
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
    // Dynamic list based on selected tab and user score
    val entries = when (state.activeLeaderboardTab) {
        LeaderboardTab.GENERAL -> MockData.GeneralLeaderboard.map {
            if (it.isCurrentUser) it.copy(score = state.userScore) else it
        }
        LeaderboardTab.FRIENDS -> MockData.FriendsLeaderboard.map {
            if (it.isCurrentUser) it.copy(score = state.userScore) else it
        }
        LeaderboardTab.SEASONS -> MockData.SeasonsLeaderboard.map {
            if (it.isCurrentUser) it.copy(score = state.userScore + 1070) else it
        }
    }

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
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(42.dp)
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

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = "Classement",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Filter Tabs (Pills): Général, Amis, Saisons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                LeaderboardTab.entries.forEach { tab ->
                    val isSelected = state.activeLeaderboardTab == tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) PacmanYellow else CardSurface)
                            .border(1.dp, if (isSelected) PacmanYellow else BorderSubtle, RoundedCornerShape(20.dp))
                            .clickable { onSelectTab(tab) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab.label,
                            color = if (isSelected) DarkContrast else TextSecondary,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // LazyColumn Leaderboard List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                items(entries) { entry ->
                    LeaderboardRow(entry = entry)
                }
            }
        }
    }
}

@Composable
private fun LeaderboardRow(entry: LeaderboardEntry) {
    val numberFormat = NumberFormat.getNumberInstance(Locale.FRANCE)
    val formattedScore = "${numberFormat.format(entry.score)} pts"

    val rowBackground = if (entry.isCurrentUser) SelectedSurface else CardSurface
    val rowBorder = if (entry.isCurrentUser) PacmanYellow.copy(alpha = 0.6f) else BorderSubtle

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, rowBorder, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = rowBackground)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank Badge or Number
            Box(
                modifier = Modifier.width(36.dp),
                contentAlignment = Alignment.Center
            ) {
                when (entry.rank) {
                    1, 2, 3 -> TrophyComposable(size = 26.dp, rank = entry.rank)
                    else -> Text(
                        text = entry.rank.toString(),
                        color = TextSecondary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Round Avatar
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(NavyIntermediate)
                    .border(2.dp, if (entry.isCurrentUser) PacmanYellow else entry.avatarColor.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (entry.isCurrentUser) {
                    PacmanComposable(size = 28.dp, color = PacmanYellow, isAnimated = true)
                } else {
                    GhostComposable(size = 28.dp, baseColor = entry.avatarColor, isScared = false)
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Player Name
            Text(
                text = entry.name,
                color = if (entry.isCurrentUser) PacmanYellow else TextPrimary,
                fontSize = 15.sp,
                fontWeight = if (entry.isCurrentUser) FontWeight.Black else FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )

            // Score with optional gold trophy
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = formattedScore,
                    color = if (entry.isCurrentUser) PacmanYellow else TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                if (entry.rank == 1) {
                    Spacer(modifier = Modifier.width(6.dp))
                    TrophyComposable(size = 18.dp, rank = 1)
                }
            }
        }
    }
}
