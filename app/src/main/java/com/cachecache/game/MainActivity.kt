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
                            Screen.LEADERBOARD -> viewModel.setScreen(Screen.SPLASH)
                            Screen.RADAR_FILTERS -> viewModel.setScreen(Screen.GAME)
                            Screen.GUIDE -> viewModel.setScreen(Screen.SPLASH)
                            Screen.PROFILE -> viewModel.setScreen(Screen.SPLASH)
                            Screen.SPLASH -> finish()
                        }
                    }

                    // Screen Router
                    when (state.currentScreen) {
                        Screen.SPLASH -> {
                            SplashScreen(
                                state = state,
                                onStartGame = { viewModel.startGame() },
                                onOpenLeaderboard = { viewModel.setScreen(Screen.LEADERBOARD) },
                                onOpenGuide = { viewModel.setScreen(Screen.GUIDE) },
                                onOpenSettings = { viewModel.openSettingsDialog(true) },
                                onCloseSettings = { viewModel.openSettingsDialog(false) },
                                onSaveSettings = { seekerMin, hidingMin ->
                                    viewModel.updateSettings(seekerMin, hidingMin)
                                }
                            )
                        }
                        Screen.GAME -> {
                            GameScreen(
                                state = state,
                                onBackToHome = { viewModel.setScreen(Screen.SPLASH) },
                                onNavigate = { screen -> viewModel.setScreen(screen) },
                                onGoToSuspectZone = { viewModel.goToSuspectZone() },
                                onDismissSuspectZoneModal = { viewModel.dismissSuspectZoneModal() },
                                onMapTap = { geo -> viewModel.onMapTapped(geo) },
                                onPlayerTap = { player -> viewModel.onPlayerTapped(player) },
                                onRecenter = { viewModel.recenterOnCampus() },
                                onDismissCaptureDialog = { viewModel.dismissCaptureDialog() },
                                onTestInstantCapture = { viewModel.triggerSimulatedCaptureTest() }
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
                                onSelectBadge = { badge -> viewModel.selectBadge(badge) }
                            )
                        }
                    }
                }
            }
        }
    }
}
