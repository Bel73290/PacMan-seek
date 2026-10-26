package com.cachecache.game

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.cachecache.game.data.MatchSessionStore
import com.cachecache.game.model.*
import com.cachecache.game.ui.screens.*
import com.cachecache.game.ui.theme.CacheCacheTheme
import com.cachecache.game.ui.theme.DeepNavy
import com.cachecache.game.viewmodel.*

class MainActivity : ComponentActivity() {
    private val viewModel: GameViewModel by viewModels()
    private val matchViewModel: MatchViewModel by viewModels()
    private val online: OnlineViewModel by viewModels()
    private val sessions by lazy { MatchSessionStore(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        sessions.load()?.let { viewModel.resumeMatch(it) }
        setContent {
            CacheCacheTheme {
                val state by viewModel.uiState.collectAsState()
                val account by online.state.collectAsState()
                val snack = remember { SnackbarHostState() }
                val navigate: (Screen) -> Unit = { screen ->
                    viewModel.setScreen(screen)
                    if (screen == Screen.LEADERBOARD) online.loadLeaderboard()
                }
                LaunchedEffect(state.activeMatch) {
                    state.activeMatch?.let { sessions.save(it); matchViewModel.start(it) } ?: sessions.clear()
                }
                LaunchedEffect(account.error, account.message) {
                    if (state.currentScreen == Screen.SPLASH) {
                        val message = account.error ?: account.message
                        if (message != null) {
                            if (snack.showSnackbar(message, actionLabel = if(account.error != null) "Réessayer" else null) == SnackbarResult.ActionPerformed) online.retry()
                            online.clearNotice()
                        }
                    }
                }
                BackHandler(enabled = state.currentScreen != Screen.SPLASH && state.currentScreen != Screen.MATCH) {
                    navigate(if(state.currentScreen == Screen.FRIENDS) Screen.PROFILE else Screen.SPLASH)
                }
                Surface(Modifier.fillMaxSize(), color=DeepNavy) {
                    Box(Modifier.fillMaxSize()) {
                        when(state.currentScreen) {
                            Screen.SPLASH -> SplashScreen(
                                state=state,
                                onCreateGame={viewModel.createGame()}, onJoinGame={viewModel.joinGame(it)},
                                onLaunchGame={viewModel.launchLobbyGame()}, onReady={viewModel.setReady()},
                                onInviteFriends={navigate(Screen.FRIENDS)},
                                onLeaveLobby={viewModel.leaveLobby()}, onClearLobbyError={viewModel.clearLobbyError()},
                                onOpenMenu={navigate(Screen.PROFILE)},
                                onSaveSettings={hide,hunt->viewModel.updateSettings(hide,hunt)},
                                canPlay=account.profile.isNotEmpty() && !account.loading
                            )
                            Screen.PROFILE -> OnlineProfileScreen(account,navigate,{online.retry()},
                                {existing->online.connect(this@MainActivity,existing)},
                                {name->online.action("saveProfile",mapOf("name" to name))},
                                {id->online.action("equipSkin",mapOf("skinId" to id))},
                                {code->viewModel.joinGame(code)})
                            Screen.LEADERBOARD -> OnlineLeaderboardScreen(account,navigate,{scope,more->online.loadLeaderboard(scope,more)})
                            Screen.SHOP -> OnlineShopScreen(account,navigate,{online.retry()},
                                {id->online.action("purchaseSkin",mapOf("skinId" to id),"Skin ajouté à ta collection.")},
                                {id->online.action("equipSkin",mapOf("skinId" to id))})
                            Screen.FRIENDS -> FriendsScreen(account,navigate,{online.retry()},
                                {name,data->online.action(name,data)}, {code->viewModel.joinGame(code)})
                            Screen.MATCH -> {
                                val match by matchViewModel.state.collectAsState()
                                MatchScreen(match,{matchViewModel.onPermissionsResult()},{matchViewModel.catchGhost()},
                                    {matchViewModel.leave();viewModel.exitMatch();online.loadLeaderboard()})
                            }
                            Screen.GUIDE -> RulesGuideScreen(state,{navigate(Screen.SPLASH)},{navigate(Screen.SPLASH)})
                            else -> LaunchedEffect(Unit) { navigate(Screen.SPLASH) }
                        }
                        SnackbarHost(snack,Modifier.align(Alignment.BottomCenter).navigationBarsPadding())
                    }
                }
            }
        }
    }
}
