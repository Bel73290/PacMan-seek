package com.cachecache.game

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.ViewModelProvider
import com.cachecache.game.model.GamePhase
import com.cachecache.game.viewmodel.GameViewModel
import com.google.firebase.auth.FirebaseAuth
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class OfflineGuestFlowTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun soloStartsWithoutAccountOrServerAndCanBeRestarted() {
        val uidBefore=FirebaseAuth.getInstance().currentUser?.uid
        compose.onNodeWithText("Jouer sans compte").assertIsEnabled().performClick()
        compose.onNodeWithText("Mode solo · sans compte").assertIsDisplayed()
        compose.activityRule.scenario.onActivity { activity ->
            val state=ViewModelProvider(activity)[GameViewModel::class.java].uiState.value
            assertEquals(GamePhase.SEARCHING,state.gamePhase)
            assertTrue(state.isGameRunning)
            assertNull(state.activeMatch)
            assertTrue(state.hiddenPlayers.isNotEmpty())
        }
        assertEquals(uidBefore,FirebaseAuth.getInstance().currentUser?.uid)
        compose.onNodeWithContentDescription("Retour").performClick()
        compose.onNodeWithText("Jouer sans compte").assertIsDisplayed().performClick()
        compose.onNodeWithText("Mode solo · sans compte").assertIsDisplayed()
        compose.activityRule.scenario.onActivity { activity ->
            val state=ViewModelProvider(activity)[GameViewModel::class.java].uiState.value
            assertEquals(0,state.userScore)
            assertTrue(state.hiddenPlayers.none { it.isFound })
        }
        assertEquals(uidBefore,FirebaseAuth.getInstance().currentUser?.uid)
    }
}
