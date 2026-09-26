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
import androidx.compose.ui.draw.shadow
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
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 48.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .shadow(12.dp, RoundedCornerShape(29.dp), spotColor = PacmanYellow.copy(alpha = 0.25f)),
            shape = RoundedCornerShape(29.dp),
            color = NavyIntermediate.copy(alpha = 0.96f),
            border = androidx.compose.foundation.BorderStroke(1.2.dp, BorderSubtle)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val isLeaderboardSelected = currentScreen == Screen.LEADERBOARD
                BottomNavIconItem(
                    icon = Icons.Default.EmojiEvents,
                    contentDescription = "Classement",
                    isSelected = isLeaderboardSelected,
                    onClick = { onNavigate(Screen.LEADERBOARD) }
                )

                val isShopSelected = currentScreen == Screen.SHOP
                BottomNavIconItem(
                    icon = Icons.Default.Storefront,
                    contentDescription = "Boutique",
                    isSelected = isShopSelected,
                    onClick = { onNavigate(Screen.SHOP) }
                )

                val isProfileSelected = currentScreen == Screen.PROFILE
                BottomNavIconItem(
                    icon = Icons.Default.Person,
                    contentDescription = "Profil",
                    isSelected = isProfileSelected,
                    onClick = { onNavigate(Screen.PROFILE) }
                )
            }
        }
    }
}

@Composable
private fun BottomNavIconItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val activeColor = PacmanYellow
    val inactiveColor = TextSecondary

    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(if (isSelected) PacmanYellow.copy(alpha = 0.18f) else Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (isSelected) activeColor else inactiveColor,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun SettingsDialog(
    currentSettings: GameSettings,
    onDismiss: () -> Unit,
    onSave: (Int, Int) -> Unit
) {
    var freezeSeconds by remember { mutableStateOf(currentSettings.freezeDurationSeconds) }
    var huntMinutes by remember { mutableStateOf(currentSettings.huntDurationMinutes) }

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
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Fermer", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Temps pour se cacher (freeze / délai d'attente initial en secondes)
                TimerStepperRow(
                    title = "Temps pour se cacher",
                    description = "Délai d'attente avant le début de la traque",
                    valueDisplay = "$freezeSeconds s",
                    iconColor = GhostPink,
                    onMinus = { if (freezeSeconds > 10) freezeSeconds -= 5 },
                    onPlus = { if (freezeSeconds < 180) freezeSeconds += 5 }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Temps pour la traque (chasse aux fantômes en minutes)
                TimerStepperRow(
                    title = "Temps pour la traque",
                    description = "Temps alloué pour trouver tous les fantômes",
                    valueDisplay = "$huntMinutes min",
                    iconColor = PacmanYellow,
                    onMinus = { if (huntMinutes > 1) huntMinutes-- },
                    onPlus = { if (huntMinutes < 30) huntMinutes++ }
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
                        onSave(freezeSeconds, huntMinutes)
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
    valueDisplay: String,
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
                    text = valueDisplay,
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
