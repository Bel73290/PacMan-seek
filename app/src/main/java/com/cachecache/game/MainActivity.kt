package com.cachecache.game

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.cachecache.game.model.Screen
import com.cachecache.game.ui.screens.*
import com.cachecache.game.ui.theme.CacheCacheTheme
import com.cachecache.game.ui.theme.DeepNavy
import com.cachecache.game.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CacheCacheTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DeepNavy
                ) {
                    val state by viewModel.uiState.collectAsState()

                    // Hardware Back Button Navigation
                    BackHandler(enabled = state.currentScreen != Screen.SPLASH) {
                        when (state.currentScreen) {
                            Screen.GAME -> viewModel.setScreen(Screen.SPLASH)
                            Screen.STREET_VIEW -> viewModel.closeStreetView()
                            Screen.LEADERBOARD -> viewModel.setScreen(Screen.SPLASH)
                            Screen.SHOP -> viewModel.setScreen(Screen.SPLASH)
                            Screen.RADAR_FILTERS -> viewModel.setScreen(Screen.GAME)
                            Screen.GUIDE -> viewModel.setScreen(Screen.SPLASH)
                            Screen.PROFILE -> viewModel.setScreen(Screen.SPLASH)
                            Screen.SPLASH -> finish()
                        }
                    }

                    // Screen Router for All 8 Screens
                    when (state.currentScreen) {
                        Screen.SPLASH -> {
                            SplashScreen(
                                state = state,
                                onStartGame = { viewModel.startGame() },
                                onOpenMenu = { viewModel.setScreen(Screen.LEADERBOARD) }
                            )
                        }
                        Screen.GAME -> {
                            GameScreen(
                                state = state,
                                onBackToHome = { viewModel.setScreen(Screen.SPLASH) },
                                onNavigate = { screen -> viewModel.setScreen(screen) },
                                onOpenStreetView = { viewModel.openStreetView() },
                                onGoToSuspectZone = { viewModel.goToSuspectZone() },
                                onDismissSuspectZoneModal = { viewModel.dismissSuspectZoneModal() },
                                onDismissProximityAlert = { viewModel.dismissProximityAlert() },
                                onMapTap = { geo -> viewModel.onMapTapped(geo) },
                                onPlayerTap = { player -> viewModel.onPlayerTapped(player) },
                                onRecenter = { viewModel.recenterOnCampus() },
                                onDismissCaptureDialog = { viewModel.dismissCaptureDialog() },
                                onTestInstantCapture = { viewModel.triggerSimulatedCaptureTest() },
                                onReplay = { viewModel.restartGame() }
                            )
                        }
                        Screen.STREET_VIEW -> {
                            StreetViewScreen(
                                state = state,
                                onBack = { viewModel.closeStreetView() },
                                onCaptureTrigger = { viewModel.triggerSimulatedCaptureTest() }
                            )
                        }
                        Screen.LEADERBOARD -> {
                            LeaderboardScreen(
                                state = state,
                                onBack = { viewModel.setScreen(Screen.SPLASH) },
                                onNavigate = { screen -> viewModel.setScreen(screen) },
                                onSelectTab = { tab -> viewModel.setLeaderboardTab(tab) }
                            )
                        }
                        Screen.RADAR_FILTERS -> {
                            RadarFilterScreen(
                                state = state,
                                onNavigate = { screen -> viewModel.setScreen(screen) },
                                onToggleFilterMenu = { viewModel.openFilterMenu(!state.showFilterMenu) },
                                onSelectFilter = { filter -> viewModel.setFilter(filter) },
                                onMapTap = { geo -> viewModel.onMapTapped(geo) },
                                onPlayerTap = { player -> viewModel.onPlayerTapped(player) },
                                onRecenter = { viewModel.recenterOnCampus() }
                            )
                        }
                        Screen.GUIDE -> {
                            RulesGuideScreen(
                                state = state,
                                onBack = { viewModel.setScreen(Screen.SPLASH) },
                                onStartGame = { viewModel.startGame() }
                            )
                        }
                        Screen.PROFILE -> {
                            ProfileScreen(
                                state = state,
                                onNavigate = { screen -> viewModel.setScreen(screen) },
                                onOpenSettings = { viewModel.openSettingsDialog(true) },
                                onEquipAvatar = { avatarId -> viewModel.equipAvatar(avatarId) },
                                onSelectAchievementSkin = { ach -> viewModel.selectAchievementSkin(ach) },
                                onSelectBadge = { badge -> viewModel.selectBadge(badge) }
                            )
                        }
                        Screen.SHOP -> {
                            ShopScreen(
                                state = state,
                                onBack = { viewModel.setScreen(Screen.SPLASH) },
                                onNavigate = { screen -> viewModel.setScreen(screen) },
                                onEquipAvatar = { avatarId -> viewModel.equipAvatar(avatarId) },
                                onUnlockAvatar = { avatar -> viewModel.unlockAndEquipAvatar(avatar) }
                            )
                        }
                    }

                    if (state.showSettingsDialog && state.currentScreen != Screen.SPLASH) {
                        com.cachecache.game.ui.components.SettingsDialog(
                            currentSettings = state.settings,
                            onDismiss = { viewModel.openSettingsDialog(false) },
                            onSave = { freezeSec, huntMin ->
                                viewModel.updateSettings(freezeSec, huntMin)
                            }
                        )
                    }
                }
            }
        }
    }
}
