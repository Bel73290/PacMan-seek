package com.cachecache.game

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.cachecache.game.ui.components.SkinAvatar
import com.cachecache.game.ui.theme.CacheCacheTheme
import org.junit.Rule
import org.junit.Test

class CharacterPreviewTest {
    @get:Rule val compose=createComposeRule()
    @Test fun previewUprightViewsAndHorizontalPacmanSkins() {
        compose.setContent { CacheCacheTheme { Surface {
            Column(Modifier.fillMaxSize().padding(horizontal=16.dp,vertical=40.dp),verticalArrangement=Arrangement.spacedBy(20.dp)) {
                Text("Vues des fantômes",style=MaterialTheme.typography.titleLarge)
                GhostRow("ghost_red_classic")
                GhostRow("ghost_red_headphones")
                Text("Pacman d’origine · droite / gauche",style=MaterialTheme.typography.titleLarge)
                listOf("pacman_neon" to "Pacman Néon","pacman_pirate" to "Pacman Pirate","pacman_wizard" to "Pacman Mage").forEach{(id,name)->
                    Text(name)
                    Row(horizontalArrangement=Arrangement.spacedBy(32.dp)){SkinAvatar(id,76.dp,90f);SkinAvatar(id,76.dp,270f)}
                }
            }
        } } }
        compose.onNodeWithText("Pacman Pirate").assertIsDisplayed()
        compose.onNodeWithText("Pacman Mage").assertIsDisplayed()
        val instrumentation=androidx.test.platform.app.InstrumentationRegistry.getInstrumentation()
        val resolver=instrumentation.targetContext.contentResolver
        val values=android.content.ContentValues().apply {
            put(android.provider.MediaStore.Images.Media.DISPLAY_NAME,"pac-characters.png")
            put(android.provider.MediaStore.Images.Media.MIME_TYPE,"image/png")
            put(android.provider.MediaStore.Images.Media.RELATIVE_PATH,"Pictures/PacSeekQA")
        }
        val uri=checkNotNull(resolver.insert(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values))
        resolver.openOutputStream(uri)!!.use{instrumentation.uiAutomation.takeScreenshot().compress(android.graphics.Bitmap.CompressFormat.PNG,100,it)}
    }
    @Composable private fun GhostRow(id:String) {
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){if(id.endsWith("_classic"))repeat(6){SkinAvatar(id,48.dp,it*60f)} else SkinAvatar(id,64.dp)}
    }
}
