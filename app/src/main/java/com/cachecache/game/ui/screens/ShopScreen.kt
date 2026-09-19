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

@Composable
fun ShopScreen(
    state: GameState,
    onBack: () -> Unit,
    onNavigate: (Screen) -> Unit,
    onEquipAvatar: (String) -> Unit,
    onUnlockAvatar: (AvatarItem) -> Unit
) {
    var selectedCategory by remember { mutableStateOf(AvatarCategory.CLASSIC) }
    var previewAvatarId by remember(state.equippedAvatarId) { mutableStateOf(state.equippedAvatarId) }

    val currentCatalog = MockData.AvatarItemsCatalog
    val activePreviewAvatar = currentCatalog.firstOrNull { it.id == previewAvatarId }
        ?: currentCatalog.first()

    val numberFormat = NumberFormat.getNumberInstance(Locale.FRANCE)

    Scaffold(
        containerColor = DeepNavy,
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
                .padding(paddingValues)
                .statusBarsPadding()
        ) {
            // 1. Header: Back Arrow, Title, User Points Balance
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
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

                    Spacer(modifier = Modifier.width(14.dp))

                    Text(
                        text = "Boutique PP",
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
                            text = "${numberFormat.format(state.userScore)} pts",
                            color = PacmanYellow,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // 2. Big Live Avatar Preview Showcase Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .border(1.5.dp, BorderSubtle, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = CardSurface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Avatar Showcase Circle with Glow
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .shadow(12.dp, CircleShape, spotColor = PacmanYellow)
                            .clip(CircleShape)
                            .background(NavyIntermediate)
                            .border(3.dp, if (state.equippedAvatarId == activePreviewAvatar.id) PacmanYellow else BorderSubtle, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        AvatarVisual(avatar = activePreviewAvatar, size = 60.dp)
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = activePreviewAvatar.name,
                                color = TextPrimary,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            if (activePreviewAvatar.isPremium) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFFFD700).copy(alpha = 0.2f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD700))
                                ) {
                                    Text(
                                        text = "★ ÉLITE",
                                        color = Color(0xFFFFD700),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            if (state.equippedAvatarId == activePreviewAvatar.id) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = PacmanYellow.copy(alpha = 0.2f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, PacmanYellow)
                                ) {
                                    Text(
                                        text = "ÉQUIPÉ",
                                        color = PacmanYellow,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = activePreviewAvatar.description,
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

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
                                    .height(34.dp)
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(17.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = buttonColor,
                                    contentColor = if (isUnlocked) TextPrimary else DarkContrast
                                ),
                                enabled = isUnlocked || state.userScore >= activePreviewAvatar.price
                            ) {
                                if (isUnlocked) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = "Équiper cette PP", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                } else {
                                    Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = "Débloquer (${activePreviewAvatar.price} pts)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 3. Category Filter Tabs: "Classiques" vs "✨ Premium"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AvatarCategory.entries.forEach { category ->
                    val isSelected = selectedCategory == category
                    val isPremiumTab = category == AvatarCategory.PREMIUM
                    val activeColor = if (isPremiumTab) Color(0xFFFFD700) else PacmanYellow
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
                            .clickable { selectedCategory = category },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = category.label,
                            color = if (isSelected) DarkContrast else if (isPremiumTab) Color(0xFFFFD700) else TextSecondary,
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 4. Grid of Avatar Selection Cards
            val filteredAvatars = currentCatalog.filter { it.category == selectedCategory }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
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
                                RoundedCornerShape(20.dp)
                            )
                            .clickable {
                                previewAvatarId = avatar.id
                                if (isUnlocked && !isEquipped) {
                                    onEquipAvatar(avatar.id)
                                }
                            },
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isEquipped) SelectedSurface else if (avatar.isPremium) NavyIntermediate else CardSurface
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (avatar.isPremium) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFFFD700).copy(alpha = 0.2f),
                                    border = androidx.compose.foundation.BorderStroke(0.8.dp, Color(0xFFFFD700)),
                                    modifier = Modifier.padding(bottom = 6.dp)
                                ) {
                                    Text(
                                        text = "★ ÉLITE",
                                        color = Color(0xFFFFD700),
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                    )
                                }
                            }

                            // Avatar Visual
                            Box(
                                modifier = Modifier
                                    .size(62.dp)
                                    .clip(CircleShape)
                                    .background(NavyIntermediate)
                                    .border(
                                        1.5.dp,
                                        if (isEquipped) PacmanYellow else if (avatar.isPremium) Color(0xFFFFD700).copy(alpha = 0.5f) else BorderSubtle,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                AvatarVisual(avatar = avatar, size = 46.dp)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = avatar.name,
                                color = if (isEquipped) PacmanYellow else if (avatar.isPremium) Color(0xFFFFE066) else TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // Status Pill or Price
                            when {
                                isEquipped -> {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = PacmanYellow,
                                        modifier = Modifier.height(26.dp)
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
                                isUnlocked -> {
                                    Button(
                                        onClick = { onEquipAvatar(avatar.id) },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = ScaredBlue,
                                            contentColor = TextPrimary
                                        ),
                                        modifier = Modifier.height(28.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
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
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = null,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "${avatar.price} pts",
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
