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
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cachecache.game.R
import com.cachecache.game.data.MockData
import com.cachecache.game.model.BadgeItem
import com.cachecache.game.model.GameState
import com.cachecache.game.model.HistoryItem
import com.cachecache.game.model.Screen
import com.cachecache.game.ui.components.*
import com.cachecache.game.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ProfileScreen(
    state: GameState,
    onNavigate: (Screen) -> Unit,
    onOpenSettings: () -> Unit,
    onSelectBadge: (BadgeItem?) -> Unit
) {
    val numberFormat = NumberFormat.getNumberInstance(Locale.FRANCE)

    Scaffold(
        containerColor = DeepNavy,
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
                .padding(paddingValues)
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            // Header: "Mon profil" on left + Gear Settings Icon on right
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Mon profil",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )

                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(NavyIntermediate)
                        .border(1.dp, BorderSubtle, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Réglages",
                        tint = PacmanYellow
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 1. Player Identity Card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Large Round Avatar Pacman
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(NavyIntermediate)
                        .border(3.dp, PacmanYellow, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    PacmanComposable(
                        size = 56.dp,
                        isAnimated = true
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Toi",
                        color = TextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black
                    )

                    Text(
                        text = "Niveau ${state.userLevel}",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // XP Progress Bar
                    Column {
                        LinearProgressIndicator(
                            progress = { (state.userXp.toFloat() / state.maxXp.toFloat()).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = PacmanYellow,
                            trackColor = CardSurface
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "${state.userXp} / ${state.maxXp} pts",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            modifier = Modifier.align(Alignment.End)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 2. Three Key Metrics Grid (Points with Star, Parties with Gamepad, Victoires with Trophy)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Points (Star)
                StatCard(
                    modifier = Modifier.weight(1f),
                    value = numberFormat.format(state.userScore),
                    label = "Points",
                    iconContent = { StarComposable(size = 24.dp, color = PacmanYellow) }
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
                            modifier = Modifier.size(24.dp)
                        )
                    }
                )

                // Victoires (Trophy)
                StatCard(
                    modifier = Modifier.weight(1f),
                    value = state.totalVictories.toString(),
                    label = "Victoires",
                    iconContent = { TrophyComposable(size = 24.dp, rank = 1) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 3. Section "Mes badges"
            Text(
                text = "Mes badges",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MockData.BadgesList.forEach { badge ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onSelectBadge(badge) }
                            .padding(8.dp)
                    ) {
                        BadgeComposable(badge = badge, size = 64.dp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = badge.title,
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 4. Section "Historique récent"
            Text(
                text = "Historique récent",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(14.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                state.recentHistory.forEach { historyItem ->
                    HistoryRow(item = historyItem)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Badge Detail Dialog
        state.selectedBadgeDetail?.let { badge ->
            BadgeDetailDialog(
                badge = badge,
                onDismiss = { onSelectBadge(null) }
            )
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
        modifier = modifier.border(1.dp, BorderSubtle, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = NavyIntermediate)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            iconContent()

            Spacer(modifier = Modifier.height(8.dp))

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
                fontWeight = FontWeight.Medium
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
                text = "+${item.deltaScore} pts",
                color = PacmanYellow,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}
