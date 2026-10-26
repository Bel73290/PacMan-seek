package com.cachecache.game

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.cachecache.game.ui.components.BattlePassSection
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class BattlePassPreviewTest {
    @get:Rule val compose=createComposeRule()
    @Test fun lockedRewardsCanBePreviewedWithoutBeingEquipped() {
        var equipped=false
        compose.setContent {MaterialTheme {
            BattlePassSection(1,0,emptySet(),"ghost_red_classic",false,true,{equipped=true},{error("Locked reward claimed")})
        }}
        compose.onNodeWithText("PASS DE COMBAT").assertIsDisplayed()
        compose.onNodeWithText("Niveau 10 requis").assertIsNotEnabled()
        compose.onNodeWithText("NIV. 20").performClick()
        compose.onNodeWithText("Shérif de l'Ouest").assertIsDisplayed()
        compose.onNodeWithText("Niveau 20 requis").assertIsNotEnabled()
        assertFalse(equipped)
    }
}
