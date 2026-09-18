package com.cachecache.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cachecache.game.model.*
import com.cachecache.game.ui.components.BottomNavigationBar
import com.cachecache.game.ui.components.OsmMapView
import com.cachecache.game.ui.theme.*

@Composable
fun RadarFilterScreen(
    state: GameState,
    onNavigate: (Screen) -> Unit,
    onToggleFilterMenu: () -> Unit,
    onSelectFilter: (FilterType) -> Unit,
    onMapTap: (GeoPoint) -> Unit,
    onPlayerTap: (HiddenPlayer) -> Unit,
    onRecenter: () -> Unit
) {
    // Filtered players according to active filter
    val filteredPlayers = remember(state.hiddenPlayers, state.activeFilter) {
        when (state.activeFilter) {
            FilterType.ALL -> state.hiddenPlayers
            FilterType.NEARBY -> state.hiddenPlayers.filter { it.distanceMeters <= 50.0 }
            FilterType.SUSPECT_ZONE -> state.hiddenPlayers.filter { it.id in listOf(1, 2) }
            FilterType.SCARED -> state.hiddenPlayers.filter { it.isScared }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepNavy)
    ) {
        // 1. Fullscreen OSM Map with Continuous Concentric Radar Scanning
        OsmMapView(
            seekerLocation = state.seekerLocation,
            seekerHeading = state.seekerHeading,
            isFacingRight = state.isFacingRight,
            hiddenPlayers = filteredPlayers,
            suspectZone = state.suspectZone,
            pacDotsRoute = emptyList(),
            isRadarScanning = true,
            onMapTap = onMapTap,
            onPlayerTap = onPlayerTap
        )

        // 2. Top Bar: Menu Button Left & "Filtres" Button Right
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Hamburger Menu
            IconButton(
                onClick = { onNavigate(Screen.GUIDE) },
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(NavyIntermediate.copy(alpha = 0.95f))
                    .border(1.dp, BorderSubtle, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu",
                    tint = TextPrimary
                )
            }

            // "Filtres" Pill Button
            Box {
                Button(
                    onClick = onToggleFilterMenu,
                    shape = RoundedCornerShape(22.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NavyIntermediate.copy(alpha = 0.95f),
                        contentColor = TextPrimary
                    ),
                    modifier = Modifier
                        .height(44.dp)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(22.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filtres",
                        tint = PacmanYellow,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Filtres : ${state.activeFilter.label}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                // Filter Dropdown Menu
                DropdownMenu(
                    expanded = state.showFilterMenu,
                    onDismissRequest = onToggleFilterMenu,
                    modifier = Modifier
                        .background(NavyIntermediate)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                ) {
                    FilterType.entries.forEach { filter ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = filter.label,
                                    color = if (state.activeFilter == filter) PacmanYellow else TextPrimary,
                                    fontWeight = if (state.activeFilter == filter) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            onClick = { onSelectFilter(filter) }
                        )
                    }
                }
            }
        }

        // 3. Bottom Controls & Bottom Navigation Bar
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.End
        ) {
            // Recenter GPS Button
            IconButton(
                onClick = onRecenter,
                modifier = Modifier
                    .padding(end = 16.dp, bottom = 12.dp)
                    .size(50.dp)
                    .shadow(8.dp, CircleShape)
                    .clip(CircleShape)
                    .background(NavyIntermediate)
                    .border(1.dp, BorderSubtle, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = "Recentrer",
                    tint = PacmanYellow,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Fixed Bottom Navigation Bar
            BottomNavigationBar(
                currentScreen = Screen.RADAR_FILTERS,
                onNavigate = onNavigate
            )
        }
    }
}
