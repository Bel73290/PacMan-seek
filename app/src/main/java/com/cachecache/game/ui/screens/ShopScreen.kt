package com.cachecache.game.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cachecache.game.data.MockData
import com.cachecache.game.model.*
import com.cachecache.game.ui.components.BottomNavigationBar
import com.cachecache.game.ui.components.GhostComposable
import com.cachecache.game.ui.components.PacmanComposable
import com.cachecache.game.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

enum class ShopTab(val label: String) {
    CLASSIC("Classiques"),
    PREMIUM("Premium"),
    INVENTORY("Inventaire")
}

@Composable
fun ShopScreen(
    state: GameState,
    onBack: () -> Unit,
    onNavigate: (Screen) -> Unit,
    onEquipAvatar: (String) -> Unit,
    onUnlockAvatar: (AvatarItem) -> Unit
) {
    var selectedTab by remember { mutableStateOf(ShopTab.CLASSIC) }
    var previewAvatarId by remember(state.equippedAvatarId) { mutableStateOf(state.equippedAvatarId) }

    val currentCatalog = MockData.AvatarItemsCatalog
    val activePreviewAvatar = currentCatalog.firstOrNull { it.id == previewAvatarId }
        ?: currentCatalog.first()

    val numberFormat = NumberFormat.getNumberInstance(Locale.FRANCE)

    Scaffold(
        containerColor = DeepNavy,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            BottomNavigationBar(
                currentScreen = Screen.SHOP,
                onNavigate = onNavigate
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(bottom = paddingValues.calculateBottomPadding())
        ) {
            // 1. Header: Back Arrow, Title, User Points Balance
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 14.dp, end = 14.dp, top = 2.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
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

                    Spacer(modifier = Modifier.width(14.dp))

                    Text(
                        text = "Boutique",
                        style = MaterialTheme.typography.headlineSmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.Black
                    )
                }

                // Points Balance Pill
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = NavyIntermediate,
                    border = androidx.compose.foundation.BorderStroke(1.dp, PacmanYellow.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stars,
                            contentDescription = "Points",
                            tint = PacmanYellow,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${formatCompactScore(state.userScore)} pts",
                            color = PacmanYellow,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 2. Live Avatar Preview Showcase Card (Compact)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp)
                    .border(1.2.dp, BorderSubtle, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardSurface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Avatar Showcase Circle with Glow
                    Box(
                        modifier = Modifier
                            .size(62.dp)
                            .shadow(8.dp, CircleShape, spotColor = PacmanYellow)
                            .clip(CircleShape)
                            .background(NavyIntermediate)
                            .border(2.5.dp, if (state.equippedAvatarId == activePreviewAvatar.id) PacmanYellow else BorderSubtle, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        AvatarVisual(avatar = activePreviewAvatar, size = 50.dp)
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = activePreviewAvatar.name,
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            maxLines = 1
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Action Button inside showcase
                        val isUnlocked = state.unlockedAvatarIds.contains(activePreviewAvatar.id)
                        val isEquipped = state.equippedAvatarId == activePreviewAvatar.id

                        if (!isEquipped) {
                            val buttonColor = when {
                                isUnlocked -> ScaredBlue
                                activePreviewAvatar.isPremium -> Color(0xFFFFD700)
                                else -> PacmanYellow
                            }
                            Button(
                                onClick = {
                                    if (isUnlocked) {
                                        onEquipAvatar(activePreviewAvatar.id)
                                    } else {
                                        onUnlockAvatar(activePreviewAvatar)
                                    }
                                },
                                modifier = Modifier
                                    .height(40.dp)
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = buttonColor,
                                    contentColor = if (isUnlocked) TextPrimary else DarkContrast
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                                enabled = isUnlocked || state.userScore >= activePreviewAvatar.price
                            ) {
                                if (isUnlocked) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = "Équiper ce skin", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                } else {
                                    Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = "Débloquer (${formatCompactScore(activePreviewAvatar.price)} pts)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        } else {
                            Text(
                                text = "Skin actuellement équipé",
                                color = PacmanYellow,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 3. Category Filter Tabs: "Classiques", "Premium", "Inventaire"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ShopTab.entries.forEach { tab ->
                    val isSelected = selectedTab == tab
                    val isPremiumTab = tab == ShopTab.PREMIUM
                    val isInventoryTab = tab == ShopTab.INVENTORY
                    val activeColor = when {
                        isInventoryTab -> ScaredBlue
                        isPremiumTab -> Color(0xFFFFD700)
                        else -> PacmanYellow
                    }
                    val textColor = when {
                        isSelected && isInventoryTab -> TextPrimary
                        isSelected -> DarkContrast
                        isPremiumTab -> Color(0xFFFFD700)
                        isInventoryTab -> ScaredBlue
                        else -> TextSecondary
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .clip(RoundedCornerShape(21.dp))
                            .background(if (isSelected) activeColor else CardSurface)
                            .border(
                                1.dp,
                                if (isSelected) activeColor else if (isPremiumTab) Color(0xFFFFD700).copy(alpha = 0.4f) else BorderSubtle,
                                RoundedCornerShape(21.dp)
                            )
                            .clickable { selectedTab = tab },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab.label,
                            color = textColor,
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // 4. Grid of Avatar Selection Cards (3 per line)
            val filteredAvatars = when (selectedTab) {
                ShopTab.CLASSIC -> currentCatalog.filter { it.category == AvatarCategory.CLASSIC }
                ShopTab.PREMIUM -> currentCatalog.filter { it.category == AvatarCategory.PREMIUM }
                ShopTab.INVENTORY -> currentCatalog.filter { state.unlockedAvatarIds.contains(it.id) }
            }

            if (selectedTab == ShopTab.INVENTORY) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${filteredAvatars.size} skins débloqués dans votre collection",
                        color = ScaredBlue,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(10.dp))
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(filteredAvatars) { avatar ->
                    val isEquipped = state.equippedAvatarId == avatar.id
                    val isUnlocked = state.unlockedAvatarIds.contains(avatar.id)
                    val isPreviewed = previewAvatarId == avatar.id

                    val cardBorder = when {
                        isEquipped -> PacmanYellow
                        isPreviewed -> ScaredBlue
                        avatar.isPremium -> Color(0xFFFFD700).copy(alpha = 0.7f)
                        else -> BorderSubtle
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                if (isEquipped || isPreviewed || avatar.isPremium) 1.5.dp else 1.dp,
                                cardBorder,
                                RoundedCornerShape(16.dp)
                            )
                            .clickable {
                                previewAvatarId = avatar.id
                                if (isUnlocked && !isEquipped) {
                                    onEquipAvatar(avatar.id)
                                }
                            },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isEquipped) SelectedSurface else if (avatar.isPremium) NavyIntermediate else CardSurface
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Avatar Visual - Compact
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(NavyIntermediate)
                                    .border(
                                        1.5.dp,
                                        if (isEquipped) PacmanYellow else if (avatar.isPremium) Color(0xFFFFD700).copy(alpha = 0.5f) else BorderSubtle,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                AvatarVisual(avatar = avatar, size = 32.dp)
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = avatar.name,
                                color = if (isEquipped) PacmanYellow else if (avatar.isPremium) Color(0xFFFFE066) else TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // Status or Action Button
                            when {
                                isEquipped -> {
                                    Box(
                                        modifier = Modifier.height(28.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Équipé",
                                            tint = PacmanYellow,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                isUnlocked -> {
                                    Button(
                                        onClick = { onEquipAvatar(avatar.id) },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = ScaredBlue,
                                            contentColor = TextPrimary
                                        ),
                                        modifier = Modifier.height(28.dp),
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                                    ) {
                                        Text(
                                            text = "Équiper",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                                else -> {
                                    val canAfford = state.userScore >= avatar.price
                                    val unlockBtnColor = if (avatar.isPremium) Color(0xFFFFD700) else PacmanYellow
                                    Button(
                                        onClick = { onUnlockAvatar(avatar) },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (canAfford) unlockBtnColor else CardSurface,
                                            contentColor = if (canAfford) DarkContrast else TextSecondary
                                        ),
                                        border = if (canAfford) null else androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                                        modifier = Modifier.height(28.dp),
                                        enabled = canAfford,
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = null,
                                            modifier = Modifier.size(11.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = "${formatCompactScore(avatar.price)} pts",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AvatarVisual(
    avatar: AvatarItem,
    size: androidx.compose.ui.unit.Dp
) {
    when {
        avatar.isAnimatedPacman -> {
            PacmanComposable(
                size = size,
                color = PacmanYellow,
                isAnimated = true,
                rotationAngle = 0f
            )
        }
        avatar.drawableResId != null -> {
            Image(
                painter = painterResource(id = avatar.drawableResId),
                contentDescription = avatar.name,
                modifier = Modifier.size(size),
                contentScale = ContentScale.Fit
            )
        }
        avatar.ghostColor != null -> {
            GhostComposable(
                size = size,
                baseColor = avatar.ghostColor,
                isScared = avatar.id == "ghost_scared"
            )
        }
        else -> {
            PacmanComposable(size = size, isAnimated = false)
        }
    }
}
