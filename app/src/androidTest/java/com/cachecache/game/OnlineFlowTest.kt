package com.cachecache.game

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Rule
import org.junit.Test

/** Run only with -PuseFirebaseEmulators=true and the local Firebase suite running. */
class OnlineFlowTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    @get:Rule val permissions = androidx.test.rule.GrantPermissionRule.grant(
        android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION,
        android.Manifest.permission.BLUETOOTH_SCAN, android.Manifest.permission.BLUETOOTH_ADVERTISE,
        android.Manifest.permission.BLUETOOTH_CONNECT
    )

    @Test fun lobbyStartsOnlyWhenSecondPlayerIsReadyAndHostDepartureCancels() {
        check(BuildConfig.USE_FIREBASE_EMULATORS)
        compose.waitUntil(45_000) { compose.onAllNodesWithText("Jouer").filter(isEnabled()).fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Jouer").performClick()
        compose.onNodeWithText("Créer une partie").performClick()
        val auth=com.google.firebase.auth.FirebaseAuth.getInstance()
        val db=com.google.firebase.firestore.FirebaseFirestore.getInstance()
        var code:String?=null
        compose.waitUntil(30_000) {
            code=com.google.android.gms.tasks.Tasks.await(db.collection("users").document(auth.currentUser!!.uid).get()).getString("activeGame")
            code!=null
        }
        compose.onNodeWithText("Lancer la partie").assertIsNotEnabled()
        val context=androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().targetContext
        val second=com.google.firebase.FirebaseApp.initializeApp(context,com.google.firebase.FirebaseApp.getInstance().options,"second-${System.nanoTime()}")
        try {
            val secondAuth=com.google.firebase.auth.FirebaseAuth.getInstance(second)
            secondAuth.useEmulator("10.0.2.2",9099)
            com.google.android.gms.tasks.Tasks.await(secondAuth.signInAnonymously())
            val functions=com.google.firebase.functions.FirebaseFunctions.getInstance(second,"europe-west1")
            functions.useEmulator("10.0.2.2",5001)
            fun call(name:String,data:Map<String,Any?> = emptyMap()) = com.google.android.gms.tasks.Tasks.await(functions.getHttpsCallable(name).call(data))
            call("ensureProfile")
            call("joinGame",mapOf("code" to code))
            call("setReady",mapOf("code" to code,"ready" to true))
            compose.waitUntil(30_000) {compose.onAllNodesWithText("Lancer la partie").filter(isEnabled()).fetchSemanticsNodes().isNotEmpty()}
            compose.onNodeWithText("Lancer la partie").performClick()
            compose.waitUntil(30_000) {compose.onAllNodesWithText("Tu es Pacman !").fetchSemanticsNodes().isNotEmpty()}
            compose.onNodeWithText("Recentrer GPS").assertIsDisplayed()
            screenshot("map")
            compose.onNodeWithContentDescription("Quitter").performClick()
            compose.onNodeWithText("Quitter").performClick()
            compose.waitUntil(30_000) {com.google.android.gms.tasks.Tasks.await(db.collection("games").document(code!!).get()).getString("status")=="cancelled"}
        } finally {second.delete()}
    }

    @Test fun profileShopLeaderboardAndFriendsUseConnectedState() {
        check(BuildConfig.USE_FIREBASE_EMULATORS) { "This test must never run against production Firebase." }
        compose.onNodeWithText("Menu").performClick()
        compose.waitUntil(45_000) { compose.onAllNodesWithText("Connecter Google Play Jeux").fetchSemanticsNodes().isNotEmpty() }
        compose.waitUntil(45_000) { compose.onAllNodesWithText("Enregistrer le pseudo").filter(isEnabled()).fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Mon profil").assertIsDisplayed()
        compose.onNodeWithText("Pseudo").performTextReplacement("Joueur Test")
        androidx.test.espresso.Espresso.closeSoftKeyboard()
        compose.onNodeWithText("Enregistrer le pseudo").performClick()
        compose.waitUntil(30_000) { compose.onAllNodesWithText("Enregistré.").fetchSemanticsNodes().isNotEmpty() }
        screenshot("profile")
        compose.onNodeWithContentDescription("Boutique").performClick()
        compose.onNodeWithText("0 pièces").assertIsDisplayed()
        compose.onNodeWithText("Pacman", useUnmergedTree=false).performClick()
        compose.onAllNodesWithText("Pacman Classique").onFirst().assertIsDisplayed()
        screenshot("shop")
        compose.onNodeWithContentDescription("Classement").performClick()
        compose.waitUntil(30_000) { compose.onAllNodesWithText("Joueur Test (toi)").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Joueur Test (toi)").assertIsDisplayed()
        screenshot("leaderboard")
        compose.onNodeWithContentDescription("Profil").performClick()
        compose.onNodeWithText("Mes amis et invitations").performScrollTo().performClick()
        compose.onNodeWithText("Mes amis").assertIsDisplayed()
        compose.onNodeWithText("Code ami").assertExists()
    }

    private fun screenshot(name: String) {
        val instrumentation=androidx.test.platform.app.InstrumentationRegistry.getInstrumentation()
        val resolver=instrumentation.targetContext.contentResolver
        val values=android.content.ContentValues().apply {
            put(android.provider.MediaStore.Images.Media.DISPLAY_NAME,"pac-$name.png")
            put(android.provider.MediaStore.Images.Media.MIME_TYPE,"image/png")
            put(android.provider.MediaStore.Images.Media.RELATIVE_PATH,"Pictures/PacSeekQA")
        }
        val uri=checkNotNull(resolver.insert(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values))
        resolver.openOutputStream(uri)!!.use { instrumentation.uiAutomation.takeScreenshot().compress(android.graphics.Bitmap.CompressFormat.PNG,100,it) }
    }
}
