package com.cachecache.game.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.cachecache.game.model.*
import com.cachecache.game.ui.theme.*

@Composable
fun BottomNavigationBar(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp),
        color = NavyIntermediate,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val isMapSelected = currentScreen == Screen.GAME || currentScreen == Screen.RADAR_FILTERS
            BottomNavItem(
                label = "Carte",
                icon = Icons.Default.Map,
                isSelected = isMapSelected,
                onClick = { onNavigate(Screen.GAME) }
            )

            val isShopSelected = currentScreen == Screen.SHOP
            BottomNavItem(
                label = "Boutique",
                icon = Icons.Default.Palette,
                isSelected = isShopSelected,
                onClick = { onNavigate(Screen.SHOP) }
            )

            val isLeaderboardSelected = currentScreen == Screen.LEADERBOARD
            BottomNavItem(
                label = "Classement",
                icon = Icons.Default.EmojiEvents,
                isSelected = isLeaderboardSelected,
                onClick = { onNavigate(Screen.LEADERBOARD) }
            )

            val isProfileSelected = currentScreen == Screen.PROFILE
            BottomNavItem(
                label = "Profil",
                icon = Icons.Default.Person,
                isSelected = isProfileSelected,
                onClick = { onNavigate(Screen.PROFILE) }
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val activeColor = PacmanYellow
    val inactiveColor = TextSecondary

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) activeColor else inactiveColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = label,
            color = if (isSelected) activeColor else inactiveColor,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
fun SettingsDialog(
    currentSettings: GameSettings,
    onDismiss: () -> Unit,
    onSave: (Int, Int) -> Unit
) {
    var seekerTime by remember { mutableStateOf(currentSettings.seekerDurationMinutes) }
    var hidingTime by remember { mutableStateOf(currentSettings.hidingDurationMinutes) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = NavyIntermediate),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BorderSubtle, RoundedCornerShape(24.dp))
                .padding(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Réglages de la partie",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Fermer", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Seeker time stepper
                TimerStepperRow(
                    title = "Temps du chercheur",
                    description = "Temps alloué pour débusquer les cachés",
                    minutes = seekerTime,
                    iconColor = PacmanYellow,
                    onMinus = { if (seekerTime > 1) seekerTime-- },
                    onPlus = { if (seekerTime < 30) seekerTime++ }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Hiding time stepper
                TimerStepperRow(
                    title = "Temps des cachés",
                    description = "Temps pour se dissimuler sur le campus",
                    minutes = hidingTime,
                    iconColor = GhostPink,
                    onMinus = { if (hidingTime > 1) hidingTime-- },
                    onPlus = { if (hidingTime < 30) hidingTime++ }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Règle campus Savoie Technolac : capture valide à 30 cm",
                    color = ScaredBlue,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        onSave(seekerTime, hidingTime)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(25.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PacmanYellow,
                        contentColor = DarkContrast
                    )
                ) {
                    Text(
                        text = "Enregistrer les réglages",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun TimerStepperRow(
    title: String,
    description: String,
    minutes: Int,
    iconColor: Color,
    onMinus: () -> Unit,
    onPlus: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(text = description, color = TextSecondary, fontSize = 11.sp)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(BorderSubtle)
                        .clickable { onMinus() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Moins", tint = TextPrimary, modifier = Modifier.size(18.dp))
                }

                Text(
                    text = "$minutes min",
                    color = iconColor,
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(BorderSubtle)
                        .clickable { onPlus() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Plus", tint = TextPrimary, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
fun BadgeDetailDialog(
    badge: BadgeItem,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = NavyIntermediate),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BorderSubtle, RoundedCornerShape(24.dp))
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BadgeComposable(badge = badge, size = 84.dp)

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = badge.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Obtenu le ${badge.unlockDate}",
                    color = PacmanYellow,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = badge.description,
                    color = TextSecondary,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = RoundedCornerShape(23.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BorderSubtle,
                        contentColor = TextPrimary
                    )
                ) {
                    Text(text = "Fermer", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
