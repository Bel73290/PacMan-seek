package com.cachecache.game

import android.graphics.Bitmap
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.activity.ComponentActivity
import org.osmdroid.views.MapView
import org.osmdroid.util.MapTileIndex
import kotlin.math.*
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import com.cachecache.game.model.*
import com.cachecache.game.ui.screens.MatchScreen
import com.cachecache.game.ui.screens.OnlineProfileScreen
import com.cachecache.game.ui.screens.OnlineShopScreen
import com.cachecache.game.ui.theme.CacheCacheTheme
import com.cachecache.game.viewmodel.OnlineState
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class MapAndProfileTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    @get:Rule val permissions = GrantPermissionRule.grant(
        android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION,
        android.Manifest.permission.BLUETOOTH_SCAN, android.Manifest.permission.BLUETOOTH_ADVERTISE,
        android.Manifest.permission.BLUETOOTH_CONNECT
    )

    private val pacman = GeoPoint(45.6410, 5.8672)
    private val ghost = GeoPoint(45.6413, 5.8675)

    private fun match(role: MatchRole) = MatchState(
        code="TESTAA",role=role,phase=MatchPhase.SEEKING,remainingSeconds=450,
        myLocation=if(role==MatchRole.PACMAN) pacman else ghost,
        pacmanLocation=pacman,pacmanHeading=90f,gpsAccuracy=5f,totalGhosts=1,
        ghosts=listOf(MatchGhost("ghost","Camille","Pinky",ghost,false,role==MatchRole.GHOST,"ghost_pink_classic",90f,true)),
        pacDotsRoute=listOf(pacman,GeoPoint(45.6411,5.8672),GeoPoint(45.6412,5.8674),ghost)
    )

    @Test fun seekerAndGhostBothHaveTheOtherPlayerOnTheirMap() {
        var state by mutableStateOf(match(MatchRole.PACMAN))
        compose.setContent { CacheCacheTheme { MatchScreen(state,{},{},{}) } }
        compose.onNodeWithContentDescription("Carte OpenStreetMap • Pacman • Fantôme : Camille").assertExists()
        compose.onNodeWithText("Voir les joueurs").assertDoesNotExist()
        // Check real tile downloads, not only that the Android map view exists.
        compose.waitUntil(30_000) { tilesVisible() }
        screenshot("map-seeker")
        compose.runOnIdle { state=match(MatchRole.GHOST) }
        compose.onNodeWithContentDescription("Carte OpenStreetMap • Pacman • Fantôme : Camille").assertExists()
        compose.onNodeWithText("Recentrer GPS").performClick()
        compose.waitUntil(30_000) { tilesVisible() }
        screenshot("map-ghost")
    }

    @Test fun profileEditsAreDraftsUntilSavedAndCancelRestoresThem() {
        var state by mutableStateOf(OnlineState(uid="fixture",loading=false,profile=mapOf(
            "name" to "Alex","ghostSkin" to "ghost_red_classic","pacmanSkin" to "pacman_classic",
            "ownedSkins" to listOf("ghost_red_classic","pacman_classic","ghost_pink_classic","pacman_neon"))))
        var saved: Triple<String,String,String>? = null
        compose.setContent { CacheCacheTheme { Surface {
            OnlineProfileScreen(state,{},{},{},{},{},{},onSaveProfile={name,skin,pac->
                saved=Triple(name,skin,pac)
                state=state.copy(profile=state.profile+mapOf("name" to name,"ghostSkin" to skin,"pacmanSkin" to pac),message="Profil enregistré.")
            })
        } } }
        compose.onNodeWithText("Modifier mes infos").performClick()
        compose.onNodeWithText("Pseudo").performClick().performTextReplacement("Camille")
        compose.onNodeWithTag("online-page").performScrollToNode(hasText("Choisir"))
        compose.onAllNodesWithContentDescription("Pacman").assertCountEquals(0)
        compose.onNodeWithText("Choisir").performClick()
        assertNull(saved)
        compose.onNodeWithText("Enregistrer les modifications").assertIsDisplayed().performClick()
        compose.runOnIdle { assertEquals(Triple("Camille","ghost_pink_classic","pacman_classic"),saved) }
        compose.onAllNodesWithText("Profil enregistré.").onFirst().assertExists()
        compose.onNodeWithTag("online-page").performScrollToNode(hasText("Modifier mes infos"))
        compose.onNodeWithText("Modifier mes infos").performClick()
        compose.onNodeWithText("Pseudo").performClick().performTextReplacement("Non enregistre")
        compose.onNodeWithText("Annuler").performClick()
        compose.onNodeWithText("Pseudo").assertTextContains("Camille")
        screenshot("profile-save")
    }

    @Test fun shopHasNoPacmanSkinChoicesEvenForLegacyOwners() {
        val state=OnlineState(loading=false,profile=mapOf("ownedSkins" to listOf("ghost_red_classic","pacman_pirate")))
        compose.setContent { CacheCacheTheme { OnlineShopScreen(state,{},{},{},{}) } }
        compose.onNodeWithText("Pacman").assertDoesNotExist()
        compose.onNodeWithText("Pacman Pirate").assertDoesNotExist()
        compose.onAllNodesWithContentDescription("Pacman").assertCountEquals(0)
    }

    private fun tilesVisible(): Boolean {
        fun findMap(view: android.view.View): MapView? {
            if(view is MapView) return view
            if(view is android.view.ViewGroup) for(i in 0 until view.childCount) findMap(view.getChildAt(i))?.let { return it }
            return null
        }
        var ready=false
        compose.runOnIdle {
            val map=findMap(compose.activity.window.decorView)
            if(map != null && map.width>0 && map.height>0) {
                val zoom=map.zoomLevelDouble.toInt()
                val count=2.0.pow(zoom)
                ready=listOf(.15,.5,.85).all { x -> listOf(.2,.5,.8).all { y ->
                    val point=map.projection.fromPixels((map.width*x).toInt(),(map.height*y).toInt())
                    val lat=Math.toRadians(point.latitude)
                    val tileX=floor((point.longitude+180)/360*count).toInt()
                    val tileY=floor((1-ln(tan(lat)+1/cos(lat))/PI)/2*count).toInt()
                    map.tileProvider.getMapTile(MapTileIndex.getTileIndex(zoom,tileX,tileY)) != null
                } }
            }
        }
        return ready
    }

    private fun screenshot(name: String) {
        val instrumentation=InstrumentationRegistry.getInstrumentation()
        val resolver=instrumentation.targetContext.contentResolver
        val values=android.content.ContentValues().apply {
            put(android.provider.MediaStore.Images.Media.DISPLAY_NAME,"pac-$name.png")
            put(android.provider.MediaStore.Images.Media.MIME_TYPE,"image/png")
            put(android.provider.MediaStore.Images.Media.RELATIVE_PATH,"Pictures/PacSeekQA")
        }
        val uri=checkNotNull(resolver.insert(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values))
        resolver.openOutputStream(uri)!!.use {
            instrumentation.uiAutomation.takeScreenshot().compress(Bitmap.CompressFormat.PNG,100,it)
        }
    }
}
